package ua.com.fielden.platform.entity_centre.review.criteria;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IGeneratedEntityController;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.serialisation.api.ISerialiser;

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
    private Function<Map<String, Object>, Stream<AbstractEntity<?>>> exportQueryRunner;

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

    public void setFreshCentreSupplier(final Supplier<ICentreDomainTreeManagerAndEnhancer> freshCentreSupplier) {
        this.freshCentreSupplier = freshCentreSupplier;
    }

    public Supplier<ICentreDomainTreeManagerAndEnhancer> freshCentreSupplier() {
        return freshCentreSupplier;
    }

    public Function<Map<String, Object>, Stream<AbstractEntity<?>>> exportQueryRunner() {
        return exportQueryRunner;
    }

    public void setExportQueryRunner(final Function<Map<String, Object>, Stream<AbstractEntity<?>>> exportQueryRunner) {
        this.exportQueryRunner = exportQueryRunner;
    }
}
