package ua.com.fielden.platform.web.centre;

import static java.util.Optional.empty;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.AND_BEFORE;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.DATE_MNEMONIC;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.DATE_PREFIX;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.EXCLUSIVE;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.EXCLUSIVE2;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.NOT;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.OR_NULL;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.VALUE;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.VALUE2;
import static ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancerCache.CACHE;
import static ua.com.fielden.platform.serialisation.api.SerialiserEngines.JACKSON;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.web.centre.CentreUpdater.applyDifferences;
import static ua.com.fielden.platform.web.centre.CentreUpdater.createDifferences;
import static ua.com.fielden.platform.web.centre.CentreUpdater.createEmptyDifferences;
import static ua.com.fielden.platform.web.centre.CentreUpdater.propDiff;
import static ua.com.fielden.platform.web.centre.EntityCentre.createDefaultCentreFrom;
import static ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.options.DefaultValueOptions.multi;
import static ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.options.DefaultValueOptions.range;
import static ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder.centreFor;
import static ua.com.fielden.platform.web.interfaces.ILayout.Device.DESKTOP;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutComposer.mkGridForCentre;
import static ua.com.fielden.snappy.DateRangePrefixEnum.NEXT;
import static ua.com.fielden.snappy.DateRangePrefixEnum.PREV;
import static ua.com.fielden.snappy.MnemonicEnum.MONTH;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.joda.time.DateTime;
import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.testing.ClassProviderForTestingPurposes;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.proxy.IIdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.sample.domain.TgCentreDiffSerialisation;
import ua.com.fielden.platform.sample.domain.TgCentreDiffSerialisationPersistentChild;
import ua.com.fielden.platform.serialisation.api.ISerialisationTypeEncoder;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.impl.IdOnlyProxiedEntityTypeCacheForTests;
import ua.com.fielden.platform.serialisation.api.impl.SerialisationTypeEncoder;
import ua.com.fielden.platform.serialisation.api.impl.SerialiserForDomainTreesTestingPurposes;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.ui.menu.sample.MiTgCentreDiffSerialisation;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;

/**
 * Unit tests for {@link CentreUpdater} API methods, particularly for
 * <p>
 * 1. {@link CentreUpdater#createDifferences(ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer, ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer, Class)}<br>
 * 2. {@link CentreUpdater#applyDifferences(ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer, java.util.Map, Class)}
 * 
 * @author TG Team
 *
 */
