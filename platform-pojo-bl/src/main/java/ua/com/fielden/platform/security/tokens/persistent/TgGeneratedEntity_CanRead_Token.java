package ua.com.fielden.platform.security.tokens.persistent;

import ua.com.fielden.platform.sample.domain.TgGeneratedEntity;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;

import static java.lang.String.format;

/// A security token for entity [TgGeneratedEntity] to guard Read.
///
public class TgGeneratedEntity_CanRead_Token implements ISecurityToken {
    public final static String TITLE = format(Template.READ.forTitle(), TgGeneratedEntity.ENTITY_TITLE);
    public final static String DESC = format(Template.READ.forDesc(), TgGeneratedEntity.ENTITY_TITLE);
}
