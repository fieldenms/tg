package ua.com.fielden.platform.file_reports;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.joda.time.DateTime;
import org.junit.Test;
import ua.com.fielden.platform.domaintree.testing.MasterEntity;
import ua.com.fielden.platform.domaintree.testing.MasterEntity.EnumType;
import ua.com.fielden.platform.domaintree.testing.ShortSlaveEntity;
import ua.com.fielden.platform.domaintree.testing.SlaveEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity_centre.review.criteria.DynamicColumnForExport;
import ua.com.fielden.platform.types.Money;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.mapOf;
import static ua.com.fielden.platform.utils.Pair.pair;

public class WorkbookExporterTest {

    private static final List<List<DynamicColumnForExport>> EMPTY_DYNAMIC_PROPERTIES = List.of();

    @Test
    public void date_property_can_be_exported() {
        final MasterEntity entityToExport = new MasterEntity();
        entityToExport.setDateProp(new DateTime(2000, 1, 1, 0, 0).toDate());
        final String[] propertyNames = { "dateProp" };
        final String[] propertyTitles = { "Date property" };
        final Sheet sheet = WorkbookExporter.export(Stream.of(entityToExport), propertyNames, propertyTitles).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("Date property of the exported row is incorrect", new DateTime(2000, 1, 1, 0, 0).toDate(), exportedRow.getCell(0).getDateCellValue());
    }

    @Test
    public void date_time_property_can_be_exported() {
        final EntityWithDateTimeProp entityToExport = new EntityWithDateTimeProp();
        entityToExport.setDateTimeProp(new DateTime(2000, 1, 1, 0, 0));
        final String[] propertyNames = { "dateTimeProp" };
        final String[] propertyTitles = { "Date Time property" };
        final Sheet sheet = WorkbookExporter.export(Stream.of(entityToExport), propertyNames, propertyTitles).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("Date time property of the exported row is incorrect", new DateTime(2000, 1, 1, 0, 0).toDate(), exportedRow.getCell(0).getDateCellValue());
    }

    @Test
    public void boolean_property_can_be_exported() {
        final MasterEntity entityToExport = new MasterEntity();
        entityToExport.setBooleanProp(true);
        final String[] propertyNames = { "booleanProp" };
        final String[] propertyTitles = { "Boolean property" };
        final Sheet sheet = WorkbookExporter.export(Stream.of(entityToExport), propertyNames, propertyTitles).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("Boolean property of the exported row is incorrect", true, exportedRow.getCell(0).getBooleanCellValue());
    }

    @Test
    public void money_property_can_be_exported() {
        final MasterEntity entityToExport = new MasterEntity();
        final var amount = new Money("1.00");
        entityToExport.setMoneyProp(amount);
        final String[] propertyNames = { "moneyProp" };
        final String[] propertyTitles = { "Money property" };
        final Sheet sheet = WorkbookExporter.export(Stream.of(entityToExport), propertyNames, propertyTitles).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        final DataFormatter formatter = new DataFormatter();
        final String formattedCellValue = formatter.formatCellValue(exportedRow.getCell(0));
        assertEquals("Money property of the exported row is formatted incorrectly.", amount.toString(), formattedCellValue);
        assertEquals("Money property of the exported row is incorrect.", 1.0d, exportedRow.getCell(0).getNumericCellValue(), 0.0);
    }

    @Test
    public void string_property_can_be_exported() {
        final MasterEntity entityToExport = new MasterEntity();
        entityToExport.setStringProp("master1");
        final String[] propertyNames = { "stringProp" };
        final String[] propertyTitles = { "String property" };
        final Sheet sheet = WorkbookExporter.export(Stream.of(entityToExport), propertyNames, propertyTitles).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("String property of the exported row is incorrect", "master1", exportedRow.getCell(0).getStringCellValue());
    }

    @Test
    public void long_string_property_should_be_also_exportable() {
        final MasterEntity entityToExport = new MasterEntity();
        final String value = "very long description very long description very long description very long description very long description "
                + "very long description very long description very long description very long description very long description very long description "
                + "very long description very long description very long description very long description very long description very long description "
                + "very long description very long description very long description very long description very long description very long description";
        entityToExport.setStringProp(value);
        final String[] propertyNames = { "stringProp" };
        final String[] propertyTitles = { "String property" };
        final Sheet sheet = WorkbookExporter.export(Stream.of(entityToExport), propertyNames, propertyTitles).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("String property of the exported row is incorrect", value, exportedRow.getCell(0).getStringCellValue());
    }

