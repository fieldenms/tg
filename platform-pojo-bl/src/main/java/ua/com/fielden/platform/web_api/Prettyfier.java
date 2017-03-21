package ua.com.fielden.platform.web_api;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Prettyfier {
    public static String prettyString(final Object data, final int indent, final boolean firstPartShouldBeIndented) {
        final String indentStr = multiply(" ", indent);
        // return indentStr + " = " + prettyString(data, indent);
        final StringBuilder sb = new StringBuilder();
        if (data instanceof Map) {
            sb.append(firstPartShouldBeIndented ? indentStr + "{" : "{");
            final Map<String, Object> map = (Map<String, Object>) data;
            final Iterator<Entry<String, Object>> iter = map.entrySet().iterator();
            if (iter.hasNext()) {
                sb.append("\n");
                while (iter.hasNext()) {
                    final Entry<String, Object> entry = iter.next();
                    sb.append(prettyString(entry.getKey(), entry.getValue(), indent + 2));
                }
                sb.append(indentStr + "}\n");
            } else {
                sb.append("}\n");
            }
        } else if (data instanceof List) {
            sb.append(firstPartShouldBeIndented ? indentStr + "[" : "[");
            final List<Object> list = (List<Object>) data;
            final Iterator<Object> iter = list.iterator();
            if (iter.hasNext()) {
                sb.append(prettyString(iter.next(), indent + 2, false));
                while (iter.hasNext()) {
                    sb.append(prettyString(iter.next(), indent + 2, true));
                }
                sb.append(indentStr + "]\n");
            } else {
                sb.append("]\n");
            }
        } else {
            sb.append(data + "\n");
        }
        return sb.toString();
    }
    
    private static String prettyString(final String property, final Object data, final int indent) {
        final String indentStr = multiply(" ", indent);
        return indentStr + property + " = " + prettyString(data, indent, false);
    }
    
    private static String multiply(final String s, final int count) {
        if (count == 0) {
            return "";
        } else {
            return s + multiply(s, count - 1);
        }
    }
}
