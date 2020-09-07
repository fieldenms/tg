package ua.com.fielden.platform.file_reports;

import static java.lang.Math.min;
import static java.lang.Math.round;
import static org.apache.commons.lang.StringUtils.join;
import static ua.com.fielden.platform.types.tuples.T2.t2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.Deflater;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.joda.time.DateTime;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity_centre.review.criteria.DynamicColumnForExport;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.serialisation.GZipOutputStreamEx;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * A set of utility methods for exporting data into MS Excel.
 *
 * @author TG Team
 *
 */
public class WorkbookExporter {

    private static final int MAX_COLUMN_WIDTH = 255 * 256;
    private static final int MAX_ROWS = SpreadsheetVersion.EXCEL97.getLastRowIndex();
    private static final String DEFAULT_SHEET_TITLE = "Exported data";

    private WorkbookExporter() {}

    public static <M extends AbstractEntity<?>> HSSFWorkbook export(final Stream<M> entities, final String[] propertyNames, final String[] propertyTitles, final List<List<DynamicColumnForExport>> dynamicProperties) {
        final List<T2<String, String>> propNamesAndTitles = new ArrayList<>();

        for (int index = 0; index < propertyNames.length && index < propertyTitles.length; index++) {
            propNamesAndTitles.add(t2(propertyNames[index], propertyTitles[index]));
        }
        
        // add property names and titles for dynamic properties
        final Map<String, DynamicColumnForExport> collectionalProps = new LinkedHashMap<>();
        dynamicProperties.forEach(listOfProps -> {
            listOfProps.forEach(prop -> {
                propNamesAndTitles.add(t2(prop.getGroupPropValue(), prop.getTitle()));
                collectionalProps.put(prop.getGroupPropValue(), prop);
            });
        });

        final DataForWorkbookSheet<M> dataForWorkbookSheet = new DataForWorkbookSheet<>(DEFAULT_SHEET_TITLE, entities, propNamesAndTitles, collectionalProps);
        final List<DataForWorkbookSheet<? extends AbstractEntity<?>>> sheetsData = new ArrayList<>();
        sheetsData.add(dataForWorkbookSheet);
        return export(sheetsData);
    }

    public static <M extends AbstractEntity<?>> HSSFWorkbook export(final Stream<M> entities, final String[] propertyNames, final String[] propertyTitles) {
        final List<T2<String, String>> propNamesAndTitles = new ArrayList<>();
        for (int index = 0; index < propertyNames.length && index < propertyTitles.length; index++) {
            propNamesAndTitles.add(t2(propertyNames[index], propertyTitles[index]));
        }
        
        final DataForWorkbookSheet<M> dataForWorkbookSheet = new DataForWorkbookSheet<>(DEFAULT_SHEET_TITLE, entities, propNamesAndTitles, new LinkedHashMap<>());
        final List<DataForWorkbookSheet<? extends AbstractEntity<?>>> sheetsData = new ArrayList<>();
        sheetsData.add(dataForWorkbookSheet);
        return export(sheetsData);
    }

    public static byte[] convertToGZipByteArray(final HSSFWorkbook workbook) throws IOException {
        final ByteArrayOutputStream oStream = new ByteArrayOutputStream();
        final GZipOutputStreamEx zOut = new GZipOutputStreamEx(oStream, Deflater.BEST_COMPRESSION);
        workbook.write(zOut);
        zOut.flush();
        zOut.close();
        oStream.flush();
        oStream.close();
        return oStream.toByteArray();
    }

    public static byte[] convertToByteArray(final HSSFWorkbook workbook) throws IOException {
        final ByteArrayOutputStream oStream = new ByteArrayOutputStream();
        workbook.write(oStream);
        oStream.flush();
        oStream.close();
        return oStream.toByteArray();
    }

    public static HSSFWorkbook export(final List<DataForWorkbookSheet<? extends AbstractEntity<?>>> sheetsData) {
        final HSSFWorkbook wb = new HSSFWorkbook();
        for (final DataForWorkbookSheet<? extends AbstractEntity<?>> sheetData : sheetsData) {
            addSheetWithData(wb, sheetData);
        }
        return wb;
    }

    private static <M extends AbstractEntity<?>> void addSheetWithData(final HSSFWorkbook wb, final DataForWorkbookSheet<M> sheetData) {
        final HSSFSheet sheet = wb.createSheet(sheetData.getSheetTitle());
        // Create a header row.
        final HSSFRow headerRow = sheet.createRow(0);
        // Create a new font and alter it
        final HSSFFont font = wb.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Courier New");
        font.setBold(true);
        // Fonts are set into a style so create a new one to use
        final HSSFCellStyle headerCellStyle = wb.createCellStyle();
        headerCellStyle.setFont(font);
        headerCellStyle.setBorderBottom(BorderStyle.THIN);
        headerCellStyle.setWrapText(true);
        final HSSFCellStyle headerInnerCellStyle = wb.createCellStyle();
        headerInnerCellStyle.setFont(font);
        headerInnerCellStyle.setBorderBottom(BorderStyle.THIN);
        headerInnerCellStyle.setBorderRight(BorderStyle.HAIR);
        headerInnerCellStyle.setWrapText(true);
        // Create cells and put column names there
        for (int index = 0; index < sheetData.getPropTitles().size(); index++) {
            final HSSFCell cell = headerRow.createCell(index);
            cell.setCellValue(sheetData.getPropTitles().get(index));
            cell.setCellStyle(index < sheetData.getPropTitles().size() - 1 ? headerInnerCellStyle : headerCellStyle);
        }

        final CellStyle dateCellStyle = wb.createCellStyle();
        final CreationHelper createHelper = wb.getCreationHelper();
        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy hh:mm"));

        // let's make cell style to handle borders
        final Map<String, String> shortCollectionalProps = new HashMap<>();
        final HSSFCellStyle dataCellStyle = wb.createCellStyle();
        dataCellStyle.setBorderRight(BorderStyle.HAIR);
        final AtomicInteger index = new AtomicInteger(0);
        sheetData.getEntities().limit(MAX_ROWS).forEach(entity -> addRow(index, entity, sheetData, sheet, dateCellStyle, shortCollectionalProps, dataCellStyle));

        // adjusting columns widths
        for (int propIndex = 0; propIndex < sheetData.getPropNames().size(); propIndex++) {
            sheet.autoSizeColumn(propIndex);
            final int newSize = (int) min(round(sheet.getColumnWidth(propIndex) * 1.05), MAX_COLUMN_WIDTH);
            sheet.setColumnWidth(propIndex, newSize);
        }

        // tripling first row height
        sheet.getRow(0).setHeight((short) (sheet.getRow(0).getHeight() * 3));

        // freezing first row
        sheet.createFreezePane(0, 1);
    }

