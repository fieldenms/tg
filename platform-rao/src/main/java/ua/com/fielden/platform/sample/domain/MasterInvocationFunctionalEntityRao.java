package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import com.google.inject.Inject;

/** 
 * RAO implementation for companion object {@link IMasterInvocationFunctionalEntity}.
 * 
 * @author Developers
 *
 */
@EntityType(MasterInvocationFunctionalEntity.class)
public class MasterInvocationFunctionalEntityRao extends CommonEntityRao<MasterInvocationFunctionalEntity> implements IMasterInvocationFunctionalEntity {

    @Inject
    public MasterInvocationFunctionalEntityRao(final RestClientUtil restUtil) {
        super(restUtil);
    }

}