package ua.com.fielden.uds.designer.zui.component.generic.filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a document filter, which allows numbers, words and numbers with measures like d (day), m(minutes) etc.
 * 
 * @author 01es
 * 
 */
public final class MixedDocumentFilter extends AbstractDocumentFilter {
    private static final long serialVersionUID = 6684954556153897458L;

    public boolean allowInput(String value) {
        if ("-".equals(value) || ".".equals(value)) {
            return false;
        }

        Pattern p = Pattern.compile("-?\\d*\\.?\\d*");
        Matcher m = p.matcher(value);
        boolean isNumber = m.matches();

        p = Pattern.compile("[a-zA-Z]+\\w*");
        m = p.matcher(value);
        boolean isWord = m.matches();

        p = Pattern.compile("(-?\\d*\\.?\\d*$)||(-?\\d*\\.?\\d*[m|h|d|M|y]$)"); // m -- minutes, d - days, M -- months, y -- years
        m = p.matcher(value);
        boolean isNumberWithMeasures = m.matches();

        p = Pattern.compile("([A-Z]+,?)*"); // this is required for things like status codes etc.
        m = p.matcher(value);
        boolean isSetOfConstants = m.matches();

        return (isWord || isNumber || isNumberWithMeasures || isSetOfConstants);
    }

    public static void main(String[] args) {
        MixedDocumentFilter filter = new MixedDocumentFilter();
        System.out.println(filter.allowInput("233"));
        System.out.println(filter.allowInput("2sss"));
        System.out.println(filter.allowInput(""));
        System.out.println(filter.allowInput("F,B"));
        System.out.println(filter.allowInput("F,"));
        System.out.println(filter.allowInput("sddd"));

        /*
         * Pattern p = Pattern.compile("([a-zA-Z]+$)||([A-Z]+,*[A-Z]+$)"); Matcher m = p.matcher("F,B"); System.out.println(m.matches());
         */
    }
}
