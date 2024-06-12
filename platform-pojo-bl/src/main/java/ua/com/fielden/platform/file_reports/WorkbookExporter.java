package ua.com.fielden.platform.file_reports;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.joda.time.DateTime;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity_centre.review.criteria.DynamicColumnForExport;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.serialisation.GZipOutputStreamEx;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.utils.StreamUtils;
import ua.com.fielden.platform.web.interfaces.IEntityMasterUrlProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.Deflater;

import static java.lang.Math.min;
import static java.lang.Math.round;
import static org.apache.commons.lang3.StringUtils.join;
import static ua.com.fielden.platform.types.tuples.T2.t2;

/**
 * A set of utility methods for exporting data into MS Excel.
 *
 * @author TG Team
 *
 */
public class WorkbookExporter {

    private static final int MAX_COLUMN_WIDTH = 255 * 256;
    private static final String DEFAULT_SHEET_TITLE = "Exported data";
    private static final int SXSSF_WINDOW_SIZE = 1000;
    private static final int MAX_COUNT_OF_HYPERLINKS_IN_EXCEL = 65_530;

    private WorkbookExporter() {}

    public static <M extends AbstractEntity<?>> SXSSFWorkbook export(final List<Stream<M>> entities, final List<Pair<String[], String[]>> propTitles, final List<List<List<DynamicColumnForExport>>> dynamicProperties, final List<String> sheetTitles, final IEntityMasterUrlProvider entityMasterUrlProvider) {
        final List<DataForWorkbookSheet<? extends AbstractEntity<?>>> sheetsData = new ArrayList<>();
        if (entities.size() == propTitles.size() && entities.size() == dynamicProperties.size()) {
            for (int sheetIdx = 0; sheetIdx < entities.size(); sheetIdx++) {
                sheetsData.add(export(entities.get(sheetIdx), propTitles.get(sheetIdx).getKey(), propTitles.get(sheetIdx).getValue(), dynamicProperties.get(sheetIdx), sheetTitles.get(sheetIdx)));
            }
        }
        return export(sheetsData, Stream.empty(), Optional.of(entityMasterUrlProvider));
    }

    private static <M extends AbstractEntity<?>> DataForWorkbookSheet<M> export(final Stream<M> entities, final String[] propertyNames, final String[] propertyTitles, final List<List<DynamicColumnForExport>> dynamicProperties, final String sheetTitle) {
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

        return new DataForWorkbookSheet<>(sheetTitle, entities, propNamesAndTitles, collectionalProps);
    }

    /**
     * Export {@code entities}. Only properties listed in {@code propertyNames} are exported with titles defined in {@code propertyTitles}.
     * If {@code entityMasterUrlProvider} is supplied, it is used to assign Entity Master hyperlinks.
     *
     * @param entities
     * @param propertyNames
     * @param propertyTitles
     * @param entityMasterUrlProvider
     * @return
     * @param <M>
     */
    public static <M extends AbstractEntity<?>> SXSSFWorkbook export(final Stream<M> entities, final String[] propertyNames, final String[] propertyTitles, final IEntityMasterUrlProvider entityMasterUrlProvider) {
        return export(entities, Stream.empty(), propertyNames, propertyTitles, Optional.of(entityMasterUrlProvider));
    }

    /**
     * The same as {@link #export(Stream, String[], String[], IEntityMasterUrlProvider)}, where hyperlinks are supplied by {@code hyperlinks} instead of {@link IEntityMasterUrlProvider}, which provides a map between property names and URLs.
     * There should be a correspondence between elements of {@code entities} and {@code hyperlinks}.
     *
     * @param entities
     * @param hyperlinks
     * @param propertyNames
     * @param propertyTitles
     * @return
     * @param <M>
     */
    public static <M extends AbstractEntity<?>> SXSSFWorkbook export(final Stream<M> entities, final Stream<Map<String, String>> hyperlinks, final String[] propertyNames, final String[] propertyTitles) {
        return export(entities, hyperlinks, propertyNames, propertyTitles, Optional.empty());
    }

