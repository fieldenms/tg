package ua.com.fielden.uds.designer.zui.component.generic.filter;

import java.io.Serializable;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

public abstract class AbstractDocumentFilter extends DocumentFilter implements Serializable {
    private static final long serialVersionUID = -729812571298150237L;

    public void insertString(DocumentFilter.FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
        if (text == null) {
            return;
        } else {
            replace(fb, offset, 0, text, attr);
        }
    }

    public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        Document doc = fb.getDocument();
        int currentLength = doc.getLength();
        String currentContent = doc.getText(0, currentLength);
        String before = currentContent.substring(0, offset);
        String after = currentContent.substring(length + offset, currentLength);
        String newValue = before + (text == null ? "" : text) + after;
        if (allowInput(newValue)) {
            fb.replace(offset, length, text, attrs);
        }
    }

    public void remove(DocumentFilter.FilterBypass fb, int offset, int length) throws BadLocationException {
        replace(fb, offset, length, "", null);
    }

    public abstract boolean allowInput(String value);
}
