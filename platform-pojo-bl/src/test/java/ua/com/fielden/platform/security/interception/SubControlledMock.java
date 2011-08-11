package ua.com.fielden.platform.security.interception;

import ua.com.fielden.platform.security.Authorise;

/**
 * This class models a controller with fake business logic located in methods with and without authorisation annotation.
 * 
 * @author 01es
 * 
 */
public class SubControlledMock extends ControlledMock {

    @Authorise(NoAccessToken.class)
    public void newMethodWithNoAccess() {
    }
}
