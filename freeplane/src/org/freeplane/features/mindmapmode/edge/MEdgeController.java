/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Dimitry Polivaev in 2008.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.features.mindmapmode.edge;

import java.awt.Color;
import java.util.ListIterator;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.undo.IUndoableActor;
import org.freeplane.features.common.edge.EdgeController;
import org.freeplane.features.common.edge.EdgeModel;

/**
 * @author Dimitry Polivaev
 */
public class MEdgeController extends EdgeController {
	public MEdgeController(final ModeController modeController) {
		super(modeController);
		final Controller controller = modeController.getController();
		modeController.addAction("edgeColor", new EdgeColorAction(controller));
		modeController.addAction("EdgeWidth_WIDTH_PARENT", new EdgeWidthAction(modeController, EdgeModel.WIDTH_PARENT));
		modeController.addAction("EdgeWidth_WIDTH_THIN", new EdgeWidthAction(modeController, EdgeModel.WIDTH_THIN));
		modeController.addAction("EdgeWidth_1", new EdgeWidthAction(modeController, 1));
		modeController.addAction("EdgeWidth_2", new EdgeWidthAction(modeController, 2));
		modeController.addAction("EdgeWidth_4", new EdgeWidthAction(modeController, 4));
		modeController.addAction("EdgeWidth_8", new EdgeWidthAction(modeController, 8));
		modeController.addAction("EdgeStyle_linear", new EdgeStyleAction(modeController, EdgeModel.EDGESTYLE_LINEAR));
		modeController.addAction("EdgeStyle_bezier", new EdgeStyleAction(modeController, EdgeModel.EDGESTYLE_BEZIER));
		modeController.addAction("EdgeStyle_sharp_linear", new EdgeStyleAction(modeController,
		    EdgeModel.EDGESTYLE_SHARP_LINEAR));
		modeController.addAction("EdgeStyle_sharp_bezier", new EdgeStyleAction(modeController,
		    EdgeModel.EDGESTYLE_SHARP_BEZIER));
	}

	public void setColor(final NodeModel node, final Color color) {
		final ModeController modeController = node.getModeController();
		final Color oldColor = getColor(node);
		if (color.equals(oldColor)) {
			return;
		}
		final IUndoableActor actor = new IUndoableActor() {
			public void act() {
				EdgeModel.createEdge(node).setColor(color);
				modeController.getMapController().nodeChanged(node);
			}

			public String getDescription() {
				return "setColor";
			}

			public void undo() {
				EdgeModel.createEdge(node).setColor(oldColor);
				modeController.getMapController().nodeChanged(node);
			}
		};
		modeController.execute(actor);
	}

	public void setStyle(final NodeModel node, final String style) {
		final ModeController modeController = node.getModeController();
		final String oldStyle = getStyle(node);
		if (style.equals(oldStyle)) {
			return;
		}
		final IUndoableActor actor = new IUndoableActor() {
			public void act() {
				EdgeModel.createEdge(node).setStyle(style);
				modeController.getMapController().nodeChanged(node);
				edgeStyleRefresh(node);
			}

			private void edgeStyleRefresh(final NodeModel node) {
				final ListIterator childrenFolded = modeController.getMapController().childrenFolded(node);
				while (childrenFolded.hasNext()) {
					final NodeModel child = (NodeModel) childrenFolded.next();
					final EdgeModel edge = EdgeModel.getModel(child);
					if (edge == null || edge.getStyle() == null) {
						modeController.getMapController().nodeRefresh(child);
						edgeStyleRefresh(child);
					}
				}
			}

			public String getDescription() {
				return "setStyle";
			}

			public void undo() {
				EdgeModel.createEdge(node).setStyle(oldStyle);
				modeController.getMapController().nodeChanged(node);
				edgeStyleRefresh(node);
			}
		};
		modeController.execute(actor);
	}

	public void setWidth(final NodeModel node, final int width) {
		final ModeController modeController = node.getModeController();
		final int oldWidth = getWidth(node);
		if (width == oldWidth) {
			return;
		}
		final IUndoableActor actor = new IUndoableActor() {
			public void act() {
				EdgeModel.createEdge(node).setWidth(width);
				modeController.getMapController().nodeChanged(node);
				edgeWidthRefresh(node);
			}

			private void edgeWidthRefresh(final NodeModel node) {
				final ListIterator childrenFolded = modeController.getMapController().childrenFolded(node);
				while (childrenFolded.hasNext()) {
					final NodeModel child = (NodeModel) childrenFolded.next();
					final EdgeModel edge = EdgeModel.getModel(child);
					if (edge == null || edge.getWidth() == EdgeModel.WIDTH_PARENT) {
						modeController.getMapController().nodeRefresh(child);
						edgeWidthRefresh(child);
					}
				}
			}

			public String getDescription() {
				return "setWidth";
			}

			public void undo() {
				EdgeModel.createEdge(node).setWidth(oldWidth);
				modeController.getMapController().nodeChanged(node);
				edgeWidthRefresh(node);
			}
		};
		modeController.execute(actor);
	}
}
