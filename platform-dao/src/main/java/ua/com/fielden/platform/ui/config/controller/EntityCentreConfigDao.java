package ua.com.fielden.platform.ui.config.controller;

import static ua.com.fielden.platform.companion.PersistentEntitySaver.ERR_COULD_NOT_RESOLVE_CONFLICTING_CHANGES;
import static ua.com.fielden.platform.utils.EntityUtils.isConflicting;

import java.util.Map;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfig;

/**
 * DAO implementation of {@link IEntityCentreConfig}.
 * 
 * @author TG Team
 * 
 */
@EntityType(EntityCentreConfig.class)
public class EntityCentreConfigDao extends CommonEntityDao<EntityCentreConfig> implements IEntityCentreConfig {
    
    @Inject
    protected EntityCentreConfigDao(final IFilter filter) {
        super(filter);
    }
    
    @Override
    @SessionRequired
    public void delete(final EntityCentreConfig entity) {
        defaultDelete(entity);
    }
    
    @Override
    @SessionRequired
    public void delete(final EntityResultQueryModel<EntityCentreConfig> model, final Map<String, Object> paramValues) {
        defaultDelete(model, paramValues);
    }
    
    @Override
    @SessionRequired
    public int batchDelete(final EntityResultQueryModel<EntityCentreConfig> model) {
        return defaultBatchDelete(model);
    }
    
    @Override
    @SessionRequired
    public EntityCentreConfig save(final EntityCentreConfig entity) {
        try {
            return super.save(entity);
        } catch (final EntityCompanionException companionException) {
            if (companionException.getMessage() != null && companionException.getMessage().contains(ERR_COULD_NOT_RESOLVE_CONFLICTING_CHANGES)) { // conflict could occur for concrete user, miType and surrogate+saveAs name
                // Need to repeat saving of entity in case of "self conflict": in a concurrent environment the same user on the same entity centre configuration can trigger multiple concurrent validations with different parameters.
                final EntityCentreConfig persistedEntity = findByEntityAndFetch(null, entity);
                for (final MetaProperty<?> prop : entity.getDirtyProperties()) { // the only two properties that can be conflicting: 'configBody' (most cases) and 'desc' (rare, but possible); other properties are the parts of key and will not conflict
                    final String name = prop.getName();
                    if (isConflicting(prop.getValue(), prop.getOriginalValue(), persistedEntity.get(name))) {
                        persistedEntity.set(name, prop.getValue());
                    }
                }
                return save(persistedEntity); // repeat the procedure of 'conflict-aware' saving in cases of subsequent conflicts
            } else {
                throw companionException;
            }
        }
    }
    
}