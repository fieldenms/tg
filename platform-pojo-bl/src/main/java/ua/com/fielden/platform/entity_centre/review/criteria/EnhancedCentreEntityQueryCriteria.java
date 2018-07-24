package ua.com.fielden.platform.entity_centre.review.criteria;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IGeneratedEntityController;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.web.centre.LoadableCentreConfig;

/**
 * This class is the base class to enhance with criteria and resultant properties.
 *
 * @author TG Team
 *
 * @param <T>
 * @param <DAO>
 */
public class EnhancedCentreEntityQueryCriteria<T extends AbstractEntity<?>, DAO extends IEntityDao<T>> extends EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, DAO> {
    private Supplier<ICentreDomainTreeManagerAndEnhancer> previouslyRunCentreSupplier;
    private Function<Map<String, Object>, EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>>> freshCentreApplier;
    private BiFunction<String, String, Map<String, Object>> centreEditor;
    private BiFunction<String, String, Map<String, Object>> centreSaver;
    private Runnable centreDeleter;
    private Runnable freshCentreSaver;
    private Runnable configDuplicateAction;
    private Consumer<String> inheritedCentreUpdater;
    private Runnable defaultCentreClearer;
    private Supplier<List<LoadableCentreConfig>> loadableCentresSupplier;
    private Supplier<Optional<String>> saveAsNameSupplier;
    private Consumer<Optional<String>> preferredConfigMaker;
    private Function<Optional<String>, Optional<T2<String, String>>> centreTitleAndDescGetter;
    private Supplier<ICentreDomainTreeManagerAndEnhancer> baseCentreSupplier;
    private Supplier<Boolean> centreChangedGetter;
    private Function<Optional<String>, EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>>> criteriaValidationPrototypeCreator;
    private BiFunction<EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>>, Optional<String>, Map<String, Object>> centreCustomObjectGetter;
    /**
     * This function represents centre query runner for export action which is dependent on configuration of the passed <code>customObject</code>.
     * Running of this fully-fledged query depends on query context (see property centreContextHolder).
     */
    private Function<Map<String, Object>, Stream<AbstractEntity<?>>> exportQueryRunner;
    private Consumer<Consumer<ICentreDomainTreeManagerAndEnhancer>> centreAdjuster;
    private Consumer<Consumer<ICentreDomainTreeManagerAndEnhancer>> centreColumnWidthsAdjuster;
    private CentreContextHolder centreContextHolder;
    
    /**
     * Constructs {@link EnhancedCentreEntityQueryCriteria} with specified {@link IValueMatcherFactory}. Needed mostly for instantiating through injector.
     *
     * @param entityDao
     * @param valueMatcherFactory
     */
    @SuppressWarnings("rawtypes")
    @Inject
    protected EnhancedCentreEntityQueryCriteria(final IValueMatcherFactory valueMatcherFactory, final IGeneratedEntityController generatedEntityController, final ISerialiser serialiser, final ICompanionObjectFinder controllerProvider) {
        super(valueMatcherFactory, generatedEntityController, serialiser, controllerProvider);
    }

    public void setCentreColumnWidthsAdjuster(final Consumer<Consumer<ICentreDomainTreeManagerAndEnhancer>> centreColumnWidthsAdjuster) {
        this.centreColumnWidthsAdjuster = centreColumnWidthsAdjuster;
    }

    public void adjustColumnWidths(final Consumer<ICentreDomainTreeManagerAndEnhancer> consumer) {
        centreColumnWidthsAdjuster.accept(consumer);
    }

    public void setCentreAdjuster(final Consumer<Consumer<ICentreDomainTreeManagerAndEnhancer>> centreAdjuster) {
        this.centreAdjuster = centreAdjuster;
    }

    public void adjustCentre(final Consumer<ICentreDomainTreeManagerAndEnhancer> consumer) {
        centreAdjuster.accept(consumer);
    }

