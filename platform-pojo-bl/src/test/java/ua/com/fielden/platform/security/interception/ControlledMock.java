package ua.com.fielden.platform.security.interception;

import ua.com.fielden.platform.security.Authorise;

/**
 * This class models a controller with fake business logic located in methods with and without authorisation annotation.
 * 
 * @author TG Team
 * 
 */
public class ControlledMock {

    @Authorise(NoAccessToken.class)
    public void methodWithNoAccess() {
    }

    @Authorise(AccessToken.class)
    public void methodWithAccess() {
    }

    public void methodWithoutAuthCheck() {
    }

    public void methodWithUnauthorisedSubCall() {
	methodWithNoAccess();
    }

    @Authorise(AccessToken.class)
    public void authorisedMethodWithUnauthorisedSubCall() {
	methodWithNoAccess();
    }

    @Authorise(NoAccessToken.class)
    public void unauthorisedMethodWithAuthorisedSubCall() {
	methodWithAccess();
    }

}
