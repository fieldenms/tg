package ua.com.fielden.platform.entity.query.model.elements;

import ua.com.fielden.platform.reflection.Finder;

public class RealEntityTypeDataProvider implements IEntQuerySourceDataProvider {

    private final Class entityType;

    public RealEntityTypeDataProvider(final Class entityType) {
	this.entityType = entityType;
    }

    @Override
    public Class propType(final String propSimpleName) {
	return Finder.findFieldByName(entityType, propSimpleName).getType();
    }

    @Override
    public Class parentType() {
	return entityType;
    }
}
