package ua.com.fielden.platform.entity.query;

import org.junit.Test;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.proxy.IIdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata.CritOnly;
import ua.com.fielden.platform.meta.PropertyMetadata.Plain;
import ua.com.fielden.platform.meta.PropertyTypeMetadata.CompositeKey;
import ua.com.fielden.platform.meta.TestDomainMetadata;
import ua.com.fielden.platform.sample.domain.TgAuthor;
import ua.com.fielden.platform.sample.domain.TgSystem;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgWagon;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.util.List;

import static org.junit.Assert.*;

public class IdOnlyProxiedEntityTypeCacheTest extends AbstractDaoTestCase {

    private final EntityFactory entityFactory = getInstance(EntityFactory.class);
    private final IIdOnlyProxiedEntityTypeCache idOnlyProxiedTypeCache = getInstance(IIdOnlyProxiedEntityTypeCache.class);
    private final TestDomainMetadata domainMetadata = TestDomainMetadata.wrap(getInstance(IDomainMetadata.class));

    @Test
    public void collectional_properties_are_proxied() {
        final var proxiedType = getIdOnlyProxiedTypeFor(TgWagon.class);
        assertNotNull(proxiedType);
        domainMetadata.forProperty(TgWagon.class, "slots").type().assertCollectional();
        final var entity = entityFactory.newEntity(proxiedType);
        final var propName = "slots";
        assertTrue("[%s.%s] should be proxied.".formatted(entity.getType().getSimpleName(), propName),
                entity.proxiedPropertyNames().contains(propName));
    }

    @Test
    public void plain_properties_of_persistent_entities_are_not_proxied() {
        final var proxiedType = getIdOnlyProxiedTypeFor(TgVehicleModel.class);
        assertNotNull(proxiedType);
        domainMetadata.forProperty(TgVehicleModel.class, "ordinaryIntProp").assertIs(Plain.class);
        assertNotProxied(entityFactory.newEntity(proxiedType), "ordinaryIntProp");
    }

    @Test
    public void property_id_is_not_proxied() {
        List.of(TgVehicleModel.class).forEach(type -> {
            final var proxiedType = getIdOnlyProxiedTypeFor(type);
            assertNotNull(proxiedType);
            assertNotProxied(entityFactory.newEntity(proxiedType), "id");
        });
    }

    @Test
    public void critOnly_properties_are_not_proxied() {
        final var proxiedType = getIdOnlyProxiedTypeFor(TgSystem.class);
        assertNotNull(proxiedType);
        domainMetadata.forProperty(TgSystem.class, "critOnlySingleCategory").assertIs(CritOnly.class);
        assertNotProxied(entityFactory.newEntity(proxiedType), "critOnlySingleCategory");
    }

    @Test
    public void composite_key_property_is_not_proxied() {
        final var proxiedType = getIdOnlyProxiedTypeFor(TgAuthor.class);
        assertNotNull(proxiedType);
        domainMetadata.forProperty(TgAuthor.class, "key").type().assertIs(CompositeKey.class);
        assertNotProxied(entityFactory.newEntity(proxiedType), "key");
    }

    // **************************************************************************
    // * Utils

    private <T extends AbstractEntity<?>> Class<? extends T> getIdOnlyProxiedTypeFor(final Class<T> originalType) {
        return idOnlyProxiedTypeCache.getIdOnlyProxiedTypeFor(originalType);
    }

    private void assertNotProxied(final AbstractEntity<?> entity, final CharSequence propName) {
        assertFalse("[%s.%s] should not be proxied.".formatted(entity.getType().getSimpleName(), propName),
                    entity.proxiedPropertyNames().contains(propName.toString()));
    }

}
