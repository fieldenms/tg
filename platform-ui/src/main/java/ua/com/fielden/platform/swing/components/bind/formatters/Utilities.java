package ua.com.fielden.platform.swing.components.bind.formatters;

import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;

public class Utilities {

    public static boolean isComposedTextAttributeDefined(final AttributeSet as) {
        return ((as != null) && (as.isDefined(StyleConstants.ComposedTextAttribute)));
    }

}
