package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import com.google.inject.Inject;

/** 
 * DAO implementation for companion object {@link ITgAuthor}.
 * 
 * @author Developers
 *
 */
@EntityType(TgAuthor.class)
public class TgAuthorDao extends CommonEntityDao<TgAuthor> implements ITgAuthor {
    @Inject
    public TgAuthorDao(final IFilter filter) {
        super(filter);
    }

}