    /**
     * Export without hyperlinks.
     *
     * @param entities
     * @param propertyNames
     * @param propertyTitles
     * @return
     * @param <M>
     */
    public static <M extends AbstractEntity<?>> SXSSFWorkbook export(final Stream<M> entities, final String[] propertyNames, final String[] propertyTitles) {
        return export(entities, Stream.empty(), propertyNames, propertyTitles, Optional.empty());
    }

    private static <M extends AbstractEntity<?>> SXSSFWorkbook export(final Stream<M> entities, final Stream<Map<String, String>> hyperlinks, final String[] propertyNames, final String[] propertyTitles, final Optional<IEntityMasterUrlProvider> maybeEntityMasterUrlProvider) {
        final List<T2<String, String>> propNamesAndTitles = new ArrayList<>();
        for (int index = 0; index < propertyNames.length && index < propertyTitles.length; index++) {
            propNamesAndTitles.add(t2(propertyNames[index], propertyTitles[index]));
        }

        final DataForWorkbookSheet<M> dataForWorkbookSheet = new DataForWorkbookSheet<>(DEFAULT_SHEET_TITLE, entities, propNamesAndTitles, new LinkedHashMap<>());
        final List<DataForWorkbookSheet<? extends AbstractEntity<?>>> sheetsData = new ArrayList<>();
        sheetsData.add(dataForWorkbookSheet);
        return export(sheetsData, hyperlinks, maybeEntityMasterUrlProvider);
    }

    /**
     * Converts {@code workbook} to a byte array of a zipped output. Disposes {@code workbook} to remove temporary files SXSSF creates to hold the data.
     *
     * @param workbook
     * @return
     * @throws IOException
     */
    public static byte[] convertToGZipByteArray(final SXSSFWorkbook workbook) throws IOException {
        try (final ByteArrayOutputStream oStream = new ByteArrayOutputStream();
             final GZipOutputStreamEx zOut = new GZipOutputStreamEx(oStream, Deflater.BEST_COMPRESSION)
        ) {
            workbook.write(zOut);
            zOut.flush();
            oStream.flush();
            return oStream.toByteArray();
        } finally {
            workbook.dispose();
        }
    }

    /**
     * Converts {@code workbook} to a byte array. Disposes {@code workbook} to remove temporary files SXSSF creates to hold the data.
     *
     * @param workbook
     * @return
     * @throws IOException
     */
    public static byte[] convertToByteArray(final SXSSFWorkbook workbook) throws IOException {
        try (final ByteArrayOutputStream oStream = new ByteArrayOutputStream()) {
            workbook.write(oStream);
            oStream.flush();
            return oStream.toByteArray();
        } finally {
            workbook.dispose();
        }
    }

    private static SXSSFWorkbook export(final List<DataForWorkbookSheet<? extends AbstractEntity<?>>> sheetsData, final Stream<Map<String, String>> propertiesToHyperlinks, final Optional<IEntityMasterUrlProvider> maybeEntityMasterUrlProvider) {
        final SXSSFWorkbook wb = new SXSSFWorkbook(SXSSF_WINDOW_SIZE);
        for (final DataForWorkbookSheet<? extends AbstractEntity<?>> sheetData : sheetsData) {
            addSheetWithData(wb, sheetData, propertiesToHyperlinks, maybeEntityMasterUrlProvider);
        }
        return wb;
    }

