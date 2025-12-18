package ua.com.fielden.platform.file_reports;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.joda.time.DateTime;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity_centre.review.criteria.DynamicColumnForExport;
import ua.com.fielden.platform.serialisation.GZipOutputStreamEx;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.utils.StreamUtils;
import ua.com.fielden.platform.web.interfaces.IEntityMasterUrlProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.zip.Deflater;

import static java.lang.Math.min;
import static java.lang.Math.round;
import static org.apache.commons.lang3.StringUtils.join;
import static ua.com.fielden.platform.reflection.Finder.getKeyMembers;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.EntityUtils.*;

/// A set of utility methods for exporting data into MS Excel.
///
public class WorkbookExporter {

    private static final int MAX_COLUMN_WIDTH = 255 * 256;
    private static final String DEFAULT_SHEET_TITLE = "Exported data";
    private static final int SXSSF_WINDOW_SIZE = 1000;
    private static final int MAX_COUNT_OF_HYPERLINKS_IN_EXCEL = 65_530;

    private WorkbookExporter() {}

    /// Exports entities into a workbook.
    ///
    /// Only those properties whose names are specified in `propNamesAndTitles` are exported.
    ///
    /// @param entities  list of entity groups, each of which is assigned a separate sheet
    /// @param propNamesAndTitles  list of groups of property names and titles
    /// @param dynamicProperties  list of groups of dynamic properties
    /// @param sheetTitles  list of sheet titles
    ///
    public static <M extends AbstractEntity<?>> SXSSFWorkbook export(
            final List<Stream<M>> entities,
            final List<Pair<String[], String[]>> propNamesAndTitles,
            final List<List<List<DynamicColumnForExport>>> dynamicProperties,
            final List<String> sheetTitles,
            final IEntityMasterUrlProvider entityMasterUrlProvider)
    {
        final List<DataForWorkbookSheet<? extends AbstractEntity<?>>> sheetsData = new ArrayList<>();
        if (entities.size() == propNamesAndTitles.size() && entities.size() == dynamicProperties.size()) {
            for (int sheetIdx = 0; sheetIdx < entities.size(); sheetIdx++) {
                sheetsData.add(export(entities.get(sheetIdx), propNamesAndTitles.get(sheetIdx).getKey(), propNamesAndTitles.get(sheetIdx).getValue(), dynamicProperties.get(sheetIdx), sheetTitles.get(sheetIdx)));
            }
        }
        return export_(sheetsData, Optional.of(entityMasterUrlProvider));
    }

