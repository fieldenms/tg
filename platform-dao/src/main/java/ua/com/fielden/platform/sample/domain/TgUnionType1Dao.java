package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link TgUnionType1Co}.
 *
 * @author TG Team
 */
@EntityType(TgUnionType1.class)
public class TgUnionType1Dao extends CommonEntityDao<TgUnionType1> implements TgUnionType1Co {

    @Override
    public TgUnionType1 new_() {
        return super.new_().setActive(true);
    }

}