    private static <M extends AbstractEntity<?>> void addSheetWithData(final SXSSFWorkbook wb, final DataForWorkbookSheet<M> sheetData, final Stream<Map<String, String>> propertiesToHyperlinks, final Optional<IEntityMasterUrlProvider> maybeEntityMasterUrlProvider) {
        final SXSSFSheet sheet = wb.createSheet(sheetData.getSheetTitle());
        // Create a header row.
        final Row headerRow = sheet.createRow(0);
        // Create a new font and alter it
        final Font font = wb.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Courier New");
        font.setBold(true);
        // Fonts are set into a style so create a new one to use
        final CellStyle headerCellStyle = wb.createCellStyle();
        headerCellStyle.setFont(font);
        headerCellStyle.setBorderBottom(BorderStyle.THIN);
        headerCellStyle.setWrapText(true);
        final CellStyle headerInnerCellStyle = wb.createCellStyle();
        headerInnerCellStyle.setFont(font);
        headerInnerCellStyle.setBorderBottom(BorderStyle.THIN);
        headerInnerCellStyle.setBorderRight(BorderStyle.HAIR);
        headerInnerCellStyle.setWrapText(true);
        // Create cells and put column names there
        for (int index = 0; index < sheetData.getPropTitles().size(); index++) {
            final Cell cell = headerRow.createCell(index);
            cell.setCellValue(sheetData.getPropTitles().get(index));
            cell.setCellStyle(index < sheetData.getPropTitles().size() - 1 ? headerInnerCellStyle : headerCellStyle);
        }

        // tripling first row height
        sheet.getRow(0).setHeight((short) (sheet.getRow(0).getHeight() * 3));

        // freezing first row
        sheet.createFreezePane(0, 1);

        // define cell styles for different data types
        final CellStyle dateCellStyle = wb.createCellStyle();
        dateCellStyle.setDataFormat(wb.createDataFormat().getFormat("dd/mm/yyyy hh:mm"));

        final CellStyle integerCellStyle = wb.createCellStyle();
        integerCellStyle.setDataFormat((short) 3); // refer BuiltinFormats

        final CellStyle decimalCellStyle = wb.createCellStyle();
        decimalCellStyle.setDataFormat((short) 4); // refer BuiltinFormats

        final CellStyle moneyCellStyle = wb.createCellStyle();
        moneyCellStyle.setDataFormat((short) 8); // refer BuiltinFormats

        // let's make cell style to handle borders
        final CellStyle dataCellStyle = wb.createCellStyle();
        dataCellStyle.setBorderRight(BorderStyle.HAIR);

        final var rowIndex = new AtomicInteger(0);
        final var countHyperlinks = new AtomicInteger(0);
        final var helper = wb.getCreationHelper();
        final var cacheShortCollectionalProps = new HashMap<String, String>();
        // zip entities with corresponding stream of hyperlinks, while taking care of situations where not links are provided
        final Stream<T2<M, Map<String, String>>> entitiesMaybeWithHyperlinks = StreamUtils.zip(sheetData.getEntities(), StreamUtils.supplyIfEmpty(propertiesToHyperlinks, Collections::emptyMap), T2::t2);
        // and now let's export each entity with hyperlinks, if provided
        entitiesMaybeWithHyperlinks.forEach(entityMaybeWithHyperlinks -> addRow(rowIndex, entityMaybeWithHyperlinks, sheetData, wb, maybeEntityMasterUrlProvider, countHyperlinks, sheet, helper, cacheShortCollectionalProps, dateCellStyle, integerCellStyle, decimalCellStyle, moneyCellStyle, dataCellStyle));

        // adjusting columns widths
        for (int propIndex = 0; propIndex < sheetData.getPropNames().size(); propIndex++) {
            sheet.trackColumnForAutoSizing(propIndex);
            sheet.autoSizeColumn(propIndex);
            final int newSize = (int) min(round(sheet.getColumnWidth(propIndex) * 1.05), MAX_COLUMN_WIDTH);
            sheet.setColumnWidth(propIndex, newSize);
        }
    }

