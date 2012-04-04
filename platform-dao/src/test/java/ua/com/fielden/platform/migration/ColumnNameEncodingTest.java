package ua.com.fielden.platform.migration;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests the correctness of column name encoding as part of data migration methodology.
 *
 * @author TG team
 *
 */
public class ColumnNameEncodingTest {

    @Test
    public void testEncoding() {
	assertEquals("Incorrect encoding result", "property_name_", AbstractRetriever.encodePropertyName("propertyName"));
	assertEquals("Incorrect encoding result", "property_long_name_", AbstractRetriever.encodePropertyName("propertyLongName"));
	assertEquals("Incorrect encoding result", "property_", AbstractRetriever.encodePropertyName("property"));
	assertEquals("Incorrect encoding result", "property_name__subproperty_name_", AbstractRetriever.encodePropertyName("propertyName.subpropertyName"));
	assertEquals("Incorrect encoding result", "property_name__subproperty_name__key_", AbstractRetriever.encodePropertyName("propertyName.subpropertyName.key"));
	assertEquals("Incorrect encoding result", "property__subproperty_name__key_", AbstractRetriever.encodePropertyName("property.subpropertyName.key"));
    }
}
