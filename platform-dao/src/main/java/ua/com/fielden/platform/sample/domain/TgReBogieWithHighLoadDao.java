package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * @author TG Team
 * 
 */
@EntityType(TgReBogieWithHighLoad.class)
public class TgReBogieWithHighLoadDao extends CommonEntityDao<TgReBogieWithHighLoad> implements TgReBogieWithHighLoadCo {

    @Inject
    protected TgReBogieWithHighLoadDao(final IFilter filter) {
        super(filter);
    }

}
