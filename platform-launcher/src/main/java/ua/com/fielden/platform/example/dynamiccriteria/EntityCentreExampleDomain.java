package ua.com.fielden.platform.example.dynamiccriteria;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.domain.PlatformDomainTypes;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.example.dynamiccriteria.entities.SimpleCompositeEntity;
import ua.com.fielden.platform.example.dynamiccriteria.entities.SimpleECEEntity;
import ua.com.fielden.platform.example.dynamiccriteria.entities.SimpleNestedEntity;

public class EntityCentreExampleDomain implements IApplicationDomainProvider {

    private static final List<Class<? extends AbstractEntity<?>>> entityTypes = new ArrayList<Class<? extends AbstractEntity<?>>>();
    private static final List<Class<? extends AbstractEntity<?>>> domainTypes = new ArrayList<Class<? extends AbstractEntity<?>>>();

    static void add(final Class<? extends AbstractEntity<?>> domainType) {
        entityTypes.add(domainType);
        domainTypes.add(domainType);
    }

    static {
        entityTypes.addAll(PlatformDomainTypes.types);
        add(SimpleECEEntity.class);
        add(SimpleNestedEntity.class);
        add(SimpleCompositeEntity.class);
    }

    @Override
    public List<Class<? extends AbstractEntity<?>>> entityTypes() {
        return entityTypes;
    }

    public List<Class<? extends AbstractEntity<?>>> domainTypes() {
        return domainTypes;
    }
}
