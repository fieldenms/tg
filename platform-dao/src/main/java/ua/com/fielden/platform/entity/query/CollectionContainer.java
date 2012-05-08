package ua.com.fielden.platform.entity.query;

import java.util.Collection;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;

public final class CollectionContainer<R extends AbstractEntity<?>> {
    private Collection<R> data;
    private List<EntityContainer<R>> containers;

    public CollectionContainer(final Collection<R> data, final List<EntityContainer<R>> containers) {
	super();
	this.data = data;
	this.containers = containers;
    }

    public Collection<R> instantiate(final EntityFactory entFactory, final boolean userViewOnly) {
	for (final EntityContainer<R> container : containers) {
	    if (!container.notYetInitialised()) {
		data.add(container.instantiate(entFactory, userViewOnly));
	    }
	}

	return data;
    }
}