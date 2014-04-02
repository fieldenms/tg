package ua.com.fielden.platform.swing.utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.basic.autocompleter.PojoValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.components.smart.autocompleter.development.AutocompleterLogic;
import ua.com.fielden.platform.swing.components.textfield.IntegerTextField;
import ua.com.fielden.platform.swing.components.textfield.Options;
import ua.com.fielden.platform.swing.components.textfield.UpperCaseTextField;

import com.jidesoft.grid.CellStyleProvider;
import com.jidesoft.grid.HierarchicalTable;
import com.jidesoft.grid.HierarchicalTableModel;
import com.jidesoft.grid.RowStripeCellStyleProvider;
import com.jidesoft.grid.SortEvent;
import com.jidesoft.grid.SortListener;
import com.jidesoft.grid.SortableTableModel;
import com.jidesoft.grid.TableModelWrapperUtils;
import com.jidesoft.swing.JideTabbedPane;
import com.jidesoft.swing.StyledLabel;
import com.jidesoft.swing.StyledLabelBuilder;

/**
 * UI builder, which is introduced to simplify UI prototyping. However, at some stage it could become a convenient day-to-day tool for UI creation.
 * 
 * @author 01es
 * 
 */
public class DummyBuilder {

    /**
     * Creates StyledLabel (JIDE) for criteria property ("name: ").
     * 
     * Important : there are some problems when using setText(stringOfLessLength). Use StyledLabel.clearStyleRanges() to remove internal layout exceptions (BasicStyledLabelUI).
     * 
     * @param caption
     * @return
     */
    public static JLabel label(final String caption) {
        return label(caption, new Color(0x646464)); // 0x858585
    }

    public static JLabel label(final String caption, final Color color) {
        final StyledLabel styledLabel = StyledLabelBuilder.createStyledLabel(caption);//  add().createLabel();
        styledLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        styledLabel.setForeground(color);
        return styledLabel;
    }

    public static IntegerTextField itf(final Long value, final Options... options) {
        return new IntegerTextField(value, options);
    }

    public static IntegerTextField itf(final int value, final Options... options) {
        return itf(new Long(value), options);
    }

    public static JSpinner spinner(final int initValue, final int min, final int max, final Options... options) {
        final SpinnerModel model = new SpinnerNumberModel(initValue, min, max, 1);
        final JSpinner spinner = new JSpinner(model);
        spinner.setEnabled(options.length == 0);
        return spinner;
    }

    public static UpperCaseTextField uctf(final String value, final Options... options) {
        return new UpperCaseTextField(value, options);
    }

    /**
     * Creates and returns {@link JTabbedPane} with no buttons on tabs.
     * 
     * @return
     */
    public static JideTabbedPane createBoldTabbedPane() {
        final JideTabbedPane tabbedPane = new JideTabbedPane();
        tabbedPane.setShowCloseButtonOnSelectedTab(false);
        tabbedPane.setShowTabButtons(false);
        tabbedPane.setBoldActiveTab(true);
        tabbedPane.setColorTheme(JideTabbedPane.COLOR_THEME_OFFICE2003);
        tabbedPane.setTabShape(JideTabbedPane.SHAPE_OFFICE2003);
        return tabbedPane;
    }

    public static JTextField dateField(final Date value, final Options... options) {
        final String dateStr = value != null ? new SimpleDateFormat("dd/MM/yyyy").format(value) + " " + DateFormat.getTimeInstance(DateFormat.SHORT).format(value) : "";
        final JTextField component = new JTextField(dateStr);
        component.setCaretPosition(0);
        for (final Options option : options) {
            option.set(component);
        }
        return component;
    }

    public static JCheckBox chb(final String label) {
        return new JCheckBox(label);
    }

    public static JCheckBox chb(final Action action) {
        return new JCheckBox(action);
    }

    public static CellStyleProvider csp() {
        return new RowStripeCellStyleProvider(new Color[] { new Color(242, 242, 242), new Color(255, 255, 255) });
    }

