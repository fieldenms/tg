package ua.com.fielden.platform.reflection.asm.api;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.reflection.asm.api.test_utils.NewPropertyTestUtils.assertPropertyEquals;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.test_entities.Entity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.Generated;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.factory.CalculatedAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.IsPropertyAnnotation;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.asm.exceptions.NewPropertyException;
import ua.com.fielden.platform.reflection.asm.impl.entities.EntityWithCollectionalPropety;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.types.Money;

/**
 * {@link NewProperty} test case to ensure correct instantiation.
 * 
 * @author TG Team
 * 
 */
public class NewPropertyTest {

    private final EntityTestIocModuleWithPropertyFactory module = new CommonEntityTestIocModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);
    
    private static final NewProperty<List> npRawList = NewProperty.create("elements", List.class, "title", "desc", 
            new IsPropertyAnnotation(String.class).newInstance());
    private static final NewProperty<List> npParamList = NewProperty.create("elements", List.class,
            List.of(String.class), "title", "desc", new IsPropertyAnnotation(String.class).newInstance());
    private static final NewProperty<PropertyDescriptor> npPropDescriptor = NewProperty.create("elements", PropertyDescriptor.class,
            List.of(Entity.class), "title", "desc", new IsPropertyAnnotation(Entity.class).newInstance());

    @Test
    public void test_annotation_description_presence() {
        final Calculated calculated = new CalculatedAnnotation().contextualExpression("some expression").newInstance();
        final IsProperty isProperty = new IsPropertyAnnotation(Money.class).newInstance();

        final NewProperty<List> np = NewProperty.create("prop_name", List.class, "title", "desc", calculated, isProperty);
        assertTrue("Should have recognised the presence of annotation description.", np.containsAnnotationDescriptorFor(IsProperty.class));
        assertTrue("Should have recognised the presence of annotation description.", np.containsAnnotationDescriptorFor(Calculated.class));
        assertFalse("Should have not recognised the presence of annotation description.", np.containsAnnotationDescriptorFor(CritOnly.class));
    }

    @Test
    public void collectional_property_with_parameterized_type_returns_correct_generic_type() {
        final Type genericType = npParamList.genericType();
        assertTrue("Collectional property's generic type should be an instance of ParameterizedType.",
                ParameterizedType.class.isInstance(genericType));
        assertArrayEquals("Should be parameterized with String", 
                new Type[] {String.class}, ((ParameterizedType) genericType).getActualTypeArguments());
    }
    
    /**
     * For collectional properties with raw types (e.g. <code>List</code>) the method {@link NewProperty#genericType()} should return
     * a correct representation by looking up the value of {@link IsProperty} annotation.
     */
    @Test
    public void collectional_property_with_raw_type_returns_correct_generic_type() {
        final Type genericType = npRawList.genericType();
        assertTrue("Collectional property's generic type should be an instance of ParameterizedType.",
                ParameterizedType.class.isInstance(genericType));
        assertArrayEquals("Should be parameterized with String.", 
                new Type[] {String.class}, ((ParameterizedType) genericType).getActualTypeArguments());
    }
    
    /**
     * For collectional properties with raw types (e.g. <code>List</code>) the method {@link NewProperty#genericTypeAsDeclared()} 
     * should return the raw type, i.e., it should ignore the value of {@link IsProperty}.
     */
    @Test
    public void collectional_property_with_raw_type_returns_correct_generic_declared_type() {
        final Type genericTypeAsDeclared = npRawList.genericTypeAsDeclared();
        assertTrue("Incorrect representation of property's declared type.", Class.class.isInstance(genericTypeAsDeclared));
        assertEquals("Incorrect type returned as property's declared type.", List.class, (Class<?>) genericTypeAsDeclared);
    }
    
    @Test
    public void changing_the_type_argument_of_collectional_property_does_not_modify_the_value_of_IsProperty_annotation() {
        // copy NewProperty and manually copy @IsProperty annotation
        final NewProperty<List> np = npRawList.copy().setAnnotations(new IsPropertyAnnotation(String.class).newInstance());
        // update type arguments
        np.setTypeArguments(Double.class);

        assertEquals("Incorrect property type arguments.", List.of(Double.class), np.getTypeArguments());
        assertEquals("Incorrect @IsProperty.value().", String.class, np.getIsProperty().value());
    }

    @Test
    public void changing_the_type_argument_of_PropertyDescriptor_does_not_modify_the_value_of_IsProperty_annotation() {
        // copy NewProperty and manually copy @IsProperty annotation
        final NewProperty<PropertyDescriptor> np = npPropDescriptor.copy().setAnnotations(
                new IsPropertyAnnotation(Entity.class).newInstance());
        // update type arguments to any other entity
        np.setTypeArguments(EntityWithCollectionalPropety.class);

        assertEquals("Incorrect property type arguments.", List.of(EntityWithCollectionalPropety.class), np.getTypeArguments());
        assertEquals("Incorrect @IsProperty.value().", Entity.class, np.getIsProperty().value());
    }
    
    @Test
    public void the_value_of_IsProperty_annotation_can_be_changed() {
        final NewProperty<List> np = npRawList.changeTypeArguments(Double.class);
        final IsProperty oldAtIsProperty = np.getIsProperty();
        // previous value
        assertEquals("Incorrect @IsProperty.value().", String.class, oldAtIsProperty.value());

        // change IsProperty.value()
        np.changeIsPropertyValue(Double.class);
        
        final IsProperty newAtIsProperty = np.getIsProperty();
        assertNotSame("New @IsProperty refers to the old @IsProperty.", oldAtIsProperty, newAtIsProperty);
        // previous value
        assertEquals("Incorrect old @IsProperty.value().", String.class, oldAtIsProperty.value());
        // new value
        assertEquals("Incorrect new @IsProperty.value().", Double.class, newAtIsProperty.value());
    }
    
    @Test
    public void Title_annotation_is_missing_if_both_title_and_desc_are_null() {
        final NewProperty<String> npNoTitle = NewProperty.create("prop", String.class, null, null);
        assertFalse(npNoTitle.containsAnnotationDescriptorFor(Title.class));
        
        // only title present, desc is null
        final NewProperty<String> npWithTitle = NewProperty.create("prop", String.class, "title", null);
        final Title atTitle = npWithTitle.getAnnotationByType(Title.class);
        assertNotNull(atTitle);
        assertEquals("title", atTitle.value());
        assertEquals("", atTitle.desc());

        // title is null, only desc present
        final NewProperty<String> npWithDesc = NewProperty.create("prop", String.class, null, "desc");
        final Title atTitle1 = npWithDesc.getAnnotationByType(Title.class);
        assertNotNull(atTitle1);
        assertEquals("", atTitle1.value());
        assertEquals("desc", atTitle1.desc());
    }
    
    @Test
    public void IsProperty_annotation_is_always_present() {
        // an instance without explicitly provided @IsProperty
        final NewProperty<String> npImplicit = NewProperty.create("prop", String.class, "title", "desc");
        // 2: @Title and @IsProperty
        assertEquals("Incorrect number of annotations.", 2, npImplicit.getAnnotations().size());
        assertTrue("@IsProperty should be implicitly added.", npImplicit.containsAnnotationDescriptorFor(IsProperty.class));
        
        // explicitly provide @IsProperty
        final NewProperty<String> npExplicit = NewProperty.create("prop", String.class, "title", "desc",
                new IsPropertyAnnotation(String.class).newInstance());

        // 2: @Title and @IsProperty
        assertEquals("Incorrect number of annotations.", 2, npImplicit.getAnnotations().size());
        final IsProperty atIsProperty = npExplicit.getAnnotationByType(IsProperty.class);
        assertNotNull("@IsProperty should be present when added explicitly.", atIsProperty);
        assertEquals("Incorrect value of IsProperty.value().", String.class, atIsProperty.value());
    }
    
    @Test
    public void annotations_can_be_retrieved_in_a_generic_way_safely() {
        final IsProperty atIsProperty = npRawList.getAnnotationByType(IsProperty.class);
        assertNotNull(atIsProperty);
        final Title atTitle = npRawList.getAnnotationByType(Title.class);
        assertNotNull(atTitle);
        final Generated atGenerated = npRawList.getAnnotationByType(Generated.class);
        assertNull(atGenerated);
    }
    
    @Test
    public void initialization_value_can_be_provided() {
        final NewProperty<Double> np = NewProperty.create("prop", Double.class, "title", "desc").setValueSupplier(() -> Double.valueOf(125));
        final Object value = np.getValueSupplier().get();
        assertTrue("Property's value should have been initialized.", np.isInitialised());
        assertNotNull("Property's value should have been initialized.", value);
        assertEquals("Incorrect type of property init value.", Double.class, value.getClass());
        assertEquals("Incorrect property init value.", 125d, value);
        
        final NewProperty<List> npCollectional = npParamList.copy().setValueSupplier(() -> new LinkedList<>(List.of("hello", "world")));
        final Object list = npCollectional.getValueSupplier().get();
        assertTrue("Collectional property's value should have been initialized.", npCollectional.isInitialised());
        assertNotNull("Collectional property's value should have been initialized.", list);
        assertEquals("Incorrect type of property init value.", LinkedList.class, list.getClass());
        assertEquals("Incorrect property init value.", List.of("hello", "world"), list);
    }
    
    @Test
    public void can_be_created_from_Field() {
        final Field field = Finder.getFieldByName(Entity.class, "firstProperty");
        final NewProperty<?> np = NewProperty.fromField(field);
        assertNotNull("Failed to create NewProperty from Field.", np);
        assertPropertyEquals(np, field);
    }

    @Test
    public void can_be_created_from_Field_and_initalised() {
        final Entity instance = factory.newByKey(Entity.class, "new").setFirstProperty(125);
        final Field field = Finder.getFieldByName(Entity.class, "firstProperty");

        final NewProperty<?> np;
        try {
            np = NewProperty.fromField(field).setValueSupplierOrThrow(() -> instance.getFirstProperty());
        } catch (NewPropertyException e) {
            fail("Failed to initialize NewProperty constructed from Field: " + e.getMessage());
            return;
        }

        assertNotNull(np);
        assertPropertyEquals(np, field);
        assertEquals("Incorrect property initialized value.", 125, np.getValueSupplier().get());
    }

    @Test
    public void properties_can_be_initialised_with_equal_but_referentially_different_values() {
        final Field field = Finder.getFieldByName(Entity.class, "entity");

        final Supplier<Entity> initValueSupplier = () -> factory.newByKey(Entity.class, "new");
        final NewProperty<?> np1 = NewProperty.fromField(field).setValueSupplierOrThrow(initValueSupplier);
        final NewProperty<?> np2 = NewProperty.fromField(field).setValueSupplierOrThrow(initValueSupplier);

        final Entity np1Value = (Entity) np1.getValueSupplier().get();
        final Entity np2Value = (Entity) np2.getValueSupplier().get();
        assertEquals(np1Value, np2Value);
        assertNotEquals(System.identityHashCode(np1Value), System.identityHashCode(np2Value));
    }

    @Test
    public void throws_when_created_from_Field_but_initialization_value_is_incompatible_with_property_type() {
        final String value = "125"; // initialization value
        // this field is of type Integer
        final Field field = Finder.getFieldByName(Entity.class, "firstProperty");
        assertFalse(field.getType().isInstance(value));

        Assert.assertThrows(NewPropertyException.class, () -> NewProperty.fromField(field).setValueSupplierOrThrow(() -> value));
    }

    @Test
    public void new_properties_are_equal_iff_their_names_are_equal() {
        final var name = "propName";
        assertEquals(NewProperty.create(name, String.class, null, null), NewProperty.create(name, Double.class, null, null));
        assertNotEquals(NewProperty.create(name + "1", String.class, null, null), NewProperty.create(name, String.class, null, null));
    }

    @Test
    public void new_property_cannot_be_instantiated_with_blank_name_or_null_type() {
        Assert.assertThrows(NewPropertyException.class, () -> NewProperty.create(null, String.class, null, null));
        Assert.assertThrows(NewPropertyException.class, () -> NewProperty.create("", String.class, null, null));
        Assert.assertThrows(NewPropertyException.class, () -> NewProperty.create(" ", String.class, null, null));
        Assert.assertThrows(NewPropertyException.class, () -> NewProperty.create("name", (Class<String>) null, null, null));
    }

}