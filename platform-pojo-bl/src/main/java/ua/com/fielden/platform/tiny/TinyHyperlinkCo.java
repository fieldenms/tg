package ua.com.fielden.platform.tiny;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;

import static ua.com.fielden.platform.utils.EntityUtils.fetch;

public interface TinyHyperlinkCo extends IEntityDao<TinyHyperlink> {

    IFetchProvider<TinyHyperlink> FETCH_PROVIDER = fetch(TinyHyperlink.class).with(
            "entityTypeName",
            "entityId");

    <E extends AbstractEntity<?>> E entityFromLink(TinyHyperlink link);

}
