package ua.com.fielden.platform.dao;

import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.persistence.types.EntityWithRichText;
import ua.com.fielden.platform.persistence.types.EntityWithRichTextCo;

@EntityType(EntityWithRichText.class)
public class EntityWithRichTextDao extends CommonEntityDao<EntityWithRichText> implements EntityWithRichTextCo {

    @Override
    protected IFetchProvider<EntityWithRichText> createFetchProvider() {
        return FETCH_PROVIDER;
    }

}
