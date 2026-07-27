package ua.com.fielden.platform.security.interception;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.security.AbstractAuthorisationModel;
import ua.com.fielden.platform.security.AuthorisationException;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;

import com.google.inject.Injector;

/// A test case to ensure correct method interception as part of the authorisation process covering cases for super and sub types.
///
public class AuthorisationProcessTest {

    private final Injector injector = new ApplicationInjectorFactory().add(new CommonEntityTestIocModuleWithPropertyFactory()).add(new AuthenticationTestIocModule()).getInjector();

    @Test
    public void test_that_method_with_no_authorisation_cannot_be_invoked() {
        final ControlledMock controller = injector.getInstance(ControlledMock.class);
        // method with no permission to be invoked
        try {
            controller.methodWithNoAccess();
            fail("Authorisation logic should have prevented method execution.");
        } catch (final Exception ex) {
            assertTrue("Incorrect authorisation exception type.", ex instanceof Result);
            assertTrue("Incorrect authorisation exception cause.", ((Result) ex).getEx() instanceof AuthorisationException);
        }
    }

    @Test
    public void test_that_method_with_authorisation_can_be_invoked() {
        final ControlledMock controller = injector.getInstance(ControlledMock.class);
        // method with permission to be invoked
        try {
            controller.methodWithAccess();
        } catch (final Exception ex) {
            fail("Authorisation logic should not have prevented method execution.");
        }
    }

    @Test
    public void test_that_authorised_method_with_unauthorised_subcalls_can_be_invoked() {
        final ControlledMock controller = injector.getInstance(ControlledMock.class);
        // method with permission to be invoked
        try {
            controller.methodWithAccessAndUnauthorisedSubCalls();
        } catch (final Exception ex) {
            ex.printStackTrace();
            fail("Authorisation logic should not have prevented method execution.");
        }
        // the next part check that the above call correctly finishes the authentication process
        try {
            controller.methodWithNoAccess();
            fail("Authorisation logic should have prevented method execution.");
        } catch (final Exception ex) {
        }
    }

    @Test
    public void test_that_method_with_no_security_token_can_be_invoked() {
        final ControlledMock controller = injector.getInstance(ControlledMock.class);
        // method without an authorisation token at all
        try {
            controller.methodWithoutAuthCheck();
        } catch (final Exception ex) {
            fail("Authorisation logic is not applicable.");
        }
    }

    @Test
    public void test_that_method_with_no_security_token_but_with_unauthorised_sub_call_cannot_be_invoked() {
        final ControlledMock controller = injector.getInstance(ControlledMock.class);
        // method without an authorisation token at all
        try {
            controller.methodWithUnauthorisedSubCall();
            fail("Authorisation logic should have prevented method execution.");
        } catch (final Exception ex) {
            assertTrue("Incorrect authorisation exception type.", ex instanceof Result);
            assertTrue("Incorrect authorisation exception cause.", ((Result) ex).getEx() instanceof AuthorisationException);
        }
    }

    @Test
    public void test_that_method_with_no_authorisation_but_with_authorised_sub_call_cannot_be_invoked() {
        final ControlledMock controller = injector.getInstance(ControlledMock.class);
        // method without an authorisation token at all
        try {
            controller.unauthorisedMethodWithAuthorisedSubCall();
            fail("Authorisation logic should have prevented method execution.");
        } catch (final Exception ex) {
            assertTrue("Incorrect authorisation exception type.", ex instanceof Result);
            assertTrue("Incorrect authorisation exception cause.", ((Result) ex).getEx() instanceof AuthorisationException);
        }
    }

    @Test
    public void test_that_method_with_authorisation_but_unauthorised_sub_call_can_be_invoked() {
        final ControlledMock controller = injector.getInstance(ControlledMock.class);
        // method without an authorisation token at all
        try {
            controller.authorisedMethodWithUnauthorisedSubCall();
        } catch (final Exception ex) {
            fail("Authorisation logic is not applicable.");
        }
    }

    @Test
    public void test_that_new_method_with_no_authorisation_cannot_be_invoked_for_sub_type() {
        final ControlledMock controller = injector.getInstance(SubControlledMock.class);
        // new method is under no access
        try {
            ((SubControlledMock) controller).newMethodWithNoAccess();
            fail("Authorisation logic should have prevented method execution.");
        } catch (final Exception ex) {
            assertTrue("Incorrect authorisation exception type.", ex instanceof Result);
            assertTrue("Incorrect authorisation exception cause.", ((Result) ex).getEx() instanceof AuthorisationException);
        }
    }

