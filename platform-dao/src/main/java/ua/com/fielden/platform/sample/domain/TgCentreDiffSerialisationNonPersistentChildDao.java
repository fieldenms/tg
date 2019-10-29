package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.fluent.fetch;

/**
 * DAO implementation for companion object {@link ITgCentreDiffSerialisationNonPersistentChild}.
 *
 * @author TG Team
 *
 */
@EntityType(TgCentreDiffSerialisationNonPersistentChild.class)
public class TgCentreDiffSerialisationNonPersistentChildDao extends CommonEntityDao<TgCentreDiffSerialisationNonPersistentChild> implements ITgCentreDiffSerialisationNonPersistentChild {
    
    @Inject
    public TgCentreDiffSerialisationNonPersistentChildDao(final IFilter filter) {
        super(filter);
    }
    
    @Override
    public TgCentreDiffSerialisationNonPersistentChild findByKeyAndFetch(final fetch<TgCentreDiffSerialisationNonPersistentChild> fetchModel, final Object... keyValues) {
        return TgCentreDiffSerialisationNonPersistentChild.GroupingProperty.findByKey((String) keyValues[0]).map(v -> v.value).orElse(null);
    }
    
    @Override
    public boolean entityExists(final TgCentreDiffSerialisationNonPersistentChild entity) {
        return TgCentreDiffSerialisationNonPersistentChild.GroupingProperty.findByKey(entity.getKey()).isPresent();
    }
    
}