package ua.com.fielden.platform.migration;

import org.junit.Test;

import ua.com.fielden.platform.migration.AbstractRetriever;
import static org.junit.Assert.assertEquals;

/**
 * Tests the correctness of column name decoding as part of data migration methodology.
 *
 * @author 01es
 *
 */
public class ColumnNameDecodingTest {

    @Test
    public void testUndescoreStipping() {
	assertEquals("Undescore should have been stripped", "column", AbstractRetriever.decodePropertyName("column_"));
	assertEquals("Undescore should have been stripped", "column", AbstractRetriever.decodePropertyName("column_ "));
	assertEquals("Undescore should have been stripped", "column", AbstractRetriever.decodePropertyName(" column_"));
	assertEquals("Undescore should have been stripped", "column", AbstractRetriever.decodePropertyName(" column_ "));
    }

    @Test
    public void testDecoding() {
	assertEquals("Incorrect encoding result", "propertyName", AbstractRetriever.decodePropertyName("property_name_"));
	assertEquals("Incorrect encoding result", "propertyLongName", AbstractRetriever.decodePropertyName("property_long_name_ "));
	assertEquals("Incorrect encoding result", "propertyName", AbstractRetriever.decodePropertyName(" property_name_"));
	assertEquals("Incorrect encoding result", "propertyLongName", AbstractRetriever.decodePropertyName(" property_long_name_ "));

	assertEquals("Incorrect encoding result", "propertyName", AbstractRetriever.decodePropertyName("PROPERTY_NAME_"));
	assertEquals("Incorrect encoding result", "propertyLongName", AbstractRetriever.decodePropertyName("PROPERTY_LONG_NAME_ "));
	assertEquals("Incorrect encoding result", "propertyName", AbstractRetriever.decodePropertyName(" PROPERTY_NAME_"));
	assertEquals("Incorrect encoding result", "propertyLongName", AbstractRetriever.decodePropertyName(" PROPERTY_LONG_NAME_ "));
    }

    @Test
    public void testSubpropertiesDecoding() {
	assertEquals("Incorrect encoding result", "propertyName.subpropertyName", AbstractRetriever.decodePropertyName("property_name__subproperty_name_"));
    }

}
