/*
 * Created by JFormDesigner on Mon Feb 25 14:15:25 GMT+02:00 2008
 */

package ua.com.fielden.platform.swing.components.examples;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import ua.com.fielden.platform.swing.components.FilteredComboBoxDecorator;

/**
 * This is an example on how to use {@link FilteredComboBoxDecorator} for decorating JComboBox with filtering capabilities.
 * 
 * @author Yura
 */
public class FilteredComboBoxDecoratorExample extends JFrame {
    private static final long serialVersionUID = 1L;

    public FilteredComboBoxDecoratorExample() {
        initComponents();
        initComboBox();
    }

    private static String[] stringList = new String[] { "abcdef", "abcefd", "abcged", "adef", "ad", "bcgeh", "bcaeg", "ba", "1234", "123" };

    private void initComboBox() {
        FilteredComboBoxDecorator.decorate(comboBox, new FilteredComboBoxDecorator.StringAutocompleter() {

            public String[] getStringsCorrespondingTo(final String value) {
                if ("".equals(value)) {
                    return new String[] {};
                }
                int count = 0;
                for (int index = 0; index < stringList.length; index++) {
                    if (stringList[index].startsWith(value)) {
                        count++;
                    }
                }
                final String[] result = new String[count];
                int j = 0;
                for (int index = 0; index < stringList.length; index++) {
                    if (stringList[index].startsWith(value)) {
                        result[j++] = stringList[index];
                    }
                }
                return result;
            }

        });
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY //GEN-BEGIN:initComponents
        comboBox = new JComboBox();

        // ======== this ========
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        final Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        // ---- comboBox ----
        comboBox.setEditable(true);
        contentPane.add(comboBox, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization //GEN-END:initComponents
    }

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final FilteredComboBoxDecoratorExample frame = new FilteredComboBoxDecoratorExample();
                frame.setVisible(true);
            }
        });
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY //GEN-BEGIN:variables
    private JComboBox comboBox;
    // JFormDesigner - End of variables declaration //GEN-END:variables
}
