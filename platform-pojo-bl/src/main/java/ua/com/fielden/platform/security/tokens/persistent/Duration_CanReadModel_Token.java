package ua.com.fielden.platform.security.tokens.persistent;

import static java.lang.String.format;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.security.tokens.Template.READ_MODEL;

import ua.com.fielden.platform.entity.Duration;
import ua.com.fielden.platform.security.ISecurityToken;

/**
 * A security token for entity {@link Duration} to guard READ_MODEL.
 * 
 * @author TG Team
 */
public class Duration_CanReadModel_Token implements ISecurityToken {
    private final static String ENTITY_TITLE = getEntityTitleAndDesc(Duration.class).getKey();
    public final static String TITLE = format(READ_MODEL.forTitle(), ENTITY_TITLE);
    public final static String DESC = format(READ_MODEL.forDesc(), ENTITY_TITLE);
}
