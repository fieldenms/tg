package ua.com.fielden.platform.mcp.test_config;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.restlet.Request;
import org.restlet.Response;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.mcp.web.IMcpAuthenticationModel;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.try_wrapper.FailableComputation;
import ua.com.fielden.platform.types.try_wrapper.FailableRunnable;

import static org.restlet.data.Status.CLIENT_ERROR_FORBIDDEN;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.test_config.AbstractDaoTestCase.UNIT_TEST_USER;

/// This is a permissive authentication model by default, which supports in-memory restriction of access.
///
/// Every request is authenticated as [AbstractDaoTestCase#UNIT_TEST_USER], so that tests can make MCP requests without going through the OAuth flow.
/// While access is restricted, every request is rejected with `403 Forbidden`.
///
/// The restriction is visible to all threads, as requests are authenticated in threads of the web server rather than in the thread of a test.
///
@Singleton
public class TestMcpAuthenticationModel implements IMcpAuthenticationModel {

    private volatile boolean restricted;
    private final ICompanionObjectFinder coFinder;

    @Inject
    TestMcpAuthenticationModel(final ICompanionObjectFinder coFinder) {
        this.coFinder = coFinder;
    }

    @Override
    public Result authenticate(final Request request, final Response response) {
        if (restricted) {
            response.setStatus(CLIENT_ERROR_FORBIDDEN);
            return failure("Access denied due to restriction.");
        }
        return successful(coFinder.find(User.class, true).findByKey(UNIT_TEST_USER));
    }

    /// Restricts access, so that [#authenticate] fails for every request.
    ///
    public void restrict() {
        restricted = true;
    }

    /// Lifts the restriction of access.
    /// Call this during test teardown to prevent the restriction from leaking across tests.
    ///
    public void unrestrict() {
        restricted = false;
    }

    /// Restricts access for the duration of `f`.
    /// Returns the result of `f`.
    ///
    public <R> R withRestricted(final FailableComputation<R> f) {
        try {
            restrict();
            return f.get();
        } catch (final Exception e) {
            throw e instanceof RuntimeException it ? it : new RuntimeException(e);
        } finally {
            unrestrict();
        }
    }

    /// Restricts access for the duration of `f`.
    ///
    public void withRestrictedDo(final FailableRunnable f) {
        try {
            restrict();
            f.run();
        } catch (final Exception e) {
            throw e instanceof RuntimeException it ? it : new RuntimeException(e);
        } finally {
            unrestrict();
        }
    }

}
