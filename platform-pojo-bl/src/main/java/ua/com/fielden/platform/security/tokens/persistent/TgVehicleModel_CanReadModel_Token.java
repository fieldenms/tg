package ua.com.fielden.platform.security.tokens.persistent;

import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;

import static java.lang.String.format;

/// A security token for entity [TgVehicleModel] to guard Read Model.
///
public class TgVehicleModel_CanReadModel_Token implements ISecurityToken {
    public final static String TITLE = format(Template.READ_MODEL.forTitle(), TgVehicleModel.ENTITY_TITLE);
    public final static String DESC = format(Template.READ_MODEL.forDesc(), TgVehicleModel.ENTITY_TITLE);
}
