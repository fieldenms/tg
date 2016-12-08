package ua.com.fielden.platform.file_reports;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.joda.time.DateTime;
import org.junit.Test;

import ua.com.fielden.platform.domaintree.testing.MasterEntity;
import ua.com.fielden.platform.domaintree.testing.MasterEntity.EnumType;
import ua.com.fielden.platform.domaintree.testing.ShortSlaveEntity;
import ua.com.fielden.platform.domaintree.testing.SlaveEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.types.Money;

public class WorkbookExporterTest {


    @Test
    public void short_collectional_property_can_be_exported() {
        final MasterEntity master1 = new MasterEntity();
        master1.setKey("master key1");
        master1.setDateProp(new DateTime(2000, 1, 1, 0, 0).toDate());
        master1.setBooleanProp(true);
        master1.setMoneyProp(new Money("1.00"));
        master1.setStringProp("master1");
        master1.setIntegerProp(Integer.valueOf(1));
        master1.setDoubleProp(null);
        master1.setEnumProp(EnumType.ONE);
        final MasterEntity master2 = new MasterEntity();
        master2.setKey("master key2");
        master2.setDateProp(new DateTime(2000, 1, 2, 0, 0).toDate());
        master2.setBooleanProp(true);
        master2.setMoneyProp(new Money("2.00"));
        master2.setStringProp("master2");
        master2.setIntegerProp(Integer.valueOf(2));
        master2.setDoubleProp(null);
        master2.setEnumProp(EnumType.TWO);
        final List<MasterEntity> entities = new ArrayList<MasterEntity>();
        entities.add(master1);
        entities.add(master2);
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
        master1.setShortCollection(Arrays.asList(shortSlave1, shortSlave2));
        master1.setEntityProp(slave1);
        master1.setCollection(Arrays.asList(slave1, slave2));
        final ShortSlaveEntity shortSlave3 = new ShortSlaveEntity();
        final SlaveEntity slave3 = new SlaveEntity();
        slave3.setMasterEntityProp(master2);
        slave3.setIntegerProp(Integer.valueOf(3));
        shortSlave3.setMasterEntityProp(master2);
        shortSlave3.setKey2(slave3);
        final ShortSlaveEntity shortSlave4 = new ShortSlaveEntity();
        final SlaveEntity slave4 = new SlaveEntity();
        slave4.setMasterEntityProp(master2);
        slave4.setIntegerProp(Integer.valueOf(4));
        shortSlave4.setMasterEntityProp(master2);
        shortSlave4.setKey2(slave4);
        master2.setShortCollection(Arrays.asList(shortSlave3, shortSlave4));
        master2.setEntityProp(slave3);
        master2.setCollection(Arrays.asList(slave3, slave4));
        final String[] propertyNames = { "dateProp", "booleanProp", "moneyProp", "stringProp", "integerProp", "shortCollection", "entityProp", "collection",
                "doubleProp", "enumProp" };
        final String[] propertyTitles = { "Date property", "Boolean property", "Money property", "String property", "Integer property", "Short collection",
                "Entity property", "Collection", "Double property", "Enumeration property" };
        final HSSFSheet sheet = WorkbookExporter.export(entities, propertyNames, propertyTitles).getSheetAt(0);
        final HSSFRow firstRow = sheet.getRow(1);
        assertEquals("Date property of the first row is incorrect", new DateTime(2000, 1, 1, 0, 0).toDate(), firstRow.getCell(0).getDateCellValue());
        assertEquals("Boolean property of the first row is incorrect", true, firstRow.getCell(1).getBooleanCellValue());
        assertEquals("Money property of the first row is incorrect", "$1.00", firstRow.getCell(2).getStringCellValue());
        assertEquals("String property of the first row is incorrect", "master1", firstRow.getCell(3).getStringCellValue());
        assertEquals("Integer property of the first row is incorrect", Double.valueOf(1), Double.valueOf(firstRow.getCell(4).getNumericCellValue()));
        assertEquals("Short collection property of the first row is incorrect", "master key1 1, master key1 2", firstRow.getCell(5).getStringCellValue());
        assertEquals("Entity property of the first row is incorrect", "master key1 1", firstRow.getCell(6).getStringCellValue());
        assertEquals("Collectional property of the first row is incorrect", "master key1 1, master key1 2", firstRow.getCell(7).getStringCellValue());
        assertEquals("Double property of the first row is incorrect", HSSFCell.CELL_TYPE_BLANK, firstRow.getCell(8).getCellType());
        assertEquals("Enumeration property of the first row is incorrect", "ONE", firstRow.getCell(9).getStringCellValue());
        final HSSFRow secondRow = sheet.getRow(2);
        assertEquals("Date property of the second row is incorrect", new DateTime(2000, 1, 2, 0, 0).toDate(), secondRow.getCell(0).getDateCellValue());
        assertEquals("Boolean property of the second row is incorrect", true, secondRow.getCell(1).getBooleanCellValue());
        assertEquals("Money property of the second row is incorrect", "$2.00", secondRow.getCell(2).getStringCellValue());
        assertEquals("String property of the second row is incorrect", "master2", secondRow.getCell(3).getStringCellValue());
        assertEquals("Integer property of the second row is incorrect", Double.valueOf(2), Double.valueOf(secondRow.getCell(4).getNumericCellValue()));
        assertEquals("short collection property of the second row is incorrect", "master key2 3, master key2 4", secondRow.getCell(5).getStringCellValue());
        assertEquals("Entity property of the second row is incorrect", "master key2 3", secondRow.getCell(6).getStringCellValue());
        assertEquals("Collectional property of the second row is incorrect", "master key2 3, master key2 4", secondRow.getCell(7).getStringCellValue());
        assertEquals("Double property of the second row is incorrect", HSSFCell.CELL_TYPE_BLANK, secondRow.getCell(8).getCellType());
        assertEquals("Enumeration property of the second row is incorrect", "TWO", secondRow.getCell(9).getStringCellValue());
    }

