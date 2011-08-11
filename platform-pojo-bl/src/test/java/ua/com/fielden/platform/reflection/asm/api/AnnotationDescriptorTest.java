package ua.com.fielden.platform.reflection.asm.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * {@link AnnotationDescriptor} test case to ensure correct instantiation.
 *
 * @author TG Team
 *
 */
public class AnnotationDescriptorTest {

    @Test
    public void test_annotation_descriptor_can_be_created_for_valid_params() {
	final Map<String, Object> params = new HashMap<String, Object>();
	params.put("value", "string");
	params.put("intValue", 1);
	params.put("doubleValue", 0.1);

	final AnnotationDescriptor ad = new AnnotationDescriptor(AnnotationForTesting.class, params);

	assertEquals("incorrect type value", AnnotationForTesting.class, ad.type);
	assertEquals("incorrect number of params", 3, ad.params.size());
	assertEquals("incorrect param value", params.get("value"), ad.params.get("value"));
	assertEquals("incorrect param value", params.get("intValue"), ad.params.get("intValue"));
	assertEquals("incorrect param value", params.get("doubleValue"), ad.params.get("doubleValue"));
    }

    @Test
    public void test_annotation_descriptor_validation_of_type_parameter() {
	try {
	    new AnnotationDescriptor(null, null);
	    fail("Should have failed due to absent annotation type value.");
	} catch (final Exception e) {
	}
    }

    @Test
    public void test_only_valid_annotation_parameters_are_uses_during_instantiaton() {
	final Map<String, Object> params = new HashMap<String, Object>();
	params.put("value", "string");
	params.put("intValue", 1);
	params.put("doubleValue", 0.1);
	params.put("someOther1", 0.1);
	params.put("someOther2", 0.1);

	final AnnotationDescriptor ad = new AnnotationDescriptor(AnnotationForTesting.class, params);

	assertEquals("incorrect type value", AnnotationForTesting.class, ad.type);
	assertEquals("incorrect number of params", 3, ad.params.size());
	assertEquals("incorrect param value", params.get("value"), ad.params.get("value"));
	assertEquals("incorrect param value", params.get("intValue"), ad.params.get("intValue"));
	assertEquals("incorrect param value", params.get("doubleValue"), ad.params.get("doubleValue"));
	assertNull("incorrect param value", ad.params.get("someOther1"));
	assertNull("incorrect param value", ad.params.get("someOther2"));
    }
}
