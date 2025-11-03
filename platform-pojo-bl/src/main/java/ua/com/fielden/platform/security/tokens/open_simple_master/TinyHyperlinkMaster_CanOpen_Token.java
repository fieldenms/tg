package ua.com.fielden.platform.security.tokens.open_simple_master;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;
import ua.com.fielden.platform.tiny.TinyHyperlink;

/// A security token for entity [TinyHyperlink] to guard MASTER_OPEN.
///
public class TinyHyperlinkMaster_CanOpen_Token implements ISecurityToken {
    public final static String TITLE = String.format(Template.MASTER_OPEN.forTitle(), TinyHyperlink.ENTITY_TITLE + " Master");
    public final static String DESC = String.format(Template.MASTER_OPEN.forDesc(), TinyHyperlink.ENTITY_TITLE);
}
