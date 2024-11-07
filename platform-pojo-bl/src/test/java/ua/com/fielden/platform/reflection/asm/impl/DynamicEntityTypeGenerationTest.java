package ua.com.fielden.platform.reflection.asm.impl;

import static java.util.stream.Collectors.toCollection;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.startModification;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.test_entities.Entity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.factory.DescTitleAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.IsPropertyAnnotation;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;
import ua.com.fielden.platform.reflection.asm.exceptions.TypeMakerException;
import ua.com.fielden.platform.reflection.asm.impl.entities.TopLevelEntity;
import ua.com.fielden.platform.reflection.test_entities.EntityWithConstructors;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.utils.Pair;

/**
 * A test case to ensure correct generation of dynamic entity types in terms of basic characteristics.
 * <p>
 * More specific tests, such as those focused on entity properties, are {@link DynamicEntityTypePropertiesAdditionTest} and
 * {@link DynamicEntityTypePropertiesModificationTest}.
 *
 * @author TG Team
 *
 */
public class DynamicEntityTypeGenerationTest {
    private static final Class<Entity> DEFAULT_ORIG_TYPE = Entity.class;

    private final EntityTestIocModuleWithPropertyFactory module = new CommonEntityTestIocModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void generated_types_have_the_same_class_loader() throws Exception {
        // generated types that share the same original type
        final Class<? extends AbstractEntity<String>> newType1 = startModification(DEFAULT_ORIG_TYPE)
                .modifyTypeName(DEFAULT_ORIG_TYPE.getName() + "_enhanced1")
                .endModification();
        final ClassLoader expectedCL = newType1.getClassLoader();

        final Class<? extends AbstractEntity<String>> newType2 = startModification(DEFAULT_ORIG_TYPE)
                .modifyTypeName(DEFAULT_ORIG_TYPE.getName() + "_enhanced2")
                .endModification();
        assertEquals(expectedCL, newType2.getClassLoader());
        
        // generated type that has a different original type
        final Class<? extends AbstractEntity<String>> newType3 = startModification(TopLevelEntity.class)
                .modifyTypeName(TopLevelEntity.class.getName() + "_enhanced3")
                .endModification();
        assertEquals(expectedCL, newType3.getClassLoader());
        
        // generated type that is based on another generated type
        final Class<? extends AbstractEntity<String>> newType4 = startModification(newType3)
                .modifyTypeName(newType3.getName() + "_enhanced4")
                .endModification();
        assertEquals(expectedCL, newType4.getClassLoader());
    }
    
    @Test
    public void members_of_java_top_level_package_can_not_be_enhanced() {
        assertThrows("%s class should not be allowed to undergo modification.".formatted(String.class), 
                TypeMakerException.class, () -> {
                    startModification(String.class);
                });
    }
    
    @Test
    public void a_generated_type_is_generated_as_a_subclass_of_the_original_type() throws Exception {
        final Class<? extends AbstractEntity<String>> newType = startModification(DEFAULT_ORIG_TYPE)
                .endModification();
        
        assertEquals("Incorrect type hierarchy.", DEFAULT_ORIG_TYPE, newType.getSuperclass());
        
        // the same applies for generated types based on other generated types
        final Class<? extends AbstractEntity<String>> newTypeFromNewType = startModification(newType)
                .endModification();
        
        assertEquals("Incorrect type hierarchy.", newType, newTypeFromNewType.getSuperclass());
    }
    
    @Test
    public void generated_types_do_not_redeclare_properties_of_original_type() throws Exception {
        final Class<? extends AbstractEntity<String>> newType = startModification(DEFAULT_ORIG_TYPE)
                .endModification();

        // a field is represented as *type name & field name* pair
        final Set<Pair<String, String>> newTypeDeclaredFields = Arrays.stream(newType.getDeclaredFields())
                .map(field -> Pair.pair(field.getGenericType().toString(), field.getName()))
                .collect(toCollection(HashSet::new));

        Finder.streamProperties(DEFAULT_ORIG_TYPE)
            .map(prop -> Pair.pair(prop.getGenericType().toString(), prop.getName()))
            .forEach(pair -> assertFalse(newTypeDeclaredFields.contains(pair)));
    }

    @Test
    public void class_name_can_be_specified_for_a_generated_type() throws Exception {
        final String newTypeName = DynamicTypeNamingService.nextTypeName(DEFAULT_ORIG_TYPE.getName());
        final Class<? extends AbstractEntity<String>> newType = startModification(DEFAULT_ORIG_TYPE)
                .modifyTypeName(newTypeName)
                .endModification();

        assertEquals("Incorrect type name.", newTypeName, newType.getName());
    }
    
    @Test
    public void generated_types_are_implicitly_named_with_unique_class_names_that_are_prefixed_by_the_original_type_name() throws Exception {
        final Class<? extends AbstractEntity<String>> newType = startModification(DEFAULT_ORIG_TYPE)
                .endModification();
        
        assertTrue("Incorrect generated type name prefix.", newType.getName().startsWith(
                DEFAULT_ORIG_TYPE.getName() + DynamicTypeNamingService.APPENDIX + "_"));
    }

