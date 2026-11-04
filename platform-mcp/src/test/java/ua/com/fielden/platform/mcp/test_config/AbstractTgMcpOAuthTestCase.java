package ua.com.fielden.platform.mcp.test_config;

import org.junit.runner.RunWith;

/// Base class for tests of OAuth-based authentication of MCP requests.
///
@RunWith(TgMcpOAuthTestRunner.class)
public abstract class AbstractTgMcpOAuthTestCase extends AbstractTgMcpServerTestCase {}
