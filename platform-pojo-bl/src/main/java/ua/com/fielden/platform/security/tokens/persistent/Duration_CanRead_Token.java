package ua.com.fielden.platform.security.tokens.persistent;

import static java.lang.String.format;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.security.tokens.Template.READ;

import ua.com.fielden.platform.entity.Duration;
import ua.com.fielden.platform.security.ISecurityToken;

/**
 * A security token for entity {@link Duration} to guard READ.
 * 
 * @author TG Team
 */
public class Duration_CanRead_Token implements ISecurityToken {
    private final static String ENTITY_TITLE = getEntityTitleAndDesc(Duration.class).getKey();
    public final static String TITLE = format(READ.forTitle(), ENTITY_TITLE);
    public final static String DESC = format(READ.forDesc(), ENTITY_TITLE);
}