    private static <M extends AbstractEntity<?>> void addRow(
            final AtomicInteger index,
            final T2<M, Map<String, String>> entityMaybeWithHyperlinks,
            final DataForWorkbookSheet<M> sheetData,
            final SXSSFWorkbook wb,
            final Optional<IEntityMasterUrlProvider> maybeEntityMasterUrlProvider,
            final AtomicInteger countHyperlinks,
            final Sheet sheet,
            final CreationHelper helper,
            final Map<String, String> cacheShortCollectionalProps,
            final CellStyle dateCellStyle,
            final CellStyle integerCellStyle,
            final CellStyle decimalCellStyle,
            final CellStyle moneyCellStyle,
            final CellStyle dataCellStyle) {
        final Row row = sheet.createRow(index.incrementAndGet()); // new row starting with 1

        final M entity = entityMaybeWithHyperlinks._1;
        final var propsWithHyperlinks = entityMaybeWithHyperlinks._2.isEmpty() ? determinePropsForHyperlinks(entity, sheetData, maybeEntityMasterUrlProvider) : entityMaybeWithHyperlinks._2;

        // Iterate through values in the current table row, which correspond to entity properties, and populate the row
        for (int propIndex = 0; propIndex < sheetData.getPropNames().size(); propIndex++) {
            final Cell cell = row.createCell(propIndex); // create new cell
            if (propIndex < sheetData.getPropNames().size() - 1) { // the last column should not have right border
                cell.setCellStyle(dataCellStyle);
            }

            final String propertyName = sheetData.getPropNames().get(propIndex);
            final Object value = StringUtils.isEmpty(propertyName) ? entity : sheetData.getValue(entity, propertyName); // get the value

            // If the current property has a URL determined for it and the total number of cells with hyperlinks is less than the supported maximum, then provide the current cell with a hyperlink.
            if (propsWithHyperlinks.containsKey(propertyName) && countHyperlinks.get() < MAX_COUNT_OF_HYPERLINKS_IN_EXCEL) {
                final Hyperlink newLink = helper.createHyperlink(HyperlinkType.URL);
                newLink.setAddress(propsWithHyperlinks.get(propertyName));
                cell.setHyperlink(newLink);
                countHyperlinks.incrementAndGet();
            }

            // need to try to do the best job with types
            if (value == null) { // if null then leave the cell blank
                cell.setBlank();
            }
            else if (value instanceof AbstractEntity<?> entityValue) {
                cell.setCellValue(value.toString());
            }
            else if (value instanceof Date dateValue) {
                cell.setCellValue(dateValue);
                cell.setCellStyle(dateCellStyle);
            }
            else if (value instanceof DateTime dateTimeValue) {
                cell.setCellValue(dateTimeValue.toDate());
                cell.setCellStyle(dateCellStyle);
            }
            else if (value instanceof Integer integerValue) {
                cell.setCellValue(integerValue.intValue());
                cell.setCellStyle(integerCellStyle);
            }
            else if (value instanceof Number numberValue) { // covers BigDecimal
                cell.setCellValue(numberValue.doubleValue());
                cell.setCellStyle(decimalCellStyle);
            }
            else if (value instanceof Money moneyValue) {
                cell.setCellValue(moneyValue.getAmount().doubleValue());
                cell.setCellStyle(moneyCellStyle);
            }
            else if (value instanceof Boolean booleanValue) {
                cell.setCellValue(booleanValue);
            }
            // Initially cacheShortCollectionalProps is empty, but it gets populated when processing collectional properties, so that the result of that processing could be reused.
            // This cache check should be done before the processing of collectional properties, where it gets populated.
            else if (cacheShortCollectionalProps.containsKey(propertyName)) {
                cell.setCellValue(join(createShortCollection((Collection<AbstractEntity<?>>) value, cacheShortCollectionalProps.get(propertyName)), ", "));
            }
            else if (EntityUtils.isCollectional(value.getClass())) {
                // Collectional values get exported by concatenating their elements into a comma separated string.
                // Special treatment is required to short-collectional values, where the property representing "one" in the one-2-many relationship needs to be excluded from the final string representation.
                // In addition, if value recognised as short-collectional, the property and the result of its processing is cached as cacheShortCollectionalProps to avoid repeated computations of other entity instances that are being exported.
                final Optional<String> keyToInclude = findKeyToExclude((Collection<?>) value);
                if (keyToInclude.isPresent()) {
                    cacheShortCollectionalProps.put(propertyName, keyToInclude.get());
                    cell.setCellValue(join(createShortCollection((Collection<AbstractEntity<?>>) value, keyToInclude.get()), ", "));
                } else {
                    cell.setCellValue(join((Collection<?>) value, ", "));
                }
            }
            // Otherwise, let's treat the value as string
            else {
                cell.setCellValue(value.toString());
            }
        }
    }

