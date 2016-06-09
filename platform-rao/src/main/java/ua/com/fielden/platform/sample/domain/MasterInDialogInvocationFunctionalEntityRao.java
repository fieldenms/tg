package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import com.google.inject.Inject;

/** 
 * RAO implementation for companion object {@link IMasterInDialogInvocationFunctionalEntity}.
 * 
 * @author Developers
 *
 */
@EntityType(MasterInDialogInvocationFunctionalEntity.class)
public class MasterInDialogInvocationFunctionalEntityRao extends CommonEntityRao<MasterInDialogInvocationFunctionalEntity> implements IMasterInDialogInvocationFunctionalEntity {

    @Inject
    public MasterInDialogInvocationFunctionalEntityRao(final RestClientUtil restUtil) {
        super(restUtil);
    }

}