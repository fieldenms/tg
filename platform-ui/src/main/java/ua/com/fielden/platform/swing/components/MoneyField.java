/**
 *
 */
package ua.com.fielden.platform.swing.components;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.utils.SimpleLauncher;
import ua.com.fielden.platform.types.Money;

/**
 * Represents editor component for Money property type
 *
 * @author Yura
 */
public class MoneyField extends ValidationLayer<JTextField> {

    private static final long serialVersionUID = 1L;

    /**
     * Initialises instance with default {@link IParsingRule} and empty {@link Money} value
     */
    public MoneyField() {
        this(null, defaultParsingRule);
    }

    /**
     * Initializes instance with default {@link IParsingRule} and passed {@link Money} value
     *
     * @param value
     */
    public MoneyField(final Money value) {
        this(value, defaultParsingRule);
    }

    /**
     * Initializes instance with given {@link IParsingRule} and sets passed value as text inside
     *
     * @param value
     * @param parsingRule
     */
    public MoneyField(final Money value, final IParsingRule parsingRule) {
        super(new JTextField(value != null ? value.toString() : ""));
        getView().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(final DocumentEvent e) {
            }

            @Override
            public void insertUpdate(final DocumentEvent e) {
                validate(e);
            }

            @Override
            public void removeUpdate(final DocumentEvent e) {
                validate(e);
            }

            private void validate(final DocumentEvent e) {
                setResult(parsingRule.parseString(getView().getText()));
            }
        });
    }

    /**
     * Returns null if entered value cannot be parsed {@link IParsingRule#parseString(String)} returns unsuccessful {@link Result}. Returns {@link Money} instance, that is obtained
     * from {@link Result#getInstance()} method, when it is successful.
     */
    public Money getValue() {
        if (getResult() != null && getResult().isSuccessful()) {
            return (Money) getResult().getInstance();
        } else {
            return null;
        }
    }

    /**
     * Interface determining parsing rule, that should convert {@link String}s into {@link Money} values.
     *
     * @author Yura
     */
    public static interface IParsingRule {
        /**
         * Should return successful {@link Result} with instance set to converted {@link Money} type, if it is possible to convert passed {@link String} value to {@link Money}
         * value.<br>
         * Should return unsuccessful {@link Result} with proper message and {@link Exception} set (instance is ignored), if it not possible to convert passed {@link String} value
         * to {@link Money} value.<br>
         *
         * @param value
         * @return
         */
        public Result parseString(String value);
    }

    /**
     * Tries to parse given {@link String} value directly as {@link Double}. If not succeeded, then tries to parse given {@link String} using currency formatting rules. If not
     * succeeded this time, then removes all commas and tries for the last time.<br>
     * Note : empty {@link String} is considered as legal value and represents null.
     */
    public static final IParsingRule defaultParsingRule = new IParsingRule() {
        @Override
        public Result parseString(final String value) {
            if ("".equals(value)) {
                return new Result(null, "");
            }
            BigDecimal amount = null;
            try {
                // trying to parse directly like a number
                amount = new BigDecimal(Double.valueOf(value));
            } catch (final Exception exc) {
                try {
                    // trying to parse using currency formatting
                    final Number enteredAmount = NumberFormat.getCurrencyInstance().parse(value);
                    amount = enteredAmount instanceof Double ? new BigDecimal(enteredAmount.doubleValue()) : new BigDecimal(enteredAmount.longValue());
                    if (amount.doubleValue() <= 0) {
                        // if negative, then could not parse
                        return new Result(null, new Exception("Couldn't parse '" + value + "' value"));
                    }
                } catch (final Exception e) {
                    // the worst case - removing all commas and trying to parse like that
                    final String newValue = value.replace(",", "");
                    try {
                        amount = new BigDecimal(Double.valueOf(newValue));
                    } catch (final Exception exc2) {
                        return new Result(null, new Exception("Couldn't parse '" + value + "' value"));
                    }
                }
            }
            return new Result(new Money(amount), "");
        }
    };

    /**
     * Example of usage of this class
     *
     * @param args
     */
    public static void main(final String[] args) {
        final MoneyField mf = new MoneyField();
        mf.setPreferredSize(new Dimension(100, 25));

        final JButton button = new JButton("Print value to console");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Money money = mf.getValue();
                System.out.println(money != null ? money.toString() : "null");
            }
        });
        SimpleLauncher.show("Test", new FlowLayout(FlowLayout.CENTER), mf, button);
    }

}
