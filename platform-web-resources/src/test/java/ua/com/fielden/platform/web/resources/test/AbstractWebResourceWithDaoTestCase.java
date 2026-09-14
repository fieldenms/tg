package ua.com.fielden.platform.web.resources.test;

import org.junit.runner.RunWith;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

/// Base class for web resource tests that enables the use of testing facilities from `platform-dao`.
///
/// @see WebResourcesTestRunner
///
@RunWith(WebResourcesTestRunner.class)
public abstract class AbstractWebResourceWithDaoTestCase extends AbstractDaoTestCase {}
