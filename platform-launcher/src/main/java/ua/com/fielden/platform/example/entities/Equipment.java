package ua.com.fielden.platform.example.entities;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Equipment business entity.
 *
 * @author nc
 */

public abstract class Equipment<T extends Comparable<T>> extends AbstractEntity<T> {

    protected Equipment() {
    }

    public Equipment (final Long id, final T key, final String desc) {
	super(id, key, desc);
    }
}