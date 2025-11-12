package ua.com.fielden.platform.utils;

import com.google.inject.Injector;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;
import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.dashboard.DashboardRefreshFrequency;
import ua.com.fielden.platform.dashboard.DashboardRefreshFrequencyUnit;
import ua.com.fielden.platform.domain.PlatformDomainTypes;
import ua.com.fielden.platform.domain.metadata.DomainExplorer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.UserDefinableHelp;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.factory.CalculatedAnnotation;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.keygen.KeyNumber;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.security.user.*;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.test_entities.ChildEntity;
import ua.com.fielden.platform.test_entities.Entity;
import ua.com.fielden.platform.test_entities.EntityExt;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.tuples.T2;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static java.math.RoundingMode.HALF_EVEN;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.AbstractEntity.*;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.startModification;
import static ua.com.fielden.platform.utils.CollectionUtil.linkedSetOf;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.utils.EntityUtils.*;

public class EntityUtilsTest {
    private final EntityTestIocModuleWithPropertyFactory module = new CommonEntityTestIocModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void safe_comparison_considers_two_null_values_equal() {
        assertTrue(safeCompare(null, null) == 0);
    }

    @Test
    public void safe_comparison_considers_null_smaller_than_non_null() {
        assertTrue(EntityUtils.safeCompare(42, null) > 0);
        assertTrue(EntityUtils.safeCompare(null, 42) < 0);
    }

    @Test
    public void safe_comparison_of_non_null_values_equals_to_the_result_of_comparing_values_directly() {
        assertEquals(Integer.valueOf(42).compareTo(Integer.valueOf(13)), EntityUtils.safeCompare(42, 13));
        assertEquals(Integer.valueOf(13).compareTo(Integer.valueOf(42)), EntityUtils.safeCompare(13, 42));
    }

    @Test
    public void copy_copies_all_properties_if_non_are_skipped() {
        final Entity entity = factory.newEntity(Entity.class);
        entity.setVersion(42L);
        entity.setId(1L);
        entity.setKey("key");
        entity.setDesc("description");
        entity.setMoney(new Money("23.25"));

        final Entity copy = factory.newEntity(Entity.class);
        EntityUtils.copy(entity, copy);

        assertEquals("Copy does not equal to the original instance", entity, copy);
        assertTrue("Entity with no id should be recognized as drity.", copy.isDirty());
        assertEquals("Id should have been copied", entity.getId(), copy.getId());
        assertEquals("Version should have been copied", entity.getVersion(), copy.getVersion());
        assertEquals("Property desc does not match", entity.getDesc(), copy.getDesc());
        assertEquals("Property money does not match", entity.getMoney(), copy.getMoney());
    }

    @Test
    public void copy_does_not_copy_skipped_VERSION_and_ID() {
        final Entity entity = factory.newEntity(Entity.class);
        entity.setVersion(42L);
        entity.setId(1L);
        entity.setKey("key");
        entity.setDesc("description");
        entity.setMoney(new Money("23.25"));

        final Entity copy = factory.newEntity(Entity.class);
        EntityUtils.copy(entity, copy, VERSION, ID);

        assertEquals("Copy does not equal to the original instance", entity, copy);
        assertTrue("Entity with no id should be recognized as drity.", copy.isDirty());
        assertNull("Id should have not been copied", copy.getId());
        assertEquals("Version should have not been copied", Long.valueOf(0), copy.getVersion());
        assertEquals("Property desc does not match", entity.getDesc(), copy.getDesc());
        assertEquals("Property money does not match", entity.getMoney(), copy.getMoney());
    }

    @Test
    public void copy_does_not_copy_skipped_properties() {
        final Entity entity = factory.newEntity(Entity.class);
        entity.setVersion(42L);
        entity.setId(1L);
        entity.setKey("key");
        entity.setDesc("description");
        entity.setMoney(new Money("23.25"));

        final Entity copy = factory.newEntity(Entity.class);
        EntityUtils.copy(entity, copy, "money", DESC);

        assertEquals("Copy does not equal to the original instance", entity, copy);
        assertTrue("Entity with no id should be recognized as drity.", copy.isDirty());
        assertEquals("Id should have been copied", entity.getId(), copy.getId());
        assertEquals("Version should have been copied", entity.getVersion(), copy.getVersion());
        assertNull("Property desc should have not been copied", copy.getDesc());
        assertNull("Property money should have not been copied", copy.getMoney());
    }

