package ua.com.fielden.platform.file_reports;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Optional;

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
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.web.interfaces.IUriGenerator;

public class WorkbookExporterTest {

    private class UriGeneratorStub implements IUriGenerator {

        @Override
        public <T extends AbstractEntity<?>> Optional<String> generateUri(final T entity) {
            return Optional.of(format("http://tgdev.com/#/master/%s/%s", entity.getType().getName(), entity.getKey().toString().replace(" ", "_")));
        }
    }

    @Test
    public void date_property_can_be_exported() {
        final MasterEntity entityToExport = new MasterEntity();
        entityToExport.setDateProp(new DateTime(2000, 1, 1, 0, 0).toDate());
        final String[] propertyNames = { "dateProp" };
        final String[] propertyTitles = { "Date property" };
        final Sheet sheet = WorkbookExporter.export(Arrays.asList(entityToExport).stream(), propertyNames, propertyTitles, empty()).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("Date property of the exported row is incorrect", new DateTime(2000, 1, 1, 0, 0).toDate(), exportedRow.getCell(0).getDateCellValue());
    }

    @Test
    public void date_time_property_can_be_exported() {
        final EntityWithDateTimeProp entityToExport = new EntityWithDateTimeProp();
        entityToExport.setDateTimeProp(new DateTime(2000, 1, 1, 0, 0));
        final String[] propertyNames = { "dateTimeProp" };
        final String[] propertyTitles = { "Date Time property" };
        final Sheet sheet = WorkbookExporter.export(Arrays.asList(entityToExport).stream(), propertyNames, propertyTitles, empty()).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("Date time property of the exported row is incorrect", new DateTime(2000, 1, 1, 0, 0).toDate(), exportedRow.getCell(0).getDateCellValue());
    }

    @Test
    public void boolean_property_can_be_exported() {
        final MasterEntity entityToExport = new MasterEntity();
        entityToExport.setBooleanProp(true);
        final String[] propertyNames = { "booleanProp" };
        final String[] propertyTitles = { "Boolean property" };
        final Sheet sheet = WorkbookExporter.export(Arrays.asList(entityToExport).stream(), propertyNames, propertyTitles, empty()).getSheetAt(0);
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
        final Sheet sheet = WorkbookExporter.export(Arrays.asList(entityToExport).stream(), propertyNames, propertyTitles, empty()).getSheetAt(0);
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
        final Sheet sheet = WorkbookExporter.export(Arrays.asList(entityToExport).stream(), propertyNames, propertyTitles, empty()).getSheetAt(0);
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
        final Sheet sheet = WorkbookExporter.export(Arrays.asList(entityToExport).stream(), propertyNames, propertyTitles, empty()).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("String property of the exported row is incorrect", value, exportedRow.getCell(0).getStringCellValue());
    }

    @Test
    public void integer_property_can_be_exported() {
        final MasterEntity entityToExport = new MasterEntity();
        entityToExport.setIntegerProp(Integer.valueOf(1));
        final String[] propertyNames = { "integerProp" };
        final String[] propertyTitles = { "Integer property" };
        final Sheet sheet = WorkbookExporter.export(Arrays.asList(entityToExport).stream(), propertyNames, propertyTitles, empty()).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("Integer property of the exported row is incorrect", Double.valueOf(1), Double.valueOf(exportedRow.getCell(0).getNumericCellValue()));
    }

    @Test
    public void null_property_can_be_exported() {
        final MasterEntity entityToExport = new MasterEntity();
        entityToExport.setDoubleProp(null);
        final String[] propertyNames = { "doubleProp" };
        final String[] propertyTitles = { "Double property" };
        final Sheet sheet = WorkbookExporter.export(Arrays.asList(entityToExport).stream(), propertyNames, propertyTitles, empty()).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("Null property should have blank style", CellType.BLANK, exportedRow.getCell(0).getCellType());
    }

    @Test
    public void enum_property_can_be_exported() {
        final MasterEntity entityToExport = new MasterEntity();
        entityToExport.setEnumProp(EnumType.ONE);
        final String[] propertyNames = { "enumProp" };
        final String[] propertyTitles = { "Enumeration property" };
        final Sheet sheet = WorkbookExporter.export(Arrays.asList(entityToExport).stream(), propertyNames, propertyTitles, empty()).getSheetAt(0);
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
        final Sheet sheet = WorkbookExporter.export(Arrays.asList(entityToExport).stream(), propertyNames, propertyTitles, empty()).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("Entity property of the exported row is incorrect", "master key1 1", exportedRow.getCell(0).getStringCellValue());
    }

    @Test
    public void entity_property_can_be_exported_with_hyperlink() {
        final MasterEntity entityToExport = new MasterEntity();
        entityToExport.setKey("master key1");
        final SlaveEntity slave1 = new SlaveEntity();
        slave1.setMasterEntityProp(entityToExport);
        slave1.setIntegerProp(Integer.valueOf(1));
        entityToExport.setEntityProp(slave1);
        final String[] propertyNames = { "entityProp" };
        final String[] propertyTitles = { "Entity property" };
        final Sheet sheet = WorkbookExporter.export(Arrays.asList(entityToExport).stream(), propertyNames, propertyTitles, Optional.of(new UriGeneratorStub())).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("Entity property of the exported row is incorrect", "master key1 1", exportedRow.getCell(0).getStringCellValue());
        assertEquals("Entity property hyperlink of the exported row is incorrect", "http://tgdev.com/#/master/ua.com.fielden.platform.domaintree.testing.SlaveEntity/master_key1_1", exportedRow.getCell(0).getHyperlink().getAddress());
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
        final Sheet sheet = WorkbookExporter.export(Arrays.asList(entityToExport).stream(), propertyNames, propertyTitles, empty()).getSheetAt(0);
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
        final Sheet sheet = WorkbookExporter.export(Arrays.asList(entityToExport).stream(), propertyNames, propertyTitles, empty()).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("Short collection property of the exported row is incorrect", "master key1 1, master key1 2", exportedRow.getCell(0).getStringCellValue());
    }

