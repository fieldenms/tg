package ua.com.fielden.platform.web.centre;

import static java.util.Optional.empty;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancerCache.CACHE;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.web.centre.CentreDiffSerialiser.CENTRE_DIFF_SERIALISER;
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

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.joda.time.DateTime;

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
import ua.com.fielden.platform.sample.domain.TgCentreDiffSerialisationNonPersistentChild;
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
 * Testing framework, separated from <code>CentreUpdaterTest</code> and <code>CentreUpdaterDbTest</code> (both residing in <code>src/test/java</code>), that needs to reside in <code>src/main/java</code> to be compilable from javac.
 * 
 * @author TG Team
 *
 */
public class CentreUpdaterTestMixin {
    private static final Class<?> ROOT_GENERIC = TgCentreDiffSerialisation.class;
    static final Class<AbstractEntity<?>> ROOT = (Class<AbstractEntity<?>>) ROOT_GENERIC;
    private static final Class<? extends MiWithConfigurationSupport<?>> MI_TYPE = MiTgCentreDiffSerialisation.class;
    private static final EntityModuleWithPropertyFactory MODULE = new CommonTestEntityModuleWithPropertyFactory();
    private static final Injector INJECTOR = new ApplicationInjectorFactory().add(MODULE).getInjector();
    private static final EntityFactory FACTORY = INJECTOR.getInstance(EntityFactory.class);
    private static final ISerialiser SERIALISER = new SerialiserForDomainTreesTestingPurposes(FACTORY, new ClassProviderForTestingPurposes(), CACHE);
    private static final ISerialisationTypeEncoder serialisationTypeEncoder = new SerialisationTypeEncoder();
    private static final IIdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache = new IdOnlyProxiedEntityTypeCacheForTests();
    static final Date d2018 = new DateTime(2018, 1, 1, 0, 0).toDate();
    static final Date d2019 = new DateTime(2019, 1, 1, 0, 0).toDate();
    private static final EntityCentreConfig<TgCentreDiffSerialisation> DSL_CONFIG = 
        centreFor(TgCentreDiffSerialisation.class)
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
        .addCrit("nonPersistentEntityPropCritSingle").asSingle().autocompleter(TgCentreDiffSerialisationNonPersistentChild.class).also()
        .addCrit("propertyDescriptorProp").asMulti().autocompleter(PropertyDescriptor.class).also()
        .addCrit("propertyDescriptorPropCrit").asMulti().autocompleter(PropertyDescriptor.class).also()
        .addCrit("propertyDescriptorPropCritSingle").asSingle().autocompleter(PropertyDescriptor.class).also()
        .addCrit("stringProp").asMulti().text().also()
        .addCrit("stringPropDefault").asMulti().text().setDefaultValue(multi().string().setValues("0*", "*1").value()).also()
        .addCrit("stringPropCrit").asMulti().text().also()
        .addCrit("stringPropCritSingle").asSingle().text().also()
        .addCrit("booleanProp").asMulti().bool().also()
        .addCrit("booleanPropDefault").asMulti().bool().setDefaultValue(multi().bool().setIsValue(false).setIsNotValue(false).value()).also()
        .addCrit("booleanPropCrit").asMulti().bool().also()
        .addCrit("booleanPropCritSingle").asSingle().bool().also()
        .addCrit("integerProp").asRange().integer().also()
        .addCrit("integerPropDefault").asRange().integer().setDefaultValue(range().integer().setFromValue(0).setToValue(0).value()).also()
        .addCrit("integerPropCrit").asRange().integer().also()
        .addCrit("integerPropCritSingle").asSingle().integer().also()
        .addCrit("longProp").asRange().integer().also()
        .addCrit("longPropDefault").asRange().integer().setDefaultValue(range().integer().setFromValue(0).setToValue(0).value()).also()
        .addCrit("longPropCrit").asRange().integer().also()
        .addCrit("longPropCritSingle").asSingle().integer().also()
        .addCrit("bigDecimalProp").asRange().decimal().also()
        .addCrit("bigDecimalPropDefault").asRange().decimal().setDefaultValue(range().decimal().setFromValue(new BigDecimal(0).setScale(3)).setToValue(new BigDecimal(0).setScale(3)).value()).also()
        .addCrit("bigDecimalPropCrit").asRange().decimal().also()
        .addCrit("bigDecimalPropCritSingle").asSingle().decimal().also()
        .addCrit("moneyProp").asRange().decimal().also()
        .addCrit("moneyPropDefault").asRange().decimal().setDefaultValue(range().decimal().setFromValue(new BigDecimal(0).setScale(3)).setToValue(new BigDecimal(0).setScale(3)).value()).also()
        .addCrit("moneyPropCrit").asRange().decimal().also()
        .addCrit("moneyPropCritSingle").asSingle().decimal()
        .setLayoutFor(DESKTOP, empty(), mkGridForCentre(40, 1))
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
        final byte[] serialisedDiff = CENTRE_DIFF_SERIALISER.serialise(diff);
        final Map<String, Object> deserialisedDiff = CENTRE_DIFF_SERIALISER.deserialise(serialisedDiff);
        
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
    static void testDiffCreationAndApplication(
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
    static void testDiffCreationAndApplication(
        final Supplier<ICentreDomainTreeManagerAndEnhancer> defaultCentreCreator,
        final Consumer<ICentreDomainTreeManagerAndEnhancer> centreMutator,
        final Map<String, Object> expectedDiff) {
        testDiffCreationAndApplication(defaultCentreCreator, centreMutator, expectedDiff, null);
    }
    
    static ICentreDomainTreeManagerAndEnhancer create() {
        return createDefaultCentreFrom(DSL_CONFIG, SERIALISER, centre -> centre, true, TgCentreDiffSerialisation.class, CACHE, MI_TYPE, INJECTOR);
    }
    
    static Map<String, Object> expectedDiffWithValue(final String property, final String category, final Object value) {
        final Map<String, Object> expectedDiff = createEmptyDifferences();
        propDiff(property, expectedDiff).put(category, value);
        return expectedDiff;
    }
    
    static Map<String, Object> expectedDiffWithValues(final String property, final T2<String, Object> ... categoryAndValues) {
        final Map<String, Object> expectedDiff = createEmptyDifferences();
        for (final T2<String, Object> categoryAndValue: categoryAndValues) {
            propDiff(property, expectedDiff).put(categoryAndValue._1, categoryAndValue._2);
        }
        return expectedDiff;
    }
    
}