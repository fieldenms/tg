package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link ITgCentreDiffSerialisation}.
 *
 * @author TG Team
 *
 */
@EntityType(TgCentreDiffSerialisation.class)
public class TgCentreDiffSerialisationDao extends CommonEntityDao<TgCentreDiffSerialisation> implements ITgCentreDiffSerialisation {
    
    @Inject
    public TgCentreDiffSerialisationDao(final IFilter filter) {
        super(filter);
    }
    
}