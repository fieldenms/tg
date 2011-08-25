package ua.com.fielden.platform.security.interception;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import ua.com.fielden.platform.entity.ioc.AuthorisationModule;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.security.AuthorisationException;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;

import com.google.inject.Injector;

/**
 * A test case to ensure correct method interception as part of the authorisation process covering cases for super and sub types.
 *
 * @author TG Team
 *
 */
public class AuthorisationProcessTest {

    private final Injector injector = new ApplicationInjectorFactory()
    .add(new CommonTestEntityModuleWithPropertyFactory())
    .add(new AuthBindingModule())
    .add(new AuthorisationModule())
    .getInjector();


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
}
