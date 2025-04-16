package ua.com.fielden.platform.entity_centre.review.criteria;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IGeneratedEntityController;
import ua.com.fielden.platform.dashboard.DashboardRefreshFrequency;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.centre.LoadableCentreConfig;
import ua.com.fielden.platform.web.interfaces.DeviceProfile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Optional.empty;


/**
 * This class is the base class to enhance with criteria and resultant properties.
 *
 * @author TG Team
 *
 * @param <T>
 * @param <DAO>
 */
public class EnhancedCentreEntityQueryCriteria<T extends AbstractEntity<?>, DAO extends IEntityDao<T>> extends EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, DAO> {
    private Supplier<ICentreDomainTreeManagerAndEnhancer> freshCentreSupplier;
    private Supplier<ICentreDomainTreeManagerAndEnhancer> savedCentreSupplier;
    private Supplier<ICentreDomainTreeManagerAndEnhancer> previouslyRunCentreSupplier;
    /** IMPORTANT WARNING: avoids centre config self-conflict checks; ONLY TO BE USED NOT IN ANOTHER SessionRequired TRANSACTION SCOPE. */
    private Function<Map<String, Object>, EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>>> freshCentreApplier;
    private Function<String, Function<String, Function<Boolean, Function<DashboardRefreshFrequency, Map<String, Object>>>>> centreEditor;
    private Function<String, Function<String, Function<Boolean, Function<DashboardRefreshFrequency, Map<String, Object>>>>> centreSaver;
    private Function<Boolean, Map<String, Object>> centreConfigurator;
    private Runnable centreDeleter;
    /** IMPORTANT WARNING: avoids centre config self-conflict checks; ONLY TO BE USED NOT IN ANOTHER SessionRequired TRANSACTION SCOPE. */
    private Supplier<BiFunction<Map<String, Object>, String, Map<String, Object>>> freshCentreSaver;
    /** IMPORTANT WARNING: avoids centre config self-conflict checks; ONLY TO BE USED NOT IN ANOTHER SessionRequired TRANSACTION SCOPE. */
    private Runnable configDuplicateAction;
    private Consumer<String> inheritedFromBaseCentreUpdater;
    private Function<String, Function<String, Optional<String>>> inheritedFromSharedCentreUpdater;
    private Runnable defaultCentreClearer;
    private Function<Optional<Optional<String>>, Supplier<List<LoadableCentreConfig>>> loadableCentresSupplier;
    private Supplier<Optional<String>> saveAsNameSupplier;
    private Consumer<Optional<String>> preferredConfigMaker;
    private Function<Optional<String>, Optional<T2<String, String>>> centreTitleAndDescGetter;
    private Function<Optional<String>, Boolean> centreDashboardableGetter;
    private Function<Optional<String>, DashboardRefreshFrequency> centreDashboardRefreshFrequencyGetter;
    private Function<Optional<String>, Boolean> centreRunAutomaticallyGetter;
    private Supplier<Boolean> defaultRunAutomaticallySupplier;
    private Function<Optional<String>, Optional<String>> centreConfigUuidGetter;
    private Supplier<Boolean> centreDirtyGetter;
    private Function<Optional<String>, Function<Supplier<ICentreDomainTreeManagerAndEnhancer>, Boolean>> centreDirtyCalculator;
    private Function<Supplier<ICentreDomainTreeManagerAndEnhancer>, Function<Optional<String>, Function<Supplier<ICentreDomainTreeManagerAndEnhancer>, Boolean>>> centreDirtyCalculatorWithSavedSupplier;
    private Function<Optional<String>, EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>>> criteriaValidationPrototypeCreator;
    private Function<EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>>, Function<Optional<String>, Function<Optional<Optional<String>>, Function<Optional<Integer>, Function<Optional<Optional<String>>, Map<String, Object>>>>>> centreCustomObjectGetter;
    /**
     * This function represents centre query runner for export action which is dependent on configuration of the passed <code>customObject</code>.
     * Running of this fully-fledged query depends on query context (see property centreContextHolder).
     */
    private Function<Map<String, Object>, Stream<AbstractEntity<?>>> exportQueryRunner;
    private Function<Consumer<ICentreDomainTreeManagerAndEnhancer>, ICentreDomainTreeManagerAndEnhancer> centreAdjuster; // returns applied fresh centre
    private Consumer<Consumer<ICentreDomainTreeManagerAndEnhancer>> centreColumnWidthsAdjuster;
    private Supplier<Optional<String>> shareErrorSupplier;
    private CentreContextHolder centreContextHolder;
    private DeviceProfile device;
    private Class<? extends MiWithConfigurationSupport<?>> miType;

