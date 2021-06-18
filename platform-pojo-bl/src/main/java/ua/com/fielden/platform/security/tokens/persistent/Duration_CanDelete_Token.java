package ua.com.fielden.platform.security.tokens.persistent;

import static java.lang.String.format;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.security.tokens.Template.DELETE;

import ua.com.fielden.platform.entity.Duration;
import ua.com.fielden.platform.security.ISecurityToken;

/**
 * A security token for entity {@link Duration} to guard DELETE.
 * 
 * @author TG Team
 */
public class Duration_CanDelete_Token implements ISecurityToken {
    private final static String ENTITY_TITLE = getEntityTitleAndDesc(Duration.class).getKey();
    public final static String TITLE = format(DELETE.forTitle(), ENTITY_TITLE);
    public final static String DESC = format(DELETE.forDesc(), ENTITY_TITLE);
}
