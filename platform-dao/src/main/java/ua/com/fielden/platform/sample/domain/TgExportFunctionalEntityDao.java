package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
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

        // TODO restore master entity, which is represented by SavingInfoHolder
        System.out.println(context.getMasterEntity());
        
        entity.setParentEntity(co.findByKeyAndFetch(fetchAll(TgPersistentEntityWithProperties.class), "DEMO01"));
        entity.setValue(300);
        
        return entity;
    }
    
    @Override
    public IFetchProvider<TgExportFunctionalEntity> createFetchProvider() {
        return super.createFetchProvider()
                // .with("key") // this property is "required" (necessary during saving) -- should be declared as fetching property
                .with("parentEntity.integerProp", "value");
    }

}