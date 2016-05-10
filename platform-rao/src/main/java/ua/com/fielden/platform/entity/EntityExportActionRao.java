package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import com.google.inject.Inject;

/** 
 * RAO implementation for companion object {@link IEntityExportAction}.
 * 
 * @author Developers
 *
 */
@EntityType(EntityExportAction.class)
public class EntityExportActionRao extends CommonEntityRao<EntityExportAction> implements IEntityExportAction {

    @Inject
    public EntityExportActionRao(final RestClientUtil restUtil) {
        super(restUtil);
    }

}