package ua.com.fielden.platform.sample.domain.compound;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.tokens.persistent.TgCompoundEntity_CanDelete_Token;
import ua.com.fielden.platform.security.tokens.persistent.TgCompoundEntity_CanSave_Token;

/** 
 * DAO implementation for companion object {@link ITgCompoundEntity}.
 * 
 * @author TG Team
 *
 */
@EntityType(TgCompoundEntity.class)
public class TgCompoundEntityDao extends CommonEntityDao<TgCompoundEntity> implements ITgCompoundEntity {

    @Inject
    public TgCompoundEntityDao(final IFilter filter) {
        super(filter);
    }

    @Override
    @SessionRequired
    @Authorise(TgCompoundEntity_CanSave_Token.class)
    public TgCompoundEntity save(final TgCompoundEntity entity) {
        // firstly make sure that the entity is valid
        final Result isValid = entity.isValid();
        if (!isValid.isSuccessful()) {
            throw isValid;
        }
        final boolean wasPersisted = entity.isPersisted();
        final TgCompoundEntity savedEntity = super.save(entity);
        
        if (!wasPersisted) {
            // Also create and save an instance of detail entity
            final ITgCompoundEntityDetail coDetail = co$(TgCompoundEntityDetail.class);
            final TgCompoundEntityDetail detailEntity = coDetail.new_().setKey(savedEntity).setDesc(savedEntity.getKey() + " detail");
            coDetail.save(detailEntity);
            // Also create and save an instance of child entity (only for NEWXX entities);
            // this is used in web tests for 'NEW case' where we save entity and move to 'children' menu item.
            if (savedEntity.getKey().startsWith("NEW")) {
                final ITgCompoundEntityChild coChild = co$(TgCompoundEntityChild.class);
                final TgCompoundEntityChild childEntity = coChild.new_().setTgCompoundEntity(savedEntity).setDate(new Date()).setDesc(savedEntity.getKey() + " child");
                coChild.save(childEntity);
            }
        }
        return savedEntity;
    }

    @Override
    @SessionRequired
    @Authorise(TgCompoundEntity_CanDelete_Token.class)
    public int batchDelete(final Collection<Long> entitiesIds) {
        return defaultBatchDelete(entitiesIds);
    }
    
    @Override
    @SessionRequired
    @Authorise(TgCompoundEntity_CanDelete_Token.class)
    public int batchDelete(final List<TgCompoundEntity> entities) {
        return defaultBatchDelete(entities);
    }
    
    @Override
    protected IFetchProvider<TgCompoundEntity> createFetchProvider() {
        return FETCH_PROVIDER;
    }
    
    @Override
    public TgCompoundEntity new_() {
        final TgCompoundEntity entity = super.new_();
        entity.setActive(true);
        return entity;
    }

}