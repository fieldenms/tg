package ua.com.fielden.platform.tiny;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;

import static ua.com.fielden.platform.tiny.ShareAction.HYPERLINK;
import static ua.com.fielden.platform.tiny.ShareAction.QR_CODE;
import static ua.com.fielden.platform.utils.EntityUtils.fetch;

public interface ShareActionCo extends IEntityDao<ShareAction> {

    IFetchProvider<ShareAction> FETCH_PROVIDER = fetch(ShareAction.class).with(
            HYPERLINK,
            QR_CODE);

}
