package ua.com.fielden.platform.sample.domain;

import static java.lang.String.*;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * DAO implementation for companion object {@link ITgExportFunctionalEntity}.
 *
 * @author Developers
 *
 */
@EntityType(TgExportFunctionalEntity.class)
public class TgExportFunctionalEntityDao extends CommonEntityDao<TgExportFunctionalEntity> implements ITgExportFunctionalEntity {

    private final ITgPersistentEntityWithProperties co;
    
    @Inject
    public TgExportFunctionalEntityDao(final ITgPersistentEntityWithProperties co, final IFilter filter) {
        super(filter);
        this.co = co;
    }

    @Override
    @SessionRequired
    public TgExportFunctionalEntity save(final TgExportFunctionalEntity entity) {
        final CentreContext<?,?> context = entity.getContext();

        final TgPersistentEntityWithProperties me = (TgPersistentEntityWithProperties) context.getMasterEntity();
        System.out.println(format("IS MASTER ENTITY DIRTY? %s", me.isDirty()));
        if (me.isDirty()) {
            throw Result.failure("This action is applicable only to a saved entity! Please save entity and try again!");
        }
        
        if (me.getRequiredValidatedProp() != null && me.getRequiredValidatedProp() < 300) {
            entity.setValue(300);
            entity.setParentEntity(co.findByKeyAndFetch(fetchAll(TgPersistentEntityWithProperties.class), "DEMO01"));
        } else if (me.getRequiredValidatedProp() == null) {
            entity.setValue(99);
        } else {
            entity.setValue(me.getRequiredValidatedProp());
            entity.setParentEntity(me.getEntityProp());
        }
        
        return entity;
    }
    
    @Override
    public IFetchProvider<TgExportFunctionalEntity> createFetchProvider() {
        return super.createFetchProvider()
                // .with("key") // this property is "required" (necessary during saving) -- should be declared as fetching property
                .with("parentEntity.integerProp", "value");
    }

}