    /// Exports a group of entities into a sheet.
    ///
    private static <M extends AbstractEntity<?>> DataForWorkbookSheet<M> export(
            final Stream<M> entities,
            final String[] propertyNames,
            final String[] propertyTitles,
            final List<List<DynamicColumnForExport>> dynamicProperties,
            final String sheetTitle)
    {
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

    /// Exports entities into a workbook.
    /// This method specialises [#export(List,List,List,List,IEntityMasterUrlProvider)] for a single entity group.
    ///
    /// @param entityMasterUrlProvider  used to assign Entity Master hyperlinks
    ///
    public static <M extends AbstractEntity<?>> SXSSFWorkbook export(
            final Stream<M> entities,
            final String[] propertyNames,
            final String[] propertyTitles,
            final IEntityMasterUrlProvider entityMasterUrlProvider)
    {
        return export(entities, Stream.empty(), propertyNames, propertyTitles, Optional.of(entityMasterUrlProvider));
    }

    /// The same as [#export(Stream,String[],String[],IEntityMasterUrlProvider)], where hyperlinks are supplied
    /// by `hyperlinks` instead of [IEntityMasterUrlProvider].
    ///
    /// For each entity in `entities`, there should be an element in `hyperlinks` (their lengths should be equal).
    /// Alternatively, `hyperlinks` may be empty, in which case some default hyperlinks will be used.
    ///
    /// @param hyperlinks  a stream of maps with property names as keys and URLs as values
    ///
    public static <M extends AbstractEntity<?>> SXSSFWorkbook export(
            final Stream<M> entities,
            final Stream<Map<String, String>> hyperlinks,
            final String[] propertyNames,
            final String[] propertyTitles)
    {
        return export(entities, hyperlinks, propertyNames, propertyTitles, Optional.empty());
    }

    /// Equivalent to [#export(Stream,Stream,String[],String[])] but without the ability to specify hyperlinks.
    ///
    public static <M extends AbstractEntity<?>> SXSSFWorkbook export(final Stream<M> entities, final String[] propertyNames, final String[] propertyTitles) {
        return export(entities, Stream.empty(), propertyNames, propertyTitles, Optional.empty());
    }

    private static <M extends AbstractEntity<?>> SXSSFWorkbook export(
            final Stream<M> entities,
            final Stream<Map<String, String>> hyperlinks,
            final String[] propertyNames,
            final String[] propertyTitles,
            final Optional<IEntityMasterUrlProvider> maybeEntityMasterUrlProvider)
    {
        final List<T2<String, String>> propNamesAndTitles = new ArrayList<>();
        for (int index = 0; index < propertyNames.length && index < propertyTitles.length; index++) {
            propNamesAndTitles.add(t2(propertyNames[index], propertyTitles[index]));
        }

        final DataForWorkbookSheet<M> dataForWorkbookSheet = new DataForWorkbookSheet<>(DEFAULT_SHEET_TITLE, entities, propNamesAndTitles, new LinkedHashMap<>());
        final List<DataForWorkbookSheet<? extends AbstractEntity<?>>> sheetsData = new ArrayList<>();
        sheetsData.add(dataForWorkbookSheet);
        return export_(sheetsData, List.of(hyperlinks), maybeEntityMasterUrlProvider);
    }

    /// Converts `workbook` to a byte array of a zipped output.
    /// `workbook` is decated in try-with-resources to remove temporary files SXSSF creates to hold the data.
    ///
    public static byte[] convertToGZipByteArray(final SXSSFWorkbook workbook) throws IOException {
        try (workbook;
             final ByteArrayOutputStream oStream = new ByteArrayOutputStream();
             final GZipOutputStreamEx zOut = new GZipOutputStreamEx(oStream, Deflater.BEST_COMPRESSION))
        {
            workbook.write(zOut);
            zOut.flush();
            oStream.flush();
            return oStream.toByteArray();
        }
    }

    /// Converts `workbook` to a byte array.
    /// `workbook` is decated in try-with-resources to remove temporary files SXSSF creates to hold the data.
    ///
    public static byte[] convertToByteArray(final SXSSFWorkbook workbook) throws IOException {
        try (workbook;
             final ByteArrayOutputStream oStream = new ByteArrayOutputStream())
        {
            workbook.write(oStream);
            oStream.flush();
            return oStream.toByteArray();
        }
    }

    private static SXSSFWorkbook export_(
            final List<DataForWorkbookSheet<? extends AbstractEntity<?>>> sheetsData,
            final List<Stream<Map<String, String>>> propertiesToHyperlinksList,
            final Optional<IEntityMasterUrlProvider> maybeEntityMasterUrlProvider)
    {
        final SXSSFWorkbook wb = new SXSSFWorkbook(SXSSF_WINDOW_SIZE);

        for (int index = 0; index < sheetsData.size(); index++) {
            final var sheetData = sheetsData.get(index);
            final var propertiesToHyperlinks = propertiesToHyperlinksList.get(index);
            addSheetWithData(wb, sheetData, propertiesToHyperlinks, maybeEntityMasterUrlProvider);
        }

        return wb;
    }

    private static SXSSFWorkbook export_(
            final List<DataForWorkbookSheet<? extends AbstractEntity<?>>> sheetsData,
            final Optional<IEntityMasterUrlProvider> maybeEntityMasterUrlProvider)
    {
        return export_(sheetsData,
                       Stream. <Stream<Map<String, String>>> generate(Stream::empty).limit(sheetsData.size()).toList(),
                       maybeEntityMasterUrlProvider);
    }

    private static <M extends AbstractEntity<?>> void addSheetWithData(
            final SXSSFWorkbook wb,
            final DataForWorkbookSheet<M> sheetData,
            final Stream<Map<String, String>> propertiesToHyperlinks,
            final Optional<IEntityMasterUrlProvider> maybeEntityMasterUrlProvider)
    {
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
                cell.setCellValue(integerValue);
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

    /// Determines what entity properties should have hyperlinks associated with them, and where those hyperlinks should lead to.
    ///
    /// @return a map between property names, including dot-noted paths, and corresponding URLs.
    ///
    private static <M extends AbstractEntity<?>> Map<String, String> determinePropsForHyperlinks(final M entity, final DataForWorkbookSheet<M> sheetData, final Optional<IEntityMasterUrlProvider> maybeEntityMasterUrlProvider) {
        final var propsWithHyperlinks = new HashMap<String, String>();
        final var maybeMainEntityMaster = maybeEntityMasterUrlProvider.flatMap(g -> getMasterUrlFor(entity, g));

        if (maybeMainEntityMaster.isPresent()) { // If the URL for the given entity is present
            // If 'this' property is present, set the URL for it.
            if (sheetData.getPropNames().stream().anyMatch(StringUtils::isEmpty)) {
                propsWithHyperlinks.put("", maybeMainEntityMaster.get());
            }
            // If the URL has not been set for any property yet,
            // set it for the first key member present in the export data sheet.
            if (propsWithHyperlinks.isEmpty()) {
                getFirstPresentKeyProperty(entity.getType(), sheetData).ifPresent(presentKeyMember -> {
                    propsWithHyperlinks.put(presentKeyMember, maybeMainEntityMaster.get());
                });
            }
            // If the URL has not been set for any property yet,
            // set it for the first sub-key member of the key member
            // that is present in the export data sheet.
            if (propsWithHyperlinks.isEmpty()) {
                getFirstPresentSubKeyProperty(entity.getType(), sheetData).ifPresent(presentSubKeyMember -> {
                    propsWithHyperlinks.put(presentSubKeyMember, maybeMainEntityMaster.get());
                });
            }
        } else { // If the given entity has no associated URL
            // Find the first entity key member that is present in the export data
            // and has an entity master, then generate and set its URL.
            getFirstPresentKeyValue(entity, sheetData, maybeEntityMasterUrlProvider).ifPresent(presentKeyUrl -> {
                propsWithHyperlinks.put(presentKeyUrl._1, presentKeyUrl._2);
            });
            // If no key members are present in the export data,
            // find the first sub-key member of the first composite key member that has an entity master,
            // and then set the URL for that composite key member.
            if (propsWithHyperlinks.isEmpty()) {
                getFirstPresentSubKeyValue(entity, sheetData, maybeEntityMasterUrlProvider).ifPresent(presentKeyUrl -> {
                    propsWithHyperlinks.put(presentKeyUrl._1, presentKeyUrl._2);
                });
            }
        }
        return propsWithHyperlinks;
    }

    private static <M extends AbstractEntity<?>> Optional<T2<String, String>> getFirstPresentSubKeyValue(final M entity, final DataForWorkbookSheet<M> sheetData, final Optional<IEntityMasterUrlProvider> maybeEntityMasterUrlProvider) {
        return getKeyMembersWithMaster(entity, sheetData, maybeEntityMasterUrlProvider).stream()
                .<T2<String, String>>mapMulti((keyFieldPair, consumer) -> {
                    if (isCompositeEntity((Class<? extends AbstractEntity<?>>) keyFieldPair._1.getType())) {
                        getKeyMembers((Class<AbstractEntity<?>>)keyFieldPair._1.getType())
                                .stream()
                                .map(subKeyField -> keyFieldPair._1.getName() + "." + subKeyField.getName())
                                .filter(subKeyDotNotation -> isPropertyPresent(subKeyDotNotation, sheetData))
                                .findFirst().ifPresent(keyProp -> consumer.accept(T2.t2(keyProp, keyFieldPair._2)));
                    }
                })
                .findFirst();

    }

    private static <M extends AbstractEntity<?>> Optional<T2<String, String>> getFirstPresentKeyValue(final M entity, final DataForWorkbookSheet<M> sheetData, final Optional<IEntityMasterUrlProvider> maybeEntityMasterUrlProvider) {
        return getKeyMembersWithMaster(entity, sheetData, maybeEntityMasterUrlProvider).stream()
                .<T2<String, String>>mapMulti((t2, consumer) -> {
                    if (isPropertyPresent(t2._1.getName(), sheetData)) {
                        consumer.accept(T2.t2(t2._1.getName(), t2._2));
                    }
                }).findFirst();
    }

    private static <M extends AbstractEntity<?>> List<T2<Field, String>> getKeyMembersWithMaster(final M entity, final DataForWorkbookSheet<M> sheetData, final Optional<IEntityMasterUrlProvider> maybeEntityMasterUrlProvider) {
        return getKeyMembers(entity.getType()).stream()
                .<T2<Field, String>>mapMulti((keyField, consumer) -> {
                    if(isEntityType(keyField.getType()) && !entity.proxiedPropertyNames().contains(keyField.getName())) {
                        final var keyMemberValue = (AbstractEntity<?>) sheetData.getValue(entity, keyField.getName());
                        maybeEntityMasterUrlProvider.flatMap(g -> getMasterUrlFor(keyMemberValue, g))
                                .ifPresent(url -> consumer.accept(T2.t2(keyField, url)));
                    }
                })
                .toList();
    }

    private static <M extends AbstractEntity<?>> Optional<String> getFirstPresentSubKeyProperty(final Class<? extends AbstractEntity<?>> type, final DataForWorkbookSheet<M> sheetData) {
        return getKeyMembers(type).stream()
                .filter(keyField -> isEntityType(keyField.getType()) && isCompositeEntity((Class<? extends AbstractEntity<?>>) keyField.getType()))
                .flatMap(entityKeyFiled -> getKeyMembers((Class<AbstractEntity<?>>)entityKeyFiled.getType())
                        .stream()
                        .map(subKeyField -> entityKeyFiled.getName() + "." + subKeyField.getName()))
                .filter(subKeyDotNotation -> isPropertyPresent(subKeyDotNotation, sheetData))
                .findFirst();
    }

    private static <M extends AbstractEntity<?>> Optional<String> getFirstPresentKeyProperty(final Class<? extends AbstractEntity<?>> type, final DataForWorkbookSheet<M> sheetData) {
        return getKeyMembers(type).stream()
                .<String>mapMulti((keyField, consumer) -> {
                    if (isPropertyPresent(keyField.getName(), sheetData)) {
                        consumer.accept(keyField.getName());
                    }
                })
                .findFirst();
    }

    private static <M extends AbstractEntity<?>> boolean isPropertyPresent(String propertyName, final DataForWorkbookSheet<M> sheetData) {
        return sheetData.getPropNames().indexOf(propertyName) >= 0;
    }

    /// Returns the URL for the specified entity.
    /// This logic accounts for a one-2-one relationship where one entity acts as an extension of another and cannot exist independently.
    /// If the specified entity is part of such a relationship, the URL of the master entity is returned.
    /// Otherwise, the URL of the specified entity is returned.
    ///
    private static <M extends AbstractEntity<?>> Optional<String> getMasterUrlFor(final M entity, final IEntityMasterUrlProvider g) {
        if (entity != null && isOneToOne(entity.getType())) {
            return getMasterUrlFor((AbstractEntity<?>) entity.getKey(), g);
        }
        return g.masterUrlFor(entity);

    }

    private static List<? extends AbstractEntity<?>> createShortCollection(final Collection<AbstractEntity<?>> collection, final String keyToInclude) {
        return collection.stream().map(entityElement -> (AbstractEntity<?>) entityElement.get(keyToInclude)).toList();
    }

    @SuppressWarnings("unchecked")
    private static Optional<String> findKeyToExclude(final Collection<?> collection) {
        return collection.stream()
                .filter(Objects::nonNull)
                .findFirst()
                .map(firstElem -> {
                    final Class<?> elementType = firstElem.getClass();
                    final boolean isShortCollection = isEntityType(elementType) &&
                            isCompositeEntity((Class<AbstractEntity<?>>) elementType) &&
                            getKeyMembers((Class<? extends AbstractEntity<?>>) elementType).size() == 2 &&
                            getKeyMembers((Class<? extends AbstractEntity<?>>)elementType).stream().allMatch(field -> isEntityType(field.getType()));
                    if (isShortCollection) {
                        final AbstractEntity<?> firstEntity = (AbstractEntity<?>) firstElem;
                        final List<String> keyProps = getKeyMembers((Class<? extends AbstractEntity<?>>) elementType).stream().map(Field::getName).toList();
                        final Object key1 = firstEntity.get(keyProps.get(0));
                        final Object key2 = firstEntity.get(keyProps.get(1));
                        if (collection.stream().filter(Objects::nonNull).allMatch(elem -> EntityUtils.equalsEx(((AbstractEntity<?>) elem).get(keyProps.getFirst()), key1))) {
                            return keyProps.get(1);
                        } else if (collection.stream().filter(Objects::nonNull).allMatch(elem -> EntityUtils.equalsEx(((AbstractEntity<?>) elem).get(keyProps.get(1)), key2))) {
                            return keyProps.getFirst();
                        } else {
                            return null;
                        }
                    }
                    return null;
                });
    }

}