    @Test
    public void integer_property_can_be_exported() {
        final MasterEntity entityToExport = new MasterEntity();
        entityToExport.setIntegerProp(Integer.valueOf(1));
        final String[] propertyNames = { "integerProp" };
        final String[] propertyTitles = { "Integer property" };
        final Sheet sheet = WorkbookExporter.export(Stream.of(entityToExport), propertyNames, propertyTitles).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("Integer property of the exported row is incorrect", Double.valueOf(1), Double.valueOf(exportedRow.getCell(0).getNumericCellValue()));
    }

    @Test
    public void null_property_can_be_exported() {
        final MasterEntity entityToExport = new MasterEntity();

        entityToExport.setBigDecimalProp(null);
        final String[] propertyNames = { "bigDecimalProp" };
        final String[] propertyTitles = { "BigDecimal property" };

        final Sheet sheet = WorkbookExporter.export(Stream.of(entityToExport), propertyNames, propertyTitles).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("Null property should have blank style", CellType.BLANK, exportedRow.getCell(0).getCellType());
    }

    @Test
    public void enum_property_can_be_exported() {
        final MasterEntity entityToExport = new MasterEntity();
        entityToExport.setEnumProp(EnumType.ONE);
        final String[] propertyNames = { "enumProp" };
        final String[] propertyTitles = { "Enumeration property" };
        final Sheet sheet = WorkbookExporter.export(Stream.of(entityToExport), propertyNames, propertyTitles).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("Enum property of the exported row is incorrect", "ONE", exportedRow.getCell(0).getStringCellValue());
    }

    @Test
    public void entity_property_can_be_exported() {
        final MasterEntity entityToExport = new MasterEntity();
        entityToExport.setKey("master key1");
        final SlaveEntity slave1 = new SlaveEntity();
        slave1.setMasterEntityProp(entityToExport);
        slave1.setIntegerProp(Integer.valueOf(1));
        entityToExport.setEntityProp(slave1);
        final String[] propertyNames = { "entityProp" };
        final String[] propertyTitles = { "Entity property" };
        final Sheet sheet = WorkbookExporter.export(Stream.of(entityToExport), propertyNames, propertyTitles).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("Entity property of the exported row is incorrect", "master key1 1", exportedRow.getCell(0).getStringCellValue());
    }

    @Test
    public void exporting_entities_with_this_included_associates_the_main_hyperlink_with_those_cells() {
        final MasterEntity entityToExport = new MasterEntity();
        entityToExport.setKey("master key1");
        final SlaveEntity slave1 = new SlaveEntity();
        slave1.setMasterEntityProp(entityToExport);
        slave1.setIntegerProp(Integer.valueOf(1));
        entityToExport.setEntityProp(slave1);
        final String[] propertyNames = { "", "entityProp" };
        final String[] propertyTitles = { "This", "Entity property" };
        final Sheet sheet = WorkbookExporter.export(Stream.of(entityToExport), propertyNames, propertyTitles, entity -> of("http://tgdev.com")).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);

        assertEquals("Unexpected cell value for ”this”.", "master key1", exportedRow.getCell(0).getStringCellValue());
        assertNotNull("Hyperlinks are expected to be associated with cells for “this”.", exportedRow.getCell(0).getHyperlink());

