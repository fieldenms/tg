package ua.com.fielden.platform.security.tokens.web_api.query;

import fielden.test_app.close_leave.TgCloseLeaveExampleDetail;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.security.tokens.web_api.WebApiTemplate;
import ua.com.fielden.platform.security.tokens.web_api.WebApiToken;


/**
 * A security token for entity {@link TgCloseLeaveExampleDetail} to guard Web API querying.
 */
public class TgCloseLeaveExampleDetail_WebApi_CanQuery_Token
    extends WebApiToken
{
    private static final String ENTITY_TITLE = TitlesDescsGetter.getEntityTitleAndDesc(TgCloseLeaveExampleDetail.class).getKey();
    public static final String TITLE = String.format(WebApiTemplate.QUERY.forTitle(), ENTITY_TITLE);
    public static final String DESC = String.format(WebApiTemplate.QUERY.forDesc(), "tgCloseLeaveExampleDetail");
}
