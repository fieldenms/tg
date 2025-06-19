package ua.com.fielden.platform.entity.union;

import com.google.inject.Injector;
import org.junit.Test;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.exceptions.EntityException;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.reflection.test_entities.SecondLevelEntity;
import ua.com.fielden.platform.reflection.test_entities.SimplePartEntity;
import ua.com.fielden.platform.reflection.test_entities.UnionEntityForReflector;
import ua.com.fielden.platform.sample.domain.EntityOne;
import ua.com.fielden.platform.sample.domain.EntityThree;
import ua.com.fielden.platform.sample.domain.EntityTwo;
import ua.com.fielden.platform.sample.domain.UnionEntity;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.meta.MetaProperty.ERR_REQUIRED;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;

/**
 * A test case covering union rules and definition of {@link AbstractUnionEntity} descendants.
 *
 * @author TG Team
 *
 */
public class AbstractUnionEntityTest {
    final Injector injector = new ApplicationInjectorFactory().add(new CommonEntityTestIocModuleWithPropertyFactory()).getInjector();
    final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void instantiation_of_correctly_defined_union_entity_succeeds() {
        try {
            factory.newEntity(UnionEntity.class);
        } catch (final Exception e) {
            fail("Instantiation should have succeeded.");
        }
    }

    @Test
    public void instantiation_of_union_entities_with_non_entity_typed_properties_fails() {
        try {
            factory.newEntity(UnionEntityWithKindOneError.class);
            fail("Instantiation should have been prevented.");
        } catch (final Exception ex) {
            assertEquals("Union entity should not contain properties of ordinary type. Check property [string].", ex.getCause().getMessage());
        }
    }

    @Test
    public void instantiation_of_union_entities_with_more_than_one_property_of_the_same_entity_type_fails() {
        try {
            factory.newEntity(UnionEntityWithKindTwoError.class);
            fail("Creation should have been prevented.");
        } catch (final Exception ex) {
            assertEquals("Union entity should contain only properties of unique types. Check property [propertyThree].", ex.getCause().getMessage());
        }
    }

    @Test
    public void union_entity_without_active_property_is_not_valid() {
        final var unionEntity = factory.newEntity(UnionEntity.class);
        final var isValidResult = unionEntity.isValid();
        assertFalse(isValidResult.isSuccessful());
        final var expectedError = ERR_REQUIRED.formatted(getTitleAndDesc(KEY, UnionEntity.class).getKey(), getEntityTitleAndDesc(UnionEntity.class).getKey());
        assertEquals(expectedError, isValidResult.getMessage());
    }

    @Test
    public void union_entity_with_active_property_is_valid() {
        final var unionEntity = factory.newEntity(UnionEntity.class)
            .setPropertyOne(factory.newEntity(EntityOne.class, 1L, "KEY VALUE"));
        assertTrue(unionEntity.isValid().isSuccessful());
    }

    @Test
    public void commonProperties_identifies_all_common_properties_amongst_union_properties() {
        final Set<String> commonProps = AbstractUnionEntity.commonProperties(UnionEntity.class);
        assertEquals(Set.of("desc", "stringProperty", "key"), commonProps);
    }

    @Test
    public void unionProperties_identifies_all_union_properties() {
        final List<Field> unionProperties = AbstractUnionEntity.unionProperties(UnionEntity.class);
        assertEquals(Set.of("propertyOne", "propertyTwo"), unionProperties.stream().map(Field::getName).collect(toSet()));
    }

    @Test
    public void commonMethods_idenfities_getters_and_setters_for_all_common_properties() {
        assertThat(AbstractUnionEntity.commonMethodNames(UnionEntity.class))
                .containsExactlyInAnyOrder("getStringProperty", "setStringProperty",
                                           "getDesc", "setDesc",
                                           "getKey", "setKey");
    }

