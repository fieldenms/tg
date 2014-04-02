package ua.com.fielden.uds.designer.zui.component.generic.filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a document filter, which allows only integers (digits) to be typed.
 * 
 * @author Jhou
 * 
 */
public class StringDocumentFilter extends AbstractDocumentFilter {
    private static final long serialVersionUID = 6684954556153897458L;

    public boolean allowInput(String value) {
        if ("".equals(value)) {
            return true;
        }
        Pattern p = Pattern.compile("[^\n\r]*");
        Matcher m = p.matcher(value);
        return m.matches();
    }
}
