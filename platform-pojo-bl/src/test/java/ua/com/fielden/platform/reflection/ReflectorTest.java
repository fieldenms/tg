package ua.com.fielden.platform.reflection;

import com.google.inject.Injector;
import org.junit.Test;
import ua.com.fielden.platform.associations.one2many.MasterEntityWithOneToManyAssociation;
import ua.com.fielden.platform.associations.one2one.DetailEntityForOneToOneAssociationWithOneToManyAssociation;
import ua.com.fielden.platform.associations.one2one.MasterEntityWithOneToOneAssociation;
import ua.com.fielden.platform.associations.test_entities.EntityWithManyToOneAssociations;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.annotation.factory.HandlerAnnotation;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.annotation.factory.ParamAnnotation;
import ua.com.fielden.platform.entity.annotation.mutator.DateParam;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.before_change_event_handling.BeforeChangeEventHandler;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.validation.annotation.GreaterOrEqual;
import ua.com.fielden.platform.entity.validation.annotation.Max;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.reflection.test_entities.*;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.test_entities.Entity;
import ua.com.fielden.platform.utils.Pair;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ua.com.fielden.platform.reflection.AnnotationReflector.isPropertyAnnotationPresent;
import static ua.com.fielden.platform.reflection.Reflector.isMethodOverriddenOrDeclared;
import static ua.com.fielden.platform.reflection.Reflector.isPropertyPersistent;
import static ua.com.fielden.platform.utils.EntityUtils.*;

/**
 * Test case for {@link Reflector}.
 *
 * @author TG Team
 *
 */