    @Test
    public void copy_does_not_occur_in_the_initialisation_mode_by_default() {
        final Entity entity = factory.newEntity(Entity.class);
        entity.setVersion(42L);
        entity.setId(1L);
        entity.setKey("key");
        entity.setDesc("description");
        entity.setMoney(new Money("23.25"));

        final Entity copy = factory.newEntity(Entity.class);
        EntityUtils.copy(entity, copy);

        assertEquals("Copy does not equal to the original instance", entity, copy);
        assertTrue("Copy is not instrumented", copy.isInstrumented());
        assertTrue("Copy should be dirty", copy.isDirty());
        assertTrue("Property key is copied and should be recognised as dirty", copy.getProperty("key").isDirty());
        assertEquals("IDs do not match", entity.getId(), copy.getId());
        assertEquals("Versions do not match.", Long.valueOf(42L), copy.getVersion());
        assertEquals("Property desc does not match", entity.getDesc(), copy.getDesc());
        assertTrue("Property desc is copied and should be recognised as dirty", copy.getProperty("desc").isDirty());
        assertEquals("Property money does not match", entity.getMoney(), copy.getMoney());
        assertTrue("Property money is copied and should be recognised as dirty", copy.getProperty("money").isDirty());
    }

    @Test
    public void collectional_properties_are_correctly_identifiable() {
        final List<Field> collectionalProperties = getCollectionalProperties(User.class);
        assertEquals(1, collectionalProperties.size());

        final Field userRolesField = collectionalProperties.get(0);
        assertEquals("Incorrect field name", "roles", userRolesField.getName());
        assertEquals("Incorrect collectional entity class", UserAndRoleAssociation.class, AnnotationReflector.getAnnotation(userRolesField, IsProperty.class).value());
        assertEquals("Incorrect collectional entity link property", "user", AnnotationReflector.getAnnotation(userRolesField, IsProperty.class).linkProperty());
    }

    @Test
    public void two_nulls_are_comparible_and_equal() {
        assertEquals(0, EntityUtils.compare(null, null));
    }

    @Test
    public void null_is_smaller_than_non_null() {
        assertTrue(EntityUtils.compare(null, factory.newEntity(Entity.class)) < 0);
    }

    @Test
    public void non_null_is_greater_than_null() {
        assertTrue(EntityUtils.compare(factory.newEntity(Entity.class), null) > 0);
    }

    @Test
    public void the_result_of_comparing_two_non_nulls_matches_the_result_of_comparing_them_with_compareTo() {
        final Entity entity1 = factory.newByKey(Entity.class, "1");
        final Entity entity2 = factory.newByKey(Entity.class, "2");

        assertEquals(entity1.compareTo(entity2), EntityUtils.compare(entity1, entity2));
        assertEquals(entity2.compareTo(entity1), EntityUtils.compare(entity2, entity1));
        assertEquals(entity1.compareTo(entity1), EntityUtils.compare(entity1, entity1));
    }

    @Test
    public void non_persistent_and_non_synthetic_and_non_union_entities_are_recognised_as_such() {
        assertFalse(isPersistentEntityType(Entity.class));
        assertFalse(isSyntheticEntityType(Entity.class));
        assertFalse(isSyntheticBasedOnPersistentEntityType(Entity.class));
        assertFalse(isUnionEntityType(Entity.class));
    }

    @Test
    public void union_entity_is_recognised_as_such() {
        assertFalse(isPersistentEntityType(UnionEntity.class));
        assertFalse(isSyntheticEntityType(UnionEntity.class));
        assertFalse(isSyntheticBasedOnPersistentEntityType(UnionEntity.class));
        assertTrue(isUnionEntityType(UnionEntity.class));
    }

    @Test
    public void persistent_entity_is_recognised_as_such() {
        assertTrue(isPersistentEntityType(TgAuthor.class));
        assertFalse(isSyntheticEntityType(TgAuthor.class));
        assertFalse(isSyntheticBasedOnPersistentEntityType(TgAuthor.class));
        assertFalse(isUnionEntityType(TgAuthor.class));
    }

    @Test
    public void generated_entity_based_on_persistent_entity_is_recognised_as_persistent() throws ClassNotFoundException {
        final Class<? extends AbstractEntity<?>> newType = genNewTypeWithAggregateCalcPropBasedOn(TgAuthor.class);
        
        assertTrue(isPersistentEntityType(newType));
        assertFalse(isSyntheticEntityType(newType));
        assertFalse(isSyntheticBasedOnPersistentEntityType(newType));
        assertFalse(isUnionEntityType(newType));
    }

    @Test
    public void generated_entity_with_nested_regeneration_based_on_persistent_entity_is_recognised_as_persistent() throws ClassNotFoundException {
        final Class<? extends AbstractEntity<?>> newType = genNewTypeWithAggregateCalcPropBasedOn(TgAuthor.class, ThreadLocalRandom.current().nextInt(2, 7));
        
        assertTrue(isPersistentEntityType(newType));
        assertFalse(isSyntheticEntityType(newType));
        assertFalse(isSyntheticBasedOnPersistentEntityType(newType));
        assertFalse(isUnionEntityType(newType));
    }

