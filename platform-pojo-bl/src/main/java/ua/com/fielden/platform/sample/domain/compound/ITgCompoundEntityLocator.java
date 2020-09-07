package ua.com.fielden.platform.sample.domain.compound;

import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.dao.IEntityDao;

/** 
 * Companion object for entity {@link TgCompoundEntityLocator}.
 * 
 * @author TG Team
 *
 */
public interface ITgCompoundEntityLocator extends IEntityDao<TgCompoundEntityLocator> {

    static final IFetchProvider<TgCompoundEntityLocator> FETCH_PROVIDER = EntityUtils.fetch(TgCompoundEntityLocator.class).with("tgCompoundEntity");

}