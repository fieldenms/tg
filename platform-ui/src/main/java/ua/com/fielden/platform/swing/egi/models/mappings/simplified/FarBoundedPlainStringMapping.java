/**
 *
 */
package ua.com.fielden.platform.swing.egi.models.mappings.simplified;

import javax.swing.JTextField;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.bind.development.BoundedValidationLayer;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.EditorCase;
import ua.com.fielden.platform.swing.egi.EditorComponent;
import static ua.com.fielden.platform.swing.egi.models.mappings.ColumnTotals.NO_TOTALS;

/**
 * Not tested
 * 
 * @author Yura
 * 
 * @param <T>
 */
@SuppressWarnings("unchecked")
public class FarBoundedPlainStringMapping<T extends AbstractEntity> extends BoundedPlainStringMapping<T> {

    public FarBoundedPlainStringMapping(final Class<T> entityClass, final String propertyName, final String columnName, final Integer prefSize, final String headerTooltip, final ITooltipGetter<T> tooltipGetter, final EditorCase editorCase, final IOnCommitAction<T>... onCommitActions) {
        super(entityClass, propertyName, columnName, prefSize, headerTooltip, tooltipGetter, null, NO_TOTALS, null, editorCase, onCommitActions);
    }

    @Override
    public EditorComponent<BoundedValidationLayer<JTextField>, JTextField> createBoundedEditorFor(final T entity) {
        final ComponentFactory.IOnCommitAction[] onCommitActionWrappers = new ComponentFactory.IOnCommitAction[getOnCommitActions().length];
        for (int i = 0; i < getOnCommitActions().length; i++) {
            final IOnCommitAction<T> onCommitAction = getOnCommitActions()[i];
            onCommitActionWrappers[i] = new ComponentFactory.IOnCommitAction() {
                @Override
                public void postCommitAction() {
                    onCommitAction.postCommitAction(entity, getEntityGridInspector());
                }

                @Override
                public void postNotSuccessfulCommitAction() {
                    onCommitAction.postNotSuccessfulCommitAction(entity, getEntityGridInspector());
                }

                @Override
                public void postSuccessfulCommitAction() {
                    onCommitAction.postSuccessfulCommitAction(entity, getEntityGridInspector());
                }
            };
        }

        final String propName = getPropertyName();
        final BoundedValidationLayer<JTextField> boundedValidationLayer = ComponentFactory.createStringTextField(getCorrect(entity, propName), getLastPropertyName(propName), true, "string text field", getEditorCase(), onCommitActionWrappers);
        return new EditorComponent<BoundedValidationLayer<JTextField>, JTextField>(boundedValidationLayer, boundedValidationLayer.getView());
    }

    private AbstractEntity getCorrect(final AbstractEntity entity, final String propertyName) {
        final String[] propertyNames = propertyName.split("\\.");
        if (propertyNames.length <= 1) {
            return entity;
        } else {
            String propertyNameWithoutLast = "";
            for (int i = 0; i < propertyNames.length - 1; i++) {
                propertyNameWithoutLast += propertyNames[i] + ".";
            }
            propertyNameWithoutLast = propertyNameWithoutLast.substring(0, propertyNameWithoutLast.length() - 1);
            return (AbstractEntity) entity.get(propertyNameWithoutLast);
        }
    }

    private String getLastPropertyName(final String propertyName) {
        final String[] propertyNames = propertyName.split("\\.");
        return propertyNames[propertyNames.length - 1];
    }

}
