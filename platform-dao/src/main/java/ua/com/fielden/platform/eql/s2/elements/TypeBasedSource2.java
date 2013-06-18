package ua.com.fielden.platform.eql.s2.elements;

import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.dao.EntityMetadata;
import ua.com.fielden.platform.entity.AbstractEntity;

public class TypeBasedSource2 extends AbstractSource2 {
    private EntityMetadata<? extends AbstractEntity<?>> entityMetadata;

    public TypeBasedSource2(final EntityMetadata<? extends AbstractEntity<?>> entityMetadata, final String alias, final DomainMetadataAnalyser domainMetadataAnalyser) {
	super(alias, domainMetadataAnalyser);
	this.entityMetadata = entityMetadata;
	if (entityMetadata == null) {
	    throw new IllegalStateException("Missing entity persistence metadata for entity type: " + sourceType());
	}
    }

    @Override
    public Class<? extends AbstractEntity<?>> sourceType() {
	return entityMetadata.getType();
    }

    @Override
    public List<EntValue2> getValues() {
	return Collections.emptyList();
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = super.hashCode();
	result = prime * result + ((entityMetadata == null) ? 0 : entityMetadata.hashCode());
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (!super.equals(obj)) {
	    return false;
	}
	if (!(obj instanceof TypeBasedSource2)) {
	    return false;
	}
	final TypeBasedSource2 other = (TypeBasedSource2) obj;
	if (entityMetadata == null) {
	    if (other.entityMetadata != null) {
		return false;
	    }
	} else if (!entityMetadata.equals(other.entityMetadata)) {
	    return false;
	}
	return true;
    }
}