    public void setPreviouslyRunCentreSupplier(final Supplier<ICentreDomainTreeManagerAndEnhancer> previouslyRunCentreSupplier) {
        this.previouslyRunCentreSupplier = previouslyRunCentreSupplier;
    }

    public ICentreDomainTreeManagerAndEnhancer previouslyRunCentre() {
        return previouslyRunCentreSupplier.get();
    }

    public void setFreshCentreApplier(final Function<Map<String, Object>, EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>>> freshCentreApplier) {
        this.freshCentreApplier = freshCentreApplier;
    }

    public EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> freshCentreApplier(final Map<String, Object> modifHolder) {
        return freshCentreApplier.apply(modifHolder);
    }

    public void setCentreEditor(final BiFunction<String, String, Map<String, Object>> centreEditor) {
        this.centreEditor = centreEditor;
    }

    public Map<String, Object> editCentre(final String title, final String desc) {
        return centreEditor.apply(title, desc);
    }

    public void setCentreSaver(final BiFunction<String, String, Map<String, Object>> centreSaver) {
        this.centreSaver = centreSaver;
    }

    public Map<String, Object> saveCentre(final String title, final String desc) {
        return centreSaver.apply(title, desc);
    }

    public void setDefaultCentreClearer(final Runnable defaultCentreClearer) {
        this.defaultCentreClearer = defaultCentreClearer;
    }

    public void clearDefaultCentre() {
        defaultCentreClearer.run();
    }

    public void setInheritedCentreUpdater(final Consumer<String> inheritedCentreUpdater) {
        this.inheritedCentreUpdater = inheritedCentreUpdater;
    }

    public void updateInheritedCentre(final String saveAsNameToLoad) {
        inheritedCentreUpdater.accept(saveAsNameToLoad);
    }

    public void setConfigDuplicateAction(final Runnable configDuplicateAction) {
        this.configDuplicateAction = configDuplicateAction;
    }

    public void configDuplicateAction() {
        configDuplicateAction.run();
    }

    public void setFreshCentreSaver(final Runnable freshCentreSaver) {
        this.freshCentreSaver = freshCentreSaver;
    }

    public void saveFreshCentre() {
        freshCentreSaver.run();
    }

    public void setCentreDeleter(final Runnable centreDeleter) {
        this.centreDeleter = centreDeleter;
    }

    public void deleteCentre() {
        centreDeleter.run();
    }

    public void setLoadableCentresSupplier(final Supplier<List<LoadableCentreConfig>> loadableCentresSupplier) {
        this.loadableCentresSupplier = loadableCentresSupplier;
    }

    public List<LoadableCentreConfig> loadableCentreConfigs() {
        return loadableCentresSupplier.get();
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

    public void setBaseCentreSupplier(final Supplier<ICentreDomainTreeManagerAndEnhancer> baseCentreSupplier) {
        this.baseCentreSupplier = baseCentreSupplier;
    }

    public ICentreDomainTreeManagerAndEnhancer baseCentre() {
        return baseCentreSupplier.get();
    }

    public void setCentreCustomObjectGetter(final BiFunction<EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>>, Optional<String>, Map<String, Object>> centreCustomObjectGetter) {
        this.centreCustomObjectGetter = centreCustomObjectGetter;
    }

    public Map<String, Object> centreCustomObject(final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> critEntity, final Optional<String> saveAsName) {
        return centreCustomObjectGetter.apply(critEntity, saveAsName);
    }

    public void setCriteriaValidationPrototypeCreator(final Function<Optional<String>, EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>>> criteriaValidationPrototypeCreator) {
        this.criteriaValidationPrototypeCreator = criteriaValidationPrototypeCreator;
    }

    public EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> createCriteriaValidationPrototype(final Optional<String> saveAsName) {
        return criteriaValidationPrototypeCreator.apply(saveAsName);
    }

    public void setCentreChangedGetter(final Supplier<Boolean> centreChangedGetter) {
        this.centreChangedGetter = centreChangedGetter;
    }

    public boolean isCentreChanged() {
        return centreChangedGetter.get();
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
}
