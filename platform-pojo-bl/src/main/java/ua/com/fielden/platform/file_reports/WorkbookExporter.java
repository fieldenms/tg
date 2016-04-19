package ua.com.fielden.platform.file_reports;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.Deflater;

import org.apache.log4j.helpers.DateTimeDateFormat;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.joda.time.DateTime;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.serialisation.xstream.GZipOutputStreamEx;
import ua.com.fielden.platform.utils.Pair;

/**
 * A set of utility methods for exporting data into MS Excel.
 * 
 * @author TG Team
 *
 */
public class WorkbookExporter {

    public static <M extends AbstractEntity<?>> HSSFWorkbook export(final List<M> entities, final String[] propertyNames, final String[] propertyTitles) {
        final List<Pair<String, String>> propNamesAndTitles = new ArrayList<>();
        
        for (int index = 0; index < propertyNames.length && index < propertyTitles.length; index++) {
            propNamesAndTitles.add(new Pair<String, String>(propertyNames[index], propertyTitles[index]));
        }
        final DataForWorkbookSheet<M> dataForWorkbookSheet = new DataForWorkbookSheet<M>("Exported data", entities, propNamesAndTitles);
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
        for (DataForWorkbookSheet<? extends AbstractEntity<?>> sheetData : sheetsData) {
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
        font.setFontHeightInPoints((short) 12);
        font.setFontName("Courier New");
        font.setBoldweight((short) 1000);
        // Fonts are set into a style so create a new one to use
        final HSSFCellStyle headerCellStyle = wb.createCellStyle();
        headerCellStyle.setFont(font);
        headerCellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        final HSSFCellStyle headerInnerCellStyle = wb.createCellStyle();
        headerInnerCellStyle.setFont(font);
        headerInnerCellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        headerInnerCellStyle.setBorderRight(HSSFCellStyle.BORDER_HAIR);
        // Create cells and put column names there
        for (int index = 0; index < sheetData.getPropTitles().size(); index++) {
            final HSSFCell cell = headerRow.createCell(index);
            cell.setCellValue(sheetData.getPropTitles().get(index));
            cell.setCellStyle(index < sheetData.getPropTitles().size() - 1 ? headerInnerCellStyle : headerCellStyle);
        }

        // let's make cell style to handle borders
        final HSSFCellStyle dataCellStyle = wb.createCellStyle();
        dataCellStyle.setBorderRight(HSSFCellStyle.BORDER_HAIR);
        for (int index = 0; index < sheetData.getEntities().size(); index++) {
            final HSSFRow row = sheet.createRow(index + 1); // new row starting with 1
            // iterate through values in the current table row and populate the sheet row
            for (int propIndex = 0; propIndex < sheetData.getPropNames().size(); propIndex++) {
                final HSSFCell cell = row.createCell(propIndex); // create new cell
                if (propIndex < sheetData.getPropNames().size() - 1) { // the last column should not have right border
                    cell.setCellStyle(dataCellStyle);
                }
                final Object value = sheetData.getEntities().get(index).get(sheetData.getPropNames().get(propIndex)); // get the value
                // need to try to do the best job with types
                if (value instanceof Date || value instanceof DateTime) {
                    cell.setCellValue(DateTimeDateFormat.getDateTimeInstance().format(value));
                } else if (value instanceof Number) {
                    cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                    cell.setCellValue(((Number) value).doubleValue());
                } else if (value instanceof Boolean) {
                    cell.setCellType(HSSFCell.CELL_TYPE_BOOLEAN);
                    cell.setCellValue((Boolean) value);
                } else if (value == null) { // if null then leave call blank
                    cell.setCellType(HSSFCell.CELL_TYPE_BLANK);
                } else { // otherwise treat value as String
                    cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                    cell.setCellValue(value.toString());
                }
            }
        }
    }
}
