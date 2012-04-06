package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;

public final class fetchAll<T extends AbstractEntity<?>> extends fetch<T> {

    /**
     * Mainly used for serialisation.
     */
    protected fetchAll() {

    }

    fetchAll(final Class<T> entityType) {
	super(entityType);
	withAll();
    }

}