    @Test
    public void startModification_method_must_be_used_as_the_API_entry_point() {
        assertThrows("An exception should have been thrown due to omitted startModification call.",
                TypeMakerException.class, () -> {
                    final TypeMaker<Entity> tp = new TypeMaker<Entity>(null, Entity.class);
                    tp.addProperties(NewProperty.create("newTestProp", String.class, "Title", "Desc"))
                        .endModification();
                });

        assertThrows("An exception should have been thrown due to omitted startModification call.",
                TypeMakerException.class, () -> {
                    final TypeMaker<Entity> tp = new TypeMaker<Entity>(null, Entity.class);
                    tp.endModification();
                });
    }

    @Test
    public void generated_types_can_be_instantiated_using_entity_factory() throws Exception {
        final Class<? extends AbstractEntity<String>> newType = startModification(DEFAULT_ORIG_TYPE)
                .endModification();
        final AbstractEntity<?> entity = factory.newByKey(newType, "key");

        assertNotNull("Entity factory returned null in an attempt to instantiate a generated type.", entity);
    }

    @Test
    public void adding_valid_class_annotations_is_possible() throws Exception {
        final DescTitle newAnnot = new DescTitleAnnotation("Title", "Description").newInstance();
        final Class<? extends AbstractEntity<?>> newType = startModification(TopLevelEntity.class)
                .addClassAnnotations(newAnnot)
                .endModification();

        final DescTitle annot = newType.getAnnotation(DescTitle.class);
        assertNotNull(annot);
        assertEquals("Incorrect value of annotation element.", newAnnot.value(), annot.value());
        assertEquals("Incorrect value of annotation element.", newAnnot.desc(), annot.desc());
    }

    @Test
    public void adding_invalid_class_annotations_results_in_a_runtime_exception() throws Exception {
        assertThrows("Adding a non-class-level annotation should fail.", 
                TypeMakerException.class, () -> {
                    startModification(TopLevelEntity.class)
                    .addClassAnnotations(new IsPropertyAnnotation().newInstance())
                    .endModification();
                });
    }

    @Test
    public void a_generated_type_is_generated_with_the_same_class_annotations_as_the_original_type() throws Exception {
        final Class<? extends AbstractEntity<?>> newType = startModification(DEFAULT_ORIG_TYPE).endModification();
        assertArrayEquals("Class-level annotations declared by the generated type do not match those of the original type.",
                DEFAULT_ORIG_TYPE.getDeclaredAnnotations(), newType.getDeclaredAnnotations());
    }
    
    @Test
    public void adding_a_class_level_annotation_of_an_existing_type_has_no_effect() throws Exception {
        final DescTitle newAnnot = new DescTitleAnnotation("New Title", "New Description").newInstance();
        final DescTitle oldAnnot = DEFAULT_ORIG_TYPE.getDeclaredAnnotation(DescTitle.class);
        assertNotNull("Original type must be annotated with the annotation being tested.", oldAnnot);

        final Class<? extends AbstractEntity<?>> newType = startModification(DEFAULT_ORIG_TYPE)
                .addClassAnnotations(newAnnot)
                .endModification();

        final DescTitle annot = newType.getDeclaredAnnotation(DescTitle.class);
        assertEquals(oldAnnot.value(), annot.value());
        assertEquals(oldAnnot.desc(), annot.desc());
    }

    @Test
    public void original_type_of_a_generated_one_is_recorded_and_can_be_accessed_with_a_generated_method() throws Exception {
        final Class<? extends AbstractEntity<String>> newType = startModification(DEFAULT_ORIG_TYPE).endModification();

        assertEquals(DEFAULT_ORIG_TYPE, DynamicEntityClassLoader.getOriginalType(newType));
    }

    @Test
    public void generated_types_based_on_other_generated_types_inherit_their_original_type() throws Exception {
        final Class<? extends AbstractEntity<String>> newType1 = startModification(DEFAULT_ORIG_TYPE).endModification();
        final Class<? extends AbstractEntity<String>> newType2 = startModification(newType1).endModification();

        assertEquals(DEFAULT_ORIG_TYPE, DynamicEntityClassLoader.getOriginalType(newType1));
        assertEquals(DEFAULT_ORIG_TYPE, DynamicEntityClassLoader.getOriginalType(newType2));
    }
    
    @Test
    public void a_generated_type_declares_visible_constructors_of_the_original_type_and_inherits_runtime_annotations() throws Exception {
        final Class<? extends EntityWithConstructors> newType = startModification(EntityWithConstructors.class)
                .endModification();
        
        final Map<Boolean, List<Constructor<?>>> constructors = Arrays.stream(EntityWithConstructors.class.getDeclaredConstructors())
                .collect(Collectors.partitioningBy(constr -> Modifier.isPrivate(constr.getModifiers())));

        // private constructors are ignored
        for (final Constructor<?> origPrivateConstructor: constructors.get(true)) {
            assertThrows(NoSuchMethodException.class, () -> {
                newType.getDeclaredConstructor(origPrivateConstructor.getParameterTypes());
            });
        }
        // visible constructors are declared with runtime annotations inherited 
        for (final Constructor<?> origVisibleConstructor: constructors.get(false)) {
            final Constructor<?> newConstructor = newType.getDeclaredConstructor(origVisibleConstructor.getParameterTypes());
            assertNotNull(newConstructor);
            assertArrayEquals("Incorrect annotations declared by the generated type's constructor.", 
                    origVisibleConstructor.getDeclaredAnnotations(), newConstructor.getDeclaredAnnotations());
        }
    }

}