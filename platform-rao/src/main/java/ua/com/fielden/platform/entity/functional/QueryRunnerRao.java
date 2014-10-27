package ua.com.fielden.platform.entity.functional;

import ua.com.fielden.platform.entity.functional.centre.IQueryRunner;
import ua.com.fielden.platform.entity.functional.centre.QueryRunner;
import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/** 
 * RAO implementation for companion object {@link IQueryRunner}.
 * 
 * @author Developers
 *
 */
@EntityType(QueryRunner.class)
public class QueryRunnerRao extends CommonEntityRao<QueryRunner> implements IQueryRunner {

    @Inject
    public QueryRunnerRao(final RestClientUtil restUtil) {
        super(restUtil);
    }

}