public class CentreUpdaterTest {
    private static final Class<?> ROOT_GENERIC = TgCentreDiffSerialisation.class;
    static final Class<AbstractEntity<?>> ROOT = (Class<AbstractEntity<?>>) ROOT_GENERIC;
    private static final Class<? extends MiWithConfigurationSupport<?>> MI_TYPE = MiTgCentreDiffSerialisation.class;
    private static final EntityModuleWithPropertyFactory MODULE = new CommonTestEntityModuleWithPropertyFactory();
    private static final Injector INJECTOR = new ApplicationInjectorFactory().add(MODULE).getInjector();
    private static final EntityFactory FACTORY = INJECTOR.getInstance(EntityFactory.class);
    private static final ISerialiser SERIALISER = new SerialiserForDomainTreesTestingPurposes(FACTORY, new ClassProviderForTestingPurposes(), CACHE);
    private static final ISerialisationTypeEncoder serialisationTypeEncoder = new SerialisationTypeEncoder();
    private static final IIdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache = new IdOnlyProxiedEntityTypeCacheForTests();
    private static final Date d2018 = new DateTime(2018, 1, 1, 0, 0).toDate();
    private static final Date d2018_time = new DateTime(2018, 1, 1, 5, 6).toDate();
    private static final Date d2019 = new DateTime(2019, 1, 1, 0, 0).toDate();
    private static final Date d2019_time = new DateTime(2019, 1, 1, 5, 6).toDate();
    private static final Date d2020 = new DateTime(2020, 1, 1, 0, 0).toDate();
    private static final EntityCentreConfig<TgCentreDiffSerialisation> DSL_CONFIG = 
        centreFor(TgCentreDiffSerialisation.class)
        .addCrit("stringProp").asMulti().text().setDefaultValue(multi().string().not().setValues("A*", "B*").canHaveNoValue().value()).also()
        .addCrit("dateProp").asRange().dateTime().also()
        .addCrit("datePropDefault").asRange().dateTime().setDefaultValue(range().date().not().setFromValueExclusive(d2018).setToValueExclusive(d2019).canHaveNoValue().value()).also()
        .addCrit("datePropDefaultMnemonics").asRange().dateTime().setDefaultValue(range().date().next().monthAndBefore().value()).also()
        .addCrit("datePropCrit").asRange().dateTime().also()
        .addCrit("datePropCritSingle").asSingle().dateTime().also()
        .addCrit("datePropUtc").asRange().dateTime().also()
        .addCrit("datePropDateOnly").asRange().date().also()
        .addCrit("datePropTimeOnly").asRange().time().also()
        .addCrit("entityProp").asMulti().autocompleter(TgCentreDiffSerialisationPersistentChild.class).also()
        .addCrit("entityPropDefault").asMulti().autocompleter(TgCentreDiffSerialisationPersistentChild.class).setDefaultValue(multi().string().setValues("0*", "*1").value()).also()
        .addCrit("entityPropCrit").asMulti().autocompleter(TgCentreDiffSerialisationPersistentChild.class).also()
        .addCrit("entityPropCritSingle").asSingle().autocompleter(TgCentreDiffSerialisationPersistentChild.class).also()
        .addCrit("propertyDescriptorProp").asMulti().autocompleter(PropertyDescriptor.class).also()
        .addCrit("propertyDescriptorPropCrit").asMulti().autocompleter(PropertyDescriptor.class).also()
        .addCrit("propertyDescriptorPropCritSingle").asSingle().autocompleter(PropertyDescriptor.class)
        .setLayoutFor(DESKTOP, empty(), mkGridForCentre(7, 2))
        .addProp("stringProp")
        .build();
    static {
        SERIALISER.initJacksonEngine(serialisationTypeEncoder, idOnlyProxiedEntityTypeCache);
    }
    
    /**
     * Creates sampled version of centre and extracts diff.<br>
     * Serialises and deserialises the diff.<br>
     * Checks resultant diff against <code>expectedDiff</code>.
     * 
     * @param defaultCentreCreator -- function to create default centres
     * @param centreMutator -- mutation function that provides changes to the centre being tested (comparing to default centre)
     * @param expectedDiff -- expected diff object after extracting diff from centre being tested
     * @return -- resultant centre and diff
     */
    private static T2<ICentreDomainTreeManagerAndEnhancer, Map<String, Object>> testDiffCreation(
        final Supplier<ICentreDomainTreeManagerAndEnhancer> defaultCentreCreator,
        final Consumer<ICentreDomainTreeManagerAndEnhancer> centreMutator,
        final Map<String, Object> expectedDiff) {
        
        // create centre
        final ICentreDomainTreeManagerAndEnhancer centre = defaultCentreCreator.get();
        // and apply concrete mutations
        centreMutator.accept(centre);
        
        // create default centre
        final ICentreDomainTreeManagerAndEnhancer defaultCentre = defaultCentreCreator.get();
        
        // create diff
        final Map<String, Object> diff = createDifferences(centre, defaultCentre, ROOT);
        
        // serialise and deserialise
        final byte[] serialisedDiff = SERIALISER.serialise(diff, JACKSON);
        final Map<String, Object> deserialisedDiff = SERIALISER.deserialise(serialisedDiff, LinkedHashMap.class, JACKSON);
        
        // check expected diff against original diff
        assertEquals(expectedDiff.toString(), diff.toString());
        assertEquals(expectedDiff, diff); // TODO check whether assertEquals is sufficient or whether some "deep equals" should be envisaged
        
        // check expected diff against deserialised diff
        assertEquals(expectedDiff.toString(), deserialisedDiff.toString());
        assertEquals(expectedDiff, deserialisedDiff); // TODO check whether assertEquals is sufficient or whether some "deep equals" should be envisaged
        
        return t2(centre, deserialisedDiff);
    }
    
