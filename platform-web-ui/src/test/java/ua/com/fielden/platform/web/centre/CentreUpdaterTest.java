package ua.com.fielden.platform.web.centre;

import static java.util.Optional.empty;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.NOT;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.OR_NULL;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.VALUE;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.DATE_MNEMONIC;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.DATE_PREFIX;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.EXCLUSIVE;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.EXCLUSIVE2;
import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.AND_BEFORE;
import static ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancerCache.CACHE;
import static ua.com.fielden.platform.serialisation.api.SerialiserEngines.JACKSON;
import static ua.com.fielden.platform.types.tuples.T2.t2;
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
import ua.com.fielden.platform.entity.proxy.IIdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.sample.domain.TgCentreDiffSerialisation;
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
    private static final Class<AbstractEntity<?>> ROOT = (Class<AbstractEntity<?>>) ROOT_GENERIC;
    private static final Class<? extends MiWithConfigurationSupport<?>> MI_TYPE = MiTgCentreDiffSerialisation.class;
    private static final EntityModuleWithPropertyFactory MODULE = new CommonTestEntityModuleWithPropertyFactory();
    private static final Injector INJECTOR = new ApplicationInjectorFactory().add(MODULE).getInjector();
    private static final EntityFactory FACTORY = INJECTOR.getInstance(EntityFactory.class);
    private static final ISerialiser SERIALISER = new SerialiserForDomainTreesTestingPurposes(FACTORY, new ClassProviderForTestingPurposes(), CACHE);
    private static final ISerialisationTypeEncoder serialisationTypeEncoder = new SerialisationTypeEncoder();
    private static final IIdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache = new IdOnlyProxiedEntityTypeCacheForTests();
    private static final Date d2018 = new DateTime(2018, 1, 1, 0, 0).toDate();
    private static final Date d2019 = new DateTime(2019, 1, 1, 0, 0).toDate();
    private static final EntityCentreConfig<TgCentreDiffSerialisation> DSL_CONFIG = 
        centreFor(TgCentreDiffSerialisation.class)
        .addCrit("stringProp").asMulti().text().setDefaultValue(multi().string().not().setValues("A*", "B*").canHaveNoValue().value()).also()
        .addCrit("dateProp").asRange().dateTime().also()
        .addCrit("datePropDefault").asRange().dateTime().setDefaultValue(range().date().not().setFromValueExclusive(d2018).setToValueExclusive(d2019).canHaveNoValue().value()).also()
        .addCrit("datePropDefaultMnemonics").asRange().dateTime().setDefaultValue(range().date().next().monthAndBefore().value())
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
        assertEquals(expectedDiff, diff); // TODO check whether assertEquals is sufficient or whether some "deep equals" should be envisaged
        
        // check expected diff against deserialised diff
        assertEquals(expectedDiff, deserialisedDiff); // TODO check whether assertEquals is sufficient or whether some "deep equals" should be envisaged
        
        return t2(centre, deserialisedDiff);
    }
    
    /**
     * Creates default centre and applies diff.<br>
     * Checks resultant centre against expected centre.
     * 
     * @param defaultCentreCreator -- function to create default centres
     * @param expectedCentreAndDiff -- a pair of expected centre and a diff
     */
    private static void testDiffApplication(
        final Supplier<ICentreDomainTreeManagerAndEnhancer> defaultCentreCreator,
        final T2<ICentreDomainTreeManagerAndEnhancer, Map<String, Object>> expectedCentreAndDiff) {
        
        // create default centre
        final ICentreDomainTreeManagerAndEnhancer defaultCentre = defaultCentreCreator.get();
        
        // apply sampled diff
        final ICentreDomainTreeManagerAndEnhancer appliedCentre = applyDifferences(defaultCentre, expectedCentreAndDiff._2, ROOT);
        
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
     */
    private static void testDiffCreationAndApplication(
        final Supplier<ICentreDomainTreeManagerAndEnhancer> defaultCentreCreator,
        final Consumer<ICentreDomainTreeManagerAndEnhancer> centreMutator,
        final Map<String, Object> expectedDiff) {
        
        // test diff creation and application
        testDiffApplication(defaultCentreCreator, testDiffCreation(defaultCentreCreator, centreMutator, expectedDiff));
    }
    
    private static ICentreDomainTreeManagerAndEnhancer create() {
        return createDefaultCentreFrom(DSL_CONFIG, SERIALISER, centre -> centre, true, TgCentreDiffSerialisation.class, CACHE, MI_TYPE, INJECTOR);
    }
    
    private static Map<String, Object> expectedDiffWithValue(final String property, final String category, final Object value) {
        final Map<String, Object> expectedDiff = createEmptyDifferences();
        propDiff(property, expectedDiff).put(category, value);
        return expectedDiff;
    }
    
    private static Map<String, Object> expectedDiffWithValues(final String property, final T2<String, Object> ... categoryAndValues) {
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
        }, expectedDiffWithValues("dateProp", t2(DATE_PREFIX.name(), PREV.toString()), t2(DATE_MNEMONIC.name(), MONTH.toString())));
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
        }, expectedDiffWithValue("datePropDefaultMnemonics", DATE_PREFIX.name(), PREV.toString())); // new value: PREV MONTH AND BEFORE 
    }
    
    // date "and before"
    
    @Test
    public void date_AND_BEFORE_2_true() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> {
            centre.getFirstTick().setDatePrefix(ROOT, "dateProp", PREV);
            centre.getFirstTick().setDateMnemonic(ROOT, "dateProp", MONTH);
            centre.getFirstTick().setAndBefore(ROOT, "dateProp", true);
        }, expectedDiffWithValues("dateProp", t2(DATE_PREFIX.name(), PREV.toString()), t2(DATE_MNEMONIC.name(), MONTH.toString()), t2(AND_BEFORE.name(), true)));
    }
    
    @Test
    public void date_AND_BEFORE_2_false() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> {
            centre.getFirstTick().setDatePrefix(ROOT, "dateProp", PREV);
            centre.getFirstTick().setDateMnemonic(ROOT, "dateProp", MONTH);
            centre.getFirstTick().setAndBefore(ROOT, "dateProp", false);
        }, expectedDiffWithValues("dateProp", t2(DATE_PREFIX.name(), PREV.toString()), t2(DATE_MNEMONIC.name(), MONTH.toString()), t2(AND_BEFORE.name(), false)));
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
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "dateProp", d2018), expectedDiffWithValue("dateProp", VALUE.name(), d2018.getTime()));
    }
    
    @Test
    public void default_left_date_value_2_empty() {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "datePropDefault", null), expectedDiffWithValue("datePropDefault", VALUE.name(), null));
    }
    
}