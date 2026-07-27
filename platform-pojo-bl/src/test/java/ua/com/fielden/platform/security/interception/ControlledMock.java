package ua.com.fielden.platform.security.interception;

import ua.com.fielden.platform.security.Authorise;

import java.util.concurrent.CountDownLatch;

/// This class models a controller with fake business logic located in methods with and without authorisation annotation.
///
public class ControlledMock {

    @Authorise(NoAccessToken.class)
    public void methodWithNoAccess() {
    }

    /// Used by concurrency tests.
    /// Enters an authorised method and parks inside it — keeping the authorisation "started" window open — until released.
    /// This makes it possible to deterministically check the behaviour of another thread that calls an `@Authorise` method while this one is in progress.
    ///
    @Authorise(AccessToken.class)
    public void authorisedMethodThatBlocksUntilReleased(final CountDownLatch entered, final CountDownLatch release) {
        entered.countDown();
        try {
            release.await();
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
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
