package ua.com.fielden.platform.security.tokens.functional;

import ua.com.fielden.platform.entity.PersistentEntityInfo;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;

import static java.lang.String.format;

/// A security token for entity {@link PersistentEntityInfo} to guard Execute.
///
public class PersistentEntityInfo_CanExecute_Token implements ISecurityToken {
    public final static String TITLE = format(Template.EXECUTE.forTitle(), PersistentEntityInfo.ENTITY_TITLE);
    public final static String DESC = format(Template.EXECUTE.forDesc(), PersistentEntityInfo.ENTITY_TITLE);
}
