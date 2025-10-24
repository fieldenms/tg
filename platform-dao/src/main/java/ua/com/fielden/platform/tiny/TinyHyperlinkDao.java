package ua.com.fielden.platform.tiny;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.reflection.ClassesRetriever;

@EntityType(TinyHyperlink.class)
public class TinyHyperlinkDao extends CommonEntityDao<TinyHyperlink> implements TinyHyperlinkCo {

    @Override
    public <E extends AbstractEntity<?>> E entityFromLink(final TinyHyperlink link) {
        final var entityType = (Class<E>) ClassesRetriever.findClass(link.getEntityTypeName());
        return co(entityType).findById(link.getEntityId());
    }

    @Override
    protected IFetchProvider<TinyHyperlink> createFetchProvider() {
        return FETCH_PROVIDER;
    }

}
