package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.Annotation;

/**
 * A convenient representation of annotation {@link MapEntityTo} that can be used to easily instantiate this annotation.
 *
 * @author TG Team
 *
 */
public class MapEntityToOverride implements Annotation, MapEntityTo {

    private final String value;

    public MapEntityToOverride(final String value) {
        this.value = value;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public int hashCode() {
        return 127 * "value".hashCode() ^ value().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof MapEntityTo)) {
            return false;
        }

        final MapEntityTo annotation = (MapEntityTo) obj;
        return annotation.value().equals(value());
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return MapEntityTo.class;
    }

}