    @Inject
    protected EnhancedCentreEntityQueryCriteria(final IGeneratedEntityController generatedEntityController, final ISerialiser serialiser, final ICompanionObjectFinder controllerProvider, final IDates dates) {
        super(generatedEntityController, serialiser, controllerProvider, dates);
    }

    /**
     * Sets menu item type for this selection criteria entity.
     */
    public void setMiType(final Class<? extends MiWithConfigurationSupport<?>> miType) {
        this.miType = miType;
    }

    /**
     * Menu item type for this selection criteria entity.
     */
    public Class<? extends MiWithConfigurationSupport<?>> miType() {
        return miType;
    }

    /**
     * Sets device profile for this selection criteria entity.
     */
    public void setDevice(final DeviceProfile device) {
        this.device = device;
    }

    /**
     * Device profile for this selection criteria entity.
     */
    public DeviceProfile device() {
        return device;
    }

    public void setCentreColumnWidthsAdjuster(final Consumer<Consumer<ICentreDomainTreeManagerAndEnhancer>> centreColumnWidthsAdjuster) {
        this.centreColumnWidthsAdjuster = centreColumnWidthsAdjuster;
    }

    public void adjustColumnWidths(final Consumer<ICentreDomainTreeManagerAndEnhancer> consumer) {
        centreColumnWidthsAdjuster.accept(consumer);
    }

    public void setCentreAdjuster(final Function<Consumer<ICentreDomainTreeManagerAndEnhancer>, ICentreDomainTreeManagerAndEnhancer> centreAdjuster) {
        this.centreAdjuster = centreAdjuster;
    }

    public ICentreDomainTreeManagerAndEnhancer adjustCentre(final Consumer<ICentreDomainTreeManagerAndEnhancer> consumer) {
        return centreAdjuster.apply(consumer);
    }

    public void setSavedCentreSupplier(final Supplier<ICentreDomainTreeManagerAndEnhancer> savedCentreSupplier) {
        this.savedCentreSupplier = savedCentreSupplier;
    }

    public ICentreDomainTreeManagerAndEnhancer savedCentre() {
        return savedCentreSupplier.get();
    }

    public void setFreshCentreSupplier(final Supplier<ICentreDomainTreeManagerAndEnhancer> freshCentreSupplier) {
        this.freshCentreSupplier = freshCentreSupplier;
    }

    public ICentreDomainTreeManagerAndEnhancer freshCentre() {
        return freshCentreSupplier.get();
    }

    public void setPreviouslyRunCentreSupplier(final Supplier<ICentreDomainTreeManagerAndEnhancer> previouslyRunCentreSupplier) {
        this.previouslyRunCentreSupplier = previouslyRunCentreSupplier;
    }

    public ICentreDomainTreeManagerAndEnhancer previouslyRunCentre() {
        return previouslyRunCentreSupplier.get();
    }
    
    /**
     * IMPORTANT WARNING: avoids centre config self-conflict checks; ONLY TO BE USED NOT IN ANOTHER SessionRequired TRANSACTION SCOPE.
     * 
     * @param freshCentreApplier
     */
    public void setFreshCentreApplier(final Function<Map<String, Object>, EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>>> freshCentreApplier) {
        this.freshCentreApplier = freshCentreApplier;
    }
    
