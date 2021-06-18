package ua.com.fielden.platform.security.tokens.persistent;

import static java.lang.String.format;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.security.tokens.Template.SAVE;

import ua.com.fielden.platform.entity.Duration;
import ua.com.fielden.platform.security.ISecurityToken;

/**
 * A security token for entity {@link Duration} to guard SAVE.
 * 
 * @author TG Team
 */
public class Duration_CanSave_Token implements ISecurityToken {
    private final static String ENTITY_TITLE = getEntityTitleAndDesc(Duration.class).getKey();
    public final static String TITLE = format(SAVE.forTitle(), ENTITY_TITLE);
    public final static String DESC = format(SAVE.forDesc(), ENTITY_TITLE);
}
