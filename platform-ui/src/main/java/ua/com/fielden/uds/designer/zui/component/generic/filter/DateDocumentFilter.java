package ua.com.fielden.uds.designer.zui.component.generic.filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a document filter, which allows only integers (digits) to be typed.
 * 
 * @author Jhou
 * 
 */
public class DateDocumentFilter extends AbstractDocumentFilter {
    private static final long serialVersionUID = 6684954556153897458L;

    @Override
    public boolean allowInput(final String value) {
	if ("".equals(value)) {
	    return true;
	}

	// YYYY-MM-DD hh:mm:ss.s
	final Pattern p = Pattern
		.compile("(1?9?|2?0?)\\d?\\d?[- /.]?(0?[1-9]?|1?[012]?)[- /.]?(0?[1-9]?|[12]?[0-9]?|3?[01]?)[ ]?((0|1)?\\d?|2?[0123]?)[:]?((0|1|2|3|4|5)?\\d?)[:]?((0|1|2|3|4|5)?\\d?)[.]?\\d?");
	final Matcher m = p.matcher(value);
	return m.matches();
    }
}
