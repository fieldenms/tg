package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import java.util.Map;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import com.google.inject.Inject;

/** 
 * DAO implementation for companion object {@link ITgPublishedYearly}.
 * 
 * @author Developers
 *
 */
@EntityType(TgPublishedYearly.class)
public class TgPublishedYearlyDao extends CommonEntityDao<TgPublishedYearly> implements ITgPublishedYearly {
    @Inject
    public TgPublishedYearlyDao(final IFilter filter) {
        super(filter);
    }

}