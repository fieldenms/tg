package ua.com.fielden.platform.migration;

import org.junit.Test;

/**
 * Tests the correctness of column name decoding as part of data migration methodology.
 *
 * @author 01es
 *
 */
public class ColumnNameDecodingTest {

    @Test
    public void testUndescoreStipping() {
//	assertEquals("Undescore should have been stripped", "column", AbstractRetriever.decodePropertyName("column_"));
//	assertEquals("Undescore should have been stripped", "column", AbstractRetriever.decodePropertyName("column_ "));
//	assertEquals("Undescore should have been stripped", "column", AbstractRetriever.decodePropertyName(" column_"));
//	assertEquals("Undescore should have been stripped", "column", AbstractRetriever.decodePropertyName(" column_ "));
    }

    @Test
    public void testDecoding() {
//	assertEquals("Incorrect encoding result", "propertyName", AbstractRetriever.decodePropertyName("property_name_"));
//	assertEquals("Incorrect encoding result", "propertyLongName", AbstractRetriever.decodePropertyName("property_long_name_ "));
//	assertEquals("Incorrect encoding result", "propertyName", AbstractRetriever.decodePropertyName(" property_name_"));
//	assertEquals("Incorrect encoding result", "propertyLongName", AbstractRetriever.decodePropertyName(" property_long_name_ "));
//
//	assertEquals("Incorrect encoding result", "propertyName", AbstractRetriever.decodePropertyName("PROPERTY_NAME_"));
//	assertEquals("Incorrect encoding result", "propertyLongName", AbstractRetriever.decodePropertyName("PROPERTY_LONG_NAME_ "));
//	assertEquals("Incorrect encoding result", "propertyName", AbstractRetriever.decodePropertyName(" PROPERTY_NAME_"));
//	assertEquals("Incorrect encoding result", "propertyLongName", AbstractRetriever.decodePropertyName(" PROPERTY_LONG_NAME_ "));
//
//	assertEquals("Incorrect encoding result", "propertyLongName.anotherOne.theLast", AbstractRetriever.decodePropertyName(" PROPERTY_LONG_NAME__another_one__the_last_"));
//
//	assertEquals("Incorrect encoding result", "author.name", AbstractRetriever.decodePropertyName("AUTHOR__NAME_"));
//	assertEquals("Incorrect encoding result", "parent.parent.parent.parent.name", AbstractRetriever.decodePropertyName("PARENT__PARENT__PARENT__PARENT__NAME_"));
    }

    @Test
    public void testSubpropertiesDecoding() {
	//assertEquals("Incorrect encoding result", "propertyName.subpropertyName", AbstractRetriever.decodePropertyName("property_name__subproperty_name_"));
    }

}