    @Test
    public void method_set_works_for_union_properties_and_for_common_properties_of_those_properties_when_invoked_on_a_union_entity() {
        final SecondLevelEntity inst = new SecondLevelEntity();
        inst.setPropertyOfSelfType(inst);

        final SimplePartEntity simpleProperty = factory.newEntity(SimplePartEntity.class, 1L, "KEY");
        simpleProperty.setDesc("DESC");
        simpleProperty.setLevelEntity(inst);
        simpleProperty.setUncommonProperty("uncommon value");

        final UnionEntityForReflector unionEntity = factory.newEntity(UnionEntityForReflector.class);

        unionEntity.set("simplePartEntity", simpleProperty);
        assertEquals("The simplePartEntity value must be equla to simpleProperty", simpleProperty, unionEntity.activeEntity());

        unionEntity.set("commonProperty", "another common value");
        assertEquals("The commonProperty value of the UnionEntity must be equal to \"another common value\"", "another common value", unionEntity.get("commonProperty"));

        try {
            unionEntity.set("uncommonProperty", "uncommon value");
            fail("There is no uncommonProperty in the UnionEntity");
        } catch (final EntityException ex) {
            assertEquals("Error setting value [uncommon value] into property [uncommonProperty] for entity [KEY]@[ua.com.fielden.platform.reflection.test_entities.UnionEntityForReflector].",
                         ex.getMessage());
        }
    }

    @Test
    public void union_property_can_be_assigned_only_once() {
        final UnionEntity unionEntity = factory.newEntity(UnionEntity.class);
        unionEntity.setPropertyOne(factory.newEntity(EntityOne.class, 1L, "KEY VALUE"));
        try {
            unionEntity.setPropertyOne(null);
            fail("Should not be able to set active property more than once.");
        } catch (final EntityException ex) {
            assertEquals("Invalid attempt to set property [propertyOne] as active for union entity [UnionEntity] with active property [propertyOne].",
                         ex.getMessage());
        }
        try {
            unionEntity.setPropertyTwo(factory.newEntity(EntityTwo.class, 1L, "A"));
            fail("Should not be able to set active property more than once.");
        } catch (final EntityException ex) {
            assertEquals("Invalid attempt to set property [propertyTwo] as active for union entity [UnionEntity] with active property [propertyOne].",
                         ex.getMessage());
        }
    }

    @Test
    public void virtual_properties_id_key_and_desc_are_not_accessible_without_active_union_property() {
        final UnionEntity unionEntity = factory.newEntity(UnionEntity.class);

        try {
            unionEntity.getId();
            fail("Should not be able to access id before active property is specified");
        } catch (final EntityException ex) {
            assertEquals("Active property for union entity [UnionEntity] has not been determined.", ex.getMessage());
        }

        try {
            unionEntity.getKey();
            fail("Should not be able to access key before active property is specified");
        } catch (final EntityException ex) {
            assertEquals("Active property for union entity [UnionEntity] has not been determined.", ex.getMessage());
        }

        try {
            unionEntity.getDesc();
            fail("Should not be able to access desc before active property is specified");
        } catch (final EntityException ex) {
            assertEquals("Active property for union entity [UnionEntity] has not been determined.", ex.getMessage());
        }
    }

    @Test
    public void virtual_properties_id_key_and_desc_are_accessible_with_active_union_property() {
        final UnionEntity unionEntity = factory.newEntity(UnionEntity.class);

        final EntityOne one = factory.newEntity(EntityOne.class, 1L, "KEY VALUE");
        one.setDesc("DESC");
        unionEntity.setPropertyOne(one);

        assertEquals("Incorrect active property name", "propertyOne", unionEntity.activePropertyName());
        assertNotNull("Active property was not set.", unionEntity.activeEntity());
        assertEquals("Incorrect id.", one.getId(), unionEntity.getId());
        assertEquals("Incorrect key.", one.getKey(), unionEntity.getKey());
        assertEquals("Incorrect desc.", one.getDesc(), unionEntity.getDesc());

        one.setDesc("NEW DESC");
        assertEquals("Incorrect desc.", one.getDesc(), unionEntity.getDesc());
    }

    @Test
    public void union_entities_with_same_key_representations_but_different_active_properties_arent_equal() {
        final UnionEntity unionEntity1 = factory.newEntity(UnionEntity.class).setPropertyOne(factory.newEntity(EntityOne.class, 1L, "1"));
        final UnionEntity unionEntity2 = factory.newEntity(UnionEntity.class).setPropertyTwo(factory.newEntity(EntityTwo.class, 2L, "1"));
        assertNotEquals(unionEntity1, unionEntity2);
    }

