package ua.com.fielden.platform.web_api;

import org.junit.Test;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import static org.assertj.core.api.Assertions.assertThat;
import static ua.com.fielden.platform.web_api.GraphQLCommon.ERR_PAGE_CAPACITY_TOO_LARGE;
import static ua.com.fielden.platform.web_api.GraphQLService.DEFAULT_MAX_QUERY_DEPTH;
import static ua.com.fielden.platform.web_api.WebApiUtils.errors;
import static ua.com.fielden.platform.web_api.WebApiUtils.input;

/// Test for the GraphQL Web API maximum page capacity, configured via the `web.api.maxPageCapacity` application property.
///
public class WebApiMaxPageCapacityTest extends AbstractDaoTestCase {

    /// Creates a Web API service ([GraphQLService]) with an explicit maximum page capacity, reusing the injected collaborators.
    ///
    private IWebApi webApi(final int maxPageCapacity) {
        final var common = new GraphQLCommon(DEFAULT_MAX_QUERY_DEPTH, maxPageCapacity);
        return new GraphQLService(
                getInstance(IApplicationDomainProvider.class),
                new GraphQLSchemaFactory(
                        getInstance(ICompanionObjectFinder.class),
                        getInstance(IAuthorisationModel.class),
                        getInstance(ISecurityTokenProvider.class),
                        getInstance(EntityTypeIntrospection.class),
                        getInstance(EntityAggregation.class),
                        getInstance(FluentConditions.class),
                        common),
                common,
                getInstance(EntityTypeEnhancer.class),
                getInstance(IUserProvider.class));
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
