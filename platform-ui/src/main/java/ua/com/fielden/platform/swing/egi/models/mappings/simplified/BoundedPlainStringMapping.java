/**
 *
 */
package ua.com.fielden.platform.swing.egi.models.mappings.simplified;

import javax.swing.Action;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.bind.development.BoundedValidationLayer;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.EditorCase;
import ua.com.fielden.platform.swing.egi.EditorComponent;
import ua.com.fielden.platform.swing.egi.models.mappings.AbstractLabelPropertyColumnMapping;
import ua.com.fielden.platform.swing.egi.models.mappings.AggregationFunction;
import ua.com.fielden.platform.swing.egi.models.mappings.ColumnTotals;

/**
 * Creates bounded mapping for plain {@link String} property of {@link AbstractEntity} class. This mapping uses {@link JFormattedTextField} as editor.
 * 
 * @author Yura
 * 
 * @param <T>
 * @param <K>
 */
@SuppressWarnings("unchecked")
public class BoundedPlainStringMapping<T extends AbstractEntity> extends AbstractLabelPropertyColumnMapping<T> {

    private final IOnCommitAction<T>[] onCommitActions;

    private final EditorCase editorCase;

    public BoundedPlainStringMapping(final Class<T> entityClass, final String propertyName, final String columnName, final Integer prefSize, final String headerTooltip, final ITooltipGetter<T> tooltipGetter, final Action clickAction, final ColumnTotals columnTotals, final AggregationFunction<T> aggregationFunction, final EditorCase editorCase, final IOnCommitAction<T>... onCommitActions) {
        super(entityClass, propertyName, columnName, prefSize, headerTooltip, tooltipGetter, clickAction, columnTotals, aggregationFunction);

        this.editorCase = editorCase;
        this.onCommitActions = onCommitActions;
    }

    @Override
    public EditorComponent<BoundedValidationLayer<JTextField>, JTextField> createBoundedEditorFor(final T entity) {
        final ComponentFactory.IOnCommitAction[] onCommitActionWrappers = EgiUtilities.convert(entity, getEntityGridInspector(), onCommitActions);
        final BoundedValidationLayer<JTextField> boundedValidationLayer = ComponentFactory.createStringTextField(entity, getPropertyName(), true, "string text field", editorCase, onCommitActionWrappers);
        return new EditorComponent<BoundedValidationLayer<JTextField>, JTextField>(boundedValidationLayer, boundedValidationLayer.getView());
    }

    public IOnCommitAction<T>[] getOnCommitActions() {
        return onCommitActions;
    }

    public EditorCase getEditorCase() {
        return editorCase;
    }

}
