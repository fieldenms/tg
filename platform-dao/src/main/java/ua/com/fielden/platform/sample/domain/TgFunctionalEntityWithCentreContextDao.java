package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.error.Result;

import com.google.inject.Inject;

/**
 * DAO implementation for companion object {@link ITgFunctionalEntityWithCentreContext}.
 *
 * @author Developers
 *
 */
@EntityType(TgFunctionalEntityWithCentreContext.class)
public class TgFunctionalEntityWithCentreContextDao extends CommonEntityDao<TgFunctionalEntityWithCentreContext> implements ITgFunctionalEntityWithCentreContext {
    private final ITgPersistentEntityWithProperties dao;

    @Inject
    public TgFunctionalEntityWithCentreContextDao(final IFilter filter, final ITgPersistentEntityWithProperties dao) {
        super(filter);
        this.dao = dao;
    }

    @Override
    public IFetchProvider<TgFunctionalEntityWithCentreContext> createFetchProvider() {
        return super.createFetchProvider()
                .with("key") // this property is "required" (necessary during saving) -- should be declared as fetching property
                .with("desc")
                .with("valueToInsert", "withBrackets");
    }

    @Override
    @SessionRequired
    public TgFunctionalEntityWithCentreContext save(final TgFunctionalEntityWithCentreContext entity) {
        
        // let's introduce some delay for demonstration purposes
        try {
            Thread.sleep(2000);
        } catch (final InterruptedException e) {
        }
        // let's fail attempts to execute the action without selecting any entities to be processed
        if (entity.getSelectedEntityIds().isEmpty()) {
            throw Result.failure("There are no entities to process. Please select some and try again.");
        }
        
        for (final Long selectedEntityId : entity.getSelectedEntityIds()) {
            final TgPersistentEntityWithProperties selected = dao.findById(selectedEntityId);
            selected.set("desc", entity.getUserParam() + ": " + entity.getValueToInsert() + ": " + selected.get("desc"));
            if (entity.getWithBrackets()) {
                selected.set("desc", "[" + selected.get("desc") + "]");
            }
            dao.save(selected);
        }
        return super.save(entity);
    }
}