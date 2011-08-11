package ua.com.fielden.platform.test.query;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.dao.MappingExtractor;
import ua.com.fielden.platform.equery.ColumnInfo;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.persistence.types.EntityWithTaxMoney;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.test.domain.entities.Bogie;
import ua.com.fielden.platform.test.domain.entities.BogieClass;
import ua.com.fielden.platform.test.domain.entities.WheelsetClass;

/**
 * This is an all-in-one test to ensure that Hibernate mapping for work order entity is correct.
 *
 * @author TG Team
 *
 */
public class HibernateMappingExtractorTest extends DbDrivenTestCase {
    private final MappingExtractor mappingExtractor = injector.getInstance(MappingExtractor.class);

    private Map<String, String> extractColumnNames(final Map<String, ColumnInfo> columnsInfo) {
	final Map<String, String> result = new HashMap<String, String>();
	for (final Map.Entry<String, ColumnInfo> entry : columnsInfo.entrySet()) {
	    result.put(entry.getKey(), entry.getValue().getColumnName());
	}
	return result;
    }

    public void test_bogie_columns_retrieval() {
	final Map<String, String> expectedColumns = new HashMap<String, String>();
	expectedColumns.put("id", "ID_COLUMN");
	expectedColumns.put("version", "VERSION_COLUMN");
	expectedColumns.put("key", "ROTABLE_NO");
	expectedColumns.put("desc", "ROTABLE_DESC");
	expectedColumns.put("status", "ROTABLE_STATUS");
	expectedColumns.put("rotableClass", "EQCLASS");
	expectedColumns.put("location.class", "CURRENT_LOCATION_TYPE");
	expectedColumns.put("location", "CURRENT_LOCATION");
	assertEquals(expectedColumns, extractColumnNames(mappingExtractor.getColumns(Bogie.class, null)));
    }

    public void test_entityWithTaxMoney_columns_retrieval() {
	final Map<String, String> expectedColumns = new HashMap<String, String>();
	expectedColumns.put("id", "_ID");
	expectedColumns.put("version", "_VERSION");
	expectedColumns.put("key", "KEY_");
	expectedColumns.put("desc", "DESC_");
	expectedColumns.put("money.amount", "MONEY_AMOUNT");
	expectedColumns.put("money.taxAmount", "MONEY_TAXAMOUNT");
	expectedColumns.put("money.currency", "MONEY_CURRENCY");

	assertEquals(expectedColumns, extractColumnNames(mappingExtractor.getColumns(EntityWithTaxMoney.class, null)));
    }

    // TODO reimplement using ColumnInfo for location.class in Bogie entity
//    public void test_polymorphic_prop_type_meta_value_retrieval() {
//	assertEquals(WagonSlot.class, mappingExtractor.getPolymorphicType(Bogie.class, "location", "WASLOT"));
//	assertEquals(Workshop.class, mappingExtractor.getPolymorphicType(Bogie.class, "location", "WSHOP"));
//	assertEquals(AdvicePosition.class, mappingExtractor.getPolymorphicType(Bogie.class, "location", "ADVPOS"));
//    }

    public void test_bogie_rotable_class_table_source_retrieval() {
	final String expectedSql1 = "(SELECT * FROM RMA_ROTCLASS WHERE CLASS_TYPE = 'BO')";
	assertEquals(expectedSql1, mappingExtractor.getTableClause(BogieClass.class));
	final String expectedSql2 = "(SELECT * FROM RMA_ROTCLASS WHERE CLASS_TYPE = 'WS')";
	assertEquals(expectedSql2, mappingExtractor.getTableClause(WheelsetClass.class));
    }

    public void test_bogie_rotable_class_columns_retrieval() {
	final Map<String, String> expectedColumns = new HashMap<String, String>();
	expectedColumns.put("id", "ID_COLUMN");
	expectedColumns.put("version", "VERSION_COLUMN");
	expectedColumns.put("key", "EQCLASS");
	expectedColumns.put("desc", "EQCLASS_DESC");
	expectedColumns.put("tonnage", "TONNAGE");

	assertEquals(expectedColumns, extractColumnNames(mappingExtractor.getColumns(BogieClass.class, null)));
    }

    public void test_mapping_extractor() {
	assertEquals("Incorrect column name for property <amount> of entity: " + EntityWithMoney.class, "MONEY_AMOUNT", mappingExtractor.getColumns(EntityWithMoney.class, null).get("money.amount").getColumnName().toUpperCase());
	System.out.println(mappingExtractor.getColumns(Bogie.class, null));
	System.out.println(mappingExtractor.getColumns(EntityWithTaxMoney.class, null));
    }

    @Override
    protected String[] getDataSetPathsForInsert() {
	return new String[] { "src/test/resources/data-files/empty.flat.xml" };
    }
}