    @Test
    public void synthetic_entity_is_recognised_as_such() {
        assertFalse(isPersistentEntityType(TgAverageFuelUsage.class));
        assertTrue(isSyntheticEntityType(TgAverageFuelUsage.class));
        assertFalse(isSyntheticBasedOnPersistentEntityType(TgAverageFuelUsage.class));
        assertFalse(isUnionEntityType(TgAverageFuelUsage.class));
    }

    @Test
    public void generated_entity_based_on_synthetic_entity_is_recognised_as_synthetic() throws ClassNotFoundException {
        final Class<? extends AbstractEntity<?>> newType = genNewTypeWithAggregateCalcPropBasedOn(TgAverageFuelUsage.class);
        
        assertFalse(isPersistentEntityType(newType));
        assertTrue(isSyntheticEntityType(newType));
        assertFalse(isSyntheticBasedOnPersistentEntityType(newType));
        assertFalse(isUnionEntityType(newType));
    }

    @Test
    public void generated_entity_with_nested_regeneration_based_on_synthetic_entity_is_recognised_as_synthetic() throws ClassNotFoundException {
        final Class<? extends AbstractEntity<?>> newType = genNewTypeWithAggregateCalcPropBasedOn(TgAverageFuelUsage.class, ThreadLocalRandom.current().nextInt(2, 7));
        
        assertFalse(isPersistentEntityType(newType));
        assertTrue(isSyntheticEntityType(newType));
        assertFalse(isSyntheticBasedOnPersistentEntityType(newType));
        assertFalse(isUnionEntityType(newType));
    }

    @Test
    public void synthetic_entity_derived_from_persisten_entity_is_recognised_as_synthetic_and_as_synthetic_based_on_persistent_entity_type() {
        assertFalse(isPersistentEntityType(TgReVehicleModel.class));
        assertTrue(isSyntheticEntityType(TgReVehicleModel.class));
        assertTrue(isSyntheticBasedOnPersistentEntityType(TgReVehicleModel.class));
        assertFalse(isUnionEntityType(TgReVehicleModel.class));
    }

    @Test
    public void generated_entity_based_on_synthetic_entity_derived_from_persisten_entity_is_recognised_as_synthetic_and_as_synthetic_based_on_persistent_entity_type() throws ClassNotFoundException {
        final Class<? extends AbstractEntity<?>> newType = genNewTypeWithAggregateCalcPropBasedOn(TgReVehicleModel.class);
       
        assertFalse(isPersistentEntityType(newType));
        assertTrue(isSyntheticEntityType(newType));
        assertTrue(isSyntheticBasedOnPersistentEntityType(newType));
        assertFalse(isUnionEntityType(newType));
    }

    @Test
    public void generated_entity_with_nested_regeneration_based_on_synthetic_entity_derived_from_persisten_entity_is_recognised_as_synthetic_and_as_synthetic_based_on_persistent_entity_type() throws ClassNotFoundException {
        final Class<? extends AbstractEntity<?>> newType = genNewTypeWithAggregateCalcPropBasedOn(TgReVehicleModel.class, ThreadLocalRandom.current().nextInt(2, 7));
       
        assertFalse(isPersistentEntityType(newType));
        assertTrue(isSyntheticEntityType(newType));
        assertTrue(isSyntheticBasedOnPersistentEntityType(newType));
        assertFalse(isUnionEntityType(newType));
    }

    @Test
    public void null_does_not_belong_to_any_of_entity_type_classiciations() {
        assertFalse(isPersistentEntityType(null));
        assertFalse(isSyntheticEntityType(null));
        assertFalse(isSyntheticBasedOnPersistentEntityType(null));
        assertFalse(isUnionEntityType(null));
    }

    @Test
    public void equalsEx_correctly_compares_instances_of_BigDecimal() {
        assertTrue(equalsEx(new BigDecimal("0.42"), new BigDecimal("0.42")));
        assertTrue(equalsEx(new BigDecimal("42.00"), new BigDecimal("42.0")));
        assertTrue(equalsEx(new BigDecimal("0.00"), BigDecimal.ZERO));
        assertTrue(equalsEx(new BigDecimal("42.00").setScale(1, HALF_EVEN), new BigDecimal("42.01").setScale(1, HALF_EVEN)));
        assertFalse(equalsEx(new BigDecimal("42.00"), new BigDecimal("42.01")));
    }

    @Test
    public void isIntrospectionDenied_returns_true_for_entity_types_annotated_with_DenyIntrospection() {
        assertTrue(isIntrospectionDenied(UnionEntity.class));
    }

    @Test
    public void isIntrospectionDenied_returns_true_for_properties_annotated_with_DenyIntrospection() {
        assertTrue(isIntrospectionDenied(TgWagon.class, TgWagon.Property.internalNumber));
    }