    @Test
    public void test_that_method_with_no_authorisation_cannot_be_invoked_for_sub_type() {
        final ControlledMock controller = injector.getInstance(SubControlledMock.class);
        // method with no permission to be invoked
        try {
            controller.methodWithNoAccess();
            fail("Authorisation logic should have prevented method execution.");
        } catch (final Exception ex) {
            assertTrue("Incorrect authorisation exception type.", ex instanceof Result);
            assertTrue("Incorrect authorisation exception cause.", ((Result) ex).getEx() instanceof AuthorisationException);
        }
    }

    @Test
    public void test_that_method_with_authorisation_can_be_invoked_for_sub_type() {
        final ControlledMock controller = injector.getInstance(SubControlledMock.class);
        // method with permission to be invoked
        try {
            controller.methodWithAccess();
        } catch (final Exception ex) {
            fail("Authorisation logic should not have prevented method execution.");
        }
    }

    @Test
    public void test_that_method_with_no_security_token_can_be_invoked_for_sub_type() {
        final ControlledMock controller = injector.getInstance(SubControlledMock.class);
        // method without an authorisation token at all
        try {
            controller.methodWithoutAuthCheck();
        } catch (final Exception ex) {
            fail("Authorisation logic is not applicable.");
        }
    }

    /// The model's `started` state must be confined to the thread that set it.
    /// Concrete models are typically singletons shared across all request threads,
    /// so a plain (shared) flag would let one thread's authorisation-in-progress suppress the checks of every other thread.
    ///
    @Test
    public void started_state_is_confined_to_the_thread_that_set_it() throws Exception {
        final AbstractAuthorisationModel model = new AuthorisationModelForTests();
        model.start();
        assertTrue("The thread that called start() must observe isStarted() == true.", model.isStarted());

        final AtomicBoolean otherThreadObservedStarted = new AtomicBoolean(true);
        final Thread other = new Thread(() -> otherThreadObservedStarted.set(model.isStarted()), "auth-observer");
        other.start();
        other.join(SECONDS.toMillis(5));

        assertFalse("A concurrent thread must not observe the started state set by another thread.", otherThreadObservedStarted.get());
    }

    /// Reproduces the authorisation bypass that occurs when the model's `started` flag is shared across threads.
    /// While one thread is parked inside an authorised method (keeping the authorisation window open), a second thread invokes a method it is not authorised to call.
    ///
    /// The second thread must still be denied.
    /// If it succeeds, the `started` flag is leaking across threads and the authorisation check is being skipped.
    ///
    /// Note: `IAuthorisationModel` is bound here via `toInstance(...)` — a single shared instance — which mirrors the `@Singleton` binding used in production.
    ///
    @Test
    public void concurrent_unauthorised_call_is_denied_while_another_thread_is_inside_an_authorised_call() throws Exception {
        final ControlledMock controller = injector.getInstance(ControlledMock.class);
        final CountDownLatch threadAInside = new CountDownLatch(1);
        final CountDownLatch releaseThreadA = new CountDownLatch(1);

        // Thread A enters an authorised method and parks inside it, keeping the authorisation "started" window open.
        final Thread threadA = new Thread(() -> controller.authorisedMethodThatBlocksUntilReleased(threadAInside, releaseThreadA), "auth-thread-A");
        threadA.setDaemon(true); // belt-and-suspenders: even a leaked thread cannot keep the JVM alive
        threadA.start();
        try {
            assertTrue("Thread A did not enter the authorised method in time.", threadAInside.await(5, SECONDS));

            // While A holds the window open, B invokes a method it is NOT authorised to call.
            final AtomicBoolean unauthorisedCallSucceeded = new AtomicBoolean(false);
            final AtomicReference<Throwable> unauthorisedCallError = new AtomicReference<>();
            final Thread threadB = new Thread(() -> {
                try {
                    controller.methodWithNoAccess();
                    unauthorisedCallSucceeded.set(true);
                } catch (final Throwable ex) {
                    unauthorisedCallError.set(ex);
                }
            }, "auth-thread-B");
            threadB.start();
            threadB.join(SECONDS.toMillis(5));

            assertFalse("Authorisation was bypassed: an unauthorised call succeeded on one thread while another thread was inside an authorised call.", unauthorisedCallSucceeded.get());
            final Throwable error = unauthorisedCallError.get();
            assertTrue("Expected the unauthorised call to be denied with a Result exception, but got: " + error, error instanceof Result);
            assertTrue("Incorrect authorisation exception cause.", ((Result) error).getEx() instanceof AuthorisationException);
        } finally {
            // Always release thread A and wait for it to finish, regardless of which assertion fails above.
            releaseThreadA.countDown();
            threadA.join(SECONDS.toMillis(5));
        }
    }

}
