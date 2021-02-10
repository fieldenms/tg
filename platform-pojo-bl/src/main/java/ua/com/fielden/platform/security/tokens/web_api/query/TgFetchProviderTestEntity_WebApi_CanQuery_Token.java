package ua.com.fielden.platform.security.tokens.web_api.query;

import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.sample.domain.TgFetchProviderTestEntity;
import ua.com.fielden.platform.security.tokens.web_api.WebApiTemplate;
import ua.com.fielden.platform.security.tokens.web_api.WebApiToken;


/**
 * A security token for entity {@link TgFetchProviderTestEntity} to guard Web API querying.
 */
public class TgFetchProviderTestEntity_WebApi_CanQuery_Token
    extends WebApiToken
{
    private static final String ENTITY_TITLE = TitlesDescsGetter.getEntityTitleAndDesc(TgFetchProviderTestEntity.class).getKey();
    public static final String TITLE = String.format(WebApiTemplate.QUERY.forTitle(), ENTITY_TITLE);
    public static final String DESC = String.format(WebApiTemplate.QUERY.forDesc(), ENTITY_TITLE);
}
