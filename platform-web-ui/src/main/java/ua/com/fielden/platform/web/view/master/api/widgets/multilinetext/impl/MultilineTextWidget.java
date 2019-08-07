package ua.com.fielden.platform.web.view.master.api.widgets.multilinetext.impl;

import static ua.com.fielden.platform.entity.Mutator.SETTER;

import java.lang.reflect.Method;
import java.util.Map;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.validation.annotation.Max;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

/**
 * The implementation for web multiline text widgets.
 *
 * @author TG Team
 *
 */
public class MultilineTextWidget extends AbstractWidget {

    private static final Logger LOGGER = Logger.getLogger(MultilineTextWidget.class);

    private final Class<? extends AbstractEntity<?>> entityType;
    private int maxRows = 5;

    /**
     * Creates {@link MultilineTextWidget} from <code>entityType</code> type and <code>propertyName</code>.
     *
     * @param titleDesc
     * @param propertyName
     */
    public MultilineTextWidget(final Pair<String, String> titleDesc, final Class<? extends AbstractEntity<?>> entityType, final String propertyName) {
        super("polymer/@polymer/paper-input/paper-input", titleDesc, propertyName);
        this.entityType = entityType;
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final Map<String, Object> attributes = super.createCustomAttributes();
        final Pair<Class<?>, String> typeAndName = PropertyTypeDeterminator.transform(entityType, propertyName());
        final Class<?> propertyType = PropertyTypeDeterminator.determinePropertyType(typeAndName.getKey(), typeAndName.getValue());
        try {
            final Method setter = Reflector.getMethod(typeAndName.getKey(), SETTER.getName(typeAndName.getValue()), propertyType);
            final Max maxAnnotation = AnnotationReflector.getAnnotation(setter, Max.class);
            if (maxAnnotation != null) {
                attributes.put("max", maxAnnotation.value());
            }
        } catch (final NoSuchMethodException ex) {
            LOGGER.warn(ex);
        }
        attributes.put("max-rows", maxRows);
        return attributes;
    }

    public MultilineTextWidget setMaxVisibleRows(final int maxRows) {
        this.maxRows = maxRows;
        return this;
    }

}
