package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * DAO for {@link ITgAuthoriser}
 *
 * @author TG Team
 *
 */
@EntityType(TgAuthoriser.class)
public class TgAuthoriserDao extends CommonEntityDao<TgAuthoriser> implements ITgAuthoriser {

    @Inject
    protected TgAuthoriserDao(final IFilter filter) {
        super(filter);
    }

}
