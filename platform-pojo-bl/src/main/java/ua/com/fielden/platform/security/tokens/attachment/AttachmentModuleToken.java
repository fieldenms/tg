package ua.com.fielden.platform.security.tokens.attachment;

import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.security.ISecurityToken;

/**
 * Top attachment module token.
 * 
 * @author TG Team
 * 
 */
@KeyTitle(value = "Attachment module", desc = "High level token controlling permission to all attachment related functionality")
public class AttachmentModuleToken implements ISecurityToken {
}