    /**
     * Creates default centre and applies diff.<br>
     * Checks resultant centre against expected centre.
     * 
     * @param defaultCentreCreator -- function to create default centres
     * @param expectedCentreAndDiff -- a pair of expected centre and a diff
     * @param companionFinder
     */
    private static void testDiffApplication(
        final Supplier<ICentreDomainTreeManagerAndEnhancer> defaultCentreCreator,
        final T2<ICentreDomainTreeManagerAndEnhancer, Map<String, Object>> expectedCentreAndDiff,
        final ICompanionObjectFinder companionFinder) {
        
        // create default centre
        final ICentreDomainTreeManagerAndEnhancer defaultCentre = defaultCentreCreator.get();
        
        // apply sampled diff
        final ICentreDomainTreeManagerAndEnhancer appliedCentre = applyDifferences(defaultCentre, expectedCentreAndDiff._2, ROOT, companionFinder);
        
        // check expected centre against appliedCentre
        assertEquals(expectedCentreAndDiff._1, appliedCentre); // TODO check whether assertEquals is sufficient or whether some "deep equals" should be envisaged
    }
    
    /**
     * Creates sampled version of centre and extracts diff.<br>
     * Serialises and deserialises the diff.<br>
     * Checks resultant diff against <code>expectedDiff</code>.<br>
     * Creates default centre and applies resultant diff.<br>
     * Checks resultant centre against originally created sampled version of centre.
     * 
     * @param defaultCentreCreator -- function to create default centres
     * @param centreMutator -- mutation function that provides changes to the centre being tested (comparing to default centre)
     * @param expectedDiff -- expected diff object after extracting diff from centre being tested
     * @param companionFinder
     */
    public static void testDiffCreationAndApplication(
        final Supplier<ICentreDomainTreeManagerAndEnhancer> defaultCentreCreator,
        final Consumer<ICentreDomainTreeManagerAndEnhancer> centreMutator,
        final Map<String, Object> expectedDiff,
        final ICompanionObjectFinder companionFinder) {
        
        // test diff creation and application
        testDiffApplication(defaultCentreCreator, testDiffCreation(defaultCentreCreator, centreMutator, expectedDiff), companionFinder);
    }
    
    /**
     * Creates sampled version of centre and extracts diff.<br>
     * Serialises and deserialises the diff.<br>
     * Checks resultant diff against <code>expectedDiff</code>.<br>
     * Creates default centre and applies resultant diff.<br>
     * Checks resultant centre against originally created sampled version of centre.
     * <p>
     * This method version should be used only for the cases where crit-only single entity typed values are not needed for testing.
     * 
     * @param defaultCentreCreator -- function to create default centres
     * @param centreMutator -- mutation function that provides changes to the centre being tested (comparing to default centre)
     * @param expectedDiff -- expected diff object after extracting diff from centre being tested
     */
    private static void testDiffCreationAndApplication(
        final Supplier<ICentreDomainTreeManagerAndEnhancer> defaultCentreCreator,
        final Consumer<ICentreDomainTreeManagerAndEnhancer> centreMutator,
        final Map<String, Object> expectedDiff) {
        testDiffCreationAndApplication(defaultCentreCreator, centreMutator, expectedDiff, null);
    }
    
    static ICentreDomainTreeManagerAndEnhancer create() {
        return createDefaultCentreFrom(DSL_CONFIG, SERIALISER, centre -> centre, true, TgCentreDiffSerialisation.class, CACHE, MI_TYPE, INJECTOR);
    }
    
    public static Map<String, Object> expectedDiffWithValue(final String property, final String category, final Object value) {
        final Map<String, Object> expectedDiff = createEmptyDifferences();
        propDiff(property, expectedDiff).put(category, value);
        return expectedDiff;
    }
    