    @Test
    public void entity_aggregates_with_short_collection_can_be_exported() {
        final EntityAggregates aggregates1 = new EntityAggregates();
        aggregates1.set("dateProp", new DateTime(2000, 1, 1, 0, 0).toDate());
        aggregates1.set("booleanProp", true);
        aggregates1.set("moneyProp", new Money("1.00"));
        aggregates1.set("stringProp", "master1");
        aggregates1.set("integerProp", Integer.valueOf(1));
        aggregates1.set("doubleProp", null);
        aggregates1.set("enumProp", EnumType.ONE);
        final EntityAggregates aggregates2 = new EntityAggregates();
        aggregates2.set("dateProp", new DateTime(2000, 1, 2, 0, 0).toDate());
        aggregates2.set("booleanProp", true);
        aggregates2.set("moneyProp", new Money("2.00"));
        aggregates2.set("stringProp", "master2");
        aggregates2.set("integerProp", Integer.valueOf(2));
        aggregates2.set("doubleProp", null);
        aggregates2.set("enumProp", EnumType.TWO);
        final List<EntityAggregates> entities = new ArrayList<EntityAggregates>();
        entities.add(aggregates1);
        entities.add(aggregates2);
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
        aggregates1.set("shortCollection", Arrays.asList(shortSlave1, shortSlave2));
        aggregates1.set("entityProp", slave1);
        aggregates1.set("collection", Arrays.asList(slave1, slave2));
        final ShortSlaveEntity shortSlave3 = new ShortSlaveEntity();
        final SlaveEntity slave3 = new SlaveEntity();
        slave3.setMasterEntityProp(master2);
        slave3.setIntegerProp(Integer.valueOf(3));
        shortSlave3.setMasterEntityProp(master2);
        shortSlave3.setKey2(slave3);
        final ShortSlaveEntity shortSlave4 = new ShortSlaveEntity();
        final SlaveEntity slave4 = new SlaveEntity();
        slave4.setMasterEntityProp(master2);
        slave4.setIntegerProp(Integer.valueOf(4));
        shortSlave4.setMasterEntityProp(master2);
        shortSlave4.setKey2(slave4);
        aggregates2.set("shortCollection", Arrays.asList(shortSlave3, shortSlave4));
        aggregates2.set("entityProp", slave3);
        aggregates2.set("collection", Arrays.asList(slave3, slave4));
        final String[] propertyNames = new String[] { "dateProp", "booleanProp", "moneyProp", "stringProp", "integerProp", "shortCollection", "entityProp", "collection",
                "doubleProp", "enumProp" };
        final String[] propertyTitles = new String[] { "Date property", "Boolean property", "Money property", "String property", "Integer property", "Short collection",
                "Entity property", "Collection", "Double property", "Enumeration property" };
        final HSSFSheet sheet = WorkbookExporter.export(entities, propertyNames, propertyTitles).getSheetAt(0);
        final HSSFRow firstRow = sheet.getRow(1);
        assertEquals("Date property of the first row is incorrect", new DateTime(2000, 1, 1, 0, 0).toDate(), firstRow.getCell(0).getDateCellValue());
        assertEquals("Boolean property of the first row is incorrect", true, firstRow.getCell(1).getBooleanCellValue());
        assertEquals("Money property of the first row is incorrect", "$1.00", firstRow.getCell(2).getStringCellValue());
        assertEquals("String property of the first row is incorrect", "master1", firstRow.getCell(3).getStringCellValue());
        assertEquals("Integer property of the first row is incorrect", Double.valueOf(1), Double.valueOf(firstRow.getCell(4).getNumericCellValue()));
        assertEquals("Short collection property of the first row is incorrect", "master key1 1, master key1 2", firstRow.getCell(5).getStringCellValue());
        assertEquals("Entity property of the first row is incorrect", "master key1 1", firstRow.getCell(6).getStringCellValue());
        assertEquals("Collectional property of the first row is incorrect", "master key1 1, master key1 2", firstRow.getCell(7).getStringCellValue());
        assertEquals("Double property of the first row is incorrect", HSSFCell.CELL_TYPE_BLANK, firstRow.getCell(8).getCellType());
        assertEquals("Enumeration property of the first row is incorrect", "ONE", firstRow.getCell(9).getStringCellValue());
        final HSSFRow secondRow = sheet.getRow(2);
        assertEquals("Date property of the second row is incorrect", new DateTime(2000, 1, 2, 0, 0).toDate(), secondRow.getCell(0).getDateCellValue());
        assertEquals("Boolean property of the second row is incorrect", true, secondRow.getCell(1).getBooleanCellValue());
        assertEquals("Money property of the second row is incorrect", "$2.00", secondRow.getCell(2).getStringCellValue());
        assertEquals("String property of the second row is incorrect", "master2", secondRow.getCell(3).getStringCellValue());
        assertEquals("Integer property of the second row is incorrect", Double.valueOf(2), Double.valueOf(secondRow.getCell(4).getNumericCellValue()));
        assertEquals("short collection property of the second row is incorrect", "master key2 3, master key2 4", secondRow.getCell(5).getStringCellValue());
        assertEquals("Entity property of the second row is incorrect", "master key2 3", secondRow.getCell(6).getStringCellValue());
        assertEquals("Collectional property of the second row is incorrect", "master key2 3, master key2 4", secondRow.getCell(7).getStringCellValue());
        assertEquals("Double property of the second row is incorrect", HSSFCell.CELL_TYPE_BLANK, secondRow.getCell(8).getCellType());
        assertEquals("Enumeration property of the second row is incorrect", "TWO", secondRow.getCell(9).getStringCellValue());
    }
}
