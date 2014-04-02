package ua.com.fielden.platform.swing.categorychart;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * 
 * 
 * @author oleh
 * 
 */
public class EntityWrapper implements Comparable<EntityWrapper> {

    private final Comparable<?> comparableEntity;

    public EntityWrapper(final Comparable<?> entity) {
        this.comparableEntity = entity;
    }

    @Override
    public String toString() {
        if (comparableEntity != null) {
            return comparableEntity.toString();
        }
        return "UNKNOWN";
    }

    public String getDesc() {
        if (comparableEntity != null) {
            if (comparableEntity instanceof AbstractEntity<?>) {
                return ((AbstractEntity) comparableEntity).getDesc();
            } else {
                return comparableEntity.toString();
            }
        }
        return "UNKNOWN";
    }

    @Override
    public int compareTo(final EntityWrapper o) {
        if (comparableEntity == null) {
            if (o.getEntity() == null) {
                return 0;
            } else {
                return -1;
            }
        } else {
            if (o.getEntity() == null) {
                return 1;
            } else {
                return getEntity().compareTo(o.getEntity());
            }
        }
    }

    public Comparable getEntity() {
        return comparableEntity;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        final EntityWrapper wrappedEntity = (EntityWrapper) obj;
        if ((getEntity() == null && getEntity() != wrappedEntity.getEntity()) || (getEntity() != null && !getEntity().equals(wrappedEntity.getEntity()))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (getEntity() != null ? getEntity().hashCode() : 0);
        return result;
    }

}
