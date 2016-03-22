package ua.com.fielden.platform.web.centre.api.resultset;

import java.util.Optional;

import org.apache.commons.lang.StringUtils;

/**
 * A structure for defining custom properties for entities to be presented on an entity centre.
 * <p>
 * Properties that are defined thus and added to an entity centre result set should have a custom value calculator that is associated with that entity centre, which is responsible
 * for calculating and assigning property values.
 * <p>
 * It should be noted that custom calculators should not have any heavy computations such as query execution. If this is required, EQL model based calculated properties should be
 * used, or a synthesised entity could be considered. The whole idea of custom properties defined via this structure is to simply carry either an assigned at the time of definition
 * value, or have values assigned dynamically as part of entity centre run process based on lightweight computations.
 * <p>
 * The preferred approach is to have values specified as part of property definition. These values would then be proliferated for each entity instance.
 *
 *
 * @author TG Team
 *
 * @param <T>
 */
public final class PropDef<T> {
    public final String title;
    public final String desc;
    public final Class<T> type;
    public final Optional<T> value;

    private PropDef(final String title, final String desc, final Class<T> type, final Optional<T> value) {
        this.title = title;
        this.desc = desc;
        this.type = type;
        this.value = value;
    }

    /**
     * Defines property by title, description and value.
     *
     * @param title
     * @param desc
     * @param value
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> PropDef<T> mkProp(final String title, final String desc, final T value) {
        if (StringUtils.isEmpty(title)) {
            throw new IllegalArgumentException("Property cannot be defined without a title.");
        }
        if (value == null) {
            throw new IllegalArgumentException(String.format("Value for property '%s' should not be null.", title));
        }
        return new PropDef<T>(title, desc, (Class<T>) value.getClass(), Optional.of(value));
    }

    /**
     * Defines property by title and value. Property description is assigned to the value of title.
     *
     * @param title
     * @param value
     * @return
     */
    public static <T> PropDef<T> mkProp(final String title, final T value) {
        return mkProp(title, title, value);
    }

    /**
     * Defines property by title, description and type.
     * <p>
     * No value is assigned, which means the should be a custom logic provided as part of entity centre definition for assigning values for thus defined property for each entity
     * instance to be retrieved.
     *
     * @param title
     * @param desc
     * @param type
     * @return
     */
    public static <T> PropDef<T> mkProp(final String title, final String desc, final Class<T> type) {
        if (StringUtils.isEmpty(title)) {
            throw new IllegalArgumentException("Property cannot be defined without a title.");
        }
        if (type == null) {
            throw new IllegalArgumentException(String.format("Type for property '%s' should not be null.", title));
        }
        return new PropDef<T>(title, desc, type, Optional.empty());
    }

    /**
     * Defines property by title and type. Property description is assigned to the value of title.
     * <p>
     * No value is assigned, which means the should be a custom logic provided as part of entity centre definition for assigning values for thus defined property for each entity
     * instance to be retrieved.
     *
     * @param title
     * @param desc
     * @param type
     * @return
     */
    public static <T> PropDef<T> mkProp(final String title, final Class<T> type) {
        return mkProp(title, title, type);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((desc == null) ? 0 : desc.hashCode());
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PropDef)) {
            return false;
        }

        final PropDef<?> other = (PropDef<?>) obj;

        if (desc == null) {
            if (other.desc != null) {
                return false;
            }
        } else if (!desc.equals(other.desc)) {
            return false;
        }

        if (title == null) {
            if (other.title != null) {
                return false;
            }
        } else if (!title.equals(other.title)) {
            return false;
        }

        if (type != other.type) { // classes can be compared by reference
            return false;
        }

        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }

        return true;
    }
}
