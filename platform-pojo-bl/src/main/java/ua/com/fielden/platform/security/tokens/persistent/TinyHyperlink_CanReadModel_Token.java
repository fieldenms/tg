package ua.com.fielden.platform.security.tokens.persistent;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.Template;
import ua.com.fielden.platform.tiny.TinyHyperlink;

import static java.lang.String.format;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

/// A security token for entity [TinyHyperlink] to guard READ_MODEL.
///
public class TinyHyperlink_CanReadModel_Token implements ISecurityToken {

    private final static String ENTITY_TITLE = getEntityTitleAndDesc(TinyHyperlink.class).getKey();
    public final static String TITLE = format(Template.READ_MODEL.forTitle(), ENTITY_TITLE);
    public final static String DESC = format(Template.READ_MODEL.forDesc(), ENTITY_TITLE);
    
}
