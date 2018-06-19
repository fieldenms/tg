package ua.com.fielden.platform.entity_centre.review.criteria;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
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
import ua.com.fielden.platform.types.tuples.T3;
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
    private Supplier<ICentreDomainTreeManagerAndEnhancer> freshCentreSupplier;
    private Function<Map<String, Object>, EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>>> freshCentreApplier;
    private BiConsumer<T3<EditKind, Optional<String>, Optional<Boolean>>, String> centreEditor;
    private Runnable centreDeleter;
    private Runnable defaultCentreClearer;
    private Supplier<List<LoadableCentreConfig>> loadableCentresSupplier;
    private Supplier<Optional<String>> saveAsNameSupplier;
    private Supplier<Optional<String>> preferredConfigSupplier;
    private Supplier<T2<String, String>> centreTitleAndDescGetter;
    private Supplier<ICentreDomainTreeManagerAndEnhancer> baseCentreSupplier;
    /**
     * This function represents centre query runner for export action which is dependent on configuration of the passed <code>customObject</code>.
     * Running of this fully-fledged query depends on query context (see property centreContextHolder).
     */
    private Function<Map<String, Object>, Stream<AbstractEntity<?>>> exportQueryRunner;
    private Consumer<Consumer<ICentreDomainTreeManagerAndEnhancer>> centreAdjuster;
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
    
    public void setCentreAdjuster(final Consumer<Consumer<ICentreDomainTreeManagerAndEnhancer>> centreAdjuster) {
        this.centreAdjuster = centreAdjuster;
    }

    public Consumer<Consumer<ICentreDomainTreeManagerAndEnhancer>> centreAdjuster() {
        return centreAdjuster;
    }

    public void setFreshCentreSupplier(final Supplier<ICentreDomainTreeManagerAndEnhancer> freshCentreSupplier) {
        this.freshCentreSupplier = freshCentreSupplier;
    }

    public Supplier<ICentreDomainTreeManagerAndEnhancer> freshCentreSupplier() {
        return freshCentreSupplier;
    }

    public void setFreshCentreApplier(final Function<Map<String, Object>, EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>>> freshCentreApplier) {
        this.freshCentreApplier = freshCentreApplier;
    }

    public Function<Map<String, Object>, EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>>> freshCentreApplier() {
        return freshCentreApplier;
    }

    public void setCentreEditor(final BiConsumer<T3<EditKind, Optional<String>, Optional<Boolean>>, String> centreEditor) {
        this.centreEditor = centreEditor;
    }

    public BiConsumer<T3<EditKind, Optional<String>, Optional<Boolean>>, String> centreEditor() {
        return centreEditor;
    }

    public void setDefaultCentreClearer(final Runnable defaultCentreClearer) {
        this.defaultCentreClearer = defaultCentreClearer;
    }

    public Runnable defaultCentreClearer() {
        return defaultCentreClearer;
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

    public void setCentreTitleAndDescGetter(final Supplier<T2<String, String>> centreTitleAndDescGetter) {
        this.centreTitleAndDescGetter = centreTitleAndDescGetter;
    }

    public Supplier<T2<String, String>> centreTitleAndDescGetter() {
        return centreTitleAndDescGetter;
    }

    public void setBaseCentreSupplier(final Supplier<ICentreDomainTreeManagerAndEnhancer> defaultCentreSupplier) {
        this.baseCentreSupplier = defaultCentreSupplier;
    }

    public Supplier<ICentreDomainTreeManagerAndEnhancer> baseCentreSupplier() {
        return baseCentreSupplier;
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
