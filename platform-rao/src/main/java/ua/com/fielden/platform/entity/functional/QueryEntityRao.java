package ua.com.fielden.platform.entity.functional;

import ua.com.fielden.platform.entity.functional.centre.IQueryEntity;
import ua.com.fielden.platform.entity.functional.centre.QueryEntity;
import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/** 
 * RAO implementation for companion object {@link IQueryEntity}.
 * 
 * @author Developers
 *
 */
@EntityType(QueryEntity.class)
public class QueryEntityRao extends CommonEntityRao<QueryEntity> implements IQueryEntity {

    @Inject
    public QueryEntityRao(final RestClientUtil restUtil) {
        super(restUtil);
    }

}