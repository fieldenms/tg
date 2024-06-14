package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link ITgCentreDiffSerialisationPersistentChild}.
 *
 * @author TG Team
 *
 */
@EntityType(TgCentreDiffSerialisationPersistentChild.class)
public class TgCentreDiffSerialisationPersistentChildDao extends CommonEntityDao<TgCentreDiffSerialisationPersistentChild> implements ITgCentreDiffSerialisationPersistentChild {
    
    @Inject
    public TgCentreDiffSerialisationPersistentChildDao(final IFilter filter) {
        super(filter);
    }
    
}