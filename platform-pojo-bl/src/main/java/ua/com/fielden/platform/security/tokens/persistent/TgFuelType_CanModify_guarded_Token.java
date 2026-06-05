package ua.com.fielden.platform.security.tokens.persistent;

import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.sample.domain.TgFuelType;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;

/**
 * Represents a CanModify security token used for testing.
 *
 * @author TG Team
 *
 */
public class TgFuelType_CanModify_guarded_Token implements ISecurityToken {
    private static final String ENTITY_TITLE = TitlesDescsGetter.getEntityTitleAndDesc(TgFuelType.class).getKey();
    private static final String PROP_TITLE = "guarded"; // used for several properties, hence a "generic" title
    public static final String TITLE = Template.MODIFY.forTitle().formatted(ENTITY_TITLE, PROP_TITLE);
    public static final String DESC = Template.MODIFY.forDesc().formatted(ENTITY_TITLE);
}