    /**
     * IMPORTANT WARNING: avoids centre config self-conflict checks; ONLY TO BE USED NOT IN ANOTHER SessionRequired TRANSACTION SCOPE.
     * 
     * @param modifHolder
     * @return
     */
    public EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> freshCentreApplier(final Map<String, Object> modifHolder) {
        return freshCentreApplier.apply(modifHolder);
    }

    public void setCentreEditor(final Function<String, Function<String, Function<Boolean, Function<DashboardRefreshFrequency, Map<String, Object>>>>> centreEditor) {
        this.centreEditor = centreEditor;
    }

    public Map<String, Object> editCentre(final String title, final String desc, final boolean dashboardable, final DashboardRefreshFrequency dashboardRefreshFrequency) {
        return centreEditor.apply(title).apply(desc).apply(dashboardable).apply(dashboardRefreshFrequency);
    }

    public void setCentreConfigurator(final Function<Boolean, Map<String, Object>> centreConfigurator) {
        this.centreConfigurator = centreConfigurator;
    }

    public Map<String, Object> configureCentre(final boolean runAutomatically) {
        return centreConfigurator.apply(runAutomatically);
    }

    public void setCentreSaver(final Function<String, Function<String, Function<Boolean, Function<DashboardRefreshFrequency, Map<String, Object>>>>> centreSaver) {
        this.centreSaver = centreSaver;
    }

    public Map<String, Object> saveCentre(final String title, final String desc, final boolean dashboardable, final DashboardRefreshFrequency dashboardRefreshFrequency) {
        return centreSaver.apply(title).apply(desc).apply(dashboardable).apply(dashboardRefreshFrequency);
    }

    public void setDefaultCentreClearer(final Runnable defaultCentreClearer) {
        this.defaultCentreClearer = defaultCentreClearer;
    }

    public void clearDefaultCentre() {
        defaultCentreClearer.run();
    }

    public void setInheritedFromSharedCentreUpdater(final Function<String, Function<String, Optional<String>>> inheritedFromSharedCentreUpdater) {
        this.inheritedFromSharedCentreUpdater = inheritedFromSharedCentreUpdater;
    }

    public Optional<String> updateInheritedFromSharedCentre(final String saveAsNameToLoad, final String configUuid) {
        return inheritedFromSharedCentreUpdater.apply(saveAsNameToLoad).apply(configUuid);
    }

    public void setInheritedFromBaseCentreUpdater(final Consumer<String> inheritedFromBaseCentreUpdater) {
        this.inheritedFromBaseCentreUpdater = inheritedFromBaseCentreUpdater;
    }

    public void updateInheritedFromBaseCentre(final String saveAsNameToLoad) {
        inheritedFromBaseCentreUpdater.accept(saveAsNameToLoad);
    }
    
    /**
     * IMPORTANT WARNING: avoids centre config self-conflict checks; ONLY TO BE USED NOT IN ANOTHER SessionRequired TRANSACTION SCOPE.
     * 
     * @param configDuplicateAction
     */
    public void setConfigDuplicateAction(final Runnable configDuplicateAction) {
        this.configDuplicateAction = configDuplicateAction;
    }
    /**
     * IMPORTANT WARNING: avoids centre config self-conflict checks; ONLY TO BE USED NOT IN ANOTHER SessionRequired TRANSACTION SCOPE.
     */
    public void configDuplicateAction() {
        configDuplicateAction.run();
    }

    /**
     * IMPORTANT WARNING: avoids centre config self-conflict checks; ONLY TO BE USED NOT IN ANOTHER SessionRequired TRANSACTION SCOPE.
     * 
     * @param freshCentreSaver
     */
    public void setFreshCentreSaver(final Supplier<BiFunction<Map<String, Object>, String, Map<String, Object>>> freshCentreSaver) {
        this.freshCentreSaver = freshCentreSaver;
    }
    
