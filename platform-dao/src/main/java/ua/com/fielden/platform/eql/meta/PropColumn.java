package ua.com.fielden.platform.eql.meta;

import jakarta.annotation.Nullable;

import java.util.Objects;

public class PropColumn {
    public final String name;
    public final @Nullable Integer length;
    public final @Nullable Integer precision;
    public final @Nullable Integer scale;

    public PropColumn(final String name, final Integer length, final Integer precision, final Integer scale) {
        this.name = name.toUpperCase();
        this.length = length;
        this.precision = precision;
        this.scale = scale;
    }

    public PropColumn(final String name) {
        this(name, null, null, null);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + ((length == null) ? 0 : length.hashCode());
        result = prime * result + ((precision == null) ? 0 : precision.hashCode());
        result = prime * result + ((scale == null) ? 0 : scale.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof PropColumn)) {
            return false;
        }
        
        final PropColumn other = (PropColumn) obj;
        
        return Objects.equals(name, other.name) && (length == other.length) && (precision == other.precision) && (scale == other.scale);
    }
}
