package ua.com.fielden.platform.sample.domain.compound;

import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.dao.IEntityDao;

/** 
 * Companion object for entity {@link TgCompoundEntity}.
 * 
 * @author TG Team
 *
 */
public interface ITgCompoundEntity extends IEntityDao<TgCompoundEntity> {
    
    static final IFetchProvider<TgCompoundEntity> FETCH_PROVIDER = EntityUtils.fetch(TgCompoundEntity.class).with("key", "desc");
    
}