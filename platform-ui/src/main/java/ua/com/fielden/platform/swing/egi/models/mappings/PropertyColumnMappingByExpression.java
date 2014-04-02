/**
 *
 */
package ua.com.fielden.platform.swing.egi.models.mappings;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.components.bind.development.BoundedValidationLayer;
import ua.com.fielden.platform.swing.egi.EditorComponent;
import ua.com.fielden.platform.swing.egi.models.mappings.simplified.ITooltipGetter;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * This class represents read-only mapping to non-existent properties (i.e. having only getter) or properties not annotated with {@link Observable}. In such cases binding couldn't
 * be used, so the only way is just to display actual value (using Jexl).
 * 
 * @author TG Team
 */
@SuppressWarnings("unchecked")
public class PropertyColumnMappingByExpression<T extends AbstractEntity> extends ReadonlyPropertyColumnMapping<T> {

    public PropertyColumnMappingByExpression(final Class<T> entityClass,//
            final String propertyName, //
            final String columnName, //
            final Integer prefSize, //
            final String headerTooltip, //
            final ITooltipGetter<T> tooltipGetter, //
            final Action clickAction, //
            final ColumnTotals columnTotals, //
            final AggregationFunction<T> aggregationFunction) {
        super(entityClass, propertyName, columnName, prefSize, headerTooltip, tooltipGetter, clickAction, columnTotals, aggregationFunction, false);
    }

    public PropertyColumnMappingByExpression(final Class<T> entityClass,//
            final String propertyName, //
            final Class<?> propertyType, final String columnName, //
            final Integer prefSize, //
            final String headerTooltip, //
            final ITooltipGetter<T> tooltipGetter, //
            final Action clickAction, //
            final ColumnTotals columnTotals, //
            final AggregationFunction<T> aggregationFunction) {
        super(propertyName, propertyType, columnName, prefSize, headerTooltip, tooltipGetter, clickAction, columnTotals, aggregationFunction, false);
    }

    @Override
    public JComponent getCellRendererComponent(final T entity, final Object value, final boolean isSelected, final boolean hasFocus, final JTable table, final int row, final int column) {
        final Result result;
        if (entity.getProperty(getPropertyName()) == null) {
            result = Result.successful(entity);
        } else {
            final MetaProperty property = entity.getProperty(getPropertyName());
            if (property.isValid() && !property.hasWarnings()) {
                result = Result.successful(entity);
            } else if (!property.isValid()) {
                result = property.getFirstFailure();
            } else {
                result = property.getFirstWarning();
            }
        }

        if (isCheckBox()) {
            ((JCheckBox) getView()).setSelected(value != null ? (Boolean) value : false);
        } else {
            ((JLabel) getView()).setText(EntityUtils.toString(value, getColumnClass()));
        }

        getLayer().setResult(result);
        if (result.isSuccessfulWithoutWarning()) {
            return getView();
        } else {
            getLayer().setView(getView());
            return getLayer();
        }
    }

    @Override
    public boolean isPropertyEditable(final T entity) {
        return false;
    }

    @Override
    public boolean isNavigableTo(final T entity) {
        return false;
    }

    /**
     * Not used in this class
     */
    @Override
    public EditorComponent<? extends BoundedValidationLayer<? extends JComponent>, ? extends JComponent> createBoundedEditorFor(final T entity) {
        return null;
    }

}