    @Test
    public void entity_aggregats_with_date_property_can_be_exported() {
        final EntityAggregates entityToExport = new EntityAggregates();
        entityToExport.set("dateProp", new DateTime(2000, 1, 1, 0, 0).toDate());
        final String[] propertyNames = { "dateProp" };
        final String[] propertyTitles = { "Date property" };
        final Sheet sheet = WorkbookExporter.export(Arrays.asList(entityToExport).stream(), propertyNames, propertyTitles, empty()).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("Date property of the exported entity aggregates is incorrect", new DateTime(2000, 1, 1, 0, 0).toDate(), exportedRow.getCell(0).getDateCellValue());
    }

    @Test
    public void entity_aggregats_with_date_time_property_can_be_exported() {
        final EntityAggregates entityToExport = new EntityAggregates();
        entityToExport.set("dateTimeProp", new DateTime(2000, 1, 1, 0, 0));
        final String[] propertyNames = { "dateTimeProp" };
        final String[] propertyTitles = { "Date Time property" };
        final Sheet sheet = WorkbookExporter.export(Arrays.asList(entityToExport).stream(), propertyNames, propertyTitles, empty()).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("Date time property of the exported entity aggregates is incorrect", new DateTime(2000, 1, 1, 0, 0).toDate(), exportedRow.getCell(0).getDateCellValue());
    }

    @Test
    public void entity_aggregats_with_boolean_property_can_be_exported() {
        final EntityAggregates entityToExport = new EntityAggregates();
        entityToExport.set("booleanProp", true);
        final String[] propertyNames = { "booleanProp" };
        final String[] propertyTitles = { "Boolean property" };
        final Sheet sheet = WorkbookExporter.export(Arrays.asList(entityToExport).stream(), propertyNames, propertyTitles, empty()).getSheetAt(0);
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
        final Sheet sheet = WorkbookExporter.export(Arrays.asList(entityToExport).stream(), propertyNames, propertyTitles, empty()).getSheetAt(0);
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
        final Sheet sheet = WorkbookExporter.export(Arrays.asList(entityToExport).stream(), propertyNames, propertyTitles, empty()).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("String property of the exported entity aggregates is incorrect", "master1", exportedRow.getCell(0).getStringCellValue());
    }

    @Test
    public void entity_aggregats_with_integer_property_can_be_exported() {
        final EntityAggregates entityToExport = new EntityAggregates();
        entityToExport.set("integerProp", Integer.valueOf(1));
        final String[] propertyNames = { "integerProp" };
        final String[] propertyTitles = { "Integer property" };
        final Sheet sheet = WorkbookExporter.export(Arrays.asList(entityToExport).stream(), propertyNames, propertyTitles, empty()).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("Integer property of the exported entity aggregates is incorrect", Double.valueOf(1), Double.valueOf(exportedRow.getCell(0).getNumericCellValue()));
    }

    @Test
    public void entity_aggregats_with_null_property_can_be_exported() {
        final EntityAggregates entityToExport = new EntityAggregates();
        entityToExport.set("doubleProp", null);
        final String[] propertyNames = { "doubleProp" };
        final String[] propertyTitles = { "Double property" };
        final Sheet sheet = WorkbookExporter.export(Arrays.asList(entityToExport).stream(), propertyNames, propertyTitles, empty()).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("Entity aggregate's null property should have blank style", CellType.BLANK, exportedRow.getCell(0).getCellType());
    }

    @Test
    public void entity_aggregats_with_enum_property_can_be_exported() {
        final EntityAggregates entityToExport = new EntityAggregates();
        entityToExport.set("enumProp", EnumType.ONE);
        final String[] propertyNames = { "enumProp" };
        final String[] propertyTitles = { "Enumeration property" };
        final Sheet sheet = WorkbookExporter.export(Arrays.asList(entityToExport).stream(), propertyNames, propertyTitles, empty()).getSheetAt(0);
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
        final Sheet sheet = WorkbookExporter.export(Arrays.asList(entityToExport).stream(), propertyNames, propertyTitles, empty()).getSheetAt(0);
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
        final Sheet sheet = WorkbookExporter.export(Arrays.asList(entityToExport).stream(), propertyNames, propertyTitles, empty()).getSheetAt(0);
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
        final Sheet sheet = WorkbookExporter.export(Arrays.asList(entityToExport).stream(), propertyNames, propertyTitles, empty()).getSheetAt(0);
        final Row exportedRow = sheet.getRow(1);
        assertEquals("Short collection property of the exported row is incorrect", "master key1 1, master key1 2", exportedRow.getCell(0).getStringCellValue());
    }
}
