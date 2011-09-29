package ua.com.fielden.platform.reflection.asm.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.factory.CalculatedAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.IsPropertyAnnotation;
import ua.com.fielden.platform.types.Money;

/**
 * {@link NewProperty} test case to ensure correct instantiation.
 *
 * @author TG Team
 *
 */
public class NewPropertyTest {

    @Test
    public void test_annotation_description_presence() {
	final Calculated calculated = new CalculatedAnnotation().expression("some expression").newInstance();
	final IsProperty isProperty = new IsPropertyAnnotation(Money.class).newInstance();

	final NewProperty pd = new NewProperty("prop_name", List.class, false, "title", "desc", calculated, isProperty);
	assertTrue("Should have recognised the presence of annotation description.", pd.containsAnnotationDescriptorFor(IsProperty.class));
	assertTrue("Should have recognised the presence of annotation description.", pd.containsAnnotationDescriptorFor(Calculated.class));
	assertFalse("Should have not recognised the presence of annotation description.", pd.containsAnnotationDescriptorFor(CritOnly.class));
    }

}
