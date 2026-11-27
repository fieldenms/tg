package ua.com.fielden.platform.companion;

import org.junit.Test;
import ua.com.fielden.platform.audit.IAuditCompanionGenerator;
import ua.com.fielden.platform.audit.IAuditTypeFinder;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.sample.domain.AuditedEntity;
import ua.com.fielden.platform.sample.domain.TgActivatableVehicleModel;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import static org.assertj.core.api.Assertions.assertThat;

/// Companion types for entities annotated with `@CompanionIsGenerated` are generated at runtime.
/// Each generated type is a new class that is retained once instantiated by the injector.
/// These tests ensure that repeated lookups reuse the generated type instead of generating a new one each time.
///
public class CompanionGenerationTest extends AbstractDaoTestCase {

    private final IAuditTypeFinder auditTypeFinder = getInstance(IAuditTypeFinder.class);

    @Test
    public void companions_found_for_an_audit_entity_type_are_instances_of_the_same_class() {
        assertCompanionsFoundAreInstancesOfTheSameClass(auditTypeFinder.navigate(AuditedEntity.class).auditEntityType());
    }

    @Test
    public void companions_found_for_an_audit_prop_entity_type_are_instances_of_the_same_class() {
        assertCompanionsFoundAreInstancesOfTheSameClass(auditTypeFinder.navigate(AuditedEntity.class).auditPropType());
    }

    @Test
    public void companions_found_for_a_synthetic_audit_entity_type_are_instances_of_the_same_class() {
        assertCompanionsFoundAreInstancesOfTheSameClass(auditTypeFinder.navigate(AuditedEntity.class).synAuditEntityType());
    }

    @Test
    public void companions_found_for_a_synthetic_audit_prop_entity_type_are_instances_of_the_same_class() {
        assertCompanionsFoundAreInstancesOfTheSameClass(auditTypeFinder.navigate(AuditedEntity.class).synAuditPropType());
    }

    /// This test passes with or without caching.
    /// It guards against a cache key that is too broad, which would make different entity types share a companion type.
    ///
    @Test
    public void companion_types_for_audit_entity_types_of_different_audited_types_are_different() {
        final var generator = getInstance(ICompanionGenerator.class);

        assertThat(generator.generateCompanion(auditTypeFinder.navigate(AuditedEntity.class).auditEntityType()))
                .isNotSameAs(generator.generateCompanion(auditTypeFinder.navigate(TgVehicle.class).auditEntityType()));
    }

    /// `TgActivatableVehicleModel` has no companion class of its own, which would otherwise clash with the name of the generated type.
    /// The test domain has no entity type annotated with `@CompanionIsGenerated` that is not an audit type.
    /// Therefore, this test uses the generator directly and instantiates the cached type through the injector, as the companion finder does.
    ///
    @Test
    public void companion_type_for_an_entity_type_that_is_not_an_audit_type_is_generated_once_and_can_be_instantiated() {
        final var generator = getInstance(ICompanionGenerator.class);

        final var coType1 = generator.generateCompanion(TgActivatableVehicleModel.class);
        final var coType2 = generator.generateCompanion(TgActivatableVehicleModel.class);

        assertThat(coType1).isSameAs(coType2);
        assertThat(CommonEntityDao.class).isAssignableFrom(coType1);
        assertThat(coType1.getAnnotation(EntityType.class).value()).isEqualTo(TgActivatableVehicleModel.class);

        final var co = (CommonEntityDao<?>) getInstance(coType1);
        assertThat(co.getEntityType()).isEqualTo(TgActivatableVehicleModel.class);
    }

    /// The injector, and thus the singleton generator, is shared by all tests, so its cache content depends on the order in which they run.
    /// This test uses its own generator instance, which starts with an empty cache.
    ///
    @Test
    public void generated_type_count_increases_only_when_a_companion_type_is_generated_for_a_new_base_entity_type() {
        final var generator = new CompanionGeneratorImpl(getInstance(IAuditCompanionGenerator.class));
        final var auditEntityType = auditTypeFinder.navigate(AuditedEntity.class).auditEntityType();
        assertThat(generator.generatedTypeCount()).isZero();

        generator.generateCompanion(auditEntityType);
        assertThat(generator.generatedTypeCount()).isEqualTo(1);

        generator.generateCompanion(auditEntityType);
        assertThat(generator.generatedTypeCount()).isEqualTo(1);

        generator.generateCompanion(TgActivatableVehicleModel.class);
        assertThat(generator.generatedTypeCount()).isEqualTo(2);
    }

    /// Companions are not singletons, so each lookup returns a new instance.
    /// Their classes must be the same, which means that the companion type was generated only once.
    ///
    private void assertCompanionsFoundAreInstancesOfTheSameClass(final Class<? extends AbstractEntity<?>> entityType) {
        final var coFinder = getInstance(ICompanionObjectFinder.class);

        final Object co1 = coFinder.find(entityType);
        final Object co2 = coFinder.find(entityType);

        assertThat(co1).isNotNull().isNotSameAs(co2);
        assertThat(co1.getClass()).isSameAs(co2.getClass());
    }

}