    @Test
    public void isIntrospectionDenied_returns_false_for_entity_types_not_annotated_with_DenyIntrospection() {
        assertFalse(isIntrospectionDenied(Entity.class));
    }

    @Test
    public void isIntrospectionDenied_returns_false_for_properties_not_annotated_with_DenyIntrospection() {
        assertFalse(isIntrospectionDenied(TgWagon.class, TgWagon.Property.serialNo));
    }

    @Test
    public void coalesce_returns_first_value_if_it_is_non_null() {
        assertEquals("first", coalesce("first", "second"));
    }

    @Test
    public void coalesce_returns_second_non_null_value_if_the_first_is_null() {
        assertEquals("second", coalesce(null, "second"));
    }

    @Test
    public void coalesce_returns_the_first_non_null_value() {
        assertEquals("third", coalesce(null, null, "third"));
    }

    @Test(expected = NoSuchElementException.class)
    public void coalesce_throws_exception_if_all_values_are_null() {
        coalesce(null, null, null, null);
    }

    @Test(expected = NoSuchElementException.class)
    public void coalesce_throws_exception_if_all_values_are_null_and_gracefully_handles_null_for_array_argument() {
        coalesce(null, null, (Object[]) null /* this is an array argument */);
    }

    @Test
    public void equalEx_considers_type_hierchies_before_calling_equals() {
        final DateTimeFormatter jodaFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        final Date date = jodaFormatter.parseDateTime("2023-10-10 00:00:00").toDate();
        final Timestamp timestamp = new Timestamp(date.getTime());

        assertTrue(date.equals(timestamp));
        assertFalse(timestamp.equals(date));
        assertTrue(EntityUtils.equalsEx(timestamp, date));
        assertTrue(EntityUtils.equalsEx(date, timestamp));
    }

    @Test
    public void equalEx_supports_coerstion_of_joda_datetime_to_date() {
        final DateTimeFormatter jodaFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        final DateTime dateTime = jodaFormatter.parseDateTime("2023-10-10 00:00:00");
        final Date date = dateTime.toDate();
        final Timestamp timestamp = new Timestamp(date.getTime());

        assertFalse(date.equals(dateTime));
        assertTrue(date.equals(timestamp));

        assertFalse(timestamp.equals(date));
        assertFalse(timestamp.equals(dateTime));

        assertFalse(dateTime.equals(date));
        assertFalse(dateTime.equals(timestamp));

        assertTrue(EntityUtils.equalsEx(date, dateTime));
        assertTrue(EntityUtils.equalsEx(dateTime, date));
        assertTrue(EntityUtils.equalsEx(dateTime, timestamp));
        assertTrue(EntityUtils.equalsEx(timestamp, dateTime));
    }

    @Test
    public void toDecimal_converts_Integer_to_BigDecimal() {
        final Integer value = 42;
        assertEquals(new BigDecimal(value, new MathContext(2, RoundingMode.HALF_UP)), toDecimal(value));
        assertEquals(new BigDecimal(value, new MathContext(4, RoundingMode.HALF_UP)), toDecimal(value, 4));
    }

    @Test
    public void toDecimal_converts_Double_to_BigDecimal() {
        final Double value = 42.46;
        assertEquals(new BigDecimal(value, new MathContext(2, RoundingMode.HALF_UP)), toDecimal(value));
        assertEquals(new BigDecimal(value, new MathContext(4, RoundingMode.HALF_UP)), toDecimal(value, 4));
    }

    @Test
    public void toDecimal_rescales_BigDecimal_only_if_needed() {
        final BigDecimal value = new BigDecimal("42.46");
        assertEquals(2, value.scale());
        assertEquals(value, toDecimal(value));
        assertEquals(value.setScale(4, RoundingMode.HALF_UP), toDecimal(value, 4));
    }

    @Test
    public void traversing_valid_property_path_ending_with_non_entity_typed_property_produces_a_stream_of_prop_value_pairs_with_last_one_skipped() {
        final Entity entity1 = factory.newEntity(Entity.class);
        entity1.setKey("E1");
        final Entity entity2 = factory.newEntity(Entity.class);
        entity2.setKey("E2");
        final Entity entity3 = factory.newEntity(Entity.class);
        entity3.setKey("E3");

        entity1.setEntity(entity2.setEntity(entity3));

        final List<T2<String, Optional<? extends AbstractEntity<?>>>> trace = EntityUtils.traversePropPath(entity1, "entity.entity.date").collect(toList());
        assertEquals(3, trace.size());
        final T2<String, Optional<? extends AbstractEntity<?>>> t2_1 = trace.get(0);
        assertEquals("entity.entity", t2_1._1);
        assertEquals(entity3, t2_1._2.get());
        final T2<String, Optional<? extends AbstractEntity<?>>> t2_2 = trace.get(1);
        assertEquals("entity", t2_2._1);
        assertEquals(entity2, t2_2._2.get());
        final T2<String, Optional<? extends AbstractEntity<?>>> t2_3 = trace.get(2);
        assertEquals("", t2_3._1);
        assertEquals(entity1, t2_3._2.get());
    }