    /**
     * IMPORTANT WARNING: avoids centre config self-conflict checks; ONLY TO BE USED NOT IN ANOTHER SessionRequired TRANSACTION SCOPE.
     */
    public BiFunction<Map<String, Object>, String, Map<String, Object>> saveFreshCentre() {
        return freshCentreSaver.get();
    }

    public void setCentreDeleter(final Runnable centreDeleter) {
        this.centreDeleter = centreDeleter;
    }

    public void deleteCentre() {
        centreDeleter.run();
    }

    public void setLoadableCentresSupplier(final Function<Optional<Optional<String>>, Supplier<List<LoadableCentreConfig>>> loadableCentresSupplier) {
        this.loadableCentresSupplier = loadableCentresSupplier;
    }

    public Function<Optional<Optional<String>>, List<LoadableCentreConfig>> loadableCentreConfigs() {
        return saveAsNameOpt -> loadableCentresSupplier.apply(saveAsNameOpt).get();
    }

    public void setSaveAsNameSupplier(final Supplier<Optional<String>> saveAsNameSupplier) {
        this.saveAsNameSupplier = saveAsNameSupplier;
    }

    public Optional<String> saveAsName() {
        return saveAsNameSupplier.get();
    }

    public void setPreferredConfigMaker(final Consumer<Optional<String>> preferredConfigMaker) {
        this.preferredConfigMaker = preferredConfigMaker;
    }

    public void makePreferredConfig(final Optional<String> saveAsName) {
        preferredConfigMaker.accept(saveAsName);
    }

    public void setCentreTitleAndDescGetter(final Function<Optional<String>, Optional<T2<String, String>>> centreTitleAndDescGetter) {
        this.centreTitleAndDescGetter = centreTitleAndDescGetter;
    }

    public Optional<T2<String, String>> centreTitleAndDesc(final Optional<String> saveAsName) {
        return centreTitleAndDescGetter.apply(saveAsName);
    }

    public void setCentreDashboardableGetter(final Function<Optional<String>, Boolean> centreDashboardableGetter) {
        this.centreDashboardableGetter = centreDashboardableGetter;
    }

    public boolean centreDashboardable(final Optional<String> saveAsName) {
        return centreDashboardableGetter.apply(saveAsName);
    }

    public void setDefaultRunAutomaticallySupplier(final Supplier<Boolean> defaultRunAutomaticallySupplier) {
        this.defaultRunAutomaticallySupplier = defaultRunAutomaticallySupplier;
    }

    public boolean defaultRunAutomatically() {
        return defaultRunAutomaticallySupplier.get();
    }

    public void setCentreRunAutomaticallyGetter(final Function<Optional<String>, Boolean> centreRunAutomaticallyGetter) {
        this.centreRunAutomaticallyGetter = centreRunAutomaticallyGetter;
    }

    public boolean centreRunAutomatically(final Optional<String> saveAsName) {
        return centreRunAutomaticallyGetter.apply(saveAsName);
    }

    public void setCentreDashboardRefreshFrequencyGetter(final Function<Optional<String>, DashboardRefreshFrequency> centreDashboardRefreshFrequencyGetter) {
        this.centreDashboardRefreshFrequencyGetter = centreDashboardRefreshFrequencyGetter;
    }

    public DashboardRefreshFrequency centreDashboardRefreshFrequency(final Optional<String> saveAsName) {
        return centreDashboardRefreshFrequencyGetter.apply(saveAsName);
    }

    public void setCentreConfigUuidGetter(final Function<Optional<String>, Optional<String>> centreConfigUuidGetter) {
        this.centreConfigUuidGetter = centreConfigUuidGetter;
    }

    public Optional<String> centreConfigUuid(final Optional<String> saveAsName) {
        return centreConfigUuidGetter.apply(saveAsName);
    }

    public void setCentreCustomObjectGetter(final Function<EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>>, Function<Optional<String>, Function<Optional<Optional<String>>, Function<Optional<Integer>, Function<Optional<Optional<String>>, Map<String, Object>>>>>> centreCustomObjectGetter) {
        this.centreCustomObjectGetter = centreCustomObjectGetter;
    }

