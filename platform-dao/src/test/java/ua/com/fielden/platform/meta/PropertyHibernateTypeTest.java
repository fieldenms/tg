package ua.com.fielden.platform.meta;

import org.junit.Test;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.meta.Assertions.EntityA;
import ua.com.fielden.platform.meta.PropertyTypeMetadata.Primitive;
import ua.com.fielden.platform.sample.domain.TgBogieLocation;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.markers.ISimpleMoneyType;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static ua.com.fielden.platform.entity.query.IDbVersionProvider.constantDbVersion;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.meta.PropertyHibernateTypeTest.Case.hasHibType;
import static ua.com.fielden.platform.meta.PropertyHibernateTypeTest.Case.noHibType;
import static ua.com.fielden.platform.persistence.types.PlatformHibernateTypeMappings.PLATFORM_HIBERNATE_TYPE_MAPPINGS;

public class PropertyHibernateTypeTest {

    private final TestDomainMetadataGenerator generator = TestDomainMetadataGenerator.wrap(
            new DomainMetadataGenerator(PLATFORM_HIBERNATE_TYPE_MAPPINGS, constantDbVersion(DbVersion.MSSQL)));

    @Test
    public void hibernate_type_is_attached() {
        runCases(Entity.class,
                 List.of(hasHibType("persistentPrimitive", PropertyMetadata.Persistent.class, Primitive.class),
                         hasHibType("calculatedPrimitive", PropertyMetadata.Calculated.class, Primitive.class),
                         hasHibType("transientPrimitive", PropertyMetadata.Transient.class, Primitive.class),
                         hasHibType("critOnlyPrimitive", PropertyMetadata.CritOnly.class, Primitive.class),

                         hasHibType("persistentComposite", PropertyMetadata.Persistent.class, PropertyTypeMetadata.Composite.class),
                         hasHibType("calculatedComposite", PropertyMetadata.Calculated.class, PropertyTypeMetadata.Composite.class),
                         hasHibType("transientComposite", PropertyMetadata.Transient.class, PropertyTypeMetadata.Composite.class),
                         hasHibType("critOnlyComposite", PropertyMetadata.CritOnly.class, PropertyTypeMetadata.Composite.class),

                         hasHibType("persistentEntity", PropertyMetadata.Persistent.class, PropertyTypeMetadata.Entity.class),
                         hasHibType("calculatedEntity", PropertyMetadata.Calculated.class, PropertyTypeMetadata.Entity.class),
                         hasHibType("transientEntity", PropertyMetadata.Transient.class, PropertyTypeMetadata.Entity.class),
                         hasHibType("critOnlyEntity", PropertyMetadata.CritOnly.class, PropertyTypeMetadata.Entity.class),

                         hasHibType("persistentUnionEntity", PropertyMetadata.Persistent.class, PropertyTypeMetadata.Entity.class),
                         hasHibType("calculatedUnionEntity", PropertyMetadata.Calculated.class, PropertyTypeMetadata.Entity.class),
                         hasHibType("transientUnionEntity", PropertyMetadata.Transient.class, PropertyTypeMetadata.Entity.class),
                         hasHibType("critOnlyUnionEntity", PropertyMetadata.CritOnly.class, PropertyTypeMetadata.Entity.class),

                         hasHibType("key", PropertyMetadata.Calculated.class, PropertyTypeMetadata.CompositeKey.class),
                         noHibType ("entities", PropertyMetadata.Transient.class, PropertyTypeMetadata.Collectional.class)
                 ));
    }

    record Case (String propName,
                 Class<? extends PropertyMetadata> propNature,
                 Class<? extends PropertyTypeMetadata> propType,
                 boolean hasHibType)
    {
        static Case hasHibType(String propName, Class<? extends PropertyMetadata> propNature,
                               Class<? extends PropertyTypeMetadata> propType) {
            return new Case(propName, propNature, propType, true);
        }
        static Case noHibType(String propName, Class<? extends PropertyMetadata> propNature,
                              Class<? extends PropertyTypeMetadata> propType) {
            return new Case(propName, propNature, propType, false);
        }
    }

    private void runCases(final Class<? extends AbstractEntity<?>> entityType, final Iterable<Case> cases) {
        final var entityA = EntityA.of(generator.forEntity(entityType));

        cases.forEach(c -> {
            final var propA = entityA.getProperty(c.propName);
            propA.assertIs(c.propNature())
                    .type().assertIs(c.propType());
            if (c.hasHibType()) {
                propA.assertHasHibType();
            } else {
                propA.assertNoHibType();
            }
        });
    }

    @KeyType(DynamicEntityKey.class)
    @MapEntityTo
    static class Entity extends AbstractEntity<DynamicEntityKey> {

        @IsProperty
        @MapTo
        private String persistentPrimitive;

        @IsProperty
        @ua.com.fielden.platform.entity.annotation.Calculated("1")
        private BigDecimal calculatedPrimitive;

        @IsProperty
        private Integer transientPrimitive;

        @IsProperty
        @CritOnly
        private String critOnlyPrimitive;

        @IsProperty
        @MapTo
        private Money persistentComposite;

        @IsProperty
        @Calculated("1")
        @PersistentType(userType = ISimpleMoneyType.class)
        private Money calculatedComposite;

        @IsProperty
        private Money transientComposite;

        @IsProperty
        @CritOnly
        private Money critOnlyComposite;

        @IsProperty
        @MapTo
        private Entity persistentEntity;

        @IsProperty
        @Calculated
        private Entity calculatedEntity;
        private static final ExpressionModel calculatedEntity_ = expr().prop("persistentEntity").model();

        @IsProperty
        private Entity transientEntity;

        @IsProperty
        @CritOnly
        private Entity critOnlyEntity;

        @IsProperty(Entity.class)
        private final Set<Entity> entities = new LinkedHashSet<>();

        @IsProperty
        @MapTo
        private TgBogieLocation persistentUnionEntity;

        @IsProperty
        @Calculated
        private TgBogieLocation calculatedUnionEntity;
        private static final ExpressionModel calculatedUnionEntity_ = expr().prop("persistentUnionEntity").model();

        @IsProperty
        private TgBogieLocation transientUnionEntity;

        @IsProperty
        @CritOnly
        private TgBogieLocation critOnlyUnionEntity;
    }

}
