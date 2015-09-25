package ua.com.fielden.platform.entity.query;

import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;

public final class CollectionContainer<R extends AbstractEntity<?>> {
    private List<EntityContainer<R>> containers;

    public List<EntityContainer<R>> getContainers() {
        return containers;
    }

    public CollectionContainer(final List<EntityContainer<R>> containers) {
        super();
        this.containers = containers;
    }
}