package ua.com.fielden.platform.tiny;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.reflection.ClassesRetriever;

import java.util.Map;

@EntityType(TinyHyperlink.class)
public class TinyHyperlinkDao extends CommonEntityDao<TinyHyperlink> implements TinyHyperlinkCo {

    @Override
    public <E extends AbstractEntity<?>> E entityFromLink(final TinyHyperlink link) {
        final var entityType = (Class<E>) ClassesRetriever.findClass(link.getEntityTypeName());
        return co(entityType).findById(link.getEntityId());
    }

    @Override
    public TinyHyperlink createAndSave(final Class<? extends AbstractEntity<?>> entityType, final Map<String, Object> propertyValues) {
        return createAndSave_(getShareableEntityType(entityType), propertyValues);
    }

    @SessionRequired
    protected <S extends AbstractEntity<?>> TinyHyperlink createAndSave_(final Class<S> shareableEntityType, final Map<String, Object> propertyValues) {
        final IEntityDao<S> shareableEntityCo = co(shareableEntityType);
        final S shareableEntity = shareableEntityCo.new_();
        propertyValues.forEach(shareableEntity::set);
        final var savedShareableEntityId = shareableEntityCo.quickSave(shareableEntity);
        final var link = new_().setEntityTypeName(shareableEntityType.getCanonicalName()).setEntityId(savedShareableEntityId);
        return save(link);
    }

    @Override
    protected IFetchProvider<TinyHyperlink> createFetchProvider() {
        return FETCH_PROVIDER;
    }

    Class<? extends AbstractEntity<?>> getShareableEntityType(final Class<? extends AbstractEntity<?>> entityType) {
        throw new UnsupportedOperationException("TODO");
    }

}
