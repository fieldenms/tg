package ua.com.fielden.platform.entity.annotation.factory;

import ua.com.fielden.platform.entity.annotation.MapTo;

public class MapToAnnotation {

    private final long scale;
    private final long precision;

    public MapToAnnotation(final long scale, final long precision) {
        this.scale = scale;
        this.precision = precision;
    }

    public MapTo newInstance() {
        return new MapTo() {

            @Override
            public Class<MapTo> annotationType() {
                return MapTo.class;
            }

            @Override
            public String value() {
                return "";
            }

            @Override
            public long length() {
                return 0;
            }

            @Override
            public long precision() {
                return precision;
            }

            @Override
            public long scale() {
                return scale;
            }

        };
    }
}
