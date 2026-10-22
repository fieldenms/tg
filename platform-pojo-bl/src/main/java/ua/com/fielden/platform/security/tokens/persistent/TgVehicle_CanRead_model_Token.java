package ua.com.fielden.platform.security.tokens.persistent;

import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.security.ISecurityToken;

import static java.lang.String.format;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;
import static ua.com.fielden.platform.security.tokens.Template.READ_PROP;

/// A security token guard [TgVehicle#model] property reading.
///
public class TgVehicle_CanRead_model_Token implements ISecurityToken {
    private static final String PROP_TITLE = getTitleAndDesc("model", TgVehicle.class).getKey();
    public static final String TITLE = format(READ_PROP.forTitle(), TgVehicle.ENTITY_TITLE, PROP_TITLE);
    public static final String DESC = format(READ_PROP.forDesc(), TgVehicle.ENTITY_TITLE);
}