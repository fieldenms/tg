package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.fluent.fetch;

/**
 * DAO implementation for companion object {@link ITgCentreDiffSerialisationNonPersistentCompositeChild}.
 *
 * @author TG Team
 *
 */
@EntityType(TgCentreDiffSerialisationNonPersistentCompositeChild.class)
public class TgCentreDiffSerialisationNonPersistentCompositeChildDao extends CommonEntityDao<TgCentreDiffSerialisationNonPersistentCompositeChild> implements ITgCentreDiffSerialisationNonPersistentCompositeChild {
    
    @Inject
    public TgCentreDiffSerialisationNonPersistentCompositeChildDao(final IFilter filter) {
        super(filter);
    }
    
    @Override
    public TgCentreDiffSerialisationNonPersistentCompositeChild findByKeyAndFetch(final fetch<TgCentreDiffSerialisationNonPersistentCompositeChild> fetchModel, final Object... keyValues) {
        return TgCentreDiffSerialisationNonPersistentCompositeChild.GroupingProperty.findByKey((String) keyValues[0]).map(v -> v.value).orElse(null);
    }
    
    @Override
    public boolean entityExists(final TgCentreDiffSerialisationNonPersistentCompositeChild entity) {
        return TgCentreDiffSerialisationNonPersistentCompositeChild.GroupingProperty.findByKey(entity.getKey().toString()).isPresent();
    }
    
}