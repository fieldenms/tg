package ua.com.fielden.platform.web_api;

import org.junit.Test;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgWebApiEntity;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.persistent.TgVehicleModel_CanReadModel_Token;
import ua.com.fielden.platform.security.tokens.persistent.TgVehicleModel_CanRead_Token;
import ua.com.fielden.platform.security.tokens.persistent.TgVehicleModel_CanRead_make_Token;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.SecurityRoleAssociationCo;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.*;
import static ua.com.fielden.platform.utils.EntityUtils.fetch;
import static ua.com.fielden.platform.web_api.WebApiUtils.*;

/// Test case for GraphQL Web API reading / reading model security.
///
public class WebApiSecurityTest extends AbstractDaoTestCase {
    private final IWebApi webApi = getInstance(IWebApi.class);

    @Test
    public void first_level_data_is_not_fetched_if_CanRead_access_is_prohibited() {
        removeAccessTo(TgVehicleModel_CanRead_Token.class);

        final Map<String, Object> result = webApi.execute(input("{tgVehicleModel{key}}"));
        
        assertFalse(errors(result).isEmpty());
        assertEquals(listOf(linkedMapOf(
            t2("message", "Exception while fetching data (/tgVehicleModel) : Permission denied due to token [Tg Vehicle Model Can Read] restriction."),
            t2("locations", listOf(linkedMapOf(t2("line", 1), t2("column", 2)))),
            t2("path", listOf("tgVehicleModel")),
            t2("extensions", linkedMapOf(t2("classification", "DataFetchingException")))
        )), errors(result));
    }

    @Test
    public void data_on_other_levels_is_fetched_if_CanRead_access_is_prohibited_but_root_type_CanRead_access_is_allowed() {
        removeAccessTo(TgVehicleModel_CanRead_Token.class);

        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{model{key}}}"));

        assertTrue(errors(result).isEmpty());
        assertEquals(result(linkedMapOf(
            t2("tgWebApiEntity", listOf(
                linkedMapOf(
                    t2("model", linkedMapOf(
                        t2("key", "316")
                    ))
                )
            ))
        )), result);
    }

    @Test
    public void first_level_data_is_not_fetched_if_CanReadModel_access_is_prohibited() {
        removeAccessTo(TgVehicleModel_CanReadModel_Token.class);

        final Map<String, Object> result = webApi.execute(input("{tgVehicleModel{key}}"));

        assertFalse(errors(result).isEmpty());
        assertEquals(listOf(linkedMapOf(
            t2("message", "Validation error (FieldUndefined@[tgVehicleModel/key]) : Field 'key' in type 'TgVehicleModel' is undefined"),
            t2("locations", listOf(linkedMapOf(t2("line", 1), t2("column", 17)))),
            t2("extensions", linkedMapOf(t2("classification", "ValidationError")))
        )), errors(result));
    }

    @Test
    public void data_on_other_levels_is_not_fetched_if_CanReadModel_access_is_prohibited_but_root_type_CanRead_access_is_allowed() {
        removeAccessTo(TgVehicleModel_CanReadModel_Token.class);

        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{model{key}}}"));

        assertFalse(errors(result).isEmpty());
        assertEquals(listOf(linkedMapOf(
            t2("message", "Validation error (FieldUndefined@[tgWebApiEntity/model/key]) : Field 'key' in type 'TgVehicleModel' is undefined"),
            t2("locations", listOf(linkedMapOf(t2("line", 1), t2("column", 23)))),
            t2("extensions", linkedMapOf(t2("classification", "ValidationError")))
        )), errors(result));
    }

    @Test
    public void first_level_data_is_not_fetched_if_CanRead_property_access_is_prohibited() {
        removeAccessTo(TgVehicleModel_CanRead_make_Token.class);

        final Map<String, Object> result = webApi.execute(input("{tgVehicleModel{make{key}}}"));
        assertFalse(errors(result).isEmpty());
        assertEquals(listOf(linkedMapOf(
            t2("message", "Validation error (FieldUndefined@[tgVehicleModel/make]) : Field 'make' in type 'TgVehicleModel' is undefined"),
            t2("locations", listOf(linkedMapOf(t2("line", 1), t2("column", 17)))),
            t2("extensions", linkedMapOf(t2("classification", "ValidationError")))
        )), errors(result));
    }

    @Test
    public void data_on_other_levels_is_not_fetched_if_CanRead_property_access_is_prohibited_but_root_type_CanRead_access_is_allowed() {
        removeAccessTo(TgVehicleModel_CanRead_make_Token.class);

        final Map<String, Object> result = webApi.execute(input("{tgWebApiEntity{model{make{key}}}}"));
        assertFalse(errors(result).isEmpty());
        assertEquals(listOf(linkedMapOf(
            t2("message", "Validation error (FieldUndefined@[tgWebApiEntity/model/make]) : Field 'make' in type 'TgVehicleModel' is undefined"),
            t2("locations", listOf(linkedMapOf(t2("line", 1), t2("column", 23)))),
            t2("extensions", linkedMapOf(t2("classification", "ValidationError")))
        )), errors(result));
    }

    @Override
    public boolean saveDataPopulationScriptToFile() {
        return false;
    }

    @Override
    public boolean useSavedDataPopulationScript() {
        return false;
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();

        if (useSavedDataPopulationScript()) {
            return;
        }

        final TgVehicleMake merc = save(new_(TgVehicleMake.class, "MERC", "Mercedes"));
        final TgVehicleModel model = save(new_(TgVehicleModel.class, "316", "316").setMake(merc));
        save(new_(TgWebApiEntity.class, "VEH1", "veh1 desc").setModel(model));
    }

    /// This is more like just in case `token` would get associated with `UNIT_TEST_ROLE` by the base test class logic.
    ///
    private void removeAccessTo(final Class<? extends ISecurityToken> token) {
        final SecurityRoleAssociationCo co = co(SecurityRoleAssociation.class);
        co.removeAssociations(setOf(
                co.new_()
                        .setRole(co(UserRole.class).findByKey(UNIT_TEST_ROLE))
                        .setSecurityToken(token)
        ));
    }

}