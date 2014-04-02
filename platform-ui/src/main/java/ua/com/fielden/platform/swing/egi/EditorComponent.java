/**
 *
 */
package ua.com.fielden.platform.swing.egi;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import ua.com.fielden.platform.swing.components.ValidationLayer;

/**
 * Class, representing editor component for {@link EntityGridInspector}. Instances of this class should specify two components : top-level editor component - the one that would be
 * returned as {@link TableCellEditor} component (e.g. {@link ValidationLayer}), and the bottom-most editor component - the one that should actually receive focus during editing
 * (e.g. underlying {@link JTextField})
 * 
 * @author Yura
 * 
 * @param <ColumnType>
 * @param <CellEditorComponentType>
 *            - type of top-level editor component (e.g. {@link ValidationLayer})
 * @param <EditorItselfType>
 *            - type of bottom-most editor component (e.g. {@link JTextField})
 */
public class EditorComponent<CellEditorComponentType extends JComponent, EditorItselfType extends JComponent> {

    private final CellEditorComponentType cellEditorComponent;

    private final EditorItselfType editorItself;

    /**
     * Creates {@link EditorComponent} instance with references to top-level and bottom-most editor components set.
     * 
     * @param cellEditorComponent
     * @param editorItself
     */
    public EditorComponent(final CellEditorComponentType cellEditorComponent, final EditorItselfType editorItself) {
        this.cellEditorComponent = cellEditorComponent;
        this.editorItself = editorItself;
    }

    /**
     * Returns reference to top-level editor component
     * 
     * @return
     */
    public CellEditorComponentType getCellEditorComponent() {
        return cellEditorComponent;
    }

    /**
     * Returns reference to bottom-most editor component
     * 
     * @return
     */
    public EditorItselfType getEditorItself() {
        return editorItself;
    }

}
