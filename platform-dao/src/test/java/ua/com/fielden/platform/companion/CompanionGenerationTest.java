package ua.com.fielden.platform.companion;

import org.junit.Test;
import ua.com.fielden.platform.audit.IAuditTypeFinder;
import ua.com.fielden.platform.dao.AbstractEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionIsGenerated;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.sample.domain.AuditedEntity;
import ua.com.fielden.platform.sample.domain.TgActivatableVehicleModel;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static ua.com.fielden.platform.utils.CollectionUtil.concatList;

/// Companion objects for entities annotated with [CompanionIsGenerated] are created at runtime.
///
public class CompanionGenerationTest extends AbstractDaoTestCase {

    private final IAuditTypeFinder a3tFinder = getInstance(IAuditTypeFinder.class);
    private final ICompanionGenerator coGenerator = getInstance(ICompanionGenerator.class);
    private final ICompanionObjectFinder coFinder = getInstance(ICompanionObjectFinder.class);

    @Test
    public void companion_generated_for_an_entity_type_returns_that_type_from_getEntityType() {
        assertThat(entityTypes()).allSatisfy(type -> assertThat(coGenerator.generateCompanion(type).getEntityType()).isEqualTo(type));
    }

    @Test
    public void companion_generated_for_an_entity_type_returns_its_key_type_from_getKeyType() {
        assertThat(coGenerator.generateCompanion(TgActivatableVehicleModel.class).getKeyType()).isEqualTo(String.class);
        assertThat(auditTypes()).allSatisfy(type -> assertThat(coGenerator.generateCompanion(type).getKeyType()).isEqualTo(DynamicEntityKey.class));
    }

    @Test
    public void companion_generated_for_an_entity_type_creates_instances_of_that_type_with_new_() {
        assertThat(entityTypes()).allSatisfy(type -> assertThat(coGenerator.generateCompanion(type).new_()).isInstanceOf(type));
    }

    @Test
    public void companion_found_as_uninstrumented_for_an_entity_type_with_a_generated_companion_is_uninstrumented() {
        assertThat(auditTypes()).allSatisfy(type -> assertThat(((AbstractEntityDao<?>) coFinder.find(type, true)).instrumented()).isFalse());
    }

    @Test
    public void companion_found_as_instrumented_for_an_entity_type_with_a_generated_companion_is_instrumented() {
        assertThat(auditTypes()).allSatisfy(type -> assertThat(((AbstractEntityDao<?>) coFinder.find(type, false)).instrumented()).isTrue());
    }

    /// One audit type of each kind.
    /// Each of them is annotated with [CompanionIsGenerated].
    ///
    private List<Class<? extends AbstractEntity<?>>> auditTypes() {
        final var navigator = a3tFinder.navigate(AuditedEntity.class);
        return List.of(navigator.auditEntityType(), navigator.auditPropType(), navigator.synAuditEntityType(), navigator.synAuditPropType());
    }

    /// [#auditTypes()] and `TgActivatableVehicleModel`, which stands in for an entity type that is not an audit type.
    /// The test domain has no such type annotated with [CompanionIsGenerated].
    /// Unlike the companion finder, [ICompanionGenerator] does not require this annotation.
    ///
    private List<Class<? extends AbstractEntity<?>>> entityTypes() {
        return concatList(auditTypes(),
                          List.of(TgActivatableVehicleModel.class));
    }

}