    @Test
    public void traversing_valid_property_path_ending_with_entity_typed_property_produces_a_stream_of_prop_value_pairs_of_the_length_one_greater_than_the_path() {
        final Entity entity1 = factory.newEntity(Entity.class);
        entity1.setKey("E1");
        final Entity entity2 = factory.newEntity(Entity.class);
        entity2.setKey("E2");
        final Entity entity3 = factory.newEntity(Entity.class);
        entity3.setKey("E3");

        entity1.setEntity(entity2.setEntity(entity3));

        final List<T2<String, Optional<? extends AbstractEntity<?>>>> trace = EntityUtils.traversePropPath(entity1, "entity.entity").collect(toList());
        assertEquals(3, trace.size());
        final T2<String, Optional<? extends AbstractEntity<?>>> t2_1 = trace.get(0);
        assertEquals("entity.entity", t2_1._1);
        assertEquals(entity3, t2_1._2.get());
        final T2<String, Optional<? extends AbstractEntity<?>>> t2_2 = trace.get(1);
        assertEquals("entity", t2_2._1);
        assertEquals(entity2, t2_2._2.get());
        final T2<String, Optional<? extends AbstractEntity<?>>> t2_3 = trace.get(2);
        assertEquals("", t2_3._1);
        assertEquals(entity1, t2_3._2.get());
    }

    @Test
    public void traversing_property_paths_with_intermediate_null_values_produces_a_stream_of_prop_value_pairs_where_the_value_member_is_empty_for_null_parts() {
        final Entity entity1 = factory.newEntity(Entity.class);
        entity1.setKey("E1");
        final Entity entity2 = factory.newEntity(Entity.class);
        entity2.setKey("E2");
        final Entity entity3 = factory.newEntity(Entity.class);
        entity3.setKey("E3");

        entity1.setEntity(entity2.setEntity(entity3));

        final List<T2<String, Optional<? extends AbstractEntity<?>>>> trace = EntityUtils.traversePropPath(entity1, "entity.entity.entity.date").collect(toList());
        assertEquals(4, trace.size());
        final T2<String, Optional<? extends AbstractEntity<?>>> t2_1 = trace.get(0);
        assertEquals("entity.entity.entity", t2_1._1);
        assertFalse(t2_1._2.isPresent());
        final T2<String, Optional<? extends AbstractEntity<?>>> t2_2 = trace.get(1);
        assertEquals("entity.entity", t2_2._1);
        assertEquals(entity3, t2_2._2.get());
        final T2<String, Optional<? extends AbstractEntity<?>>> t2_3 = trace.get(2);
        assertEquals("entity", t2_3._1);
        assertEquals(entity2, t2_3._2.get());
        final T2<String, Optional<? extends AbstractEntity<?>>> t2_4 = trace.get(3);
        assertEquals("", t2_4._1);
        assertEquals(entity1, t2_4._2.get());
    }

    @Test
    public void traversing_invalid_property_paths_produces_empty_stream() {
        final Entity entity1 = factory.newEntity(Entity.class);
        entity1.setKey("E1");
        final Entity entity2 = factory.newEntity(Entity.class);
        entity2.setKey("E2");
        final Entity entity3 = factory.newEntity(Entity.class);
        entity3.setKey("E3");
        entity3.setDate(new Date());

        entity1.setEntity(entity2.setEntity(entity3));
        assertNull(entity1.getEntity().getEntity().getEntity());

        final Stream<?> stream = EntityUtils.traversePropPath(entity1, "entity.entity.date.entity");
        assertEquals(0, stream.count());
    }

    @Test
    public void traversing_path_with_one_non_entity_typed_property_produces_stream_with_root_property_and_value_pair() {
        final Entity entity1 = factory.newEntity(Entity.class);
        entity1.setKey("E1");

        final List<T2<String, Optional<? extends AbstractEntity<?>>>> trace = EntityUtils.traversePropPath(entity1, "date").collect(toList());
        assertEquals(1, trace.size());
        final T2<String, Optional<? extends AbstractEntity<?>>> t2_1 = trace.get(0);
        assertEquals("", t2_1._1);
        assertEquals(entity1, t2_1._2.get());
    }

    @Test
    public void traversing_empty_property_path_produces_stream_with_root_property_and_value_pair() {
        final Entity entity1 = factory.newEntity(Entity.class);
        entity1.setKey("E1");

        final List<T2<String, Optional<? extends AbstractEntity<?>>>> trace = EntityUtils.traversePropPath(entity1, "").collect(toList());
        assertEquals(1, trace.size());
        final T2<String, Optional<? extends AbstractEntity<?>>> t2_1 = trace.get(0);
        assertEquals("", t2_1._1);
        assertEquals(entity1, t2_1._2.get());
    }