    /**
     * Remark: "the same IDs" situation should not be possible due to contiguous nature of ID values across all entities. 
     */
    @Test
    public void union_entities_with_same_key_representations_and_same_ids_but_different_active_properties_arent_equal() {
        final UnionEntity unionEntity1 = factory.newEntity(UnionEntity.class).setPropertyOne(factory.newEntity(EntityOne.class, 1L, "1"));
        final UnionEntity unionEntity2 = factory.newEntity(UnionEntity.class).setPropertyTwo(factory.newEntity(EntityTwo.class, 1L, "1"));
        assertNotEquals(unionEntity1, unionEntity2);
    }

    /**
     * Remark: "the same IDs" situation should not be possible due to contiguous nature of ID values across all entities. 
     */
    @Test
    public void union_entities_with_different_key_representations_and_same_ids_and_same_active_property_arent_equal() {
        final var entity1 = factory.newEntity(EntityOne.class, 1L, "1");
        final var entity2 = factory.newEntity(EntityOne.class, 1L, "2");
        assertNotEquals(entity1, entity2);
        final UnionEntity unionEntity1 = factory.newEntity(UnionEntity.class).setPropertyOne(entity1);
        final UnionEntity unionEntity2 = factory.newEntity(UnionEntity.class).setPropertyOne(entity2);
        assertNotEquals(unionEntity1, unionEntity2);
    }

    @Test
    public void union_property_can_be_assigned_dynamically() {
        final EntityOne propOneValue = factory.newEntity(EntityOne.class, 1L, "KEY VALUE");
        final UnionEntity unionEntity = factory.newEntity(UnionEntity.class);
        unionEntity.setUnionProperty(propOneValue);
        assertEquals(propOneValue, unionEntity.activeEntity());
    }

    @Test
    public void union_property_cannot_be_assigned_null_dynamically() {
        final UnionEntity unionEntity = factory.newEntity(UnionEntity.class);
        try {
            unionEntity.setUnionProperty(null);
            fail("Dynamic assignment of null should not be supported.");
        } catch (final EntityException ex) {
            assertEquals("Null is not a valid value for union-properties (union entity [UnionEntity]).",
                         ex.getMessage());
        }
    }

    @Test
    public void union_property_can_be_assigned_dynamically_only_once() {
        final EntityOne propOneValue = factory.newEntity(EntityOne.class, 1L, "KEY VALUE");
        final UnionEntity unionEntity = factory.newEntity(UnionEntity.class);
        unionEntity.setUnionProperty(propOneValue);

        final EntityTwo propTwoValue = factory.newEntity(EntityTwo.class, 2L, "1");
        try {
            unionEntity.setUnionProperty(propTwoValue);
        } catch (final EntityException ex) {
            assertEquals("Invalid attempt to set property [propertyTwo] as active for union entity [UnionEntity] with active property [propertyOne].", ex.getMessage());
        }
    }

    @Test
    public void union_property_cannot_be_assigned_dynamically_if_no_property_that_matches_value_type_could_be_found() {
        final EntityThree value = factory.newEntity(EntityThree.class, 1L, 42);
        final UnionEntity unionEntity = factory.newEntity(UnionEntity.class);

        try {
            unionEntity.setUnionProperty(value);
        } catch (final EntityException ex) {
            assertEquals("None of the union properties match type [EntityThree].", ex.getMessage());
        }
    }

    @Test
    public void unionPropertyNameByType_can_find_union_property_by_type() {
        final var maybePropName = AbstractUnionEntity.unionPropertyNameByType(UnionEntity.class, EntityOne.class);
        assertTrue(maybePropName.isPresent());
        assertEquals("propertyOne", maybePropName.get());
    }

    @Test
    public void unionPropertyNameByType_returns_empty_result_if_no_matching_property_could_be_found() {
        final var maybePropName = AbstractUnionEntity.unionPropertyNameByType(UnionEntity.class, EntityThree.class);
        assertTrue(maybePropName.isEmpty());
    }

}
