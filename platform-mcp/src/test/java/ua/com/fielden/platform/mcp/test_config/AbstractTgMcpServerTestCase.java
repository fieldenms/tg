package ua.com.fielden.platform.mcp.test_config;

import org.junit.runner.RunWith;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

/// Base class for MCP server tests.
///
@RunWith(TgMcpServerTestRunner.class)
public abstract class AbstractTgMcpServerTestCase extends AbstractDaoTestCase {}