    @Test
    public void traversing_undefined_property_path_produces_stream_with_root_property_and_value_pair() {
        final Entity entity1 = factory.newEntity(Entity.class);
        entity1.setKey("E1");

        final List<T2<String, Optional<? extends AbstractEntity<?>>>> trace = EntityUtils.traversePropPath(entity1, null).collect(toList());
        assertEquals(1, trace.size());
        final T2<String, Optional<? extends AbstractEntity<?>>> t2_1 = trace.get(0);
        assertEquals("", t2_1._1);
        assertEquals(entity1, t2_1._2.get());
    }

    @Test
    public void traversing_path_with_one_entity_typed_property_produces_stream_with_two_elements() {
        final Entity entity1 = factory.newEntity(Entity.class);
        entity1.setKey("E1");

        final List<T2<String, Optional<? extends AbstractEntity<?>>>> trace = EntityUtils.traversePropPath(entity1, "entity").collect(toList());
        assertEquals(2, trace.size());

        final T2<String, Optional<? extends AbstractEntity<?>>> t2_1 = trace.get(0);
        assertEquals("entity", t2_1._1);
        assertFalse(t2_1._2.isPresent());
        final T2<String, Optional<? extends AbstractEntity<?>>> t2_2 = trace.get(1);
        assertEquals("", t2_2._1);
        assertEquals(entity1, t2_2._2.get());
    }

    @Test
    public void traversing_any_property_path_with_empty_root_produces_empty_stream() {
        final List<T2<String, Optional<? extends AbstractEntity<?>>>> trace = EntityUtils.traversePropPath(null, "any").collect(toList());
        assertEquals(0, trace.size());
    }

    @Test
    public void key_paths_works_for_simple_key() {
        assertEquals(listOf("key"),
                keyPaths(TgVehicle.class));
    }

    @Test
    public void key_paths_works_for_entity_type_key() {
        assertEquals(listOf("key.key"), 
                keyPaths(TgVehicleFinDetails.class));
    }
    
    @Test
    public void key_paths_works_for_composite_key_without_further_nesting() {
        assertEquals(listOf("vehicle.key", "readingDate"),
                keyPaths(TgMeterReading.class));
    }

    @Test
    public void key_paths_works_for_composite_key_without_further_nesting_and_with_parent_context_path() {
        assertEquals(listOf("mr.vehicle.key", "mr.readingDate"),
                keyPaths(TgMeterReading.class, "mr"));
    }

    @Test
    public void key_paths_works_for_composite_key_with_one_level_nesting() {
        assertEquals(listOf("parent.key", "name"),
                keyPaths(TgOrgUnit2.class));
    }

    @Test
    public void key_paths_works_for_composite_key_with_one_level_nesting_and_with_parent_context_path() {
        assertEquals(listOf("parent.parent.parent.key", "parent.parent.name"),
                keyPaths(TgOrgUnit2.class, "parent.parent"));
    }

    @Test
    public void key_paths_works_for_composite_key_with_two_levels_nesting() {
        assertEquals(listOf("parent.parent.key", "parent.name", "name"),
                keyPaths(TgOrgUnit3.class));
    }

    @Test
    public void key_paths_works_for_composite_key_with_three_levels_nesting() {
        assertEquals(listOf("parent.parent.parent.key", "parent.parent.name", "parent.name", "name"),
                keyPaths(TgOrgUnit4.class));
    }

    @Test
    public void key_paths_works_for_composite_key_with_four_levels_nesting() {
        assertEquals(listOf("parent.parent.parent.parent.key", "parent.parent.parent.name", "parent.parent.name", "parent.name", "name"),
                keyPaths(TgOrgUnit5.class));
    }

    @Test
    public void isNaturalOrderDescending_correctly_identifies_natural_ordering_for_entities() {
        assertTrue(isNaturalOrderDescending(Entity.class));
        assertTrue(isNaturalOrderDescending(EntityExt.class));
        assertFalse(isNaturalOrderDescending(ChildEntity.class));
    }

    @Test
    public void toString_converts_lists_to_CSV_in_square_brackets() {
        assertEquals("[]", EntityUtils.toString(listOf()));

        final List<AbstractEntity<?>> moreThanOne = listOf(factory.newEntity(Entity.class).setKey("E1"), factory.newEntity(Entity.class).setKey("E2"), factory.newEntity(Entity.class).setKey("E3"));
        assertEquals("[E1, E2, E3]", EntityUtils.toString(moreThanOne));

        final List<AbstractEntity<?>> one = listOf(factory.newEntity(Entity.class).setKey("E1"));
        assertEquals("[E1]", EntityUtils.toString(one));

        final List<AbstractEntity<?>> someAndNull = listOf(factory.newEntity(Entity.class).setKey("E1"), null, factory.newEntity(Entity.class).setKey("E3"));
        assertEquals("[E1, null, E3]", EntityUtils.toString(someAndNull));
    }

