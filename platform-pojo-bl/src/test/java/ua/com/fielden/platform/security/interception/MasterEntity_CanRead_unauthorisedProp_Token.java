package ua.com.fielden.platform.security.interception;

import ua.com.fielden.platform.dao.dynamic.MasterEntity;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;

import static java.lang.String.format;

/// A security token used in testing of the authorisation mechanism.
///
public class MasterEntity_CanRead_unauthorisedProp_Token implements ISecurityToken {
    public final static String TITLE = format(Template.READ_PROP.forTitle(), MasterEntity.ENTITY_TITLE, "unauthorisedProp");
    public final static String DESC = format(Template.READ_PROP.forDesc(), MasterEntity.ENTITY_TITLE);

}
