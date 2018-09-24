package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.KeyType;

/**
 * An entity representing an attachment preview action.
 *
 * @author TG Team
 *
 */
@KeyType(NoKey.class)
@CompanionObject(IAttachmentPreviewEntityAction.class)
public class AttachmentPreviewEntityAction extends AbstractFunctionalEntityWithCentreContext<NoKey> {

}
