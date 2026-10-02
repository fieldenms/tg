package ua.com.fielden.platform.web_api;

import org.junit.Test;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import static org.assertj.core.api.Assertions.assertThat;
import static ua.com.fielden.platform.web_api.GraphQLService.DEFAULT_MAX_QUERY_DEPTH;
import static ua.com.fielden.platform.web_api.RootEntityFetcher.ERR_PAGE_CAPACITY_TOO_LARGE;
import static ua.com.fielden.platform.web_api.WebApiUtils.errors;
import static ua.com.fielden.platform.web_api.WebApiUtils.input;

/// Test for the GraphQL Web API maximum page capacity, configured via the `web.api.maxPageCapacity` application property.
///
public class WebApiMaxPageCapacityTest extends AbstractDaoTestCase {

    /// A query nested to depth 3 (`tgWebApiEntity` -> `model` -> `make`), with `__typename` as the leaf.
    /// This is the same nesting exercised by [WebApiIntrospectionTest], so the shape is known to be valid.
    private static final String DEEP_QUERY = "{tgWebApiEntity{model{make{__typename}}}}";

    /// Creates a Web API service ([GraphQLService]) with an explicit maximum page capacity, reusing the injected collaborators.
    ///
    private IWebApi webApi(final int maxPageCapacity) {
        return new GraphQLService(
                DEFAULT_MAX_QUERY_DEPTH,
                maxPageCapacity,
                getInstance(IApplicationDomainProvider.class),
                getInstance(ICompanionObjectFinder.class),
                getInstance(IAuthorisationModel.class),
                getInstance(ISecurityTokenProvider.class),
                getInstance(EntityTypeIntrospection.class),
                getInstance(EntityAggregation.class),
                getInstance(FluentConditions.class));
    }

    @Test
    public void query_within_maximum_page_capacity_executes_successfully() {
        final var result = webApi(10).execute(input("{tgWebApiEntity(pageCapacity:10) {key}}"));
        assertThat(errors(result)).isEmpty();
    }

    @Test
    public void query_exceeding_maximum_page_capacity_includes_an_error() {
        final var result = webApi(10).execute(input("{tgWebApiEntity(pageCapacity:11) {key}}"));
        assertThat(errors(result)).isNotEmpty();
        assertThat(errors(result)).anySatisfy(err -> assertThat(err.toString()).contains(ERR_PAGE_CAPACITY_TOO_LARGE.formatted(11, 10)));
    }

}