    /**
     * Returning {@link IValueMatcher} instance supporting the same functionality as {@link PojoValueMatcher} with wildcard support and multi value matching (i.e. parsing values
     * separated with comma).
     */
    @SuppressWarnings("unchecked")
    public static <T extends AbstractEntity<?>> IValueMatcher<T> multiValuePojoMatcher(final List<T> instances, final String expression) {
        // TODO method findMatches(String) takes more than O(n) time to find matches, thus it should be optimised
        return new PojoValueMatcher<T>(instances, expression, Integer.MAX_VALUE) {
            private final String valueSeparator = ",";

            @Override
            public List<T> findMatches(final String value) {
                if (value.length() == 0) {
                    return new ArrayList<T>(getInstances());
                }

                String[] values = value.split(valueSeparator);
                if (value.endsWith(",")) {
                    values = Arrays.copyOf(values, values.length + 1);
                    values[values.length - 1] = "";
                }

                final List<T> possibleMatches = new ArrayList<T>();
                for (int i = 0; i < values.length - 1; i++) {
                    values[i] = values[i].contains("*") ? values[i].replaceAll("\\*", "%") : values[i];
                    possibleMatches.addAll(super.findMatches(values[i]));
                }
                final String typedWord = value.endsWith(",") ? "" : AutocompleterLogic.wrapTypedWord(values[values.length - 1], true);
                possibleMatches.addAll(super.findMatches(typedWord));

                return possibleMatches;
            }

        };
    }

