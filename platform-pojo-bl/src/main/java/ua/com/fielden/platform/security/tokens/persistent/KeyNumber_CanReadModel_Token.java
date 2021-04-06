package ua.com.fielden.platform.security.tokens.persistent;

import ua.com.fielden.platform.keygen.KeyNumber;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;

/**
 * A security token for entity {@link KeyNumber} to guard READ_MODEL.
 * 
 * @author TG Team
 */
public class KeyNumber_CanReadModel_Token implements ISecurityToken {
    private final static String ENTITY_TITLE = TitlesDescsGetter.getEntityTitleAndDesc(KeyNumber.class).getKey();
    public final static String TITLE = String.format(Template.READ_MODEL.forTitle(), ENTITY_TITLE);
    public final static String DESC = String.format(Template.READ_MODEL.forDesc(), ENTITY_TITLE);
}