    @Test
    public void toString_converts_sets_to_CSV_in_square_brackets() {
        assertEquals("[]", EntityUtils.toString(linkedSetOf()));

        final Set<AbstractEntity<?>> moreThanOne = linkedSetOf(factory.newEntity(Entity.class).setKey("E1"), factory.newEntity(Entity.class).setKey("E2"), factory.newEntity(Entity.class).setKey("E3"));
        assertEquals("[E1, E2, E3]", EntityUtils.toString(moreThanOne));

        final Set<AbstractEntity<?>> one = linkedSetOf(factory.newEntity(Entity.class).setKey("E1"));
        assertEquals("[E1]", EntityUtils.toString(one));

        final Set<AbstractEntity<?>> someAndNull = linkedSetOf(factory.newEntity(Entity.class).setKey("E1"), null, factory.newEntity(Entity.class).setKey("E3"));
        assertEquals("[E1, null, E3]", EntityUtils.toString(someAndNull));
    }

    @Test
    public void only_a_specific_subset_of_platform_level_entities_have_introspection_allowed() {
        final LinkedHashSet<Class<? extends AbstractEntity<?>>> filtered = PlatformDomainTypes.types.stream().filter(EntityUtils::isIntrospectionAllowed).collect(toCollection(LinkedHashSet::new));
        assertThat(filtered).containsExactlyInAnyOrder(
                Attachment.class,
                DomainExplorer.class,
                DashboardRefreshFrequency.class,
                DashboardRefreshFrequencyUnit.class,
                KeyNumber.class,
                User.class,
                ReUser.class,
                UserRole.class,
                UserAndRoleAssociation.class,
                SecurityRoleAssociation.class,
                UserDefinableHelp.class);
    }

    @Test
    public void splitPropPath_fails_if_path_contains_empty_property_names() {
        assertThrows(IllegalArgumentException.class, () -> splitPropPath("person."));
        assertThrows(IllegalArgumentException.class, () -> splitPropPath("person.."));
        assertThrows(IllegalArgumentException.class, () -> splitPropPath(".person"));
        assertThrows(IllegalArgumentException.class, () -> splitPropPath("..person"));
        assertThrows(IllegalArgumentException.class, () -> splitPropPath("person..desc"));
        assertThrows(IllegalArgumentException.class, () -> splitPropPath("person..desc."));
        assertThrows(IllegalArgumentException.class, () -> splitPropPath("person.desc."));
        assertThrows(IllegalArgumentException.class, () -> splitPropPath("person.desc.."));
        assertThrows(IllegalArgumentException.class, () -> splitPropPath(""));
        assertThrows(IllegalArgumentException.class, () -> splitPropPath("."));
        assertThrows(IllegalArgumentException.class, () -> splitPropPath(".."));
    }

    @Test
    public void splitPropPath_splits_a_dot_notated_path_into_a_list_of_simple_property_names() {
        assertEquals(List.of("person", "desc"), splitPropPath("person.desc"));
        assertEquals(List.of("person", "vehicle", "desc"), splitPropPath("person.vehicle.desc"));
    }

    @Test
    public void splitPropPath_returns_a_single_element_list_given_a_simple_property_name() {
        assertEquals(List.of("person"), splitPropPath("person"));
    }

    @Test
    public void laxSplitPropPath_allows_empty_names_in_a_path() {
        assertEquals(List.of(""), laxSplitPropPath(""));
        assertEquals(List.of(), laxSplitPropPath("."));
        assertEquals(List.of("", "person"), laxSplitPropPath(".person"));
        assertEquals(List.of("person"), laxSplitPropPath("person."));
        assertEquals(List.of("person", "", "desc"), laxSplitPropPath("person..desc"));
    }

    @Test
    public void findFirstPersistentTypeInHierarchyFor_a_persistent_type_returns_that_type() {
        final var maybePersistentType = EntityUtils.findFirstPersistentTypeInHierarchyFor(Attachment.class);
        assertTrue(maybePersistentType.isPresent());
        assertEquals(Attachment.class , maybePersistentType.get());
    }

    @Test
    public void findFirstPersistentTypeInHierarchyFor_a_synthetic_entity_based_on_persistent_type_returns_that_type() {
        final var maybePersistentType = EntityUtils.findFirstPersistentTypeInHierarchyFor(TgReVehicleModel.class);
        assertTrue(maybePersistentType.isPresent());
        assertEquals(TgVehicleModel.class , maybePersistentType.get());
    }

