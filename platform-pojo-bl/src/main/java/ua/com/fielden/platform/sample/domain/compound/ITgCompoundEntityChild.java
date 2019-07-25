package ua.com.fielden.platform.sample.domain.compound;

import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.dao.IEntityDao;

/** 
 * Companion object for entity {@link TgCompoundEntityChild}.
 * 
 * @author TG Team
 *
 */
public interface ITgCompoundEntityChild extends IEntityDao<TgCompoundEntityChild> {

    static final IFetchProvider<TgCompoundEntityChild> FETCH_PROVIDER = EntityUtils.fetch(TgCompoundEntityChild.class).with("key", "desc");

}