package ua.com.fielden.platform.reflection.asm.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.junit.Test;

import javassist.compiler.ast.Pair;
import ua.com.fielden.platform.entity.Entity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.factory.CalculatedAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.IsPropertyAnnotation;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.reflection.asm.impl.entities.EntityWithCollectionalPropety;
import ua.com.fielden.platform.types.Money;

/**
 * {@link NewProperty} test case to ensure correct instantiation.
 * 
 * @author TG Team
 * 
 */
public class NewPropertyTest {
    
    private static final NewProperty collectionalRawList = new NewProperty("elements", List.class, "title", "desc", 
            new IsPropertyAnnotation(String.class).newInstance());
    private static final NewProperty collectionalParameterizedList = new NewProperty("elements", List.class, new Type[] {String.class},
            "title", "desc", new IsPropertyAnnotation(String.class).newInstance());
    private static final NewProperty propertyDescriptor = new NewProperty("elements", PropertyDescriptor.class, new Type[] {Entity.class},
            "title", "desc", new IsPropertyAnnotation(Entity.class).newInstance());

    @Test
    public void test_annotation_description_presence() {
        final Calculated calculated = new CalculatedAnnotation().contextualExpression("some expression").newInstance();
        final IsProperty isProperty = new IsPropertyAnnotation(Money.class).newInstance();

        final NewProperty pd = new NewProperty("prop_name", List.class, false, "title", "desc", calculated, isProperty);
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
    
    @Test
    public void changing_the_type_argument_of_collectional_property_also_modifies_value_of_IsProperty_annotation() {
        // copy NewProperty and manually copy @IsProperty annotation
        final NewProperty pd = collectionalRawList.copy().setAnnotations(new IsPropertyAnnotation(String.class).newInstance());
        // update type arguments
        pd.setTypeArguments(Double.class);

        assertArrayEquals("Should be parameterized with Double.",
                new Type[] {Double.class}, ((ParameterizedType) pd.genericType()).getActualTypeArguments());
        assertEquals("The value of @IsProperty should be equal to Double.class",
                Double.class, ((IsProperty) pd.getAnnotationByType(IsProperty.class)).value());
    }

    @Test
    public void changing_the_type_argument_of_PropertyDescriptor_also_modifies_value_of_IsProperty_annotation() {
        // copy NewProperty and manually copy @IsProperty annotation
        final NewProperty pd = propertyDescriptor.copy().setAnnotations(new IsPropertyAnnotation(Entity.class).newInstance());
        // update type arguments to any other entity
        pd.setTypeArguments(EntityWithCollectionalPropety.class);

        assertArrayEquals("Should be parameterized with EntityWithCollectionalPropety.",
                new Type[] {EntityWithCollectionalPropety.class}, ((ParameterizedType) pd.genericType()).getActualTypeArguments());
        assertEquals("The value of @IsProperty should be equal to EntityWithCollectionalPropety.class",
                EntityWithCollectionalPropety.class, ((IsProperty) pd.getAnnotationByType(IsProperty.class)).value());
    }
    
    @Test
    public void changing_type_arguments_of_other_types_does_not_affect_IsProperty() {
        // Pair<String, String>
        final NewProperty pair = new NewProperty("pair", Pair.class, new Type[] {String.class, String.class}, "title", "desc");
        final Class<?> previousValue = ((IsProperty) pair.getAnnotationByType(IsProperty.class)).value();
        pair.setTypeArguments(String.class, Double.class);

        assertEquals("The value of @IsProperty should not have been modified.", 
                previousValue, ((IsProperty) pair.getAnnotationByType(IsProperty.class)).value());
    }
    
    @Test
    public void Title_annotation_is_missing_if_both_title_and_desc_are_null() {
        final NewProperty npNoTitle = new NewProperty("prop", String.class, null, null);
        assertFalse(npNoTitle.containsAnnotationDescriptorFor(Title.class));
        
        // only title present, desc is null
        final NewProperty npWithTitle = new NewProperty("prop", String.class, "title", null);
        final Title atTitle = npWithTitle.getAnnotationByType(Title.class);
        assertNotNull(atTitle);
        assertEquals("title", atTitle.value());
        assertEquals("", atTitle.desc());

        // title is null, only desc present
        final NewProperty npWithDesc = new NewProperty("prop", String.class, null, "desc");
        final Title atTitle1 = npWithDesc.getAnnotationByType(Title.class);
        assertNotNull(atTitle1);
        assertEquals("", atTitle1.value());
        assertEquals("desc", atTitle1.desc());
    }

}