public class ReflectorTest {
    final Injector injector = new ApplicationInjectorFactory().add(new CommonEntityTestIocModuleWithPropertyFactory()).getInjector();
    final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void test_that_obtain_getter_works() throws Exception {
        Method method = Reflector.obtainPropertyAccessor(SecondLevelEntity.class, "propertyOfSelfType");
        assertNotNull("Failed to located a getter method.", method);
        assertEquals("Incorrect getter.", "getPropertyOfSelfType", method.getName());
        method = Reflector.obtainPropertyAccessor(UnionEntityForReflector.class, "commonProperty");
        assertNotNull("Failed to locate a getter method for commonProperty in the UnionEntity", method);
        assertEquals("Incorect commonProperty getter", "getCommonProperty", method.getName());
        method = Reflector.obtainPropertyAccessor(UnionEntityForReflector.class, "simplePartEntity");
        assertNotNull("Failed to locate a getter method for simplePartEntity in the UnionEntity", method);
        assertEquals("Incorect simplePartEntity getter", "getSimplePartEntity", method.getName());
        try {
            Reflector.obtainPropertyAccessor(UnionEntityForReflector.class, "uncommonProperty");
            fail("there shouldn't be any getter for uncommonProperty");
        } catch (final Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void test_whether_obtainPropertySetter_works() {
        try {
            Method method = Reflector.obtainPropertySetter(ComplexKeyEntity.class, "key.key");
            assertNotNull("Couldn't find setter for key.key property of the ComplexKeyEntity class", method);
            method = Reflector.obtainPropertySetter(ComplexKeyEntity.class, "key.simpleEntity");
            assertNotNull("Couldn't find setter for key.simpleEntity property of the ComplexKeyEntity class", method);
            method = Reflector.obtainPropertySetter(ComplexKeyEntity.class, "key.simpleEntity.key");
            assertNotNull("Couldn't find setter for key.simpleEntity.key property of the ComplexKeyEntity class", method);
            method = Reflector.obtainPropertySetter(ComplexKeyEntity.class, "simpleEntity.key");
            assertNotNull("Couldn't find setter for simpleEntity.key property of the ComplexKeyEntity class", method);
            method = Reflector.obtainPropertySetter(ComplexKeyEntity.class, "key.simpleEntity.desc");
            assertNotNull("Couldn't find setter for key.simpleEntity.desc property of the ComplexKeyEntity class", method);
            method = Reflector.obtainPropertySetter(UnionEntityForReflector.class, "commonProperty");
            assertNotNull("Failed to locate a setter method for commonProperty in the UnionEntity", method);
            method = Reflector.obtainPropertyAccessor(UnionEntityForReflector.class, "simplePartEntity");
            assertNotNull("Failed to locate a setter method for simplePartEntity in the UnionEntity", method);
            method = Reflector.obtainPropertySetter(UnionEntityHolder.class, "unionEntity.commonProperty");
            assertNotNull("Couldn't find setter for unionEntity.commonProperty in the UnionEntityHolder", method);
            method = Reflector.obtainPropertySetter(UnionEntityHolder.class, "unionEntity.levelEntity.propertyOfSelfType.key");
            assertNotNull("Couldn't find setter for unionEntity.levelEntity.propertyOfSelfType.key in the UnionEntityHolder", method);
            try {
                Reflector.obtainPropertySetter(UnionEntityForReflector.class, "uncommonProperty");
                fail("there shouldn't be any setter for uncommonProperty");
            } catch (final Exception e) {
                System.out.println(e.getMessage());
            }
        } catch (final Exception ex) {
            fail(ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Test
    public void test_whether_getMethod_works() {
        final SecondLevelEntity inst = new SecondLevelEntity();
        inst.setPropertyOfSelfType(inst);
        inst.setProperty("value");

        final SimplePartEntity simpleProperty = factory.newEntity(SimplePartEntity.class, 1L, "KEY");
        simpleProperty.setDesc("DESC");
        simpleProperty.setCommonProperty("common value");
        simpleProperty.setLevelEntity(inst);
        simpleProperty.setUncommonProperty("uncommon value");

        final UnionEntityForReflector unionEntity = factory.newEntity(UnionEntityForReflector.class);
        unionEntity.setSimplePartEntity(simpleProperty);

        try {
            assertNotNull("The getProperty() method must be present in the SecondLevelEntity", Reflector.getMethod(SecondLevelEntity.class, "getProperty"));
        } catch (final NoSuchMethodException e) {
            fail("There shouldn't be any exception");
            e.printStackTrace();
        }

        try {
            assertNotNull("The getCommonProperty() must be present in UnionEntity class", Reflector.getMethod(UnionEntityForReflector.class, "getCommonProperty"));
        } catch (final NoSuchMethodException e) {
            fail("There shouldn't be any exception");
            e.printStackTrace();
        }

        try {
            assertNotNull("The getCommonProperty() must be present in UnionEntity class", Reflector.getMethod(unionEntity, "getCommonProperty"));
        } catch (final NoSuchMethodException e) {
            fail("There shouldn't be any exception");
            e.printStackTrace();
        }

        try {
            assertNull("The getUncommonProerty mustn't be presnet in UnionEntity class", Reflector.getMethod(UnionEntityForReflector.class, "getUncommonProperty"));
            fail("The getUncommonProerty mustn't be presnet in UnionEntity class");
        } catch (final Exception e) {
            System.out.println(e.getMessage());
        }

        try {
            assertNull("The getUncommonProerty mustn't be presnet in UnionEntity class", Reflector.getMethod(unionEntity, "getUncommonProperty"));
            fail("The getUncommonProerty mustn't be presnet in UnionEntity class");
        } catch (final Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static class A {
        public A() {
        }

        public A(final Integer x) {
        }

        public A(final Integer x, final Double y) {
        }
    }

    private static class B extends A {
        public B() {
        }

        public B(final Integer x, final Double y) {
        }
    }

    @Test
    public void test_constructor_retrieval() {
        try {
            final Constructor def, sing, doub, def1, sing1, doub1;

            assertNotNull("Constructor should not be null.", def = Reflector.getConstructorForClass(A.class));
            assertNotNull("Constructor should not be null.", sing = Reflector.getConstructorForClass(A.class, Integer.class));
            assertNotNull("Constructor should not be null.", doub = Reflector.getConstructorForClass(A.class, Integer.class, Double.class));

            assertNotNull("Constructor should not be null.", def1 = Reflector.getConstructorForClass(B.class));
            assertNotSame("Should not be equal.", def1, def);
            assertNotNull("Constructor should not be null.", sing1 = Reflector.getConstructorForClass(B.class, Integer.class));
            assertEquals("Should be equal.", sing1, sing);
            assertNotNull("Constructor should not be null.", doub1 = Reflector.getConstructorForClass(B.class, Integer.class, Double.class));
            assertNotSame("Should not be equal.", doub1, doub);
        } catch (final Exception e) {
            fail("Constructor retrieval failed. " + e.getMessage());
        }
    }

    @Test
    public void test_validation_limits_extraction() {
        final EntityWithValidationLimits entity = factory.newEntity(EntityWithValidationLimits.class, 1L, "KEY");
        assertEquals("Should be equal.", new Pair<Integer, Integer>(1, 12), Reflector.extractValidationLimits(entity, "month"));
        assertEquals("Should be equal.", new Pair<Integer, Integer>(1950, null), Reflector.extractValidationLimits(entity, "year"));
        assertEquals("Should be equal.", new Pair<Integer, Integer>(null, 255), Reflector.extractValidationLimits(entity, "prop"));
    }

    @Test
    public void handler_annotation_has_correct_number_of_params() {
        final List<String> params = Reflector.annotataionParams(Handler.class);
        assertEquals("Unexpected number of annotation parameters.", 12, params.size());
        assertTrue(params.contains("value"));
        assertTrue(params.contains("non_ordinary"));
        assertTrue(params.contains("clazz"));
        assertTrue(params.contains("integer"));
        assertTrue(params.contains("str"));
        assertTrue(params.contains("prop"));
        assertTrue(params.contains("dbl"));
        assertTrue(params.contains("date"));
        assertTrue(params.contains("date_time"));
        assertTrue(params.contains("money"));
        assertTrue(params.contains("enumeration"));
        assertTrue(params.contains("bool"));
    }

    @Test
    public void test_that_annotation_param_value_can_be_obtained() {
        final Handler handler = new HandlerAnnotation(BeforeChangeEventHandler.class).date(new DateParam[] { ParamAnnotation.dateParam("dateParam", "2011-12-01 00:00:00") }).newInstance();
        final Pair<Class<?>, Object> pair = Reflector.getAnnotationParamValue(handler, "date");
        final DateParam[] dateParams = (DateParam[]) pair.getValue();
        assertEquals("Incorrect number of parameter values.", 1, dateParams.length);
        final DateParam param = dateParams[0];
        assertEquals("Incorrect parameter value.", "dateParam", param.name());
        assertEquals("Incorrect parameter value.", "2011-12-01 00:00:00", param.value());
    }

    @Test
    public void test_conversion_of_relative_property_paths_to_absolute() {
        assertEquals("originator.name", Reflector.fromRelative2AbsolutePath("", "originator.name"));
        assertEquals("vehicle.driver.name", Reflector.fromRelative2AbsolutePath("vehicle.driver", "name"));
        assertEquals("originator.name", Reflector.fromRelative2AbsolutePath("vehicle.driver", "←.←.originator.name"));
        assertEquals("vehicle.owner.name", Reflector.fromRelative2AbsolutePath("vehicle.driver", "←.owner.name"));
        try {
            Reflector.fromRelative2AbsolutePath("vehicle.driver", "←.←.←.originator.name");
            fail("Validation should have prevented successful conversion.");
        } catch (final Exception ex) {
        }
    }

    @Test
    public void test_conversion_of_relative_property_paths_to_absolute_for_SELF_properties() {
        assertEquals("", Reflector.fromRelative2AbsolutePath("", "SELF"));
        assertEquals("vehicle", Reflector.fromRelative2AbsolutePath("vehicle", "SELF"));
        assertEquals("vehicle.driver", Reflector.fromRelative2AbsolutePath("vehicle.driver", "SELF"));
    }

    @Test
    public void test_conversion_of_absolute_property_paths_to_relative() {
        assertEquals("originator.name", Reflector.fromAbsolute2RelativePath("", "originator.name"));
        assertEquals("name", Reflector.fromAbsolute2RelativePath("vehicle.driver", "vehicle.driver.name"));
        assertEquals("←.←.originator.name", Reflector.fromAbsolute2RelativePath("vehicle.driver", "originator.name"));
        assertEquals("←.owner.name", Reflector.fromAbsolute2RelativePath("vehicle.driver", "vehicle.owner.name"));
    }

    @Test
    public void test_conversion_of_absolute_property_paths_to_relative_for_SELF_properties() {
        assertEquals("SELF", Reflector.fromAbsolute2RelativePath("", ""));
        assertEquals("SELF", Reflector.fromAbsolute2RelativePath("vehicle", "vehicle"));
        assertEquals("SELF", Reflector.fromAbsolute2RelativePath("vehicle.driver", "vehicle.driver"));
    }

    @Test
    public void should_have_successfully_performed_inverted_conversion_of_relative_property_path_to_absolute_for_one_to_one_association() {
        assertEquals("key.intProp", Reflector.relative2AbsoluteInverted(MasterEntityWithOneToOneAssociation.class, "one2oneAssociation", "←.intProp"));
    }

    @Test
    public void should_have_successfully_performed_inverted_conversion_of_relative_property_path_to_absolute_for_one_to_one_association_with_one_to_many_special_case_association() {
        assertEquals("key1.key.intProp", Reflector.relative2AbsoluteInverted(MasterEntityWithOneToOneAssociation.class, "one2oneAssociation.one2ManyAssociation", "←.←.intProp"));
        assertEquals("key1.intProp", Reflector.relative2AbsoluteInverted(MasterEntityWithOneToOneAssociation.class, "one2oneAssociation.one2ManyAssociation", "←.intProp"));
        assertEquals("key1.intProp", Reflector.relative2AbsoluteInverted(DetailEntityForOneToOneAssociationWithOneToManyAssociation.class, "one2ManyAssociation", "←.intProp"));
    }

    @Test
    public void should_have_successfully_performed_inverted_conversion_of_relative_property_path_to_absolute_for_one_to_many_collectional_association() {
        assertEquals("key1.intProp", Reflector.relative2AbsoluteInverted(MasterEntityWithOneToManyAssociation.class, "one2manyAssociationCollectional", "←.intProp"));
    }

    @Test
    public void should_have_successfully_performed_inverted_conversion_of_relative_property_path_to_absolute_for_one_to_many_collectional_association_of_nested_level_one_to_many_collectional_association() {
        assertEquals("key1.key1.intProp", Reflector.relative2AbsoluteInverted(MasterEntityWithOneToManyAssociation.class, "one2manyAssociationCollectional.one2manyAssociationCollectional", "←.←.intProp"));
        assertEquals("key1.intProp", Reflector.relative2AbsoluteInverted(MasterEntityWithOneToManyAssociation.class, "one2manyAssociationCollectional.one2manyAssociationCollectional", "←.intProp"));
    }

    @Test
    public void should_have_failed_inverted_conversion_of_relative_property_path_to_absolute_for_many_to_one_association() {
        try {
            Reflector.relative2AbsoluteInverted(EntityWithManyToOneAssociations.class, "many2oneProp", "←.intProp");
        } catch (final Exception ex) {
            assertEquals("Non-collectional property many2oneProp in type ua.com.fielden.platform.associations.test_entities.EntityWithManyToOneAssociations represents a Many-to-One association.", ex.getMessage());
        }
    }
    
    @Test
    public void reflector_correctly_identifies_overridden_method_as_such() {
        assertTrue(isMethodOverriddenOrDeclared(AbstractEntity.class, EntityWithValidationLimits.class, "validate"));
        assertFalse(isMethodOverriddenOrDeclared(AbstractEntity.class, EntityWithValidationLimits.class, "toString"));
    }

    @Test
    public void reflector_correctly_identifies_declared_methods_as_such() {
        assertTrue(isMethodOverriddenOrDeclared(AbstractEntity.class, EntityWithValidationLimits.class, "setMonth", Integer.class));
    }

    @Test
    public void assigning_private_static_fields_is_supported() throws NoSuchFieldException, SecurityException {
        assertTrue(AbstractEntity.isStrictModelVerification());
        assertTrue(ComplexKeyEntity.isStrictModelVerification());

        try {
            Reflector.assignStatic(AbstractEntity.class.getDeclaredField("STRICT_MODEL_VERIFICATION"), false);
            assertFalse(AbstractEntity.isStrictModelVerification());
            assertFalse(ComplexKeyEntity.isStrictModelVerification());
        } finally {
            Reflector.assignStatic(AbstractEntity.class.getDeclaredField("STRICT_MODEL_VERIFICATION"), true);
            assertTrue(AbstractEntity.isStrictModelVerification());
            assertTrue(ComplexKeyEntity.isStrictModelVerification());
        }
    }

    @Test
    public void isPropertyPersistent_is_false_for_calculated_properties() {
        assertTrue(isPropertyAnnotationPresent(Calculated.class, EntityWithPropertiesOfActivatableTypes.class, "calcCategory"));
        assertFalse(isPropertyPersistent(EntityWithPropertiesOfActivatableTypes.class, "calcCategory"));
    }

    @Test
    public void isPropertyPersistent_is_false_for_critOnly_properties() {
        assertTrue(isPropertyAnnotationPresent(CritOnly.class, EntityWithPropertiesOfActivatableTypes.class, "categoryCrit"));
        assertFalse(isPropertyPersistent(EntityWithPropertiesOfActivatableTypes.class, "categoryCrit"));
    }

    @Test
    public void isPropertyPersistent_is_false_for_plain_properties() {
        assertFalse(isPropertyAnnotationPresent(CritOnly.class, EntityWithPropertiesOfActivatableTypes.class, "plainCategory"));
        assertFalse(isPropertyPersistent(EntityWithPropertiesOfActivatableTypes.class, "plainCategory"));
        assertFalse(isPropertyAnnotationPresent(Calculated.class, EntityWithPropertiesOfActivatableTypes.class, "plainCategory"));
        assertFalse(isPropertyPersistent(EntityWithPropertiesOfActivatableTypes.class, "plainCategory"));
    }

    @Test
    public void isPropertyPersistent_is_true_for_simple_key_in_a_persistent_entity_type() {
        assertTrue(isPersistentEntityType(TgPerson.class));
        assertFalse(isCompositeEntity(TgPerson.class));
        assertTrue(isPropertyPersistent(TgPerson.class, KEY));
    }

    @Test
    public void isPropertyPersistent_is_false_for_composite_key_in_a_persistent_entity_type() {
        assertTrue(isPersistentEntityType(TgAuthorship.class));
        assertTrue(isCompositeEntity(TgAuthorship.class));
        assertFalse(isPropertyPersistent(TgAuthorship.class, KEY));
    }

    @Test
    public void isPropertyPersistent_is_false_for_composite_key_in_a_synthetic_entity_type() {
        assertTrue(isSyntheticEntityType(TgVehicleFuelUsage.class));
        assertTrue(isCompositeEntity(TgVehicleFuelUsage.class));
        assertFalse(isPropertyPersistent(TgVehicleFuelUsage.class, KEY));
    }

    @Test
    public void isPropertyPersistent_is_false_for_key_in_a_union_type() {
        assertTrue(isUnionEntityType(TgUnion.class));
        assertFalse(isPropertyPersistent(TgUnion.class, KEY));
    }

    @Test
    public void isPropertyPersistent_is_false_for_desc_in_a_union_type_without_desc() {
        assertTrue(isUnionEntityType(UnionEntityWithoutDesc.class));
        assertFalse(hasDescProperty(UnionEntityWithoutDesc.class));
        assertFalse(isPropertyPersistent(UnionEntityWithoutDesc.class, DESC));
    }

    @Test
    public void isPropertyPersistent_is_false_for_desc_in_a_union_type_with_desc() {
        assertTrue(isUnionEntityType(UnionEntityWithDesc.class));
        assertTrue(hasDescProperty(UnionEntityWithDesc.class));
        assertFalse(isPropertyPersistent(UnionEntityWithDesc.class, DESC));
    }

    @Test
    public void isPropertyPersistent_is_true_for_desc_in_a_persistent_entity_type_with_desc() {
        assertTrue(isPersistentEntityType(TgAuthor.class));
        assertTrue(hasDescProperty(TgAuthor.class));
        assertTrue(isPropertyPersistent(TgAuthor.class, DESC));
    }

    @Test
    public void isPropertyPersistent_is_false_for_desc_in_a_persistent_entity_type_without_desc() {
        assertTrue(isPersistentEntityType(TgAuthorship.class));
        assertFalse(hasDescProperty(TgAuthorship.class));
        assertFalse(isPropertyPersistent(TgAuthorship.class, DESC));
    }

    @Test
    public void isPropertyPersistent_is_true_for_properties_annotated_with_MapTo_in_persistent_entities() {
        assertTrue(isPersistentEntityType(TgVehicle.class));
        assertTrue(isPropertyAnnotationPresent(MapTo.class, TgVehicle.class, "initDate"));
        assertTrue(isPropertyPersistent(TgVehicle.class, "initDate"));
    }

    @Test
    public void isPropertyPersistent_is_true_for_properties_annotated_with_MapTo_in_union_entities() {
        assertTrue(isUnionEntityType(TgUnion.class));
        assertTrue(isPropertyAnnotationPresent(MapTo.class, TgUnion.class, "union1"));
        assertTrue(isPropertyPersistent(TgUnion.class, "union1"));
    }

    @Test
    public void isPropertyPersistent_is_false_for_properties_annotated_with_MapTo_in_synthetic_entities() {
        assertTrue(isSyntheticEntityType(TgReBogieWithHighLoad.class));
        assertTrue(isPropertyAnnotationPresent(MapTo.class, TgReBogieWithHighLoad.class, "location"));
        assertFalse(isPropertyPersistent(TgReBogieWithHighLoad.class, "location"));
    }

    @Test
    public void isPropetyPersistent_is_false_for_properties_annotated_with_MapTo_in_action_entities() {
        assertFalse(isPropertyPersistent(ExportAction.class, "count"));
    }

    @Test
    public void isPropertyCalculated_is_true_for_explicitly_calculated_properties_in_persistent_entities() {
        assertTrue(isPersistentEntityType(EntityWithPropertiesOfActivatableTypes.class));
        assertTrue(isPropertyAnnotationPresent(Calculated.class, EntityWithPropertiesOfActivatableTypes.class, "calcCategory"));
        assertTrue(Reflector.isPropertyCalculated(EntityWithPropertiesOfActivatableTypes.class, "calcCategory"));
        
        assertTrue(isPropertyAnnotationPresent(Calculated.class, EntityWithPropertiesOfActivatableTypes.class, "calcAuthor"));
        assertTrue(Reflector.isPropertyCalculated(EntityWithPropertiesOfActivatableTypes.class, "calcAuthor"));
    }

    @Test
    public void isPropertyCalculated_is_false_for_calculated_properties_in_non_persistent_non_synthetic_entities() {
        // Entity is neither persistent nor synthetic, so calculated properties should return false
        assertFalse(isPersistentEntityType(Entity.class));
        assertFalse(isSyntheticEntityType(Entity.class));
        assertTrue(isPropertyAnnotationPresent(Calculated.class, Entity.class, "firstProperty"));
        assertFalse(Reflector.isPropertyCalculated(Entity.class, "firstProperty"));
    }

    @Test
    public void isPropertyCalculated_is_true_for_implicitly_calculated_one2one_relationships_in_persistent_entities() {
        assertTrue(isPersistentEntityType(MasterEntityWithOneToOneAssociation.class));
        assertTrue(Finder.isOne2One_association(MasterEntityWithOneToOneAssociation.class, "one2oneAssociation"));
        assertTrue(Reflector.isPropertyCalculated(MasterEntityWithOneToOneAssociation.class, "one2oneAssociation"));
    }

    @Test
    public void isPropertyCalculated_is_false_for_non_calculated_persistent_properties() {
        assertTrue(isPersistentEntityType(EntityWithPropertiesOfActivatableTypes.class));
        assertFalse(isPropertyAnnotationPresent(Calculated.class, EntityWithPropertiesOfActivatableTypes.class, "category"));
        assertFalse(Finder.isOne2One_association(EntityWithPropertiesOfActivatableTypes.class, "category"));
        assertFalse(Reflector.isPropertyCalculated(EntityWithPropertiesOfActivatableTypes.class, "category"));
    }

    @Test
    public void isPropertyCalculated_is_false_for_critOnly_properties() {
        assertTrue(isPersistentEntityType(EntityWithPropertiesOfActivatableTypes.class));
        assertTrue(isPropertyAnnotationPresent(CritOnly.class, EntityWithPropertiesOfActivatableTypes.class, "categoryCrit"));
        assertFalse(Reflector.isPropertyCalculated(EntityWithPropertiesOfActivatableTypes.class, "categoryCrit"));
    }

    @Test
    public void isPropertyCalculated_is_false_for_plain_properties() {
        assertTrue(isPersistentEntityType(EntityWithPropertiesOfActivatableTypes.class));
        assertFalse(isPropertyAnnotationPresent(Calculated.class, EntityWithPropertiesOfActivatableTypes.class, "plainCategory"));
        assertFalse(Finder.isOne2One_association(EntityWithPropertiesOfActivatableTypes.class, "plainCategory"));
        assertFalse(Reflector.isPropertyCalculated(EntityWithPropertiesOfActivatableTypes.class, "plainCategory"));
    }

    @Test
    public void isPropertyCalculated_is_false_for_properties_in_action_entities() {
        assertFalse(isPersistentEntityType(ActionEntity.class));
        assertFalse(isSyntheticEntityType(ActionEntity.class));
        assertTrue(isPropertyAnnotationPresent(Calculated.class, ActionEntity.class, "calculated"));
        assertFalse(Reflector.isPropertyCalculated(ActionEntity.class, "calculated"));
    }

    @Test
    public void isPropertyCalculated_is_false_for_non_calculated_properties_in_synthetic_entities() {
        assertTrue(isSyntheticEntityType(TgReBogieWithHighLoad.class));
        // TgReBogieWithHighLoad extends TgBogie and inherits "location" property which is annotated with @MapTo but not @Calculated
        assertFalse(isPropertyAnnotationPresent(Calculated.class, TgReBogieWithHighLoad.class, "location"));
        assertFalse(Finder.isOne2One_association(TgReBogieWithHighLoad.class, "location"));
        assertFalse(Reflector.isPropertyCalculated(TgReBogieWithHighLoad.class, "location"));
    }

    @Test
    public void isPropertyCalculated_is_true_for_calculated_properties_in_synthetic_entities() {
        assertTrue(isSyntheticEntityType(TgReBogieWithHighLoad.class));
        assertTrue(isPropertyAnnotationPresent(Calculated.class, TgReBogieWithHighLoad.class, "calculated"));
        assertFalse(Finder.isOne2One_association(TgReBogieWithHighLoad.class, "calculated"));
        assertTrue(Reflector.isPropertyCalculated(TgReBogieWithHighLoad.class, "calculated"));
    }

    @Test
    public void isPropertyCalculated_throws_exception_for_dot_expressions() {
        assertThatThrownBy(() -> Reflector.isPropertyCalculated(EntityWithPropertiesOfActivatableTypes.class, "category.key"))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("must be a simple property name");
    }

    @KeyType(String.class)
    protected static class EntityWithValidationLimits extends AbstractEntity<String> {

        @IsProperty
        private Integer month;
        @IsProperty
        private Integer year;

        @IsProperty
        private String prop;

        @Observable
        @Max(255)
        public ReflectorTest.EntityWithValidationLimits setProp(final String prop) {
            this.prop = prop;
            return this;
        }

        public String getProp() {
            return prop;
        }

        public Integer getMonth() {
            return month;
        }

        @Observable
        @GreaterOrEqual(1)
        @Max(12)
        public void setMonth(final Integer month) {
            this.month = month;
        }

        public Integer getYear() {
            return year;
        }

        @Observable
        @GreaterOrEqual(1950)
        public void setYear(final Integer year) {
            this.year = year;
        }
        
        @Override
        protected Result validate() {
            return super.validate();
        }
    }

}
