package ua.com.fielden.platform.entity.functional.paginator;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import java.util.Map;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import com.google.inject.Inject;

/** 
 * DAO implementation for companion object {@link IPage}.
 * 
 * @author Developers
 *
 */
@EntityType(Page.class)
public class PageDao extends CommonEntityDao<Page> implements IPage {
    @Inject
    public PageDao(final IFilter filter) {
        super(filter);
    }

}