    /**
     * Returns {@link ListSelectionListener} that enables single-click row expanding on {@link HierarchicalTable} with children initialization using passed
     * <code>childrenLoader</code> instance <br>
     * <br>
     * Note : returned {@link ListSelectionListener} is not added to {@link HierarchicalTable} in this method, it should be added after it is returned
     * 
     * @param table
     *            - table which row should be expanded
     * @param chidlrenLoader
     * @param component
     *            - component to be blocked during row expansion
     * @return {@link ListSelectionListener}
     */
    @SuppressWarnings({ "serial", "unchecked" })
    public static <T> ListSelectionListener expandListSelectionListener(final HierarchicalTable table, final ChildrenLoader<T> childrenLoader, final BlockingIndefiniteProgressLayer blockingLayer) {
        return new ListSelectionListener() {
            private Action rowExpand = new BlockingLayerCommand<Result>("expand row", blockingLayer) {

                /**
                 * Indicates whether main block of action() method should be called or not
                 */
                private boolean shouldFireAction;

                @Override
                protected boolean preAction() {
                    if (!table.isExpanded(table.getSelectedRow()) && table.getSelectedRow() != -1) {
                        super.preAction();
                        setMessage("Opening details...");
                        shouldFireAction = true;
                    } else {
                        shouldFireAction = false;
                    }
                    return true;
                }

                @Override
                protected Result action(final ActionEvent e) throws Exception {
                    if (shouldFireAction) {
                        // defining what row in actual model (i.e. what position in getInstances()) should be expanded
                        final HierarchicalTableModel tableModel = (HierarchicalTableModel) TableModelWrapperUtils.getActualTableModel(table.getModel());
                        final int actualRow = TableModelWrapperUtils.getActualRowAt(table.getModel(), table.getSelectedRow());
                        // loading children using ChildrenLoader without any UI-interaction - just raw data processing
                        childrenLoader.loadChildren((T) tableModel.getChildValueAt(actualRow));
                        // returning successful result
                        return new Result(null, "");
                    }
                    // if main block of action() method was not called then returning unsuccessful result
                    return new Result(null, new Exception());
                }

                @Override
                protected void postAction(final Result value) {
                    if (value.isSuccessful()) {
                        // expanding row
                        table.expandRow(table.getSelectedRow());
                    }
                    super.postAction(value);
                }

            };

            @Override
            public void valueChanged(final ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    rowExpand.actionPerformed(null);
                }
            }
        };
    }

    /**
     * Interface which separates children initialization logic of some entity, from the rest logic of that entity
     * 
     * @author Yura
     * @param <T>
     *            - type of entities, which children should be loaded
     */
    public interface ChildrenLoader<T> {
        /**
         * In this method initialization of children of <code>entity</code> should be performed
         * 
         * @param entity
         */
        public void loadChildren(T entity);
    }

    /**
     * @param message
     * @param title
     * @return information message created using {@link JOptionPane}
     */
    public static JDialog infoMessage(final String message, final String title) {
        return infoMessage(null, message, title);
    }

    public static JDialog infoMessage(final Component parent, final String message, final String title) {
        return new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION).createDialog(parent, title);
    }

    /**
     * @param maxCharacters
     * @return {@link DocumentListener} that limits input to {@link Document} to <code>maxCharacters</code>
     */
    public static DocumentListener createInputLimiter(final int maxCharacters) {
        return new DocumentListener() {
            @Override
            public void changedUpdate(final DocumentEvent e) {
            }

            @Override
            public void insertUpdate(final DocumentEvent e) {
                final Document document = e.getDocument();
                if (document.getLength() > maxCharacters) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                document.remove(e.getOffset(), document.getLength() - maxCharacters);
                            } catch (final BadLocationException exc) {
                                throw new IllegalStateException(exc);
                            }
                        }
                    });
                }
            }

            @Override
            public void removeUpdate(final DocumentEvent e) {
            }

        };
    }

    /**
     * Removes all {@link ListSelectionListener}s from table and adds custom {@link ListSelectionListener} for row expanding/collapsing
     */
    public static <ChildType> void customizeSelectionOn(final HierarchicalTable hierarchicalTable, final BlockingIndefiniteProgressLayer blockingLayer, final ChildrenLoader<ChildType> loader) {
        // managing selection on hierarchical table
        // to let us handle selection manually
        hierarchicalTable.setPreserveSelectionsAfterSorting(false);
        ((SortableTableModel) hierarchicalTable.getModel()).addSortListener(new SortListener() {
            /**
             * Holding row number in actual model
             */
            private int actuallySelectedRow;

            @Override
            public void sortChanged(final SortEvent event) {
                // this method is called after sorting so using selected row number before sorting we should select corresponding row in sorted table
                final int rowInSortedTable = TableModelWrapperUtils.getRowAt(hierarchicalTable.getModel(), actuallySelectedRow);
                hierarchicalTable.getSelectionModel().setSelectionInterval(rowInSortedTable, rowInSortedTable);
            }

            @Override
            public void sortChanging(final SortEvent event) {
                // this method is fired before sorting so lets store selected row number in actual model
                actuallySelectedRow = TableModelWrapperUtils.getActualRowAt(hierarchicalTable.getModel(), hierarchicalTable.getSelectedRow());
            }
        });
        // adding our own ListSelectionListener for expanding
        final ListSelectionListener selectionListener = DummyBuilder.expandListSelectionListener(hierarchicalTable, loader, blockingLayer);
        hierarchicalTable.getSelectionModel().addListSelectionListener(selectionListener);
    }

    /**
     * Invokes {@link Runnable#run()} on passed instance, when specified {@link Component} gains focus
     * 
     * @param component
     * @param runnable
     */
    public static void invokeWhenGainedFocus(final Component component, final Runnable runnable) {
        component.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(final FocusEvent e) {
                runnable.run();
                component.removeFocusListener(this);
            }
        });
    }

    /**
     * Just for visual testing of {@link #createInputLimiter(int)} method functionality
     * 
     * @param args
     */
    public static void main(final String[] args) {
        final JTextArea area = new JTextArea();
        area.getDocument().addDocumentListener(createInputLimiter(5));
        SimpleLauncher.show("Caption", new BorderLayout(), area);
    }

}
