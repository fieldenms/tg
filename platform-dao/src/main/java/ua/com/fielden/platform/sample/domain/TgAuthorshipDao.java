package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import com.google.inject.Inject;

/** 
 * DAO implementation for companion object {@link ITgAuthorship}.
 * 
 * @author Developers
 *
 */
@EntityType(TgAuthorship.class)
public class TgAuthorshipDao extends CommonEntityDao<TgAuthorship> implements ITgAuthorship {
    @Inject
    public TgAuthorshipDao(final IFilter filter) {
        super(filter);
    }

}