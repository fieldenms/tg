package ua.com.fielden.platform.meta;

import org.junit.Test;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.IDbVersionProvider;
import ua.com.fielden.platform.meta.Assertions.EntityA;
import ua.com.fielden.platform.meta.PropertyMetadata.Calculated;
import ua.com.fielden.platform.meta.PropertyTypeMetadata.Primitive;
import ua.com.fielden.platform.persistence.types.PlatformHibernateTypeMappings;
import ua.com.fielden.platform.sample.domain.TgBogieLocation;
import ua.com.fielden.platform.sample.domain.TgFuelType;

import java.util.List;

import static ua.com.fielden.platform.entity.query.IDbVersionProvider.constantDbVersion;

public class UnionEntityMetadataTest {

    private static final IDbVersionProvider dbVersionProvider = constantDbVersion(DbVersion.MSSQL);
    private static final TestDomainMetadataGenerator generator = TestDomainMetadataGenerator.wrap(new DomainMetadataGenerator(new PlatformHibernateTypeMappings.Provider(dbVersionProvider).get(), dbVersionProvider));

    @Test
    public void union_entity_gets_union_nature() {
        EntityA.of(generator.forEntity(TgBogieLocation.class))
                .assertIs(EntityMetadata.Union.class)
                .assertJavaType(TgBogieLocation.class);
    }

    /**
     * Assert that the following properties have metadata generated for a union entity:
     * <ul>
     *  <li> Union members.
     *  <li> "key", "id", "desc", always calculated.
     *  <li> Properties common to union members, always calculated.
     * <ul>
     */
    @Test
    public void union_entity_properties_metadata() {
        EntityA.of(generator.forEntity(TgBogieLocation.class))
                .assertPropertiesExist(List.of("wagonSlot", "workshop"))
                .assertPropertiesExist(List.of("key", "id", "desc"))
                .assertPropertiesExist(List.of("fuelType"));
    }

    @Test
    public void union_entity_property_id() {
        EntityA.of(generator.forEntity(TgBogieLocation.class))
                .assertProperty("id", p -> p
                        .assertIs(Calculated.class)
                        .assertType(t -> t.assertIs(Primitive.class).assertJavaType(Long.class)));
    }

    @Test
    public void union_entity_property_key() {
        EntityA.of(generator.forEntity(TgBogieLocation.class))
                .assertProperty("key", p -> p
                        .assertIs(Calculated.class)
                        .assertType(t -> t.assertIs(Primitive.class).assertJavaType(String.class)));
    }

    @Test
    public void union_entity_property_desc() {
        EntityA.of(generator.forEntity(TgBogieLocation.class))
                .assertProperty("desc", a -> a
                        .assertIs(Calculated.class)
                        .assertType(t -> t.assertIs(Primitive.class).assertJavaType(String.class)));
    }

    @Test
    public void union_entity_common_properties() {
        EntityA.of(generator.forEntity(TgBogieLocation.class))
                .assertProperty("fuelType", a -> a
                        .assertIs(Calculated.class)
                        .assertType(t -> t.assertIs(PropertyTypeMetadata.Entity.class).assertJavaType(TgFuelType.class)));
    }

}
