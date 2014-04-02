package ua.com.fielden.platform.example.swing.egi.performance;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.egi.models.mappings.ReadonlyPropertyColumnMapping;
import ua.com.fielden.platform.swing.egi.models.mappings.simplified.ITooltipGetter;

public class ReadonlyPropertyColumnMappingsForExample<T extends AbstractEntity<?>> extends ReadonlyPropertyColumnMapping<T> {

    public ReadonlyPropertyColumnMappingsForExample(final String propertyName, final Class<?> propertyType, final String columnName, final Integer prefSize, final String headerTooltip, final ITooltipGetter<T> tooltipGetter) {
        super(propertyName, propertyType, columnName, prefSize, headerTooltip, tooltipGetter, null, null, null, false);
    }

    @Override
    public JComponent getCellRendererComponent(final T entity, final Object value, final boolean isSelected, final boolean hasFocus, final JTable table, final int row, final int column) {
        if (isCheckBox()) {
            ((JCheckBox) getView()).setSelected((Boolean) entity.get(getPropertyName()));
        } else {
            ((JLabel) getView()).setText(entity.get(getPropertyName()).toString());
        }
        return getView();
    }
}
