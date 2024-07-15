package ua.com.fielden.platform.security.session;

import ua.com.fielden.platform.error.Result;

/**
 * This is the default implementation for {@link ISsoSessionController}, needed to seamlessly support applications that do not support SSO.
 *
 * @author TG Team
 *
 */
public class DefaultDoNothingSsoSessionControllerImpl implements ISsoSessionController {

    @Override
    public Result refresh(final String sid) {
        return Result.successful(sid);
    }

    @Override
    public void invalidate(final String sid) {
    }

}