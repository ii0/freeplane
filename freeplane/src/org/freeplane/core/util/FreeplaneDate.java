/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2011 dimitry
 *
 *  This file author is dimitry
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
package org.freeplane.core.util;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.time.DateFormatUtils;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;

/**
 * @author Dimitry Polivaev
 * Mar 2, 2011
 */
@SuppressWarnings("serial")
@FactoryMethod("toObject")
@SerializationMethod("toStringISO")
public class FreeplaneDate extends Date {

	private static HashMap<String, DateFormat> dateFormatCache = new HashMap<String, DateFormat>();
	public static final String ISO_DATE_FORMAT_PATTERN = "yyyy-MM-dd";
	public static final String ISO_DATE_TIME_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mmZ";
	public static final Pattern ISO_DATE_TIME_REGEXP_PATTERN = Pattern.compile("\\d{4}(-?)\\d{2}(-?)\\d{2}" //
    + "(([ T])?\\d{2}(:?)\\d{2}(:?)(\\d{2})?(\\.\\d{3})?([-+]\\d{4})?)?");
	
	static private DateFormat defaultFormat;
	
	static private DateFormat getDefaultFormat()
	{
		if(defaultFormat != null)
			return defaultFormat;
		
		final ResourceController resourceController = ResourceController.getResourceController();
		String defaultFormatPattern = resourceController.getProperty("date_format");
		defaultFormat = new SimpleDateFormat(defaultFormatPattern);
		resourceController.addPropertyChangeListener(new IFreeplanePropertyListener() {
			public void propertyChanged(String propertyName, String newValue, String oldValue) {
				if(propertyName.equals("date_format"))
					defaultFormat = new SimpleDateFormat(newValue);
			}
		});
		return defaultFormat;
	}

	public FreeplaneDate(Date date) {
	    super(date.getTime());
    }

	public FreeplaneDate(long date) {
	    super(date);
    }

	@Override
    public String toString() {
		return getDefaultFormat().format(this);
    }

	public static String toStringISO(final FreeplaneDate date) {
    	return toStringISO((Date)date);
    }
	public static String toStringISO(final Date date) {
    	// use local timezone
    	return DateFormatUtils.format(date, ISO_DATE_TIME_FORMAT_PATTERN);
    }

	public static String toStringShortISO(final Date date) {
    	return DateFormatUtils.format(date, ISO_DATE_FORMAT_PATTERN);
    }

	public static Object toObject(String text) {
    	final FreeplaneDate date = toDateISO(text);
    	return date == null ? text : date;
    }

	public static FreeplaneDate toDate(String text) {
    	final FreeplaneDate date = toDateISO(text);
    	return date == null ? toDateUser(text) : date;
    }

	public static boolean isDate(String text) {
    	return isDateISO(text) || isDateUser(text);
    }

	public static Object createDateObject(String text) {
		final FreeplaneDate dateISO = toDateISO(text);
		if(dateISO != null)
			return dateISO;
		return text;
	}
	
	public static FreeplaneDate toDateISO(String text) {
    	//        1         2         34            5         6   7        8           9
    	// \\d{4}(-?)\\d{2}(-?)\\d{2}(([ T])?\\d{2}(:?)\\d{2}(:?)(\\d{2})?(\\.\\d{3})?([-+]\\d{4})?)?
    	final Matcher matcher = ISO_DATE_TIME_REGEXP_PATTERN.matcher(text);
    	if (matcher.matches()) {
    		StringBuilder builder = new StringBuilder("yyyy");
    		builder.append(matcher.group(1));
    		builder.append("MM");
    		builder.append(matcher.group(2));
    		builder.append("dd");
    		if (matcher.group(3) != null) {
    			if (matcher.group(4) != null) {
    				builder.append('\'');
    				builder.append(matcher.group(4));
    				builder.append('\'');
    			}
    			builder.append("HH");
    			builder.append(matcher.group(5));
    			builder.append("mm");
    			if (matcher.group(7) != null) {
    				builder.append(matcher.group(6));
    				builder.append("ss");
    			}
    			if (matcher.group(8) != null) {
    				builder.append(".SSS");
    			}
    			if (matcher.group(9) != null) {
    				builder.append("Z");
    			}
    		}
    		final String pattern = builder.toString();
    		return parseDate(text, pattern);
    	}
    	return null;
    }

	static private FreeplaneDate parseDate(String text, final String pattern) {
        DateFormat parser = dateFormatCache.get(pattern);
        if (parser == null) {
        	parser = new SimpleDateFormat(pattern);
        	dateFormatCache.put(pattern, parser);
        }
        final ParsePosition pos = new ParsePosition(0);
        final Date date = parser.parse(text, pos);
        if (date != null && pos.getIndex() == text.length()) {
        	return new FreeplaneDate(date.getTime());
        }
    	return null;
    }

	public static boolean isDateISO(String text) {
    	if (text == null)
    		return false;
    	return ISO_DATE_TIME_REGEXP_PATTERN.matcher(text).matches();
    }

	public static FreeplaneDate toDateUser(String text) {
    	return parseDate(text, ResourceController.getResourceController().getProperty("date_format"));
    }

	public static boolean isDateUser(String text) {
    	return text != null && toDateUser(text) != null;
    }
}
