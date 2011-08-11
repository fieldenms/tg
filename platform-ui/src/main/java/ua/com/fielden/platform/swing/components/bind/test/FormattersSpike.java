package ua.com.fielden.platform.swing.components.bind.test;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.text.BadLocationException;
import javax.swing.text.NumberFormatter;

import ua.com.fielden.platform.swing.utils.SimpleLauncher;

public class FormattersSpike {

    public static class CustomNumberFormatter extends NumberFormatter {
	public CustomNumberFormatter(final NumberFormat format) {
	    super(format);
	}

	private static final long serialVersionUID = 1L;
	private char typedCharacter;

	@Override
	public Object stringToValue(final String text) throws ParseException {
	    System.out.println("stringToValue : text = [" + text + "]");
	    //	    if (typedCharacter == '-' && text != null) { //  && text.equals("-0.0")
	    //		//		    setEditValid(true);
	    //		return (-0.0d);
	    //	    } else {
	    return super.stringToValue(text);
	    //	    }
	}

	@Override
	public String valueToString(final Object value) throws ParseException {
	    System.out.println("valueToString : value = [" + value + "]");
	    //	    if (value != null && value.equals(-0.0d)) {
	    //		//		    setEditValid(true);
	    //		return "-0.0";
	    //	    } else {
	    return super.valueToString(value);
	    //	    }
	}

	//
	//	/**
	//	 * Overriden to toggle the value if the positive/minus sign is inserted.
	//	 */
	//	void replace(final DocumentFilter.FilterBypass fb, final int offset, final int length, final String string, final AttributeSet attr) throws BadLocationException {
	//	    //		if (!getAllowsInvalid() && length == 0 && string != null && string.length() == 1 && toggleSignIfNecessary(fb, offset, string.charAt(0))) {
	//	    //		    return;
	//	    //		}
	//	    //		super.replace(fb, offset, length, string, attr);
	//	}

	public char getTypedCharacter() {
	    return typedCharacter;
	}

	//	public void setTypedCharacter(final char typedCharacter) {
	//	    System.out.println("setTypedCharacter(" + typedCharacter + ")");
	//	    this.typedCharacter = typedCharacter;
	//	}
    }

    public static void main(final String[] args) {
	//	final EmptyNumberFormatter format = new EmptyNumberFormatter(new DecimalFormat("0.0###################"), null);
	final DecimalFormat format = new DecimalFormat("0.0###################;_0.0###################");
	System.err.println(format.getDecimalFormatSymbols().getMinusSign());
	final CustomNumberFormatter formatter = new CustomNumberFormatter(format);
	formatter.setAllowsInvalid(false);
	try {
	    System.out.println("[-0.0] -> [" + formatter.valueToString(-0.0) + "]");
	    System.out.println("[-0.0] -> [" + formatter.stringToValue("-0.0") + "]");
	} catch (final ParseException e) {
	    e.printStackTrace();
	}
	final JFormattedTextField ftf = new JFormattedTextField(formatter);
	//	ftf.getDocument().addDocumentListener(new DocumentListener() {
	//	    @Override
	//	    public void insertUpdate(final DocumentEvent e) {
	//		//		formatter.setTypedCharacter('-');
	//		System.out.println(e);
	//
	//		if (e instanceof UndoableEdit) {
	//		    SwingUtilities.invokeLater(new Runnable() {
	//			public void run() {
	//			    ((UndoableEdit) e).undo();
	//			}
	//		    });
	//		}
	//	    }
	//
	//	    @Override
	//	    public void removeUpdate(final DocumentEvent e) {
	//	    }
	//
	//	    @Override
	//	    public void changedUpdate(final DocumentEvent e) {
	//	    }
	//	});
	ftf.setValue(0.0d);
	try {
	    ftf.getDocument().insertString(0, "-", null);
	} catch (final BadLocationException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	SimpleLauncher.show("formatters spike", ftf);
    }

}