    public static Map<String, Object> expectedDiffWithValues(final String property, final T2<String, Object> ... categoryAndValues) {
        final Map<String, Object> expectedDiff = createEmptyDifferences();
        for (final T2<String, Object> categoryAndValue: categoryAndValues) {
            propDiff(property, expectedDiff).put(categoryAndValue._1, categoryAndValue._2);
        }
        return expectedDiff;
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    @Test
    public void no_mutations() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> {}, createEmptyDifferences());
    }
    
    @Test
    public void mutate_2_the_same_values() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> {
            centre.getFirstTick().setOrNull(ROOT, "datePropDefault", true);
            centre.getFirstTick().setNot(ROOT, "datePropDefault", true);
            centre.getFirstTick().setExclusive(ROOT, "datePropDefault", true);
            centre.getFirstTick().setExclusive2(ROOT, "datePropDefault", true);
            centre.getFirstTick().setDatePrefix(ROOT, "datePropDefaultMnemonics", NEXT);
            centre.getFirstTick().setDateMnemonic(ROOT, "datePropDefaultMnemonics", MONTH);
            centre.getFirstTick().setAndBefore(ROOT, "datePropDefaultMnemonics", true);
            centre.getFirstTick().setValue(ROOT, "datePropDefault", d2018);
        }, createEmptyDifferences());
    }
    
    // missing value
    
    @Test
    public void missing_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setOrNull(ROOT, "dateProp", true), expectedDiffWithValue("dateProp", OR_NULL.name(), true));
    }
    
    @Test
    public void default_missing_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setOrNull(ROOT, "datePropDefault", null), expectedDiffWithValue("datePropDefault", OR_NULL.name(), null));
    }
    
    // negation
    
    @Test
    public void negation() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setNot(ROOT, "dateProp", true), expectedDiffWithValue("dateProp", NOT.name(), true));
    }
    
    @Test
    public void default_negation() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setNot(ROOT, "datePropDefault", null), expectedDiffWithValue("datePropDefault", NOT.name(), null));
    }
    
    // exclusiveness
    
    @Test
    public void left_exclusive() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setExclusive(ROOT, "dateProp", true), expectedDiffWithValue("dateProp", EXCLUSIVE.name(), true));
    }
    
    @Test
    public void default_left_exclusive() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setExclusive(ROOT, "datePropDefault", null), expectedDiffWithValue("datePropDefault", EXCLUSIVE.name(), null));
    }
    
    @Test
    public void right_exclusive() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setExclusive2(ROOT, "dateProp", true), expectedDiffWithValue("dateProp", EXCLUSIVE2.name(), true));
    }
    
    @Test
    public void default_right_exclusive() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setExclusive2(ROOT, "datePropDefault", null), expectedDiffWithValue("datePropDefault", EXCLUSIVE2.name(), null));
    }
    
    // date prefixes and mnemonics
    
    @Test
    public void date_prefix_and_mnemonic() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> {
            centre.getFirstTick().setDatePrefix(ROOT, "dateProp", PREV);
            centre.getFirstTick().setDateMnemonic(ROOT, "dateProp", MONTH);
        }, expectedDiffWithValues("dateProp", t2(DATE_PREFIX.name(), PREV.name()), t2(DATE_MNEMONIC.name(), MONTH.name())));
    }
    
    @Test
    public void default_date_prefix_and_mnemonic_2_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> {
            centre.getFirstTick().setDatePrefix(ROOT, "datePropDefaultMnemonics", null);
            centre.getFirstTick().setDateMnemonic(ROOT, "datePropDefaultMnemonics", null);
        }, expectedDiffWithValues("datePropDefaultMnemonics", t2(DATE_PREFIX.name(), null), t2(DATE_MNEMONIC.name(), null)));
    }
    
    @Test
    public void default_date_prefix_and_mnemonic_2_non_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> {
            centre.getFirstTick().setDatePrefix(ROOT, "datePropDefaultMnemonics", PREV); // previous value: NEXT MONTH AND BEFORE 
        }, expectedDiffWithValue("datePropDefaultMnemonics", DATE_PREFIX.name(), PREV.name())); // new value: PREV MONTH AND BEFORE 
    }
    
    // date "and before"
    
    @Test
    public void date_AND_BEFORE_2_true() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> {
            centre.getFirstTick().setDatePrefix(ROOT, "dateProp", PREV);
            centre.getFirstTick().setDateMnemonic(ROOT, "dateProp", MONTH);
            centre.getFirstTick().setAndBefore(ROOT, "dateProp", true);
        }, expectedDiffWithValues("dateProp", t2(DATE_PREFIX.name(), PREV.name()), t2(DATE_MNEMONIC.name(), MONTH.name()), t2(AND_BEFORE.name(), true)));
    }
    
    @Test
    public void date_AND_BEFORE_2_false() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> {
            centre.getFirstTick().setDatePrefix(ROOT, "dateProp", PREV);
            centre.getFirstTick().setDateMnemonic(ROOT, "dateProp", MONTH);
            centre.getFirstTick().setAndBefore(ROOT, "dateProp", false);
        }, expectedDiffWithValues("dateProp", t2(DATE_PREFIX.name(), PREV.name()), t2(DATE_MNEMONIC.name(), MONTH.name()), t2(AND_BEFORE.name(), false)));
    }
    
    @Test
    public void default_date_AND_BEFORE_2_false() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> {
            centre.getFirstTick().setAndBefore(ROOT, "datePropDefaultMnemonics", false); // previous value: NEXT MONTH AND BEFORE 
        }, expectedDiffWithValue("datePropDefaultMnemonics", AND_BEFORE.name(), false)); // new value: NEXT MONTH AND AFTER
    }
    
    @Test
    public void default_date_AND_BEFORE_2_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> {
            centre.getFirstTick().setAndBefore(ROOT, "datePropDefaultMnemonics", null); // previous value: NEXT MONTH AND BEFORE 
        }, expectedDiffWithValue("datePropDefaultMnemonics", AND_BEFORE.name(), null)); // new value: NEXT MONTH
    }
    
    // date values
    
    @Test
    public void left_date_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "dateProp", d2018), expectedDiffWithValue("dateProp", VALUE.name(), Long.toString(d2018.getTime())));
    }
    
    @Test
    public void default_left_date_value_2_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "datePropDefault", null), expectedDiffWithValue("datePropDefault", VALUE.name(), null));
    }
    
    @Test
    public void default_left_date_value_2_non_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "datePropDefault", d2019), expectedDiffWithValue("datePropDefault", VALUE.name(), Long.toString(d2019.getTime())));
    }
    
    @Test
    public void left_critOnly_date_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "datePropCrit", d2018), expectedDiffWithValue("datePropCrit", VALUE.name(), Long.toString(d2018.getTime())));
    }
    
    @Test
    public void left_critOnlySingle_date_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "datePropCritSingle", d2018), expectedDiffWithValue("datePropCritSingle", VALUE.name(), Long.toString(d2018.getTime())));
    }
    
    @Test
    public void left_UTC_date_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "datePropUtc", d2018), expectedDiffWithValue("datePropUtc", VALUE.name(), Long.toString(d2018.getTime())));
    }
    
    @Test
    public void left_dateOnly_date_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "datePropDateOnly", d2018), expectedDiffWithValue("datePropDateOnly", VALUE.name(), Long.toString(d2018.getTime())));
    }
    
    @Test
    public void left_timeOnly_date_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "datePropTimeOnly", d2018_time), expectedDiffWithValue("datePropTimeOnly", VALUE.name(), Long.toString(d2018_time.getTime())));
    }
    
    @Test
    public void right_date_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue2(ROOT, "dateProp", d2018), expectedDiffWithValue("dateProp", VALUE2.name(), Long.toString(d2018.getTime())));
    }
    
    @Test
    public void default_right_date_value_2_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue2(ROOT, "datePropDefault", null), expectedDiffWithValue("datePropDefault", VALUE2.name(), null));
    }
    
    @Test
    public void default_right_date_value_2_non_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue2(ROOT, "datePropDefault", d2020), expectedDiffWithValue("datePropDefault", VALUE2.name(), Long.toString(d2020.getTime())));
    }
    
    @Test
    public void right_critOnly_date_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue2(ROOT, "datePropCrit", d2019), expectedDiffWithValue("datePropCrit", VALUE2.name(), Long.toString(d2019.getTime())));
    }
    
    @Test
    public void right_UTC_date_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue2(ROOT, "datePropUtc", d2019), expectedDiffWithValue("datePropUtc", VALUE2.name(), Long.toString(d2019.getTime())));
    }
    
    @Test
    public void right_dateOnly_date_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue2(ROOT, "datePropDateOnly", d2019), expectedDiffWithValue("datePropDateOnly", VALUE2.name(), Long.toString(d2019.getTime())));
    }
    
    @Test
    public void right_timeOnly_date_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue2(ROOT, "datePropTimeOnly", d2019_time), expectedDiffWithValue("datePropTimeOnly", VALUE2.name(), Long.toString(d2019_time.getTime())));
    }
    
    // please note that DateTime-typed properties is not used in practice and thus will not be tested and supported (however, see EntityWithDateTimeProp / WorkbookExporterTest and EntityWithRangeProperties for [perhaps] artificial examples of such properties)
    
    // entity values
    
    @Test
    public void entity_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "entityProp", listOf("A*", "*B")), expectedDiffWithValue("entityProp", VALUE.name(), listOf("A*", "*B")));
    }
    
    @Test
    public void default_entity_value_2_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "entityPropDefault", listOf()), expectedDiffWithValue("entityPropDefault", VALUE.name(), listOf()));
    }
    
    @Test
    public void default_entity_value_2_non_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "entityPropDefault", listOf("A*", "*B")), expectedDiffWithValue("entityPropDefault", VALUE.name(), listOf("A*", "*B")));
    }
    
    @Test
    public void critOnly_entity_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "entityPropCrit", listOf("A*", "*B")), expectedDiffWithValue("entityPropCrit", VALUE.name(), listOf("A*", "*B")));
    }
    
    @Test
    public void propertyDescriptor_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "propertyDescriptorProp", listOf("A*", "*B")), expectedDiffWithValue("propertyDescriptorProp", VALUE.name(), listOf("A*", "*B")));
    }
    
    @Test
    public void critOnly_propertyDescriptor_value() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "propertyDescriptorPropCrit", listOf("A*", "*B")), expectedDiffWithValue("propertyDescriptorPropCrit", VALUE.name(), listOf("A*", "*B")));
    }
    
    @Test
    public void critOnlySingle_propertyDescriptor_value() {
        final PropertyDescriptor<TgCentreDiffSerialisationPersistentChild> propertyVal = new PropertyDescriptor<TgCentreDiffSerialisationPersistentChild>(TgCentreDiffSerialisationPersistentChild.class, "stringProp");
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "propertyDescriptorPropCritSingle", propertyVal), expectedDiffWithValue("propertyDescriptorPropCritSingle", VALUE.name(), propertyVal.toString()));
    }
    
//    @Test
//    public void left_UTC_date_value() {
//        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "datePropUtc", d2018), expectedDiffWithValue("datePropUtc", VALUE.name(), d2018.getTime()));
//    }
//    
//    @Test
//    public void left_dateOnly_date_value() {
//        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "datePropDateOnly", d2018), expectedDiffWithValue("datePropDateOnly", VALUE.name(), d2018.getTime()));
//    }
//    
//    @Test
//    public void left_timeOnly_date_value() {
//        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "datePropTimeOnly", d2018_time), expectedDiffWithValue("datePropTimeOnly", VALUE.name(), d2018_time.getTime()));
//    }
    
}