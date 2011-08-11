package ua.com.fielden.platform.test.domain.entities;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Equipment business entity.
 *
 * @author nc
 */

public abstract class Equipment<T extends Comparable<T>> extends AbstractEntity<T> {

    private static final long serialVersionUID = -5978470863908642872L;

    protected Equipment() {
    }

    public Equipment (final Long id, final T key, final String desc) {
	super(id, key, desc);
    }
}