    public Map<String, Object> centreCustomObject(final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> critEntity, final Optional<String> saveAsName, final Optional<Optional<String>> configUuid, final Optional<Integer> preferredView, final Optional<Optional<String>> shareError) {
        return centreCustomObjectGetter.apply(critEntity).apply(saveAsName).apply(configUuid).apply(preferredView).apply(shareError);
    }

    public void setCriteriaValidationPrototypeCreator(final Function<Optional<String>, EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>>> criteriaValidationPrototypeCreator) {
        this.criteriaValidationPrototypeCreator = criteriaValidationPrototypeCreator;
    }

    public EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> createCriteriaValidationPrototype(final Optional<String> saveAsName) {
        return criteriaValidationPrototypeCreator.apply(saveAsName);
    }

    public void setCentreDirtyCalculator(final Function<Optional<String>, Function<Supplier<ICentreDomainTreeManagerAndEnhancer>, Boolean>> centreDirtyCalculator) {
        this.centreDirtyCalculator = centreDirtyCalculator;
    }

    public Function<Optional<String>, Function<Supplier<ICentreDomainTreeManagerAndEnhancer>, Boolean>> centreDirtyCalculator() {
        return centreDirtyCalculator;
    }

    public void setCentreDirtyCalculatorWithSavedSupplier(final Function<Supplier<ICentreDomainTreeManagerAndEnhancer>, Function<Optional<String>, Function<Supplier<ICentreDomainTreeManagerAndEnhancer>, Boolean>>> centreDirtyCalculatorWithSavedSupplier) {
        this.centreDirtyCalculatorWithSavedSupplier = centreDirtyCalculatorWithSavedSupplier;
    }

    public Function<Supplier<ICentreDomainTreeManagerAndEnhancer>, Function<Optional<String>, Function<Supplier<ICentreDomainTreeManagerAndEnhancer>, Boolean>>> centreDirtyCalculatorWithSavedSupplier() {
        return centreDirtyCalculatorWithSavedSupplier;
    }

    public void setCentreDirtyGetter(final Supplier<Boolean> centreDirtyGetter) {
        this.centreDirtyGetter = centreDirtyGetter;
    }

    public boolean isCentreDirty() {
        return centreDirtyGetter.get();
    }

    public void setExportQueryRunner(final Function<Map<String, Object>, Stream<AbstractEntity<?>>> exportQueryRunner) {
        this.exportQueryRunner = exportQueryRunner;
    }

    public Stream<AbstractEntity<?>> export(final Map<String, Object> queryParams) {
        return exportQueryRunner.apply(queryParams);
    }

    public EnhancedCentreEntityQueryCriteria<T, DAO> setCentreContextHolder(final CentreContextHolder centreContextHolder) {
        this.centreContextHolder = centreContextHolder;
        return this;
    }

    public CentreContextHolder centreContextHolder() {
        return centreContextHolder;
    }

    public void setShareErrorSupplier(final Supplier<Optional<String>> shareErrorSupplier) {
        this.shareErrorSupplier = shareErrorSupplier;
    }

    /**
     * Validates share action context, i.e. returns non-empty of(String) error message in case where configuration can not be shared and empty Optional otherwise.<br>
     * <p>
     * The rules are the following:<br>
     * 1. default config -- `Please save and try again.`<br>
     * 2. non-existent config -- `Configuration does not exist.`<br>
     * 3. loaded link config -- `Please save and try again.`<br>
     * 4. inherited from shared / base config -- `Only sharing of your own configurations is supported. Please save as your copy and try again.`<br>
     * 5. inherited from shared / base, deleted upstream, config -- `Please duplicate, save and try again.`<br>
     * 6. inherited from shared, made unshared -- `Please duplicate, save and try again.`
     */
    public Optional<String> shareError() {
        return shareErrorSupplier != null ? shareErrorSupplier.get() : empty();
    }

}