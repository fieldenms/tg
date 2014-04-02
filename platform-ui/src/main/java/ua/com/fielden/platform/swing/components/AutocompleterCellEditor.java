package ua.com.fielden.platform.swing.components;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.TreeCellEditor;

import ua.com.fielden.platform.swing.components.smart.autocompleter.development.AutocompleterTextFieldLayer;

import com.jidesoft.grid.JideCellEditorAdapter;
import com.jidesoft.grid.JideTable;

/**
 * This is a cell editor, which provides Autocompleter support.
 * 
 * @author 01es
 */

public class AutocompleterCellEditor extends AbstractCellEditor implements TableCellEditor, TreeCellEditor {
    private static final long serialVersionUID = 1L;
    //
    //  Instance Variables
    //
    /** The Swing component being edited. */
    protected final JComponent editorComponent;
    /**
     * The delegate class which handles all methods sent from the <code>CellEditor</code>.
     */
    protected EditorDelegate delegate;
    /**
     * An integer specifying the number of clicks needed to start editing. Even if <code>clickCountToStart</code> is defined as zero, it will not initiate until a click occurs.
     */
    protected int clickCountToStart = 1;

    /**
     * Constructs a <code>DefaultCellEditor</code> that uses an Autocompleter.
     * 
     * @param autocompleter
     *            -- a <code>Autocompleter</code> instance
     * @param tble
     *            -- table to be associated with
     */
    public AutocompleterCellEditor(final AutocompleterTextFieldLayer<?> autocompleter, final JideTable table) {
        // final JTextField textField = autocompleter.getAutocompleter().getTextComponent();
        editorComponent = autocompleter;
        this.clickCountToStart = 2;
        delegate = new EditorDelegate() {
            private static final long serialVersionUID = 1L;

            @Override
            public void setValue(final Object value) {
                autocompleter.getView().setText((value != null) ? value.toString() : "");
            }

            @Override
            public Object getCellEditorValue() {
                return autocompleter.getView().getText();
            }
        };
        autocompleter.getView().addActionListener(delegate);

        autocompleter.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(final FocusEvent e) {
                autocompleter.getView().requestFocus();
            }
        });

        // JTable keeps the focus at all times therefore it is necessary to programmatically change autocompleter state
        table.addCellEditorListener(new JideCellEditorAdapter() {
            @Override
            public void editingStarted(final ChangeEvent e) {
                if (e.getSource() instanceof AutocompleterCellEditor) {
                    autocompleter.getAutocompleter().focusGained(null);
                }
            }

            @Override
            public void editingStopped(final ChangeEvent e) {
                if (e.getSource() instanceof AutocompleterCellEditor) {
                    autocompleter.getAutocompleter().focusLost(null);
                }
            }

            @Override
            public void editingCanceled(final ChangeEvent e) {
                if (e.getSource() instanceof AutocompleterCellEditor) {
                    autocompleter.getAutocompleter().focusLost(null);
                }
            }
        });
    }

    /**
     * Returns a reference to the editor component.
     * 
     * @return the editor <code>Component</code>
     */
    public Component getComponent() {
        return editorComponent;
    }

    //
    //  Modifying
    //

    /**
     * Specifies the number of clicks needed to start editing.
     * 
     * @param count
     *            an int specifying the number of clicks needed to start editing
     * @see #getClickCountToStart
     */
    public void setClickCountToStart(final int count) {
        clickCountToStart = count;
    }

    /**
     * Returns the number of clicks needed to start editing.
     * 
     * @return the number of clicks needed to start editing
     */
    public int getClickCountToStart() {
        return clickCountToStart;
    }

    //
    //  Override the implementations of the superclass, forwarding all methods
    //  from the CellEditor interface to our delegate.
    //

    /**
     * Forwards the message from the <code>CellEditor</code> to the <code>delegate</code>.
     * 
     * @see EditorDelegate#getCellEditorValue
     */
    public Object getCellEditorValue() {
        return delegate.getCellEditorValue();
    }

    /**
     * Forwards the message from the <code>CellEditor</code> to the <code>delegate</code>.
     * 
     * @see EditorDelegate#isCellEditable(EventObject)
     */
    @Override
    public boolean isCellEditable(final EventObject anEvent) {
        return delegate.isCellEditable(anEvent);
    }

    /**
     * Forwards the message from the <code>CellEditor</code> to the <code>delegate</code>.
     * 
     * @see EditorDelegate#shouldSelectCell(EventObject)
     */
    @Override
    public boolean shouldSelectCell(final EventObject anEvent) {
        return delegate.shouldSelectCell(anEvent);
    }

    /**
     * Forwards the message from the <code>CellEditor</code> to the <code>delegate</code>.
     * 
     * @see EditorDelegate#stopCellEditing
     */
    @Override
    public boolean stopCellEditing() {
        return delegate.stopCellEditing();
    }

    /**
     * Forwards the message from the <code>CellEditor</code> to the <code>delegate</code>.
     * 
     * @see EditorDelegate#cancelCellEditing
     */
    @Override
    public void cancelCellEditing() {
        delegate.cancelCellEditing();
    }

    //
    //  Implementing the TreeCellEditor Interface
    //

    /** Implements the <code>TreeCellEditor</code> interface. */
    public Component getTreeCellEditorComponent(final JTree tree, final Object value, final boolean isSelected, final boolean expanded, final boolean leaf, final int row) {
        final String stringValue = tree.convertValueToText(value, isSelected, expanded, leaf, row, false);

        delegate.setValue(stringValue);
        return editorComponent;
    }

    //
    //  Implementing the CellEditor Interface
    //
    /** Implements the <code>TableCellEditor</code> interface. */
    public Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected, final int row, final int column) {
        delegate.setValue(value);
        return editorComponent;
    }

    //
    //  Protected EditorDelegate class
    //

    /**
     * The protected <code>EditorDelegate</code> class.
     */
    protected class EditorDelegate implements ActionListener, ItemListener, Serializable {

        private static final long serialVersionUID = 1L;
        /** The value of this cell. */
        protected Object value;

        /**
         * Returns the value of this cell.
         * 
         * @return the value of this cell
         */
        public Object getCellEditorValue() {
            return value;
        }

        /**
         * Sets the value of this cell.
         * 
         * @param value
         *            the new value of this cell
         */
        public void setValue(final Object value) {
            this.value = value;
        }

        /**
         * Returns true if <code>anEvent</code> is <b>not</b> a <code>MouseEvent</code>. Otherwise, it returns true if the necessary number of clicks have occurred, and returns
         * false otherwise.
         * 
         * @param anEvent
         *            the event
         * @return true if cell is ready for editing, false otherwise
         * @see #setClickCountToStart
         * @see #shouldSelectCell
         */
        public boolean isCellEditable(final EventObject anEvent) {
            if (anEvent instanceof MouseEvent) {
                return ((MouseEvent) anEvent).getClickCount() >= clickCountToStart;
            }
            return true;
        }

        /**
         * Returns true to indicate that the editing cell may be selected.
         * 
         * @param anEvent
         *            the event
         * @return true
         * @see #isCellEditable
         */
        public boolean shouldSelectCell(final EventObject anEvent) {
            return true;
        }

        /**
         * Returns true to indicate that editing has begun.
         * 
         * @param anEvent
         *            the event
         */
        public boolean startCellEditing(final EventObject anEvent) {
            return true;
        }

        /**
         * Stops editing and returns true to indicate that editing has stopped. This method calls <code>fireEditingStopped</code>.
         * 
         * @return true
         */
        public boolean stopCellEditing() {
            fireEditingStopped();
            return true;
        }

        /**
         * Cancels editing. This method calls <code>fireEditingCanceled</code>.
         */
        public void cancelCellEditing() {
            fireEditingCanceled();
        }

        /**
         * When an action is performed, editing is ended.
         * 
         * @param e
         *            the action event
         * @see #stopCellEditing
         */
        public void actionPerformed(final ActionEvent e) {
            AutocompleterCellEditor.this.stopCellEditing();
        }

        /**
         * When an item's state changes, editing is ended.
         * 
         * @param e
         *            the action event
         * @see #stopCellEditing
         */
        public void itemStateChanged(final ItemEvent e) {
            AutocompleterCellEditor.this.stopCellEditing();
        }
    }

} // End of class

