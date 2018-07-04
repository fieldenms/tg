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
import ua.com.fielden.platform.web.centre.CentreConfigEditAction.EditKind;
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
    private BiFunction<T2<EditKind, Optional<String>>, String, Optional<Map<String, Object>>> centreEditor;
    private Runnable centreDeleter;
    private Runnable freshCentreSaver;
    private Runnable freshCentreCopier;
    private Consumer<String> inheritedCentreUpdater;
    private Runnable defaultCentreClearer;
    private Supplier<List<LoadableCentreConfig>> loadableCentresSupplier;
    private Supplier<Optional<String>> saveAsNameSupplier;
    private Supplier<Optional<String>> preferredConfigSupplier;
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

    public Consumer<Consumer<ICentreDomainTreeManagerAndEnhancer>> centreColumnWidthsAdjuster() {
        return centreColumnWidthsAdjuster;
    }

    public void setCentreAdjuster(final Consumer<Consumer<ICentreDomainTreeManagerAndEnhancer>> centreAdjuster) {
        this.centreAdjuster = centreAdjuster;
    }

    public Consumer<Consumer<ICentreDomainTreeManagerAndEnhancer>> centreAdjuster() {
        return centreAdjuster;
    }

    public void setPreviouslyRunCentreSupplier(final Supplier<ICentreDomainTreeManagerAndEnhancer> previouslyRunCentreSupplier) {
        this.previouslyRunCentreSupplier = previouslyRunCentreSupplier;
    }

    public Supplier<ICentreDomainTreeManagerAndEnhancer> previouslyRunCentreSupplier() {
        return previouslyRunCentreSupplier;
    }

    public void setFreshCentreApplier(final Function<Map<String, Object>, EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>>> freshCentreApplier) {
        this.freshCentreApplier = freshCentreApplier;
    }

    public Function<Map<String, Object>, EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>>> freshCentreApplier() {
        return freshCentreApplier;
    }

    public void setCentreEditor(final BiFunction<T2<EditKind, Optional<String>>, String, Optional<Map<String, Object>>> centreEditor) {
        this.centreEditor = centreEditor;
    }

    public BiFunction<T2<EditKind, Optional<String>>, String, Optional<Map<String, Object>>> centreEditor() {
        return centreEditor;
    }

    public void setDefaultCentreClearer(final Runnable defaultCentreClearer) {
        this.defaultCentreClearer = defaultCentreClearer;
    }

    public Runnable defaultCentreClearer() {
        return defaultCentreClearer;
    }

    public void setInheritedCentreUpdater(final Consumer<String> inheritedCentreUpdater) {
        this.inheritedCentreUpdater = inheritedCentreUpdater;
    }

    public Consumer<String> inheritedCentreUpdater() {
        return inheritedCentreUpdater;
    }

    public void setFreshCentreCopier(final Runnable freshCentreCopier) {
        this.freshCentreCopier = freshCentreCopier;
    }

    public Runnable freshCentreCopier() {
        return freshCentreCopier;
    }

    public void setFreshCentreSaver(final Runnable freshCentreSaver) {
        this.freshCentreSaver = freshCentreSaver;
    }

    public Runnable freshCentreSaver() {
        return freshCentreSaver;
    }

    public void setCentreDeleter(final Runnable centreDeleter) {
        this.centreDeleter = centreDeleter;
    }

    public Runnable centreDeleter() {
        return centreDeleter;
    }

    public void setLoadableCentresSupplier(final Supplier<List<LoadableCentreConfig>> loadableCentresSupplier) {
        this.loadableCentresSupplier = loadableCentresSupplier;
    }

    public Supplier<List<LoadableCentreConfig>> loadableCentresSupplier() {
        return loadableCentresSupplier;
    }

    public void setSaveAsNameSupplier(final Supplier<Optional<String>> saveAsNameSupplier) {
        this.saveAsNameSupplier = saveAsNameSupplier;
    }

    public Supplier<Optional<String>> saveAsNameSupplier() {
        return saveAsNameSupplier;
    }

    public void setPreferredConfigSupplier(final Supplier<Optional<String>> preferredConfigSupplier) {
        this.preferredConfigSupplier = preferredConfigSupplier;
    }

    public Supplier<Optional<String>> preferredConfigSupplier() {
        return preferredConfigSupplier;
    }

    public void setPreferredConfigMaker(final Consumer<Optional<String>> preferredConfigMaker) {
        this.preferredConfigMaker = preferredConfigMaker;
    }

    public Consumer<Optional<String>> preferredConfigMaker() {
        return preferredConfigMaker;
    }

    public void setCentreTitleAndDescGetter(final Function<Optional<String>, Optional<T2<String, String>>> centreTitleAndDescGetter) {
        this.centreTitleAndDescGetter = centreTitleAndDescGetter;
    }

    public Function<Optional<String>, Optional<T2<String, String>>> centreTitleAndDescGetter() {
        return centreTitleAndDescGetter;
    }

    public void setBaseCentreSupplier(final Supplier<ICentreDomainTreeManagerAndEnhancer> baseCentreSupplier) {
        this.baseCentreSupplier = baseCentreSupplier;
    }

    public Supplier<ICentreDomainTreeManagerAndEnhancer> baseCentreSupplier() {
        return baseCentreSupplier;
    }

    public void setCentreCustomObjectGetter(final BiFunction<EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>>, Optional<String>, Map<String, Object>> centreCustomObjectGetter) {
        this.centreCustomObjectGetter = centreCustomObjectGetter;
    }

    public BiFunction<EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>>, Optional<String>, Map<String, Object>> centreCustomObjectGetter() {
        return centreCustomObjectGetter;
    }

    public void setCriteriaValidationPrototypeCreator(final Function<Optional<String>, EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>>> criteriaValidationPrototypeCreator) {
        this.criteriaValidationPrototypeCreator = criteriaValidationPrototypeCreator;
    }

    public Function<Optional<String>, EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>>> criteriaValidationPrototypeCreator() {
        return criteriaValidationPrototypeCreator;
    }

    public void setCentreChangedGetter(final Supplier<Boolean> centreChangedGetter) {
        this.centreChangedGetter = centreChangedGetter;
    }

    public Supplier<Boolean> centreChangedGetter() {
        return centreChangedGetter;
    }

    public Function<Map<String, Object>, Stream<AbstractEntity<?>>> exportQueryRunner() {
        return exportQueryRunner;
    }

    public void setExportQueryRunner(final Function<Map<String, Object>, Stream<AbstractEntity<?>>> exportQueryRunner) {
        this.exportQueryRunner = exportQueryRunner;
    }
    
    public CentreContextHolder centreContextHolder() {
        return centreContextHolder;
    }
    
    public EnhancedCentreEntityQueryCriteria<T, DAO> setCentreContextHolder(final CentreContextHolder centreContextHolder) {
        this.centreContextHolder = centreContextHolder;
        return this;
    }
}