        assertEquals("Unexpected cell value for entity-typed property “entityProp”", "master key1 1", exportedRow.getCell(1).getStringCellValue());
        assertNull("Hyperlinks are not expected for non-key entity-typed property “entityProp”", exportedRow.getCell(1).getHyperlink());
    }

    @Test
    public void exporting_entities_with_key_included_associates_the_main_hyperlink_with_those_cells() {
        final MasterEntity entityToExport = new MasterEntity();
        entityToExport.setKey("master key1");
        final String[] propertyNames = { "key", "entityProp" };
        final String[] propertyTitles = { "Key", "Entity property" };
        final Sheet sheet = WorkbookExporter.export(Stream.of(entityToExport), propertyNames, propertyTitles, entity -> of("http://tgdev.com")).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("Unexpected cell value for ”this”.", "master key1", exportedRow.getCell(0).getStringCellValue());
        assertNotNull("Hyperlinks are expected to be associated with cells for “key”.", exportedRow.getCell(0).getHyperlink());
    }

    @Test
    public void exporting_composite_entities_without_this_but_with_key_members_included_associates_the_main_hyperlink_with_those_cells() {
        final MasterEntity master = new MasterEntity();
        master.setKey("master key1");
        final SlaveEntity entityToExport = new SlaveEntity();
        entityToExport.setMasterEntityProp(master); // key member 1
        entityToExport.setIntegerProp(Integer.valueOf(1)); // key member 2

        final String[] propertyNames = { "masterEntityProp", "integerProp" };
        final String[] propertyTitles = { "Master Entity", "integer property" };
        final Sheet sheet = WorkbookExporter.export(Stream.of(entityToExport), propertyNames, propertyTitles, entity -> of("http://tgdev.com")).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);

        assertEquals("Unexpected cell value for ”masterEntityProp”.", "master key1", exportedRow.getCell(0).getStringCellValue());
        assertNotNull("Hyperlinks are expected to be associated with cells for “masterEntityProp”.", exportedRow.getCell(0).getHyperlink());
    }

    @Test
    public void exporting_composite_entities_without_master_but_with_an_entity_typed_key_member_included_witch_has_master_associates_hyperlink_with_those_cells() {
        final MasterEntity master = new MasterEntity();
        master.setKey("master key1");
        final SlaveEntity entityToExport = new SlaveEntity();
        entityToExport.setMasterEntityProp(master); // key member 1
        entityToExport.setIntegerProp(Integer.valueOf(1)); // key member 2

        final String[] propertyNames = { "masterEntityProp", "integerProp" };
        final String[] propertyTitles = { "Master Entity", "integer property" };
        final Sheet sheet = WorkbookExporter.export(Stream.of(entityToExport), propertyNames, propertyTitles, entity -> entity instanceof SlaveEntity ? empty() : of("http://tgdev.com")).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);

        assertEquals("Unexpected cell value for ”masterEntityProp”.", "master key1", exportedRow.getCell(0).getStringCellValue());
        assertNotNull("Hyperlinks are expected for be associated with cells for “masterEntityProp”.", exportedRow.getCell(0).getHyperlink());

        assertEquals("Unexpected cell value for property “integerProp”", 1d, exportedRow.getCell(1).getNumericCellValue(), 0);
        assertNull("Hyperlinks are not expected to be associated with cells for “integerProp”.", exportedRow.getCell(1).getHyperlink());
    }

    @Test
    public void hyperlinks_can_be_provided_explicitly_per_entity_and_per_property() {
        final MasterEntity master = new MasterEntity();
        master.setKey("master key1");
        master.setBigDecimalProp(new BigDecimal("10.42"));
        final SlaveEntity entityToExport = new SlaveEntity();
        entityToExport.setMasterEntityProp(master); // key member 1
        entityToExport.setIntegerProp(Integer.valueOf(1)); // key member 2

        final var propertyNames = new String[] { "masterEntityProp", "masterEntityProp.bigDecimalProp", "integerProp" };
        final var propertyTitles = new String[] { "Master Entity", "Master Entity Bid Decimal Prop", "integer property" };
        final var hyperlinksForEntityToExport = mapOf(t2("masterEntityProp", "https://tgdev.com/#/master/1"), t2("masterEntityProp.bigDecimalProp", "https://tgdev.com/#/master/1"), t2("integerProp", "https://tgdev.com/#/master/1"));
        final Sheet sheet = WorkbookExporter.export(Stream.of(entityToExport), Stream.of(hyperlinksForEntityToExport), propertyNames, propertyTitles).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);

        assertEquals("Unexpected cell value for ”masterEntityProp”.", "master key1", exportedRow.getCell(0).getStringCellValue());
        assertNotNull("Hyperlinks are expected for be associated with cells for “masterEntityProp”.", exportedRow.getCell(0).getHyperlink());

        assertEquals("Unexpected cell value for property “masterEntityProp.bigDecimalProp”", new BigDecimal("10.42"), new BigDecimal(exportedRow.getCell(1).getNumericCellValue(), new MathContext(4, RoundingMode.HALF_UP)));
        assertNotNull("Hyperlinks are expected to be associated with cells for “masterEntityProp.bigDecimalProp”.", exportedRow.getCell(1).getHyperlink());

        assertEquals("Unexpected cell value for property “integerProp”", 1d, exportedRow.getCell(2).getNumericCellValue(), 0);
        assertNotNull("Hyperlinks are expected to be associated with cells for “integerProp”.", exportedRow.getCell(2).getHyperlink());
    }

    @Test
    public void collection_property_can_be_exported() {
        final MasterEntity entityToExport = new MasterEntity();
        entityToExport.setKey("master key1");
        final SlaveEntity slave1 = new SlaveEntity();
        slave1.setMasterEntityProp(entityToExport);
        slave1.setIntegerProp(Integer.valueOf(1));
        final SlaveEntity slave2 = new SlaveEntity();
        slave2.setMasterEntityProp(entityToExport);
        slave2.setIntegerProp(Integer.valueOf(2));
        entityToExport.setCollection(Arrays.asList(slave1, slave2));
        final String[] propertyNames = { "collection" };
        final String[] propertyTitles = { "Collection property" };
        final Sheet sheet = WorkbookExporter.export(Stream.of(entityToExport), propertyNames, propertyTitles).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("Collectional property of the exported row is incorrect", "master key1 1, master key1 2", exportedRow.getCell(0).getStringCellValue());
    }

    @Test
    public void short_collection_property_can_be_exported() {
        final MasterEntity entityToExport = new MasterEntity();
        entityToExport.setKey("master key1");
        final ShortSlaveEntity shortSlave1 = new ShortSlaveEntity();
        final SlaveEntity slave1 = new SlaveEntity();
        slave1.setMasterEntityProp(entityToExport);
        slave1.setIntegerProp(Integer.valueOf(1));
        shortSlave1.setMasterEntityProp(entityToExport);
        shortSlave1.setKey2(slave1);
        final ShortSlaveEntity shortSlave2 = new ShortSlaveEntity();
        final SlaveEntity slave2 = new SlaveEntity();
        slave2.setMasterEntityProp(entityToExport);
        slave2.setIntegerProp(Integer.valueOf(2));
        shortSlave2.setMasterEntityProp(entityToExport);
        shortSlave2.setKey2(slave2);
        entityToExport.setShortCollection(Arrays.asList(shortSlave1, shortSlave2));
        final String[] propertyNames = { "shortCollection" };
        final String[] propertyTitles = { "Short collection" };
        final Sheet sheet = WorkbookExporter.export(Stream.of(entityToExport), propertyNames, propertyTitles).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("Short collection property of the exported row is incorrect", "master key1 1, master key1 2", exportedRow.getCell(0).getStringCellValue());
    }

    @Test
    public void entity_aggregats_with_date_property_can_be_exported() {
        final EntityAggregates entityToExport = new EntityAggregates();
        entityToExport.set("dateProp", new DateTime(2000, 1, 1, 0, 0).toDate());
        final String[] propertyNames = { "dateProp" };
        final String[] propertyTitles = { "Date property" };
        final Sheet sheet = WorkbookExporter.export(Stream.of(entityToExport), propertyNames, propertyTitles).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("Date property of the exported entity aggregates is incorrect", new DateTime(2000, 1, 1, 0, 0).toDate(), exportedRow.getCell(0).getDateCellValue());
    }

    @Test
    public void entity_aggregats_with_date_time_property_can_be_exported() {
        final EntityAggregates entityToExport = new EntityAggregates();
        entityToExport.set("dateTimeProp", new DateTime(2000, 1, 1, 0, 0));
        final String[] propertyNames = { "dateTimeProp" };
        final String[] propertyTitles = { "Date Time property" };
        final Sheet sheet = WorkbookExporter.export(Stream.of(entityToExport), propertyNames, propertyTitles).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("Date time property of the exported entity aggregates is incorrect", new DateTime(2000, 1, 1, 0, 0).toDate(), exportedRow.getCell(0).getDateCellValue());
    }

    @Test
    public void entity_aggregats_with_boolean_property_can_be_exported() {
        final EntityAggregates entityToExport = new EntityAggregates();
        entityToExport.set("booleanProp", true);
        final String[] propertyNames = { "booleanProp" };
        final String[] propertyTitles = { "Boolean property" };
        final Sheet sheet = WorkbookExporter.export(Stream.of(entityToExport), propertyNames, propertyTitles).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("Boolean property of the exported entity aggregates is incorrect", true, exportedRow.getCell(0).getBooleanCellValue());
    }

    @Test
    public void entity_aggregats_with_money_property_can_be_exported() {
        final EntityAggregates entityToExport = new EntityAggregates();
        final var amount = new Money("1.00");
        entityToExport.set("moneyProp", amount);
        final String[] propertyNames = { "moneyProp" };
        final String[] propertyTitles = { "Money property" };
        final Sheet sheet = WorkbookExporter.export(Stream.of(entityToExport), propertyNames, propertyTitles).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        final DataFormatter formatter = new DataFormatter();
        final String formattedCellValue = formatter.formatCellValue(exportedRow.getCell(0));
        assertEquals("Money property of the exported row is formatted incorrectly.", amount.toString(), formattedCellValue);
        assertEquals("Money property of the exported row is incorrect.", 1.0d, exportedRow.getCell(0).getNumericCellValue(), 0.0);
    }

    @Test
    public void entity_aggregats_with_string_property_can_be_exported() {
        final EntityAggregates entityToExport = new EntityAggregates();
        entityToExport.set("stringProp", "master1");
        final String[] propertyNames = { "stringProp" };
        final String[] propertyTitles = { "String property" };
        final Sheet sheet = WorkbookExporter.export(Stream.of(entityToExport), propertyNames, propertyTitles).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("String property of the exported entity aggregates is incorrect", "master1", exportedRow.getCell(0).getStringCellValue());
    }

    @Test
    public void entity_aggregats_with_integer_property_can_be_exported() {
        final EntityAggregates entityToExport = new EntityAggregates();
        entityToExport.set("integerProp", Integer.valueOf(1));
        final String[] propertyNames = { "integerProp" };
        final String[] propertyTitles = { "Integer property" };
        final Sheet sheet = WorkbookExporter.export(Stream.of(entityToExport), propertyNames, propertyTitles).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("Integer property of the exported entity aggregates is incorrect", Double.valueOf(1), Double.valueOf(exportedRow.getCell(0).getNumericCellValue()));
    }

    @Test
    public void entity_aggregats_with_null_property_can_be_exported() {
        final EntityAggregates entityToExport = new EntityAggregates();

        entityToExport.set("bigDecimalProp", null);
        final String[] propertyNames = { "bigDecimalProp" };
        final String[] propertyTitles = { "BigDecimal property" };

        final Sheet sheet = WorkbookExporter.export(Stream.of(entityToExport), propertyNames, propertyTitles).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("Entity aggregate's null property should have blank style", CellType.BLANK, exportedRow.getCell(0).getCellType());
    }

    @Test
    public void entity_aggregats_with_enum_property_can_be_exported() {
        final EntityAggregates entityToExport = new EntityAggregates();
        entityToExport.set("enumProp", EnumType.ONE);
        final String[] propertyNames = { "enumProp" };
        final String[] propertyTitles = { "Enumeration property" };
        final Sheet sheet = WorkbookExporter.export(Stream.of(entityToExport), propertyNames, propertyTitles).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("Enum property of the exported entity aggregates is incorrect", "ONE", exportedRow.getCell(0).getStringCellValue());
    }

    @Test
    public void entity_aggregats_with_entity_property_can_be_exported() {
        final EntityAggregates entityToExport = new EntityAggregates();
        final MasterEntity master1 = new MasterEntity();
        master1.setKey("master key1");
        final SlaveEntity slave1 = new SlaveEntity();
        slave1.setMasterEntityProp(master1);
        slave1.setIntegerProp(Integer.valueOf(1));
        entityToExport.set("entityProp", slave1);
        final String[] propertyNames = { "entityProp" };
        final String[] propertyTitles = { "Entity property" };
        final Sheet sheet = WorkbookExporter.export(Stream.of(entityToExport), propertyNames, propertyTitles).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("Entity property of the exported entity aggregates is incorrect", "master key1 1", exportedRow.getCell(0).getStringCellValue());
    }

    @Test
    public void entity_aggregats_with_collection_property_can_be_exported() {
        final EntityAggregates entityToExport = new EntityAggregates();
        final MasterEntity master1 = new MasterEntity();
        master1.setKey("master key1");
        final SlaveEntity slave1 = new SlaveEntity();
        slave1.setMasterEntityProp(master1);
        slave1.setIntegerProp(Integer.valueOf(1));
        final SlaveEntity slave2 = new SlaveEntity();
        slave2.setMasterEntityProp(master1);
        slave2.setIntegerProp(Integer.valueOf(2));
        entityToExport.set("collection", Arrays.asList(slave1, slave2));
        final String[] propertyNames = { "collection" };
        final String[] propertyTitles = { "Collection property" };
        final Sheet sheet = WorkbookExporter.export(Stream.of(entityToExport), propertyNames, propertyTitles).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("Collectional property of the exported entity aggregates is incorrect", "master key1 1, master key1 2", exportedRow.getCell(0).getStringCellValue());
    }

    @Test
    public void entity_aggregats_with_short_collection_property_can_be_exported() {
        final EntityAggregates entityToExport = new EntityAggregates();
        final MasterEntity master1 = new MasterEntity();
        master1.setKey("master key1");
        final MasterEntity master2 = new MasterEntity();
        master2.setKey("master key2");
        final ShortSlaveEntity shortSlave1 = new ShortSlaveEntity();
        final SlaveEntity slave1 = new SlaveEntity();
        slave1.setMasterEntityProp(master1);
        slave1.setIntegerProp(Integer.valueOf(1));
        shortSlave1.setMasterEntityProp(master1);
        shortSlave1.setKey2(slave1);
        final ShortSlaveEntity shortSlave2 = new ShortSlaveEntity();
        final SlaveEntity slave2 = new SlaveEntity();
        slave2.setMasterEntityProp(master1);
        slave2.setIntegerProp(Integer.valueOf(2));
        shortSlave2.setMasterEntityProp(master1);
        shortSlave2.setKey2(slave2);
        entityToExport.set("shortCollection", Arrays.asList(shortSlave1, shortSlave2));
        final String[] propertyNames = { "shortCollection" };
        final String[] propertyTitles = { "Short collection" };
        final Sheet sheet = WorkbookExporter.export(Stream.of(entityToExport), propertyNames, propertyTitles).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("Short collection property of the exported row is incorrect", "master key1 1, master key1 2", exportedRow.getCell(0).getStringCellValue());
    }

    @Test
    public void multiple_sheets_can_be_exported() {
        final var entityToExport1 = new MasterEntity();
        final var date1 = new DateTime(2000, 1, 1, 0, 0).toDate();
        entityToExport1.setDateProp(date1);

        final var entityToExport2 = new MasterEntity();
        final var date2 = new DateTime(2004, 1, 1, 0, 0).toDate();
        entityToExport2.setDateProp(date2);

        final String[] propertyNames = { "dateProp" };
        final String[] propertyTitles = { "Date property" };
        final var sheets = WorkbookExporter.export(List.of(Stream.of(entityToExport1), Stream.of(entityToExport2)),
                                                   List.of(pair(propertyNames, propertyTitles), pair(propertyNames, propertyTitles)),
                                                   List.of(EMPTY_DYNAMIC_PROPERTIES, EMPTY_DYNAMIC_PROPERTIES),
                                                   List.of("Sheet 1", "Sheet 2"),
                                                   $ -> of("http://tgdev.com"));
        assertEquals(2, sheets.getNumberOfSheets());

        final var sheet1 = sheets.getSheetAt(0);
        assertEquals("Sheet 1", sheet1.getSheetName());
        assertEquals(date1, sheet1.getRow(1).getCell(0).getDateCellValue());

        final var sheet2 = sheets.getSheetAt(1);
        assertEquals("Sheet 2", sheet2.getSheetName());
        assertEquals(date2, sheet2.getRow(1).getCell(0).getDateCellValue());
    }

}
