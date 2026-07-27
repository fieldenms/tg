package ua.com.fielden.platform.security.tokens.persistent;

import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;

import static java.lang.String.format;

/// A security token for entity [TgVehicleModel] to guard Read.
///
public class TgVehicleModel_CanRead_Token implements ISecurityToken {
    public final static String TITLE = format(Template.READ.forTitle(), TgVehicleModel.ENTITY_TITLE);
    public final static String DESC = format(Template.READ.forDesc(), TgVehicleModel.ENTITY_TITLE);
}
