package ua.com.fielden.platform.reflection.asm.api;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.inject.Injector;

import javassist.compiler.ast.Pair;
import ua.com.fielden.platform.entity.Entity;
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
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;
import ua.com.fielden.platform.types.Money;

/**
 * {@link NewProperty} test case to ensure correct instantiation.
 * 
 * @author TG Team
 * 
 */
public class NewPropertyTest {

    private final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);
    
    private static final NewProperty<List> collectionalRawList = NewProperty.create("elements", List.class, "title", "desc", 
            new IsPropertyAnnotation(String.class).newInstance());
    private static final NewProperty<List> collectionalParameterizedList = NewProperty.create("elements", List.class,
            List.of(String.class), "title", "desc", new IsPropertyAnnotation(String.class).newInstance());
    private static final NewProperty<PropertyDescriptor> propertyDescriptor = NewProperty.create("elements", PropertyDescriptor.class,
            List.of(Entity.class), "title", "desc", new IsPropertyAnnotation(Entity.class).newInstance());

    @Test
    public void test_annotation_description_presence() {
        final Calculated calculated = new CalculatedAnnotation().contextualExpression("some expression").newInstance();
        final IsProperty isProperty = new IsPropertyAnnotation(Money.class).newInstance();

        final NewProperty<List> pd = NewProperty.create("prop_name", List.class, "title", "desc", calculated, isProperty);
        assertTrue("Should have recognised the presence of annotation description.", pd.containsAnnotationDescriptorFor(IsProperty.class));
        assertTrue("Should have recognised the presence of annotation description.", pd.containsAnnotationDescriptorFor(Calculated.class));
        assertFalse("Should have not recognised the presence of annotation description.", pd.containsAnnotationDescriptorFor(CritOnly.class));
    }

    @Test
    public void collectional_property_with_parameterized_type_returns_correct_generic_type() {
        final Type genericType = collectionalParameterizedList.genericType();
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
        final Type genericType = collectionalRawList.genericType();
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
        final Type genericTypeAsDeclared = collectionalRawList.genericTypeAsDeclared();
        assertTrue("Incorrect representation of property's declared type.", Class.class.isInstance(genericTypeAsDeclared));
        assertEquals("Incorrect type returned as property's declared type.", List.class, (Class<?>) genericTypeAsDeclared);
    }
    
    @Test
    public void changing_the_type_argument_of_collectional_property_also_modifies_value_of_IsProperty_annotation() {
        // copy NewProperty and manually copy @IsProperty annotation
        final NewProperty<List> pd = collectionalRawList.copy().setAnnotations(new IsPropertyAnnotation(String.class).newInstance());
        // update type arguments
        pd.setTypeArguments(Double.class);

        assertArrayEquals("Should be parameterized with Double.",
                new Type[] {Double.class}, ((ParameterizedType) pd.genericType()).getActualTypeArguments());
        assertEquals("The value of @IsProperty should be equal to Double.class",
                Double.class, pd.getAnnotationByType(IsProperty.class).value());
    }

    @Test
    public void changing_the_type_argument_of_PropertyDescriptor_also_modifies_value_of_IsProperty_annotation() {
        // copy NewProperty and manually copy @IsProperty annotation
        final NewProperty<PropertyDescriptor> pd = propertyDescriptor.copy().setAnnotations(
                new IsPropertyAnnotation(Entity.class).newInstance());
        // update type arguments to any other entity
        pd.setTypeArguments(EntityWithCollectionalPropety.class);

        assertArrayEquals("Should be parameterized with EntityWithCollectionalPropety.",
                new Type[] {EntityWithCollectionalPropety.class}, ((ParameterizedType) pd.genericType()).getActualTypeArguments());
        assertEquals("The value of @IsProperty should be equal to EntityWithCollectionalPropety.class",
                EntityWithCollectionalPropety.class, pd.getAnnotationByType(IsProperty.class).value());
    }
    
    @Test
    public void changing_type_arguments_of_other_types_does_not_affect_IsProperty() {
        // Pair<String, String>
        final NewProperty<Pair> pair = NewProperty.create("pair", Pair.class, List.of(String.class, String.class), "title", "desc");
        final Class<?> previousValue = pair.getAnnotationByType(IsProperty.class).value();
        pair.setTypeArguments(String.class, Double.class);

        assertEquals("The value of @IsProperty should not have been modified.", 
                previousValue, pair.getAnnotationByType(IsProperty.class).value());
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
        final IsProperty atIsProperty = collectionalRawList.getAnnotationByType(IsProperty.class);
        assertNotNull(atIsProperty);
        final Title atTitle = collectionalRawList.getAnnotationByType(Title.class);
        assertNotNull(atTitle);
        final Generated atGenerated = collectionalRawList.getAnnotationByType(Generated.class);
        assertNull(atGenerated);
    }
    
    @Test
    public void initialization_value_can_be_provided() {
        final NewProperty<Double> np = NewProperty.create("prop", Double.class, "title", "desc").setValue(Double.valueOf(125));
        final Object value = np.getValue();
        assertTrue("Property's value should have been initialized.", np.isInitialized());
        assertNotNull("Property's value should have been initialized.", value);
        assertEquals("Incorrect type of property init value.", Double.class, value.getClass());
        assertEquals("Incorrect property init value.", 125d, value);
        
        final NewProperty<List> npCollectional = collectionalParameterizedList.copy().setValue(new LinkedList<>(List.of("hello", "world")));
        final Object list = npCollectional.getValue();
        assertTrue("Collectional property's value should have been initialized.", npCollectional.isInitialized());
        assertNotNull("Collectional property's value should have been initialized.", list);
        assertEquals("Incorrect type of property init value.", LinkedList.class, list.getClass());
        assertEquals("Incorrect property init value.", List.of("hello", "world"), list);
    }
    
    @Test
    public void can_be_created_from_Field() {
        final Field field = Finder.getFieldByName(Entity.class, "firstProperty");
        final NewProperty<?> np = NewProperty.fromField(field);
        assertNotNull("Failed to create NewProperty from Field.", np);
        NewPropertyTest.assertSameProperty(field, np);
    }

    @Test
    public void can_be_created_from_Field_and_initalized() {
        final Entity instance = factory.newByKey(Entity.class, "new").setFirstProperty(125);
        final Field field = Finder.getFieldByName(Entity.class, "firstProperty");

        final NewProperty<?> np;
        try {
            np = NewProperty.fromField(field, instance);
        } catch (NewPropertyException e) {
            fail("Failed to initialize NewProperty constructed from Field: " + e.getMessage());
            return;
        }

        assertNotNull(np);
        NewPropertyTest.assertSameProperty(field, np);
        assertEquals("Incorrect property initialized value.", 125, np.getValue());
    }

    @Test
    public void throws_when_created_from_Field_but_initialization_value_is_incompatible_with_property_type() {
        final String value = "125"; // initialization value
        // this field is of type Integer
        final Field field = Finder.getFieldByName(Entity.class, "firstProperty");
        assertFalse(field.getType().isInstance(value));

        Assert.assertThrows(NewPropertyException.class, () -> {
            NewProperty.fromField(field).setValueThrows(value);
        });
    }
    
    /**
     * Asserts that <code>np</code> is a correct representation of <code>field</code> by testing its name, type and annotations. 
     * @param field
     * @param np
     */
    public static void assertSameProperty(final Field field, final NewProperty<?> np) {
        assertEquals("Incorrect property name.", field.getName(), np.getName());
        assertEquals("Incorrect declaration of property's generic type.", field.getGenericType(), np.genericTypeAsDeclared());

        final Annotation[] fieldAnnotations = field.getDeclaredAnnotations();
        final List<Annotation> npAnnotations = np.getAnnotations();
        assertEquals("Different number of annotations.", fieldAnnotations.length, npAnnotations.size());

        for (final Annotation fieldAnnot: fieldAnnotations) {
            assertTrue("Missing annotation.", npAnnotations.contains(fieldAnnot));
        }
    }
}
