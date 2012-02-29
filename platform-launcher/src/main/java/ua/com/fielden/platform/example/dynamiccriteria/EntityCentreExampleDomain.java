package ua.com.fielden.platform.example.dynamiccriteria;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.domain.PlatformDomainTypes;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.example.dynamiccriteria.entities.SimpleECEEntity;

public class EntityCentreExampleDomain {

    public static final List<Class<? extends AbstractEntity>> entityTypes = new ArrayList<Class<? extends AbstractEntity>>();

    static void add(final Class<? extends AbstractEntity> domainType) {
	entityTypes.add(domainType);
    }

    static {
	entityTypes.addAll(PlatformDomainTypes.types);
	add(SimpleECEEntity.class);
    }
}
