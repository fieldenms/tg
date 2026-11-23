package ua.com.fielden.platform.mcp.test_config;

import org.junit.runner.RunWith;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

/// Base class for MCP server tests.
///
@RunWith(TgMcpServerTestRunner.class)
public abstract class AbstractTgMcpServerTestCase extends AbstractDaoTestCase {

    @Override
    protected void populateDomain() {
        super.populateDomain();

        // Create a user for MCP test clients.
        final var userMcpBase = save(new_(User.class).setKey("MCP_BASE").setBase(true).setActive(true).setEmail("mcp_base@tg.dev"));
        save(new_(User.class).setKey(TgMcpServerTestCaseConfiguration.USER_MCP_TEST).setActive(true).setEmail("mcp_test@tg.dev").setBasedOnUser(userMcpBase));
    }

}
