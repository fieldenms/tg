package ua.com.fielden.platform.swing.components.textfield;

import javax.swing.JTextField;

/**
 * Enum for convenient instantiation of JTextField descendants provided in this package.
 * 
 * @author 01es
 * 
 */
public enum Options {
    READONLY() {
        public void set(final JTextField field) {
            field.setEditable(false);
        }
    },
    DISABLED() {
        public void set(final JTextField field) {
            field.setEnabled(false);
        }
    };

    public abstract void set(final JTextField field);

}
