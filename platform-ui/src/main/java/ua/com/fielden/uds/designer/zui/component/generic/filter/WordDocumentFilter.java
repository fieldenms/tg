package ua.com.fielden.uds.designer.zui.component.generic.filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a document filter, which allows only alphanumeric characters to be typed.
 * 
 * @author 01es
 * 
 */
public final class WordDocumentFilter extends AbstractDocumentFilter {
    private static final long serialVersionUID = -5568491791809289474L;

    public boolean allowInput(String value) {
	if ("".equals(value)) {
	    return true;
	}

	Pattern p = Pattern.compile("[a-zA-Z]+\\w*");
	Matcher m = p.matcher(value);
	return m.matches();
    }

    public static void main(String[] args) {
	WordDocumentFilter filter = new WordDocumentFilter();
	System.out.println(filter.allowInput("1wer"));
	System.out.println(filter.allowInput("we1r2"));
	System.out.println(filter.allowInput("we1-r2"));
	System.out.println(filter.allowInput("we1_r2"));
	System.out.println(filter.allowInput("_"));
	System.out.println(filter.allowInput(""));
    }

}
