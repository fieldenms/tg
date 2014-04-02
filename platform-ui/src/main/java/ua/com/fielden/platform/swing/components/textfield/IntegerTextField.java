package ua.com.fielden.platform.swing.components.textfield;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.swing.components.textfield.caption.CaptionTextFieldLayer;
import ua.com.fielden.platform.swing.utils.SimpleLauncher;

/**
 * <code>IntegerTextField</code> is a JTextField that accepts only digits.
 * 
 * @author TG Team
 * 
 */
public class IntegerTextField extends AbstractTextField {
    private static final long serialVersionUID = 4954244858909749947L;

    public IntegerTextField(final Long value, final Options... options) {
        super(value != null ? value.toString() : "", options);
    }

    public IntegerTextField(final Options... options) {
        super(options);
    }

    @Override
    protected Document createDefaultModel() {
        return new IntegerDocument();
    }

    /**
     * Document model that accepts only digits.
     * 
     * @author TG Team
     * 
     */
    private static class IntegerDocument extends PlainDocument {
        private static final long serialVersionUID = 0;

        @Override
        public void insertString(final int offs, final String str, final AttributeSet a) throws BadLocationException {
            if (str == null) {
                return;
            }
            if (StringUtils.isNumeric(str)) {
                super.insertString(offs, str, a);
            }
        }
    }

    public static void main(final String[] args) {
        final JPanel panel = new JPanel(new MigLayout("fill", "[:250:]"));
        final IntegerTextField field = new IntegerTextField();

        panel.add(new CaptionTextFieldLayer<JTextField>(field, "some caption"), "growx, wrap");
        panel.add(new JButton("Dummy"), "align right");
        SimpleLauncher.show("Show off the caption", panel);
    }
}