    @Test
    public void findFirstPersistentTypeInHierarchyFor_an_entity_with_no_persistent_type_in_its_hierarhcy_returns_empty_result() {
        final var maybePersistentType = EntityUtils.findFirstPersistentTypeInHierarchyFor(Entity.class);
        assertFalse(maybePersistentType.isPresent());
    }

    @Test
    public void entityTypeHierachy_includes_all_parent_entity_types_starting_from_the_most_specific() {
        assertEquals(List.of(TgReVehicleModel.class, TgVehicleModel.class, AbstractEntity.class),
                     entityTypeHierarchy(TgReVehicleModel.class, true).toList());
        assertEquals(List.of(TgReVehicleModel.class, TgVehicleModel.class),
                     entityTypeHierarchy(TgReVehicleModel.class, false).toList());
    }

    @Test
    public void isActivatableEntityType_returns_true_for_activatable_persistent_entities() {
        // TgPerson extends ActivatableAbstractEntity and is persistent.
        assertTrue(isActivatableEntityType(TgPerson.class));
    }

    @Test
    public void isActivatableEntityType_returns_false_for_non_activatable_persistent_entities() {
        // TgAuthor is persistent but does not extend ActivatableAbstractEntity.
        assertFalse(isActivatableEntityType(TgAuthor.class));
    }

    @Test
    public void isActivatableEntityType_returns_false_for_non_activatable_synthetic_entities() {
        assertFalse(isActivatableEntityType(TgAverageFuelUsage.class));
    }

    @Test
    public void isActivatableEntityType_returns_true_for_synthetic_based_on_activatable_persistent_entities() {
        // TgReActivatableVehicleModel extends activatable persistent TgActivatableVehicleModel.
        assertTrue(isActivatableEntityType(TgReActivatableVehicleModel.class));
    }

    @Test
    public void isActivatablePersistentEntityType_returns_true_for_activatable_persistent_entities() {
        // TgPerson extends ActivatableAbstractEntity and is persistent.
        assertTrue(isActivatablePersistentEntityType(TgPerson.class));
    }

    @Test
    public void isActivatablePersistentEntityType_returns_false_for_non_activatable_persistent_entities() {
        // TgAuthor is persistent but does not extend ActivatableAbstractEntity.
        assertFalse(isActivatablePersistentEntityType(TgAuthor.class));
    }

    @Test
    public void isActivatablePersistentEntityType_returns_false_for_non_activatable_synthetic_entities() {
        assertFalse(isActivatablePersistentEntityType(TgAverageFuelUsage.class));
    }

    @Test
    public void isActivatablePersistentEntityType_returns_false_for_synthetic_based_on_activatable_persistent_entities() {
        // TgReActivatableVehicleModel extends activatable persistent TgActivatableVehicleModel.
        assertFalse(isActivatablePersistentEntityType(TgReActivatableVehicleModel.class));
    }

    @Test
    public void isActivatableEntityType_returns_false_for_union_entities() {
        // Union entities are never considered activatable.
        assertFalse(isActivatableEntityType(UnionEntity.class));
    }

    @Test
    public void isActivatableEntityType_returns_false_for_null_input() {
        assertFalse(isActivatableEntityType(null));
    }

    @Test
    public void isActivatablePersistentEntityType_returns_false_for_null_input() {
        assertFalse(isActivatablePersistentEntityType(null));
    }

    /**
     * A helper factory method for generating a new type based on {@code baseType} with the {@code maxNestedLevels} of nesting (i.e., a new type gets generated based on the previously generated type sequentially).  
     *
     * @param <T>
     * @param baseType
     * @return
     * @throws ClassNotFoundException
     */
    private static <T extends AbstractEntity<?>> Class<? extends T> genNewTypeWithAggregateCalcPropBasedOn(final Class<T> baseType, final int maxNestedLevels) throws ClassNotFoundException {
        final Calculated totalCountCalculation = new CalculatedAnnotation().contextualExpression("COUNT(SELF)").newInstance();
        final NewProperty<Integer> total_count_prop = new NewProperty<>("total_count_", Integer.class, "Count", "The number of matching values.", totalCountCalculation);

        var count = 1;
        var newType = startModification(baseType).addProperties(total_count_prop).endModification();
        while (count < maxNestedLevels) {
            newType = startModification(newType).addProperties(total_count_prop).endModification();
            count++;
        }
        return newType;
    }
    
    /**
     * Executes {@link #genNewTypeWithAggregateCalcPropBasedOn(Class, int)} with nesting of 1 (i.e., the generated type has {@code baseType} as its immediate super type).
     *
     * @param <T>
     * @param baseType
     * @return
     * @throws ClassNotFoundException
     */
    private static <T extends AbstractEntity<?>> Class<? extends T> genNewTypeWithAggregateCalcPropBasedOn(final Class<T> baseType) throws ClassNotFoundException {
        return genNewTypeWithAggregateCalcPropBasedOn(baseType, 1);
    }

}
