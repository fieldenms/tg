package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * Companion object for entity {@link TgOriginatorDetails}.
 *
 * @author Developers
 *
 */
public interface ITgOriginatorDetails extends IEntityDao<TgOriginatorDetails> {

    IFetchProvider<TgOriginatorDetails> FETCH_PROVIDER = EntityUtils.fetch(TgOriginatorDetails.class)
            .with("originator", "comment");

}
