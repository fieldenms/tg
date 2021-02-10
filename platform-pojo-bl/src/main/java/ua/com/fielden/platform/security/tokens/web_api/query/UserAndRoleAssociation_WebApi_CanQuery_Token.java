package ua.com.fielden.platform.security.tokens.web_api.query;

import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.security.tokens.web_api.WebApiTemplate;
import ua.com.fielden.platform.security.tokens.web_api.WebApiToken;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;


/**
 * A security token for entity {@link UserAndRoleAssociation} to guard Web API querying.
 */
public class UserAndRoleAssociation_WebApi_CanQuery_Token
    extends WebApiToken
{
    private static final String ENTITY_TITLE = TitlesDescsGetter.getEntityTitleAndDesc(UserAndRoleAssociation.class).getKey();
    public static final String TITLE = String.format(WebApiTemplate.QUERY.forTitle(), ENTITY_TITLE);
    public static final String DESC = String.format(WebApiTemplate.QUERY.forDesc(), ENTITY_TITLE);
}
