package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import com.google.inject.Inject;

/** 
 * RAO implementation for companion object {@link IShowViewInDialogFunctionalEntity}.
 * 
 * @author Developers
 *
 */
@EntityType(ShowViewInDialogFunctionalEntity.class)
public class ShowViewInDialogFunctionalEntityRao extends CommonEntityRao<ShowViewInDialogFunctionalEntity> implements IShowViewInDialogFunctionalEntity {

    @Inject
    public ShowViewInDialogFunctionalEntityRao(final RestClientUtil restUtil) {
        super(restUtil);
    }

}