package ua.com.fielden.platform.web_api;

import org.junit.Test;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.EntityNewAction;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static ua.com.fielden.platform.web_api.UserDefinedEnhancedEntityType.BASE_TYPE;
import static ua.com.fielden.platform.web_api.UserDefinedEnhancedEntityType.NAME;
import static ua.com.fielden.platform.web_api.validators.EnhancedEntityTypeBaseTypeValidator.ERR_NO_SUCH_DOMAIN_TYPE;
import static ua.com.fielden.platform.web_api.validators.EnhancedEntityTypeNameValidator.ERR_NAME_OF_DOMAIN_TYPE;

/// Tests for the validation of [UserDefinedEnhancedEntityType].
///
public class UserDefinedEnhancedEntityTypeTest extends AbstractDaoTestCase {

    private final List<Class<? extends AbstractEntity<?>>> domainTypes = getInstance(IApplicationDomainProvider.class).entityTypes();

    @Test
    public void name_cannot_be_that_of_a_domain_entity_type() {
        assertThat(domainTypes).as("EntityNewAction is a domain entity type.").contains(EntityNewAction.class);
        final var namedAsDomainType = new_composite(UserDefinedEnhancedEntityType.class, "EntityNewAction", getUser());
        assertThat(namedAsDomainType.getProperty(NAME).getFirstFailure())
                .hasMessage(ERR_NAME_OF_DOMAIN_TYPE.formatted("EntityNewAction", EntityNewAction.class.getName()));

        // A positive control: a name that no domain entity type has is accepted.
        assertThat(domainTypes).extracting(Class::getSimpleName).as("No domain entity type is named MyVehicle.").doesNotContain("MyVehicle");
        assertThat(new_composite(UserDefinedEnhancedEntityType.class, "MyVehicle", getUser()).getProperty(NAME).isValid()).isTrue();
    }

    @Test
    public void base_type_must_be_the_name_of_a_domain_entity_type() {
        assertThat(domainTypes).extracting(Class::getName).as("No domain entity type is named no.such.Type.").doesNotContain("no.such.Type");
        final var withoutBaseType = new_composite(UserDefinedEnhancedEntityType.class, "MyVehicle", getUser()).setBaseType("no.such.Type");
        assertThat(withoutBaseType.getProperty(BASE_TYPE).getFirstFailure())
                .hasMessage(ERR_NO_SUCH_DOMAIN_TYPE.formatted("no.such.Type"));

        // A positive control: the name of a domain entity type is accepted.
        assertThat(domainTypes).as("TgVehicle is a domain entity type.").contains(TgVehicle.class);
        final var withBaseType = new_composite(UserDefinedEnhancedEntityType.class, "MyVehicle", getUser()).setBaseType(TgVehicle.class.getName());
        assertThat(withBaseType.getProperty(BASE_TYPE).isValid()).isTrue();
        assertThat(withBaseType.getBaseType()).isEqualTo(TgVehicle.class.getName());
    }

}
