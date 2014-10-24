package ua.com.fielden.platform.entity.functional;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.entity.functional.centre.FetchProp;
import ua.com.fielden.platform.entity.functional.centre.IFetchProp;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

import java.util.Map;

import ua.com.fielden.platform.dao.annotations.SessionRequired;

import com.google.inject.Inject;

/** 
 * DAO implementation for companion object {@link IFetchProp}.
 * 
 * @author Developers
 *
 */
@EntityType(FetchProp.class)
public class FetchPropDao extends CommonEntityDao<FetchProp> implements IFetchProp {
    @Inject
    public FetchPropDao(final IFilter filter) {
        super(filter);
    }

}