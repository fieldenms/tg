package ua.com.fielden.platform.swing.egi.models.mappings.simplified;

import static org.apache.commons.lang.StringUtils.isEmpty;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * This class assumes that property of each instance may also be {@link AbstractEntity} that is why it should be used carefully
 *
 * @author Yura
 *
 * @param <T>
 */
public class DefaultTooltipGetter<T extends AbstractEntity<?>> implements ITooltipGetter<T> {

    private final String propertyName;

    protected DefaultTooltipGetter() {
	this.propertyName = null;
    }

    public DefaultTooltipGetter(final String propertyName) {
	this.propertyName = propertyName;
    }

    @Override
    public String getTooltip(final T entity) {
	try {
	    final MetaProperty metaProperty = EntityUtils.findFirstFailedMetaProperty(entity, propertyName);
	    if (metaProperty.isValid() && !metaProperty.hasWarnings()) {
		// everything is valid - just showing value
		return getCorrectTooltip(entity);
	    } else {
		// MetaProperty, corresponding to last property in propertyName dot-notation is invalid - showing result message
		return !metaProperty.isValid() ? metaProperty.getFirstFailure().getMessage() : metaProperty.getFirstWarning().getMessage();
	    }
	} catch (final RuntimeException e) {
	    // in case it is not possible to find meta-property - it could be when one is using PropertyColumnMappingByExpression
	    return getCorrectTooltip(entity);
	}
    }

    private String getCorrectTooltip(final AbstractEntity<?> entity) {
	final Object value = isEmpty(propertyName) ? entity.getDesc() : entity.get(propertyName);
	final Class<?> valueType = isEmpty(propertyName) ? String.class : PropertyTypeDeterminator.determinePropertyType(entity.getType(), propertyName);
	if (value instanceof AbstractEntity) {
	    return value != null ? ((AbstractEntity<?>) value).getDesc() : null;
	} else {
	    final String tooltip = EntityUtils.formatTooltip(value, valueType);
	    return isEmpty(tooltip) ? null : tooltip;
	}
    }
}