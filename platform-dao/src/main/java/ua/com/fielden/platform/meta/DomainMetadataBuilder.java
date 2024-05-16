package ua.com.fielden.platform.meta;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.DbVersion;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DomainMetadataBuilder {
    public DomainMetadataBuilder(final Map<? extends Class, ? extends Class> hibTypesDefaults,
                                 final Injector hibTypesInjector,
                                 final Collection<? extends Class<? extends AbstractEntity<?>>> entityTypes,
                                 final DbVersion dbVersion)
    {
        throw new UnsupportedOperationException("Unimplemented");
    }

    public IDomainMetadata build() {
        throw new UnsupportedOperationException("Unimplemented");
    }
}