    private static <M extends AbstractEntity<?>> void addRow(final AtomicInteger index, final M entity, final DataForWorkbookSheet<M> sheetData, final HSSFSheet sheet, final CellStyle dateCellStyle, final Map<String, String> shortCollectionalProps, final HSSFCellStyle dataCellStyle) {
        final HSSFRow row = sheet.createRow(index.incrementAndGet()); // new row starting with 1
        // iterate through values in the current table row and populate the sheet row
        for (int propIndex = 0; propIndex < sheetData.getPropNames().size(); propIndex++) {
            final HSSFCell cell = row.createCell(propIndex); // create new cell
            if (propIndex < sheetData.getPropNames().size() - 1) { // the last column should not have right border
                cell.setCellStyle(dataCellStyle);
            }
            final String propertyName = sheetData.getPropNames().get(propIndex);
            final Object value = StringUtils.isEmpty(propertyName) ? entity : sheetData.getValue(entity, propertyName); // get the value
            // need to try to do the best job with types
            if (shortCollectionalProps.containsKey(propertyName)) {
                cell.setCellType(CellType.STRING);
                cell.setCellValue(join(createShortColection((Collection<AbstractEntity<?>>) value, shortCollectionalProps.get(propertyName)), ", "));
            } else if (value instanceof Date) {
                cell.setCellValue((Date) value);
                cell.setCellStyle(dateCellStyle);
            } else if (value instanceof DateTime) {
                cell.setCellValue(((DateTime) value).toDate());
                cell.setCellStyle(dateCellStyle);
            } else if (value instanceof Number) {
                cell.setCellType(CellType.NUMERIC);
                cell.setCellValue(((Number) value).doubleValue());
            } else if (value instanceof Boolean) {
                cell.setCellType(CellType.BOOLEAN);
                cell.setCellValue((Boolean) value);
            } else if (value == null) { // if null then leave the cell blank
                cell.setCellType(CellType.BLANK);
            } else { // otherwise treat value as String
                cell.setCellType(CellType.STRING);
                if (EntityUtils.isCollectional(value.getClass())) {
                    final Optional<String> keyToInclude = findKeyToExclude((Collection<?>) value);
                    if (keyToInclude.isPresent()) {
                        shortCollectionalProps.put(propertyName, keyToInclude.get());
                        cell.setCellValue(join(createShortColection((Collection<AbstractEntity<?>>) value, keyToInclude.get()), ", "));
                    } else {
                        cell.setCellValue(join((Collection<?>) value, ", "));
                    }
                } else {
                    cell.setCellValue(value.toString());
                }
            }
        }
    }

    private static List<AbstractEntity<?>> createShortColection(final Collection<AbstractEntity<?>> collection, final String keyToInclude) {
        return collection.stream().map(entityElement -> (AbstractEntity<?>) entityElement.get(keyToInclude)).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private static Optional<String> findKeyToExclude(final Collection<?> collection) {
        return collection.stream()
                .filter(element -> element != null)
                .findFirst()
                .map(firstElem -> {
                    final Class<?> elementType = firstElem.getClass();
                    final boolean isShortCollection = EntityUtils.isEntityType(elementType) &&
                            EntityUtils.isCompositeEntity((Class<AbstractEntity<?>>) elementType) &&
                            Finder.getKeyMembers(elementType).size() == 2 &&
                            Finder.getKeyMembers(elementType).stream().allMatch(field -> EntityUtils.isEntityType(field.getType()));
                    if (isShortCollection) {
                        final AbstractEntity<?> firstEntity = (AbstractEntity<?>) firstElem;
                        final List<String> keyProps = Finder.getKeyMembers(elementType).stream().map(field -> field.getName()).collect(Collectors.toList());
                        final Object key1 = firstEntity.get(keyProps.get(0));
                        final Object key2 = firstEntity.get(keyProps.get(1));
                        if (collection.stream().filter(element -> element != null).allMatch(elem -> EntityUtils.equalsEx(((AbstractEntity<?>) elem).get(keyProps.get(0)), key1))) {
                            return keyProps.get(1);
                        } else if (collection.stream().filter(element -> element != null).allMatch(elem -> EntityUtils.equalsEx(((AbstractEntity<?>) elem).get(keyProps.get(1)), key2))) {
                            return keyProps.get(0);
                        } else {
                            return null;
                        }
                    }
                    return null;
                });
    }
}
