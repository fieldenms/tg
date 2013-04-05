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

    @Authorise(AccessToken.class)
    public void methodWithAccessAndUnauthorisedSubCalls() {
	// need to call twice to ensure that the first call does not rest the authentication process in progress
	methodWithNoAccess();
	methodWithNoAccess(); // critical second call
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
