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
    private final String column;

    public MapEntityToOverride(final String value, final String column) {
        this.value = value;
        this.column = column;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public String keyColumn() {
        return column;
    }

    @Override
    public int hashCode() {
        return (127 * "value".hashCode() ^ value().hashCode()) +
                (127 * "keyColumn".hashCode() ^ keyColumn().hashCode());
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof MapEntityTo)) {
            return false;
        }

        final MapEntityTo annotation = (MapEntityTo) obj;
        return annotation.value().equals(value()) &&
                annotation.keyColumn().equals(keyColumn());
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return MapEntityTo.class;
    }

}
