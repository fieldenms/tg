package ua.com.fielden.platform.entity;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import ua.com.fielden.platform.associations.one2many.incorrect.*;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.entity.exceptions.EntityException;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.ioc.ObservableMutatorInterceptor;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.proxy.EntityProxyContainer;
import ua.com.fielden.platform.entity.validation.DomainValidationConfig;
import ua.com.fielden.platform.entity.validation.HappyValidator;
import ua.com.fielden.platform.entity.validation.annotation.ValidationAnnotation;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.error.Warning;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.reflection.test_entities.*;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.either.Either;
import ua.com.fielden.platform.types.either.Left;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.exceptions.EntityDefinitionException.*;
import static ua.com.fielden.platform.types.try_wrapper.TryWrapper.Try;

/**
 * Unit test for :
 * <ul>
 * <li>{@link AbstractEntity}'s change support and entity instantiation with {@link EntityFactory}.</li>
 * <li>{@link AbstractEntity}'s support for properties and validation.</li>
 * <li>{@link AbstractEntity}'s meta-property support.</li>
 * </ul>
 *
 * @author TG Team
 *
 */
public class AbstractEntityTest {
    private boolean observedForIncorrectAttempt = false; // used

    private final Injector injector = new ApplicationInjectorFactory()
            .add(new CommonEntityTestIocModuleWithPropertyFactory())
            .add(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(DomainValidationConfig.class).toInstance(newDomainValidationConfig());
                }
            })
            .getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);
    private Entity entity;

    @Before
    public void setUp() {
        entity = factory.newEntity(Entity.class, "key", "description"); // this ensures all listeners are removed
    }

    @Test
    public void testEntityFactoryReferenceIsSet() {
        assertEquals(factory, entity.getEntityFactory());
    }

    @Test
    public void testThatObservablePropertyMaintainsOriginalAndPrevValue() {
        entity.getProperty("observableProperty").setPrevValue(BigDecimal.ZERO); // setting the same value as the current one does not change prev-value
        assertEquals("current value must be changed (original value null)", true, entity.getProperty("observableProperty").isChangedFromOriginal());
        assertEquals("current value must be changed (previous value null)", true, entity.getProperty("observableProperty").isChangedFromPrevious());
        final var newValue = new BigDecimal("22.0");
        entity.setObservableProperty(newValue);
        assertEquals("Prev property value is incorrect", BigDecimal.ZERO, entity.getProperty("observableProperty").getPrevValue());
        assertNull("Original property value is incorrect", entity.getProperty("observableProperty").getOriginalValue());
        assertEquals("Property change count is incorrect", 1, entity.getProperty("observableProperty").getValueChangeCount());
        assertEquals("current value must be changed (original value 1)", true, entity.getProperty("observableProperty").isChangedFromOriginal());
        assertEquals("current value must be changed (previous value 1)", true, entity.getProperty("observableProperty").isChangedFromPrevious());

        entity.setObservableProperty(new BigDecimal("23.0"));
        assertEquals("Prev property value is incorrect", newValue, entity.getProperty("observableProperty").getPrevValue());
        assertNull("Original property value is incorrect", entity.getProperty("observableProperty").getOriginalValue());
        assertEquals("Property change count is incorrect", 2, entity.getProperty("observableProperty").getValueChangeCount());
        assertEquals("current value must be changed (original value 2)", true, entity.getProperty("observableProperty").isChangedFromOriginal());
        assertEquals("current value must be changed (previous value 2)", true, entity.getProperty("observableProperty").isChangedFromPrevious());
    }

    @Test
    public void testThatPropertyValidationWorksForNonObservable() {
        // test validation of the firstProperty
        entity.setFirstProperty(60);
        assertTrue("Property firstProperty validation failed.", entity.getProperty("firstProperty").isValid());
        assertNull("Incorrect value for last invalid value.", entity.getProperty("firstProperty").getLastInvalidValue());
        // try to assign null
        entity.setFirstProperty(null);
        assertFalse("Property validation failed.", entity.getProperty("firstProperty").isValid());
        assertEquals("Value assignment should have been prevented by the validator,", Integer.valueOf(60), entity.getFirstProperty());
        // try to assign value less than 50
        entity.setFirstProperty(23);
        assertFalse("Property validation failed.", entity.getProperty("firstProperty").isValid());
        assertEquals("Value assignment should have been prevented by the validator,", Integer.valueOf(60), entity.getFirstProperty());
        assertEquals("Incorrect value for last invalid value.", Integer.valueOf(23), entity.getProperty("firstProperty").getLastInvalidValue());
    }

    @Test
    public void testThatPropertyHasDomainValidator() {
        // test validation of the firstProperty
        entity.setFirstProperty(60);
        final Result result = entity.getProperty("firstProperty").getValidationResult(ValidationAnnotation.DOMAIN);
        assertTrue("Domain validator for property firstProperty should be successful.", result.isSuccessful());
        assertTrue("Entity and result's instance should match.", result.getInstance() == entity);
        assertNull("Incorrect value for last invalid value.", entity.getProperty("firstProperty").getLastInvalidValue());
    }

    @Test
    public void final_property_for_non_persistent_entity_can_only_be_assigned_once() {
        assertTrue(entity.getProperty("finalProperty").isEditable());
        entity.setFinalProperty(new BigDecimal("60.0"));
        assertTrue(entity.getProperty("finalProperty").isValid());
        assertEquals(new BigDecimal("60.0"), entity.getFinalProperty());
        assertFalse(entity.getProperty("finalProperty").isEditable());

        entity.setFinalProperty(new BigDecimal("31.0"));
        assertFalse(entity.getProperty("finalProperty").isValid());
        assertEquals(new BigDecimal("60.0"), entity.getFinalProperty());
        assertFalse(entity.getProperty("finalProperty").isEditable());
    }

    @Test
    public void persistentOnly_final_property_for_non_persistent_entity_yields_invalid_definition() {
        final Either<Exception, EntityInvalidDefinition> result = Try(() -> factory.newEntity(EntityInvalidDefinition.class, "key", "desc"));
        assertTrue(result instanceof Left);
        final Throwable rootCause = ExceptionUtils.getRootCause(((Left<Exception, EntityInvalidDefinition>) result).value());
        assertTrue(rootCause instanceof EntityDefinitionException);
        assertEquals(format("Non-persistent entity [%s] has property [%s], which is incorrectly annotated with @Final(persistentOnly = true).", EntityInvalidDefinition.class.getSimpleName(), "firstProperty"),
                rootCause.getMessage());
    }


    @Test
    public void testNewEntityWithDynamicKey() {
        final Integer key1 = Integer.valueOf(1);
        final String key2 = "StringCompositeKeyMember";
        final Entity key3 = entity;

        // testing if entity is created correctly
        final EntityWithDynamicEntityKey entityWithDynamicEntityKey = factory.newByKey(EntityWithDynamicEntityKey.class, key1, key2, key3);

        assertNotNull(entityWithDynamicEntityKey);
        assertNotNull(entityWithDynamicEntityKey.getKey());
        assertEquals(key1, entityWithDynamicEntityKey.getKey1());
        assertEquals(key2, entityWithDynamicEntityKey.getKey2());
        assertEquals(key3, entityWithDynamicEntityKey.getKey3());

        assertEquals(factory, entityWithDynamicEntityKey.getEntityFactory());

        // testing if exception is thrown in case of wrong number of parameters
        boolean exceptionWasThrown = false;
        try {
            factory.newByKey(EntityWithDynamicEntityKey.class, key1, key2);
        } catch (final Exception e) {
            exceptionWasThrown = true;
        }
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void testThatMetaPropertiesForSimplEntityAreAssignedCorrectKeyAndActiveValues() {
        assertTrue("Meta-property for property 'key' should be marked as a key.", entity.getProperty("key").isKey());
        assertTrue("Meta-property for property 'key' should be marked active.", entity.getProperty("key").isVisible());

        assertFalse("Meta-property for property 'desc' should not be marked as a key.", entity.getProperty("desc").isKey());
        assertTrue("Meta-property for property 'desc' should be marked active.", entity.getProperty("desc").isVisible());
        assertFalse("Meta-property for property 'firstProperty' should not be marked as a key.", entity.getProperty("firstProperty").isKey());
        assertTrue("Meta-property for property 'firstProperty' should be marked active.", entity.getProperty("firstProperty").isVisible());
        assertFalse("Meta-property for property 'observableProperty' should not be marked as a key.", entity.getProperty("observableProperty").isKey());
        assertTrue("Meta-property for property 'observableProperty' should be marked active.", entity.getProperty("observableProperty").isVisible());
        assertFalse("Meta-property for property 'finalProperty' should not be marked as a key.", entity.getProperty("finalProperty").isKey());
        assertFalse("Meta-property for property 'finalProperty' should be marked invisible.", entity.getProperty("finalProperty").isVisible());
    }

    @Test
    public void testThatMetaPropertiesForCompositeEntityAreAssignedCorrectKeyAndActiveValues() {
        final Integer key1 = Integer.valueOf(1);
        final String key2 = "StringCompositeKeyMember";
        final Entity key3 = entity;
        final EntityWithDynamicEntityKey entityWithDynamicEntityKey = factory.newByKey(EntityWithDynamicEntityKey.class, key1, key2, key3);

        assertFalse("Meta-property for property 'key' should not be marked as a key.", entityWithDynamicEntityKey.getProperty("key").isKey());
        assertFalse("Meta-property for property 'key' should not be marked active.", entityWithDynamicEntityKey.getProperty("key").isVisible());
        assertTrue("Meta-property for property 'key1' should be marked as a key.", entityWithDynamicEntityKey.getProperty("key1").isKey());
        assertTrue("Meta-property for property 'key1' should be marked active.", entityWithDynamicEntityKey.getProperty("key1").isVisible());
        assertTrue("Meta-property for property 'key2' should be marked as a key.", entityWithDynamicEntityKey.getProperty("key2").isKey());
        assertTrue("Meta-property for property 'key2' should be marked active.", entityWithDynamicEntityKey.getProperty("key2").isVisible());
        assertTrue("Meta-property for property 'key3' should be marked as a key.", entityWithDynamicEntityKey.getProperty("key3").isKey());
        assertTrue("Meta-property for property 'key3' should be marked active.", entityWithDynamicEntityKey.getProperty("key3").isVisible());

        assertFalse("Meta-property for property 'desc' should not be marked as a key.", entityWithDynamicEntityKey.getProperty("desc").isKey());
        assertTrue("Meta-property for property 'desc' should be marked active.", entityWithDynamicEntityKey.getProperty("desc").isVisible());
    }

    @Test
    public void testThatMetaInformationForCollectionalPropertiesIsDeterminedCorrectly() {
        assertTrue("Property 'bigDecimals' is not recognised as collectional.", entity.getProperty("bigDecimals").isCollectional());
        assertEquals("Meta-property for 'bigDecimals' has incorrect collectional type.", BigDecimal.class, entity.getProperty("bigDecimals").getPropertyAnnotationType());

        assertTrue("Property 'entities' is not recognised as collectional.", entity.getProperty("entities").isCollectional());
        assertEquals("Meta-property for 'entities' has incorrect collectional type.", Entity.class, entity.getProperty("entities").getPropertyAnnotationType());
    }

    /**
     * This test ensures the correct setter behaviour for a collectional property:
     * <ul>
     * <li>meta-property has correct validators (determination of validators is done by {@link IMetaPropertyFactory})
     * <li>changes done through mutators are observed
     * <li>validators are provided with correct values (this is actually done by {@link ObservableMutatorInterceptor})), which is checked indirectly
     * </ul>
     */
    @Test
    public void testThatCollectionalPropertySetterIsObservedAndValidated() {
        final MetaProperty<List<BigDecimal>> bigDecimalsProperty = entity.getProperty("bigDecimals");
        assertEquals("Incorrect number of validators.", 2, bigDecimalsProperty.getValidators().size());
        assertTrue("Should have domain validation.", bigDecimalsProperty.getValidators().containsKey(ValidationAnnotation.DOMAIN));
        assertTrue("Should have not-null validation.", bigDecimalsProperty.getValidators().containsKey(ValidationAnnotation.REQUIRED));
        assertNull("There should be no domain validation result at this stage.", bigDecimalsProperty.getValidationResult(ValidationAnnotation.DOMAIN));
        assertNull("There should be no rquiredness validation result at this stage.", bigDecimalsProperty.getValidationResult(ValidationAnnotation.REQUIRED));
        entity.setBigDecimals(List.of(new BigDecimal("2.0"), new BigDecimal("3.0")));
        entity.setBigDecimals(List.of(new BigDecimal("2.0")));

        assertNull("Incorrect original value", bigDecimalsProperty.getOriginalValue());
        assertTrue("Incorrect isChangedFrom original.", bigDecimalsProperty.isChangedFromOriginal());
        assertEquals("Incorrect previous value", List.of(new BigDecimal("2.0"), new BigDecimal("3.0")), bigDecimalsProperty.getPrevValue());
        assertTrue("Incorrect isChangedFrom previous.", bigDecimalsProperty.isChangedFromPrevious());
        assertTrue("Incorrect isDirty.", bigDecimalsProperty.isDirty());
        assertTrue("Incorrect isDirty for whole entity.", entity.isDirty());

        assertNotNull("There should be domain validation result at this stage.", bigDecimalsProperty.getValidationResult(ValidationAnnotation.DOMAIN));
        assertTrue("Domain validation result should be successful.", bigDecimalsProperty.getValidationResult(ValidationAnnotation.DOMAIN).isSuccessful());
        assertNotNull("There should be a requiredness validation result at this stage.", bigDecimalsProperty.getValidationResult(ValidationAnnotation.REQUIRED));
        assertTrue("Requirendess validation result should be successful.", bigDecimalsProperty.getValidationResult(ValidationAnnotation.REQUIRED).isSuccessful());

        entity.setBigDecimals(null);
        assertFalse("Requiredness validation result should not be successful.", bigDecimalsProperty.getValidationResult(ValidationAnnotation.REQUIRED).isSuccessful());
        assertEquals("Incorrect size for bigDecimals", 1, entity.getBigDecimals().size());
    }

    /**
     * This test ensures correct incrementor behaviour for a collectional property:
     * <ul>
     * <li>meta-property has correct validators (determination of validators is done by {@link IMetaPropertyFactory})
     * <li>changes done through mutators are observed
     * <li>validators are provided with correct values (this is actually done by {@link ObservableMutatorInterceptor})), which is checked indirectly
     * </ul>
     */
    @Test
    public void testThatCollectionalPropertyIncrementorIsObservedAndValidated() {
        final MetaProperty<List<BigDecimal>> bigDecimalsProperty = entity.getProperty("bigDecimals");
        entity.setBigDecimals(List.of(new BigDecimal("-2.0"), new BigDecimal("-3.0")));

        entity.addToBigDecimals(new BigDecimal("2.0"));

        assertEquals("Incorrect size for bigDecimals", 3, entity.getBigDecimals().size());

        assertNull("Incorrect original value", bigDecimalsProperty.getOriginalValue());
        assertTrue("Incorrect isChangedFrom original.", bigDecimalsProperty.isChangedFromOriginal());
        assertEquals("Incorrect previous value", List.of(new BigDecimal("-2.0"), new BigDecimal("-3.0")), bigDecimalsProperty.getPrevValue());
        assertTrue("Incorrect isChangedFrom previous.", bigDecimalsProperty.isChangedFromPrevious());
        assertTrue("Incorrect isDirty.", bigDecimalsProperty.isDirty());
        assertTrue("Incorrect isDirty for whole entity.", entity.isDirty());

        assertNotNull("There should be a domain validation result.", bigDecimalsProperty.getValidationResult(ValidationAnnotation.DOMAIN));
        assertNotNull("There should be requiredness validation result at this stage.", bigDecimalsProperty.getValidationResult(ValidationAnnotation.REQUIRED));
        assertTrue("Requiredness validation result should be successful.", bigDecimalsProperty.getValidationResult(ValidationAnnotation.REQUIRED).isSuccessful());

        entity.addToBigDecimals(null);
        assertFalse("Requiredness validation result should not be successful.", bigDecimalsProperty.getValidationResult(ValidationAnnotation.REQUIRED).isSuccessful());
        assertEquals("Incorrect size for bigDecimals", 3, entity.getBigDecimals().size());
        assertNull("Null value is expected for the last invalid value.", entity.getProperty("bigDecimals").getLastInvalidValue());
    }

    /**
     * This test ensures correct decrementor behaviour for a collectional property:
     * <ul>
     * <li>meta-property has correct validators (determination of validators is done by {@link IMetaPropertyFactory})
     * <li>changes done through mutators are observed
     * <li>validators are provided with correct values (this is actually done by {@link ObservableMutatorInterceptor})), which is checked indirectly
     * </ul>
     */
    @Test
    public void testThatCollectionalPropertyDecrementorIsObservedAndValidated() {
        final MetaProperty<List<BigDecimal>> bigDecimalsProperty = entity.getProperty("bigDecimals");
        entity.setBigDecimals(List.of(new BigDecimal("-2.0"), new BigDecimal("-3.0")));

        entity.removeFromBigDecimals(new BigDecimal("-2.0"));

        assertEquals("Incorrect size for bigDecimals", 1, entity.getBigDecimals().size());

        assertNull("Incorrect original value", bigDecimalsProperty.getOriginalValue());
        assertTrue("Incorrect isChangedFrom original.", bigDecimalsProperty.isChangedFromOriginal());
        assertEquals("Incorrect previous value", List.of(new BigDecimal("-2.0"), new BigDecimal("-3.0")), bigDecimalsProperty.getPrevValue());
        assertTrue("Incorrect isChangedFrom previous.", bigDecimalsProperty.isChangedFromPrevious());
        assertTrue("Incorrect isDirty.", bigDecimalsProperty.isDirty());
        assertTrue("Incorrect isDirty for whole entity.", entity.isDirty());

        assertNotNull("There should be domain validation result at this stage.", bigDecimalsProperty.getValidationResult(ValidationAnnotation.DOMAIN));
        assertTrue("Domain validation result should be successful.", bigDecimalsProperty.getValidationResult(ValidationAnnotation.DOMAIN).isSuccessful());
        assertNotNull("There should be requiredness validation result.", bigDecimalsProperty.getValidationResult(ValidationAnnotation.REQUIRED));
        assertEquals("Incorrect size for bigDecimals", 1, entity.getBigDecimals().size());
    }

    @Test
    public void testMetaPropertyComparisonLogic() {
        final Integer key1 = Integer.valueOf(1);
        final String key2 = "StringCompositeKeyMember";
        final Entity key3 = entity;

        final EntityWithDynamicEntityKey entityWithDynamicEntityKey = factory.newByKey(EntityWithDynamicEntityKey.class, key1, key2, key3);

        final Set<MetaProperty<?>> metaProperties = Finder.getMetaProperties(entityWithDynamicEntityKey);
        assertEquals("Incorrect number of meta properties.", 3, metaProperties.size());

        final List<MetaProperty<?>> list = new ArrayList<>();
        list.addAll(metaProperties);
        assertEquals("Incorrect sorting order for meta-properties.", "key1", list.get(0).getName());
        assertEquals("Incorrect sorting order for meta-properties.", "key2", list.get(1).getName());
        assertEquals("Incorrect sorting order for meta-properties.", "key3", list.get(2).getName());
    }

    @Test
    public void testValidationAndSettingRestrictionInObservableMutator() {
        // preparing
        final MetaProperty<BigDecimal> metaProperty = entity.getProperty("observableProperty");
        // 1. test the error recovery by the same value
        entity.setObservableProperty(new BigDecimal("100.0"));
        entity.setObservableProperty(null);
        entity.setObservableProperty(new BigDecimal("100.0"));
        assertTrue("the property after the error recovery have to be valid", metaProperty.isValid());

        // 2. test the error recovery by the different value
        entity.setObservableProperty(new BigDecimal("100.0"));
        entity.setObservableProperty(null);
        entity.setObservableProperty(new BigDecimal("101.0"));
        assertTrue("the property after the error recovery have to be valid", metaProperty.isValid());

        // 3. collectional property validation/observation tested in previous tests

        // 4. test simple property validation/observation invoking for different new/old values :
        entity.setObservableProperty(new BigDecimal("100.0"));
        entity.setObservableProperty(new BigDecimal("101.0"));
        assertTrue("the property after different value setted have to be valid", metaProperty.isValid());

        // 5. test simple property validation/observation not invoking for the same new/old values :
        entity.setObservableProperty(new BigDecimal("100.0"));
        entity.setObservableProperty(new BigDecimal("100.0"));
        assertTrue("the property after the same value setted have to be valid", metaProperty.isValid());

        // 6. test simple property validation/observation in case where it is initialised with NULL at construction time
        // previous value, which is also original, is null
        // property is valid since no mutator was yet invoked
        // property has NotNull validation
        entity.setObservablePropertyInitialisedAsNull(null);
        assertTrue("ObservablePropertyInitialisedAsNull should be still be recognised as valid since setting null does not change its previous value, and  thus no validation should occur.", entity.getProperty("observablePropertyInitialisedAsNull").isValid());
    }

    @Test
    public void testValidationWarnings() {
        // preparing
        final MetaProperty<Integer> metaProperty = entity.getProperty("number");
        entity.setNumber(40);
        // 1. test warning generation on Domain Validation level:
        entity.setNumber(77);
        assertTrue("DOMAIN : The property should be valid with warning.", metaProperty.isValid());
        assertTrue("DOMAIN : The property should have warnings.", metaProperty.hasWarnings());
        assertEquals("DOMAIN : Warning value should be set.", Integer.valueOf(77), entity.get("number"));

        // 2. recovery :
        entity.setNumber(40);
        assertTrue("DOMAIN : The property should remain valid after recovery.", metaProperty.isValid());
        assertFalse("DOMAIN : The property should not have warnings after recovery.", metaProperty.hasWarnings());
        assertEquals("DOMAIN : Recovery value should be set.", Integer.valueOf(40), entity.get("number"));

        // 3. test warning generation on Dynamic Validation level (validation inside setter):
        entity.setNumber(777);
        assertTrue("DYNAMIC : The property should be valid with warning.", metaProperty.isValid());
        assertTrue("DYNAMIC : The property should have warnings.", metaProperty.hasWarnings());
        assertEquals("DYNAMIC : Warning value should be set.", Integer.valueOf(777), entity.get("number"));

        // 4. recovery :
        entity.setNumber(40);
        assertTrue("The property should remain valid after recovery.", metaProperty.isValid());
        assertFalse("The property should not have warnings after recovery.", metaProperty.hasWarnings());
        assertEquals("Recovery value should be set.", Integer.valueOf(40), entity.get("number"));

        // 5. test warning generation on Dynamic Validation level (validation inside setter):
        entity.setNumber(777);
        assertTrue("DYNAMIC : The property should be valid with warning.", metaProperty.isValid());
        assertTrue("DYNAMIC : The property should have warnings.", metaProperty.hasWarnings());
        assertEquals("DYNAMIC : Warning value should be set.", Integer.valueOf(777), entity.get("number"));

        // 6. recovery by setting another warning value! :
        entity.setNumber(77);
        assertTrue("The property should remain valid after recovery (by setting another warning value).", metaProperty.isValid());
        assertTrue("The property should have warnings after recovery (by setting another warning value).", metaProperty.hasWarnings());
        assertEquals("Recovery warning value should be set.", Integer.valueOf(77), entity.get("number"));
    }

    @Test
    public void testCorrectAssignmentOfMetaPropertyPropertiesBaseOnAnnotations() {
        final MetaProperty<Integer> firstPropertyMetaProp = entity.getProperty("firstProperty");
        assertTrue("Should be requried", firstPropertyMetaProp.isRequired());
        assertFalse("Should not be editable", firstPropertyMetaProp.isEditable());
        assertEquals("Incorrect title", "First Property", firstPropertyMetaProp.getTitle());
        assertEquals("Incorrect desc", "used for testing", firstPropertyMetaProp.getDesc());

        final MetaProperty<BigDecimal> observablePropertyMetaProp = entity.getProperty("observableProperty");
        assertTrue("Should be requried", observablePropertyMetaProp.isRequired());
        assertTrue("Should be editable", observablePropertyMetaProp.isEditable());
        assertEquals("Incorrect title", "Observable Property", observablePropertyMetaProp.getTitle());
        assertEquals("Incorrect desc", "Observable Property", observablePropertyMetaProp.getDesc());

        final MetaProperty<BigDecimal> finalPropertyMetaProp = entity.getProperty("finalProperty");
        assertFalse("Should not bevisible", finalPropertyMetaProp.isVisible());

        final MetaProperty<String> keyMetaProp = entity.getProperty("key");
        assertTrue("Should be requried", keyMetaProp.isRequired());
        assertTrue("Should be editable", keyMetaProp.isEditable());
        assertEquals("Incorrect title", "Entity No", keyMetaProp.getTitle());
        assertEquals("Incorrect desc", "Key Property", keyMetaProp.getDesc());

        final MetaProperty<String> descMetaProp = entity.getProperty("desc");
        assertTrue("Should be requried", descMetaProp.isRequired());
        assertTrue("Should be editable", descMetaProp.isEditable());
        assertEquals("Incorrect title", "Description", descMetaProp.getTitle());
        assertEquals("Incorrect desc", "Description Property", descMetaProp.getDesc());
    }

    @Test
    public void required_by_declaration_property_cannot_have_null_value() {
        final MetaProperty<Integer> firstPropertyMetaProp = entity.getProperty("firstProperty");
        assertTrue("Should be required", firstPropertyMetaProp.isRequired());
        assertTrue("REQUIREDValidator should be present for 'required' property.", firstPropertyMetaProp.getValidators().containsKey(ValidationAnnotation.REQUIRED));

        entity.setFirstProperty(null);
        assertFalse("Required property is not yet populated and thus entity should be invalid.", entity.isValid().isSuccessful());

        entity.setFirstProperty(56);
        assertTrue("Required property with not null value have to be valid.", firstPropertyMetaProp.isValid());
        assertTrue("Should remain required", firstPropertyMetaProp.isRequired());
    }

    @Test
    public void not_required_by_declaration_property_may_have_its_requiredness_changed_at_runtime() {
        final MetaProperty<Money> secondMetaProperty = entity.getProperty("money");
        secondMetaProperty.setRequired(true);
        assertTrue("Should be required", secondMetaProperty.isRequired());
        assertTrue("REQUIREDValidator should be present for 'required' property.", secondMetaProperty.getValidators().containsKey(ValidationAnnotation.REQUIRED));

        secondMetaProperty.setRequired(false);
        assertFalse("Should not be required", secondMetaProperty.isRequired());
        assertTrue("REQUIREDValidator should be present for 'required in the past' property.", secondMetaProperty.getValidators().containsKey(ValidationAnnotation.REQUIRED));
        entity.setMoney(null);
        assertTrue("Not 'required' property (with no NotNullValidator) with null value have to be valid.", secondMetaProperty.isValid());
    }

    @Test
    public void required_by_declaration_property_without_a_value_is_invalid_at_both_meta_property_and_entity_levels() {
        final MetaProperty<Integer> firstPropertyMetaProp = entity.getProperty("firstProperty");

        assertNull("Required property (with null value) first failure should be null (before isValid() invoked).", firstPropertyMetaProp.getFirstFailure());
        assertFalse("Required property (with null value) should be invalid.", firstPropertyMetaProp.isValidWithRequiredCheck(false));
        assertNotNull("Required property (with null value) first failure should be null (after isValid() invoked).", firstPropertyMetaProp.getFirstFailure());
        assertFalse("Entity with required property with null value should be invalid.", entity.isValid().isSuccessful());
    }

    @Test(expected=EntityDefinitionException.class)
    public void relaxing_requiredness_for_properties_declared_as_required_is_not_permitted() {
        final MetaProperty<Integer> firstPropertyMetaProp = entity.getProperty("firstProperty");
        firstPropertyMetaProp.setRequired(false);
    }

    @Test
    public void testSetterExceptionsAndResultsHandling() {
        final MetaProperty<Integer> property = entity.getProperty("number");
        // No exception have to be thrown from setter.
        try {
            entity.setNumber(10);
        } catch (final Throwable e) {
            fail("Have not to throw NullPointerException!");
        } finally {
            assertTrue("The 'number' property have to be valid.", property.isValid());
            assertFalse("DynamicValidator have to be null before any Result thrown.", property.containsDynamicValidator());
            assertEquals("The value in the 'number' property is not correct.", (Object) 10, entity.getNumber());
        }

        // NullPointerException have to be thrown on the higher level.
        try {
            entity.setNumber(null);
            fail("Have to throw NullPointerException!");
        } catch (final Throwable e) {
            assertTrue("The throwed exception have to be instanceof NullPointerException", e instanceof NullPointerException);
        } finally {
            assertTrue("The 'number' property have to be valid. Because any validation succeeded -> just unhandled exception is thrown in setter!", property.isValid());
            assertFalse("DynamicValidator have to be null before any Result thrown.", property.containsDynamicValidator());
            assertEquals("The value in the 'number' property is not correct.", (Object) 10, entity.getNumber());
        }

        // IllegalArgumentException have to be thrown on the higher level.
        try {
            entity.setNumber(50);
            fail("Have to throw IllegalArgumentException!");
        } catch (final Throwable e) {
            assertTrue("The throwed exception have to be instanceof IllegalArgumentException", e instanceof IllegalArgumentException);
        } finally {
            assertTrue("The 'number' property have to be valid. Because any validation succeeded -> just unhandled exception is thrown in setter!", property.isValid());
            assertFalse("DynamicValidator have to be null before any Result thrown.", property.containsDynamicValidator());
            assertEquals("The value in the 'number' property is not correct.", (Object) 10, entity.getNumber());
        }

        // DomainValidation have to be not successful.
        try {
            entity.setNumber(35);
        } catch (final Throwable e) {
            fail("Have not to throw any Exception!");
        } finally {
            assertFalse("The 'number' property have to be invalid(Domain Validation).", property.isValid());
            assertFalse("DynamicValidator have to be null before any Result thrown.", property.containsDynamicValidator());
            assertEquals("The value in the 'number' property is not correct.", (Object) 10, entity.getNumber());
        }

        // Result exception (DYNAMIC validator) have to be throwed but catched in ObservableMutator (and also handled)!
        try {
            entity.setNumber(100);
        } catch (final Throwable e) {
            fail("Have not to throw Result! it have to be handled inside ObservableMutatorInterceptor!");
        } finally {
            assertFalse("The 'number' property have to be invalid.", property.isValid());
            assertTrue("DynamicValidator have to be not null after any Result has been thrown.", property.containsDynamicValidator());
            assertNotNull("The result for DYNAMIC validator have to be not null.", property.getValidationResult(ValidationAnnotation.DYNAMIC));
            assertFalse("The result for DYNAMIC validator have to be not successful.", property.getValidationResult(ValidationAnnotation.DYNAMIC).isSuccessful());
            assertEquals("The value in the 'number' property is not correct.", (Object) 10, entity.getNumber());
        }
    }

    @Test
    public void testPropertyDependencies() {
        try {
            entity.setDependent(15);
            assertTrue("Dependent property have to be valid.", entity.getProperty("dependent").isValid());
            assertEquals("Incorrect value.", (Integer) 15, entity.getDependent());

            entity.setDependent(25);
            assertFalse("Dependent property has to be invalid.", entity.getProperty("dependent").isValid());
            assertEquals("Incorrect value.", (Integer) 15, entity.getDependent());
            assertEquals("Incorrect lastInvalidValue.", 25, entity.getProperty("dependent").getLastInvalidValue());

            // Update incorrect value of the dependent property by setting the main property.
            entity.setMain(30);
            assertTrue("Dependent property have to be valid.", entity.getProperty("dependent").isValid());
            assertEquals("Incorrect value.", (Integer) 25, entity.getDependent());
            assertEquals("Incorrect lastInvalidValue.", null, entity.getProperty("dependent").getLastInvalidValue());

            assertTrue("Main property have to be valid.", entity.getProperty("main").isValid());
            assertEquals("Incorrect value.", (Integer) 30, entity.getMain());
            assertEquals("Incorrect lastInvalidValue.", null, entity.getProperty("main").getLastInvalidValue());

            // Do not update anything in the case of correct dependent property.
            entity.setDependent(15);
            assertTrue("Dependent property have to be valid.", entity.getProperty("dependent").isValid());
            assertEquals("Incorrect value.", (Integer) 15, entity.getDependent());

            entity.setMain(35);
            assertTrue("Dependent property have to be valid.", entity.getProperty("dependent").isValid());
            assertEquals("Incorrect value.", (Integer) 15, entity.getDependent());
            assertEquals("Incorrect lastInvalidValue.", null, entity.getProperty("dependent").getLastInvalidValue());

            assertTrue("Main property have to be valid.", entity.getProperty("main").isValid());
            assertEquals("Incorrect value.", (Integer) 35, entity.getMain());
            assertEquals("Incorrect lastInvalidValue.", null, entity.getProperty("main").getLastInvalidValue());
        } catch (final Result e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test_get_last_attempt_value() {
        final Entity entity = factory.newEntity(Entity.class, "key", "description");
        entity.setNumber(5);
        assertEquals(5, entity.getProperty("number").getLastAttemptedValue());

        // setting invalid value
        entity.setNumber(35);
        assertEquals(35, entity.getProperty("number").getLastAttemptedValue());

        // setting once again incorrect value
        entity.setNumber(100);
        assertEquals(100, entity.getProperty("number").getLastAttemptedValue());

        // setting correct value
        entity.setNumber(25);
        assertEquals(25, entity.getProperty("number").getLastAttemptedValue());
    }

    @Test
    public void test_original_state_can_be_restored() {
        final Entity entity = factory.newEntity(Entity.class, "key", "description");
        final Money originalMoney = entity.getMoney();
        entity.setMoney(new Money("23.25"));
        final Date originalDate = entity.getDate();
        entity.setDate(new Date());

        entity.restoreToOriginal();
        assertEquals("Could not restore to original.", originalMoney, entity.getMoney());
        assertEquals("Could not restore to original.", originalDate, entity.getDate());
    }

    @Test
    public void test_that_composite_key_is_not_affected_by_restore_to_original_operation() {
        final CorrectEntityWithDynamicEntityKey compositeEntity = factory.newEntity(CorrectEntityWithDynamicEntityKey.class, 12L);
        assertNotNull("Composite should not be null.", compositeEntity.getKey());

        final Long orig1 = compositeEntity.getProperty1();
        final Long orig2 = compositeEntity.getProperty2();

        compositeEntity.setProperty1(1L);
        compositeEntity.setProperty2(2L);

        compositeEntity.restoreToOriginal();
        assertEquals("Could not restore to original.", orig1, compositeEntity.getProperty1());
        assertEquals("Could not restore to original.", orig2, compositeEntity.getProperty2());
        assertNotNull("Composite should not be null after restoration to original.", compositeEntity.getKey());
    }

    @Test
    public void test_copy_for_same_type() {
        final Entity entity = factory.newEntity(Entity.class, 1L);
        entity.setKey("key");
        entity.setDesc("description");
        entity.setMoney(new Money("23.25"));

        final Entity copy = entity.copy(Entity.class);

        assertEquals("Copy does not equal to the original instance", entity, copy);
        assertFalse("Should have not been dirty", copy.isDirty());
        assertEquals("Property id does not match", entity.getId(), copy.getId());
        assertEquals("Property desc does not match", entity.getDesc(), copy.getDesc());
        assertEquals("Property money does not match", entity.getMoney(), copy.getMoney());
    }

    @Test
    public void copy_without_identity_is_supported() {
        final Entity entity = factory.newEntity(Entity.class, 1L);
        entity.setKey("key");
        entity.setDesc("description");
        entity.setMoney(new Money("23.25"));

        final Entity copy = entity.copyWithoutIdentity(Entity.class);

        assertEquals("Copy does not equal to the original instance", entity, copy);
        assertTrue("Entity with no id should be recognized as drity.", copy.isDirty());
        assertNull("Id should not have been copied", copy.getId());
        assertEquals("Property desc does not match", entity.getDesc(), copy.getDesc());
        assertEquals("Property money does not match", entity.getMoney(), copy.getMoney());
    }

    @Test
    public void test_copy_for_parent_to_descendant() {
        final Entity entity = factory.newEntity(Entity.class, 1L);
        entity.setKey("key");
        entity.setDesc("description");
        entity.setMoney(new Money("23.25"));

        final EntityExt copy = entity.copy(EntityExt.class);

        assertTrue("Unexpected type of copy", copy instanceof EntityExt);
        assertFalse("Copy of descendant should not be equal to the original instance because of type difference", entity.equals(copy));
        assertFalse("Should have not been dirty", copy.isDirty());
        assertEquals("Property id does not match", entity.getId(), copy.getId());
        assertEquals("Property desc does not match", entity.getDesc(), copy.getDesc());
        assertEquals("Property money does not match", entity.getMoney(), copy.getMoney());
        assertNull("Additional property should be null", copy.getAdditionalProperty());
    }

    @Test
    public void test_copy_for_descendant_back_to_parennt() {
        final EntityExt entity = factory.newEntity(EntityExt.class, 1L);
        entity.setKey("key");
        entity.setDesc("description");
        entity.setMoney(new Money("23.25"));
        entity.setAdditionalProperty(new BigDecimal("23.0"));

        final Entity copy = entity.copy(Entity.class);

        assertTrue("Unexpected type of copy", copy instanceof Entity);
        assertFalse("Copy of descendant should not be equal to the original instance because of type difference", entity.equals(copy));
        assertFalse("Should have not been dirty", copy.isDirty());
        assertEquals("Property id does not match", entity.getId(), copy.getId());
        assertEquals("Property desc does not match", entity.getDesc(), copy.getDesc());
        assertEquals("Property money does not match", entity.getMoney(), copy.getMoney());
    }

    @Test
    public void test_copy_for_entities_with_dynamic_key() {
        final CorrectEntityWithDynamicEntityKey one = factory.newEntity(CorrectEntityWithDynamicEntityKey.class, 1L);
        one.property1 = 38L;
        one.property2 = 98L;
        final DynamicEntityKey keyOne = new DynamicEntityKey(one);
        one.setKey(keyOne);

        Object[] values = one.getKey().getKeyValues();
        assertEquals("Incorrect number of values.", 2, values.length);
        assertEquals("Incorrect value for the first key property.", 38L, values[0]);
        assertEquals("Incorrect value for the second key property.", 98L, values[1]);
        assertEquals("Incorrect value for the first key property.", (Long) 38L, one.getProperty1());
        assertEquals("Incorrect value for the second key property.", (Long) 98L, one.getProperty2());

        final CorrectEntityWithDynamicEntityKey copy = one.copy(CorrectEntityWithDynamicEntityKey.class);

        values = copy.getKey().getKeyValues();
        assertEquals("Incorrect number of values.", 2, values.length);
        assertEquals("Incorrect value for the first key property.", 38L, values[0]);
        assertEquals("Incorrect value for the second key property.", 98L, values[1]);
        assertEquals("Incorrect value for the first key property.", (Long) 38L, copy.getProperty1());
        assertEquals("Incorrect value for the second key property.", (Long) 98L, copy.getProperty2());

        copy.setProperty1(40L);
        copy.setProperty2(100L);

        values = copy.getKey().getKeyValues();
        assertEquals("Incorrect number of values.", 2, values.length);
        assertEquals("Incorrect value for the first key property.", 40L, values[0]);
        assertEquals("Incorrect value for the second key property.", 100L, values[1]);
        assertEquals("Incorrect value for the first key property.", (Long) 40L, copy.getProperty1());
        assertEquals("Incorrect value for the second key property.", (Long) 100L, copy.getProperty2());
    }

    @Test
    public void copy_from_non_instrumented_instance_is_also_non_instrumented() {
        final Entity entity = new Entity();
        entity.setVersion(42L);
        entity.setId(1L);
        entity.setKey("key");
        entity.setDesc("description");
        entity.setMoney(new Money("23.25"));

        final Entity copy = entity.copy(Entity.class);

        assertEquals("Copy does not equal to the original instance", entity, copy);
        assertFalse("Copy is instrumented", copy.isInstrumented());
        assertEquals("IDs do not match", entity.getId(), copy.getId());
        assertEquals("Versions do not match.", Long.valueOf(42L), copy.getVersion());
        assertEquals("Property desc does not match", entity.getDesc(), copy.getDesc());
        assertEquals("Property money does not match", entity.getMoney(), copy.getMoney());
    }

    @Test
    public void copy_from_instrumented_instance_is_also_instrumented() {
        final Entity entity = factory.newEntity(Entity.class);
        entity.setVersion(42L);
        entity.setId(1L);
        entity.setKey("key");
        entity.setDesc("description");
        entity.setMoney(new Money("23.25"));

        final Entity copy = entity.copy(Entity.class);

        assertEquals("Copy does not equal to the original instance", entity, copy);
        assertTrue("Copy is not instrumented", copy.isInstrumented());
        assertFalse("Copy is dirty", copy.isDirty());
        assertEquals("IDs do not match", entity.getId(), copy.getId());
        assertEquals("Versions do not match.", Long.valueOf(42L), copy.getVersion());
        assertEquals("Property desc does not match", entity.getDesc(), copy.getDesc());
        assertEquals("Property money does not match", entity.getMoney(), copy.getMoney());
    }

    @Test
    public void copy_happens_in_the_initialisation_mode() {
        final Entity entity = factory.newEntity(Entity.class);
        entity.setVersion(42L);
        entity.setId(1L);
        entity.setKey("key");
        entity.setDesc("description");
        entity.setMoney(new Money("23.25"));

        final Entity copy = entity.copy(Entity.class);

        assertEquals("Copy does not equal to the original instance", entity, copy);
        assertTrue("Copy is not instrumented", copy.isInstrumented());
        assertFalse("Copy is dirty", copy.isDirty());
        assertFalse("Property key is copied, but should be recognised as not dirty", copy.getProperty("key").isDirty());
        assertEquals("IDs do not match", entity.getId(), copy.getId());
        assertEquals("Versions do not match.", Long.valueOf(42L), copy.getVersion());
        assertEquals("Property desc does not match", entity.getDesc(), copy.getDesc());
        assertFalse("Property desc is copied, but should be recognised as not dirty", copy.getProperty("desc").isDirty());
        assertEquals("Property money does not match", entity.getMoney(), copy.getMoney());
        assertFalse("Property money is copied, but should be recognised as not dirty", copy.getProperty("money").isDirty());
    }

    @Test
    public void copy_from_uninstrumented_proxied_instance_is_also_uninstrumented_and_proxied() {
        final Class<? extends Entity> type = EntityProxyContainer.proxy(Entity.class, "firstProperty", "monitoring", "observableProperty");

        final Entity entity = EntityFactory.newPlainEntity(type, 12L);
        entity.setVersion(42L);
        entity.setKey("key");
        entity.setDesc("description");

        final Entity copy = entity.copy(type);
        assertEquals("Copy does not equal to the original instance", entity, copy);
        assertFalse("Copy is instrumented", copy.isInstrumented());
        assertEquals("Property id does not match", entity.getId(), copy.getId());
        assertEquals("Versions should match.", Long.valueOf(42L), copy.getVersion());
        assertEquals("Property desc does not match", entity.getDesc(), copy.getDesc());
        assertEquals("Proxied properties do not match", entity.proxiedPropertyNames(), copy.proxiedPropertyNames());
    }

    @Test
    public void copy_from_instrumented_proxied_instance_is_also_instrumented_and_proxied() {
        final Class<? extends Entity> type = EntityProxyContainer.proxy(Entity.class, "firstProperty", "monitoring", "observableProperty");

        final Entity entity = factory.newEntity(type, 12L);
        entity.setVersion(42L);
        entity.setKey("key");
        entity.setDesc("description");

        final Entity copy = entity.copy(type);
        assertEquals("Copy does not equal to the original instance", entity, copy);
        assertTrue("Copy is not instrumented", copy.isInstrumented());
        assertEquals("Property id does not match", entity.getId(), copy.getId());
        assertEquals("Versions should match.", Long.valueOf(42L), copy.getVersion());
        assertEquals("Property desc does not match", entity.getDesc(), copy.getDesc());
        assertEquals("Proxied properties do not match", entity.proxiedPropertyNames(), copy.proxiedPropertyNames());
    }

    @Test
    public void two_instances_of_the_same_type_with_the_same_key_values_are_equal() {
        final Entity thisEntity = factory.newEntity(Entity.class, 1L);
        thisEntity.setKey("key");
        final Entity thatEntity = factory.newEntity(Entity.class, 2L);
        thatEntity.setKey("key");

        assertTrue("Should be equal", thisEntity.equals(thatEntity));
        assertTrue("Should be equal", thatEntity.equals(thisEntity));
    }

    @Test
    public void test_equals_for_instances_of_the_same_type_with_the_different_key_values() {
        final Entity thisEntity = factory.newEntity(Entity.class, 1L);
        thisEntity.setKey("key1");
        final Entity thatEntity = factory.newEntity(Entity.class, 2L);
        thatEntity.setKey("key2");

        assertFalse("Should not be equal", thisEntity.equals(thatEntity));
        assertFalse("Should not be equal", thatEntity.equals(thisEntity));
    }

    @Test
    public void test_equals_for_instances_of_the_different_types_with_the_same_key_values() {
        final Entity thisEntity = factory.newEntity(EntityExt.class, 1L);
        thisEntity.setKey("key");
        final Entity thatEntity = factory.newEntity(Entity.class, 2L);
        thatEntity.setKey("key");

        assertFalse("Should not be equal", thisEntity.equals(thatEntity));
        assertFalse("Should not be equal", thatEntity.equals(thisEntity));
    }

    @Test
    public void method_set_works_for_non_union_entities() {
        final SecondLevelEntity inst = new SecondLevelEntity();
        inst.setPropertyOfSelfType(inst);

        inst.set("property", "value");
        assertEquals("The property value of the SecondLevelInstance must be \"value\"", "value", inst.getProperty());
    }

    @Test
    public void test_illegal_propertyDescriptor_property_definition_without_type_definition() {
        try {
            factory.newEntity(MasterEntity1.class, "key", "description");
            fail("Should be failed.");
        } catch (final Exception e) {
        }
    }

    @Test
    public void test_illegal_one2many_collectional_property_definition_without_type_definition() {
        try {
            factory.newEntity(MasterEntity2.class, "key", "description");
            fail("Should be failed.");
        } catch (final Exception e) {
        }
    }

    @Test
    public void test_illegal_one2many_collectional_property_definition_without_linkProperty_definition() {
        try {
            factory.newEntity(MasterEntity3.class, "key", "description");
            fail("Should be failed.");
        } catch (final Exception e) {
        }
    }

    @Test
    public void test_legal_single_property_definition_without_linkProperty_definition_which_makes_many2one_association() {
        factory.newEntity(MasterEntity4.class, "key", "description"); // should be considered legal
        assertFalse("Should be many2one association.", Finder.isOne2Many_or_One2One_association(MasterEntity4.class, "one2manyAssociationSpecialCase"));
    }

    @Ignore // FIXME refer the explanation in AbstracEntity.earlyRuntimePropertyDefinitionValidation
    @Test
    public void test_illegal_one2one_property_definition_with_KEY_of_non_parent_type() {
        try {
            factory.newEntity(MasterEntity6.class, "key", "description");
            fail("Should be failed.");
        } catch (final Exception e) {
        }
    }

    @Test
    public void sameAs_for_two_entities_with_same_id_should_return_true() {
        final Entity thisEntity = factory.newEntity(Entity.class, 1L, "key1", "description");
        final Entity thatEntity = factory.newEntity(Entity.class, 1L, "key2", "description");

        assertTrue(thisEntity.sameAs(thatEntity));
    }

    @Test
    public void sameAs_for_non_persisted_entities_with_the_same_keys_should_return_true() {
        final Entity thisEntity = factory.newEntity(Entity.class, "key", "description");
        final Entity thatEntity = factory.newEntity(Entity.class, "key", "description");

        assertTrue(thisEntity.sameAs(thatEntity));
    }

    @Test
    public void sameAs_for_non_persisted_entities_with_the_different_keys_should_return_false() {
        final Entity thisEntity = factory.newEntity(Entity.class, "key1", "description");
        final Entity thatEntity = factory.newEntity(Entity.class, "key2", "description");

        assertFalse(thisEntity.sameAs(thatEntity));
    }

    @Test
    public void sameAs_when_that_entity_is_null_should_return_false() {
        final Entity thisEntity = factory.newEntity(Entity.class, 1L, "key1", "description");

        assertFalse(thisEntity.sameAs(null));
    }

    @Test
    public void sameAs_when_that_entity_is_not_persisted_should_return_false() {
        final Entity thisEntity = factory.newEntity(Entity.class, 1L, "key", "description");
        final Entity thatEntity = factory.newEntity(Entity.class, "key", "description");

        assertFalse(thisEntity.sameAs(thatEntity));
    }

    @Test
    public void sameAs_for_non_persisted_instance_with_different_keys_should_return_false() {
        final Entity thisEntity = factory.newEntity(Entity.class, "key1", "description");
        final Entity thatEntity = factory.newEntity(Entity.class, "key2", "description");

        assertFalse(thisEntity.sameAs(thatEntity));
    }

    @Test
    public void required_desc_should_not_permit_empty_values() {
        final Entity entity = factory.newEntity(Entity.class, "key1", "description");
        assertTrue(entity.getProperty("desc").isValid());

        entity.setDesc("");
        assertFalse(entity.getProperty("desc").isValid());

    }

    @Test
    public void required_desc_should_not_permit_blank_values() {
        final Entity entity = factory.newEntity(Entity.class, "key1", "description");
        assertTrue(entity.getProperty("desc").isValid());

        entity.setDesc("    ");
        assertFalse(entity.getProperty("desc").isValid());
    }

    @Test
    public void required_desc_should_not_permit_null_values() {
        final Entity entity = factory.newEntity(Entity.class, "key1", "description");
        assertTrue(entity.getProperty("desc").isValid());

        entity.setDesc(null);
        assertFalse(entity.getProperty("desc").isValid());
    }

    @Test
    public void required_desc_should_not_be_valid_after_instantiation() {
        final Entity entity = factory.newByKey(Entity.class, "key1");

        final MetaProperty<String> descProperty = entity.getProperty("desc");
        assertFalse(descProperty.isValid());

        // let's also check that blank value is not permitted even when the original value was blank
        entity.setDesc("    ");
        assertFalse(descProperty.isValid());
    }

    @Test
    public void required_desc_should_support_custo_error_messages_with_templating() {
        final Entity entity = factory.newByKey(Entity.class, "key1");
        final MetaProperty<String> descProperty = entity.getProperty("desc");
        assertFalse(descProperty.isValid());
        assertEquals(
                format("Property \"%s\" in entity \"%s\" does not permit blank values.",
                        TitlesDescsGetter.getTitleAndDesc("desc", Entity.class).getKey(),
                        TitlesDescsGetter.getEntityTitleAndDesc(Entity.class).getKey()),
                descProperty.getFirstFailure().getMessage());
    }

    @Test
    public void warnings_are_empty_for_entity_without_any_properties_with_warnings() {
        final EntityWithWarnings entity = factory.newByKey(EntityWithWarnings.class, "some key");
        entity.setDesc("some desc");
        entity.setIntProp(20);

        assertFalse(entity.hasWarnings());
        assertTrue(entity.warnings().isEmpty());
        assertTrue(entity.isValid().isSuccessful());
        assertTrue(entity.isValid().isSuccessfulWithoutWarning());
    }

    @Test
    public void number_of_warnings_is_equal_to_number_of_entity_properties_with_warnings() {
        final EntityWithWarnings entity = factory.newByKey(EntityWithWarnings.class, "some key");
        entity.setDesc("some desc");
        entity.setSelfRefProp(entity);
        entity.setIntProp(120);

        assertTrue(entity.hasWarnings());
        assertEquals(2, entity.warnings().size());
        assertTrue(entity.isValid().isSuccessful());
        assertFalse(entity.isValid().isSuccessfulWithoutWarning());
    }

    @Test
    public void one_warning_is_identified_for_entity_with_one_property_in_error_and_one_property_with_warning() {
        final EntityWithWarnings entity = factory.newByKey(EntityWithWarnings.class, "some key");
        entity.setSelfRefProp(null);
        entity.setIntProp(120);

        assertFalse(entity.isValid().isSuccessful());
        assertTrue(entity.hasWarnings());
        assertEquals(1, entity.warnings().size());
    }

    @Test
    public void warnings_are_identified_correctly_after_reassigning_the_property_value_to_valid_one() {
        final EntityWithWarnings entity = factory.newByKey(EntityWithWarnings.class, "some key");

        // assign warning triggering value
        entity.setIntProp(120);
        assertTrue(entity.hasWarnings());
        assertEquals(1, entity.warnings().size());

        // assign warning clearing value
        entity.setIntProp(20);
        assertFalse(entity.hasWarnings());
        assertEquals(0, entity.warnings().size());
    }

    @Test
    public void numeric_props_with_precision_but_without_scale_are_invalid() {
        final Either<Exception, EntityWithInvalidMoneyPropWithPrecision> result = Try(() -> factory.newByKey(EntityWithInvalidMoneyPropWithPrecision.class, "some key"));
        assertTrue(result instanceof Left);
        final Left<Exception, EntityWithInvalidMoneyPropWithPrecision> left = (Left<Exception, EntityWithInvalidMoneyPropWithPrecision>) result;
        final Throwable ex = left.value().getCause().getCause();
        assertTrue(ex instanceof EntityDefinitionException);
        assertEquals(format(INVALID_USE_FOR_PRECITION_AND_SCALE_MSG, "numericMoney", EntityWithInvalidMoneyPropWithPrecision.class.getName()), ex.getMessage());
    }

    @Test
    public void numeric_props_with_scale_but_without_precision_are_invalid() {
        final Either<Exception, EntityWithInvalidMoneyPropWithScale> result = Try(() -> factory.newByKey(EntityWithInvalidMoneyPropWithScale.class, "some key"));
        assertTrue(result instanceof Left);
        final Left<Exception, EntityWithInvalidMoneyPropWithScale> left = (Left<Exception, EntityWithInvalidMoneyPropWithScale>) result;
        final Throwable ex = left.value().getCause().getCause();
        assertTrue(ex instanceof EntityDefinitionException);
        assertEquals(format(INVALID_USE_FOR_PRECITION_AND_SCALE_MSG, "numericMoney", EntityWithInvalidMoneyPropWithScale.class.getName()), ex.getMessage());
    }

    @Test
    public void numeric_props_with_negative_precision_are_invalid() {
        final Either<Exception, EntityWithInvalidMoneyPropWithNegativePrecisionAndPositiveScale> result = Try(() -> factory.newByKey(EntityWithInvalidMoneyPropWithNegativePrecisionAndPositiveScale.class, "some key"));
        assertTrue(result instanceof Left);
        final Left<Exception, EntityWithInvalidMoneyPropWithNegativePrecisionAndPositiveScale> left = (Left<Exception, EntityWithInvalidMoneyPropWithNegativePrecisionAndPositiveScale>) result;
        final Throwable ex = left.value().getCause().getCause();
        assertTrue(ex instanceof EntityDefinitionException);
        assertEquals(format(INVALID_USE_FOR_PRECITION_AND_SCALE_MSG, "numericMoney", EntityWithInvalidMoneyPropWithNegativePrecisionAndPositiveScale.class.getName()), ex.getMessage());
    }

    @Test
    public void numeric_props_with_negative_scale_are_invalid() {
        final Either<Exception, EntityWithInvalidMoneyPropWithPositivePrecisionAndNegativeScale> result = Try(() -> factory.newByKey(EntityWithInvalidMoneyPropWithPositivePrecisionAndNegativeScale.class, "some key"));
        assertTrue(result instanceof Left);
        final Left<Exception, EntityWithInvalidMoneyPropWithPositivePrecisionAndNegativeScale> left = (Left<Exception, EntityWithInvalidMoneyPropWithPositivePrecisionAndNegativeScale>) result;
        final Throwable ex = left.value().getCause().getCause();
        assertTrue(ex instanceof EntityDefinitionException);
        assertEquals(format(INVALID_USE_FOR_PRECITION_AND_SCALE_MSG, "numericMoney", EntityWithInvalidMoneyPropWithPositivePrecisionAndNegativeScale.class.getName()), ex.getMessage());
    }

    @Test
    public void numeric_props_with_lendth_are_invalid() {
        final Either<Exception, EntityWithInvalidMoneyPropWithLength> result = Try(() -> factory.newByKey(EntityWithInvalidMoneyPropWithLength.class, "some key"));
        assertTrue(result instanceof Left);
        final Left<Exception, EntityWithInvalidMoneyPropWithLength> left = (Left<Exception, EntityWithInvalidMoneyPropWithLength>) result;
        final Throwable ex = left.value().getCause().getCause();
        assertTrue(ex instanceof EntityDefinitionException);
        assertEquals(format(INVALID_USE_OF_PARAM_LENGTH_MSG, "numericMoney", EntityWithInvalidMoneyPropWithLength.class.getName()), ex.getMessage());
    }

    @Test
    public void numeric_props_with_precision_less_than_scale_are_invalid() {
        final Either<Exception, EntityWithInvalidIntegerProp> result = Try(() -> factory.newByKey(EntityWithInvalidIntegerProp.class, "some key"));
        assertTrue(result instanceof Left);
        final Left<Exception, EntityWithInvalidIntegerProp> left = (Left<Exception, EntityWithInvalidIntegerProp>) result;
        final Throwable ex = left.value().getCause().getCause();
        assertTrue(ex instanceof EntityDefinitionException);
        assertEquals(format(INVALID_VALUES_FOR_PRECITION_AND_SCALE_MSG, "numericInteger", EntityWithInvalidIntegerProp.class.getName()), ex.getMessage());
    }

    @Test
    public void non_numeric_props_with_traliningZeros_but_without_precision_and_scale_are_invalid() {
        final Either<Exception, EntityWithInvalidStringPropWithTrailingZeros> result = Try(() -> factory.newByKey(EntityWithInvalidStringPropWithTrailingZeros.class, "some key"));
        assertTrue(result instanceof Left);
        final Left<Exception, EntityWithInvalidStringPropWithTrailingZeros> left = (Left<Exception, EntityWithInvalidStringPropWithTrailingZeros>) result;
        final Throwable ex = left.value().getCause().getCause();
        assertTrue(ex instanceof EntityDefinitionException);
        assertEquals(format(INVALID_USE_OF_NUMERIC_PARAMS_MSG, "stringProp", EntityWithInvalidStringPropWithTrailingZeros.class.getName()), ex.getMessage());
    }

    @Test
    public void non_numeric_props_with_precision_are_invalid() {
        final Either<Exception, EntityWithInvalidStringPropWithPrecision> result = Try(() -> factory.newByKey(EntityWithInvalidStringPropWithPrecision.class, "some key"));
        assertTrue(result instanceof Left);
        final Left<Exception, EntityWithInvalidStringPropWithPrecision> left = (Left<Exception, EntityWithInvalidStringPropWithPrecision>) result;
        final Throwable ex = left.value().getCause().getCause();
        assertTrue(ex instanceof EntityDefinitionException);
        assertEquals(format(INVALID_USE_OF_NUMERIC_PARAMS_MSG, "stringProp", EntityWithInvalidStringPropWithPrecision.class.getName()), ex.getMessage());
    }

    @Test
    public void non_numeric_props_with_scale_are_invalid() {
        final Either<Exception, EntityWithInvalidStringPropWithScale> result = Try(() -> factory.newByKey(EntityWithInvalidStringPropWithScale.class, "some key"));
        assertTrue(result instanceof Left);
        final Left<Exception, EntityWithInvalidStringPropWithScale> left = (Left<Exception, EntityWithInvalidStringPropWithScale>) result;
        final Throwable ex = left.value().getCause().getCause();
        assertTrue(ex instanceof EntityDefinitionException);
        assertEquals(format(INVALID_USE_OF_NUMERIC_PARAMS_MSG, "stringProp", EntityWithInvalidStringPropWithScale.class.getName()), ex.getMessage());
    }

    @Test
    public void default_implementation_for_isEditable_returns_failure_for_non_instrumented_entities() {
        final Entity plainEntityViaFactory = factory.newPlainEntity(Entity.class, null);
        final Result res1 = plainEntityViaFactory.isEditable();
        assertFalse(res1.isSuccessful());
        assertEquals(AbstractEntity.ERR_IS_EDITABLE_UNINSTRUMENTED, res1.getMessage());

        final Entity newEntityViaNew = new Entity();
        final Result res2 = newEntityViaNew.isEditable();
        assertFalse(res2.isSuccessful());
        assertEquals(AbstractEntity.ERR_IS_EDITABLE_UNINSTRUMENTED, res2.getMessage());
    }

    @Test
    public void default_implementation_for_isEditable_returns_success_for_instrumented_entities() {
        final Entity entity = factory.newEntity(Entity.class);
        final Result res = entity.isEditable();
        assertTrue(res.isSuccessful());
        assertEquals(entity, res.getInstance());
    }

    @Test
    public void default_implementation_for_isDirty_throws_exception_for_non_instrumented_entities() {
        final Entity plainEntityViaFactory = factory.newPlainEntity(Entity.class, null);
        try {
            plainEntityViaFactory.isDirty();
            fail();
        } catch (final EntityException ex) {
            assertEquals(format(AbstractEntity.ERR_ENSURE_INSTRUMENTED, Entity.class.getName()), ex.getMessage());
        }

        final Entity newEntityViaNew = new Entity();
        try {
            newEntityViaNew.isDirty();
            fail();
        } catch (final EntityException ex) {
            assertEquals(format(AbstractEntity.ERR_ENSURE_INSTRUMENTED, Entity.class.getName()), ex.getMessage());
        }
    }

    @Test
    public void default_implementation_for_isDirty_does_not_thorow_exceptions_for_instrumented_entities() {
        final Entity entity = factory.newEntity(Entity.class);
        assertTrue(entity.isDirty());
    }

    private static DomainValidationConfig newDomainValidationConfig() {
        final var config = new DomainValidationConfig();
        config.setValidator(Entity.class, "firstProperty", new HappyValidator());
        config.setValidator(Entity.class, "bigDecimals", new HappyValidator());
        config.setValidator(Entity.class, "number", new HappyValidator() {
            @Override
            public Result handle(final MetaProperty<Object> property, final Object newValue, final Set<Annotation> mutatorAnnotations) {
                if (newValue != null && newValue.equals(35)) {
                    return new Result(property, new Exception("Domain : Value 35 is not permitted."));
                } else if (newValue != null && newValue.equals(77)) {
                    return new Warning("DOMAIN validation : The value of 77 is dangerous.");
                }
                return super.handle(property, newValue, mutatorAnnotations);
            }
        });
        return config;
    }

}
