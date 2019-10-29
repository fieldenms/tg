package ua.com.fielden.platform.sample.domain.compound;

import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.dao.IEntityDao;

/** 
 * Companion object for entity {@link TgCompoundEntityDetail}.
 * 
 * @author TG Team
 *
 */
public interface ITgCompoundEntityDetail extends IEntityDao<TgCompoundEntityDetail> {

    static final IFetchProvider<TgCompoundEntityDetail> FETCH_PROVIDER = EntityUtils.fetch(TgCompoundEntityDetail.class).with("key", "desc");

}