package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import com.google.inject.Inject;

/** 
 * RAO implementation for companion object {@link ITgUpdateRolesAction}.
 * 
 * @author Developers
 *
 */
@EntityType(TgUpdateRolesAction.class)
public class TgUpdateRolesActionRao extends CommonEntityRao<TgUpdateRolesAction> implements ITgUpdateRolesAction {

    @Inject
    public TgUpdateRolesActionRao(final RestClientUtil restUtil) {
        super(restUtil);
    }

}