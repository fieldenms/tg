package ua.com.fielden.platform.persistence.types;

import com.google.inject.ImplementedBy;
import ua.com.fielden.platform.dao.EntityWithRichTextDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.utils.EntityUtils;

@ImplementedBy(EntityWithRichTextDao.class)
public interface EntityWithRichTextCo extends IEntityDao<EntityWithRichText> {

    IFetchProvider<EntityWithRichText> FETCH_PROVIDER = EntityUtils.fetch(EntityWithRichText.class)
            .with("text");

}
