package ua.com.fielden.platform.security.tokens.persistent;

import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.sample.domain.TgFuelType;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;

/**
 * Represents a top level security token used for testing.
 *
 * @author TG Team
 *
 */
public class TgFuelType_CanDelete_Token implements ISecurityToken {
    private final static String ENTITY_TITLE = TitlesDescsGetter.getEntityTitleAndDesc(TgFuelType.class).getKey();
    public final static String TITLE = Template.DELETE.forTitle().formatted(ENTITY_TITLE);
    public final static String DESC = Template.DELETE.forDesc().formatted(ENTITY_TITLE);
}