    /**
     * Determines what entity properties should have hyperlinks associated with them, and those hyperlinks should lead to.
     *
     * @param entity
     * @param sheetData
     * @param maybeEntityMasterUrlProvider
     * @return a map between property names, including dot-noted paths, and corresponding URLs.
     * @param <M>
     */
    private static <M extends AbstractEntity<?>> Map<String, String> determinePropsForHyperlinks(final M entity, final DataForWorkbookSheet<M> sheetData, final Optional<IEntityMasterUrlProvider> maybeEntityMasterUrlProvider) {
        final var propsWithHyperlinks = new HashMap<String, String>();
        final var thisPresent = sheetData.getPropNames().stream().anyMatch(StringUtils::isEmpty);
        final Optional<String> maybeMainEntityMaster = maybeEntityMasterUrlProvider.flatMap(g -> g.masterUrlFor(entity));
        // If the main entity has a master, that master should be used for the hyperlinks.
        if (maybeMainEntityMaster.isPresent()) {
            final var url = maybeMainEntityMaster.get();
            if (thisPresent) {
                propsWithHyperlinks.put("", url);
            }
            // key or composite key members should be associated with the main hyperlink
            else {
                final var entityType = entity.getType();
                Finder.getKeyMembers(entityType).stream().forEach(field -> {
                    final var keyMemberPropName = field.getName();
                    propsWithHyperlinks.put(keyMemberPropName, url);
                    // key members can be composite themselves, which would most likely result in their key members present in the result set
                    // if that is the case, we should associate the main hyperlink with those key members also
                    if (EntityUtils.isEntityType(field.getType()) && EntityUtils.isCompositeEntity((Class<? extends AbstractEntity<?>>) field.getType())) {
                        final Class<? extends AbstractEntity<?>> fieldTypeAsEntity = (Class<? extends AbstractEntity<?>>) field.getType();
                        Finder.getKeyMembers(fieldTypeAsEntity).stream().forEach(subKeyMemberField -> propsWithHyperlinks.put(keyMemberPropName + "." + subKeyMemberField.getName(), url));
                    }
                });
            }
        }
        // Otherwise, create hyperlinks for entity-typed key or key members.
        else {
            final var entityType = entity.getType();
            Finder.getKeyMembers(entityType).stream()
                  .forEach(field -> {
                      if (EntityUtils.isEntityType(field.getType()) && !entity.proxiedPropertyNames().contains(field.getName())) {
                          final AbstractEntity<?> keyMemberValue = (AbstractEntity<?>) sheetData.getValue(entity, field.getName());
                          maybeEntityMasterUrlProvider.flatMap(g -> g.masterUrlFor(keyMemberValue)).ifPresent(url -> {
                              final var keyMemberPropName = field.getName();
                              propsWithHyperlinks.put(keyMemberPropName, url);
                              // key members can be composite themselves, which would most likely result in their key members present in the result set
                              // if that is the case, we should associate those key members with the URL also
                              if (EntityUtils.isEntityType(field.getType()) && EntityUtils.isCompositeEntity((Class<? extends AbstractEntity<?>>) field.getType())) {
                                  final Class<? extends AbstractEntity<?>> fieldTypeAsEntity = (Class<? extends AbstractEntity<?>>) field.getType();
                                  Finder.getKeyMembers(fieldTypeAsEntity).stream().forEach(subKeyMemberField -> propsWithHyperlinks.put(keyMemberPropName + "." + subKeyMemberField.getName(), url));
                              }
                          });
                      }
                  });
        }
        return propsWithHyperlinks;
    }

    private static List<? extends AbstractEntity<?>> createShortCollection(final Collection<AbstractEntity<?>> collection, final String keyToInclude) {
        return collection.stream().map(entityElement -> (AbstractEntity<?>) entityElement.get(keyToInclude)).toList();
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
                            Finder.getKeyMembers((Class<? extends AbstractEntity<?>>) elementType).size() == 2 &&
                            Finder.getKeyMembers((Class<? extends AbstractEntity<?>>)elementType).stream().allMatch(field -> EntityUtils.isEntityType(field.getType()));
                    if (isShortCollection) {
                        final AbstractEntity<?> firstEntity = (AbstractEntity<?>) firstElem;
                        final List<String> keyProps = Finder.getKeyMembers((Class<? extends AbstractEntity<?>>) elementType).stream().map(field -> field.getName()).collect(Collectors.toList());
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
