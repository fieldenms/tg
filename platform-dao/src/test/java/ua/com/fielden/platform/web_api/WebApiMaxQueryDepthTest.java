package ua.com.fielden.platform.web_api;

import org.junit.Test;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.IDates;

import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.web_api.WebApiUtils.errors;
import static ua.com.fielden.platform.web_api.WebApiUtils.input;

/// Test for the GraphQL Web API maximum query depth limit, configured via the `web.api.maxQueryDepth` application property.
/// Queries nested deeper than the configured maximum are rejected during validation.
///
public class WebApiMaxQueryDepthTest extends AbstractDaoTestCase {

    /// A query nested to depth 3 (`tgWebApiEntity` -> `model` -> `make`), with `__typename` as the leaf.
    /// This is the same nesting exercised by [WebApiIntrospectionTest], so the shape is known to be valid.
    private static final String DEEP_QUERY = "{tgWebApiEntity{model{make{__typename}}}}";

    /// Creates a Web API service ([GraphQLService]) with an explicit maximum query depth, reusing the injected collaborators.
    /// The service is constructed directly, rather than via the `web.api.maxQueryDepth` property, so that depths below the IoC-enforced floor can be exercised.
    ///
    /// @param maxQueryDepth  the maximum query depth the service should permit
    ///
    private IWebApi webApiWithMaxQueryDepth(final int maxQueryDepth) {
        return new GraphQLService(
            maxQueryDepth,
            getInstance(IApplicationDomainProvider.class),
            getInstance(ICompanionObjectFinder.class),
            getInstance(IDates.class),
            getInstance(IAuthorisationModel.class),
            getInstance(ISecurityTokenProvider.class));
    }

    @Test
    public void query_within_the_maximum_depth_executes_successfully() {
        final Map<String, Object> result = webApiWithMaxQueryDepth(10).execute(input(DEEP_QUERY));
        assertTrue(errors(result).isEmpty());
    }

    @Test
    public void query_exceeding_the_maximum_depth_is_rejected_with_a_depth_error() {
        final Map<String, Object> result = webApiWithMaxQueryDepth(2).execute(input(DEEP_QUERY));
        final var errors = errors(result);
        assertFalse(errors.isEmpty());
        // The rejection must be due to the depth limit rather than some unrelated validation error.
        assertTrue(errors.toString().toLowerCase().contains("depth"));
    }

}
