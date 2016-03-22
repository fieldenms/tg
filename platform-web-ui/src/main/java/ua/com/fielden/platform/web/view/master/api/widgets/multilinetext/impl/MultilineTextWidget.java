package ua.com.fielden.platform.web.view.master.api.widgets.multilinetext.impl;

import java.lang.reflect.Method;
import java.util.Map;

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

    private final Class<? extends AbstractEntity<?>> entityType;

    /**
     * Creates {@link MultilineTextWidget} from <code>entityType</code> type and <code>propertyName</code>.
     *
     * @param titleDesc
     * @param propertyName
     */
    public MultilineTextWidget(final Pair<String, String> titleDesc, final Class<? extends AbstractEntity<?>> entityType, final String propertyName) {
        super("editors/tg-multiline-text-editor", titleDesc, propertyName);
        this.entityType = entityType;
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final Map<String, Object> attributes = super.createCustomAttributes();
        final Pair<Class<?>, String> typeAndName = PropertyTypeDeterminator.transform(entityType, propertyName());
        final Class<?> propertyType = PropertyTypeDeterminator.determinePropertyType(typeAndName.getKey(), typeAndName.getValue());
        try {
            final Method setter = Reflector.getMethod(typeAndName.getKey(), "set" + typeAndName.getValue().toUpperCase().charAt(0) + typeAndName.getValue().substring(1), propertyType);
            final Max maxAnnotation = AnnotationReflector.getAnnotation(setter, Max.class);
            if (maxAnnotation != null) {
                attributes.put("max", maxAnnotation.value());
            }
        } catch (final NoSuchMethodException e) {
            e.printStackTrace();
        }
        return attributes;
    }

    public MultilineTextWidget resizable() {
        // TODO implement
        // TODO implement
        // TODO implement
        // TODO implement
        // TODO implement

        // TODO must provide an ability to specify whether multiline text widget is resizable or not. Also provide an attribute in the appropriate
        // polymer component that specify whether multiline text widget is resizable or not.
        return this;
    }
}
