package ua.com.fielden.platform.web_api;

import org.junit.Test;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import static org.assertj.core.api.Assertions.assertThat;
import static ua.com.fielden.platform.reflection.Finder.isPropertyPresent;
import static ua.com.fielden.platform.web_api.UserDefinedCalculatedProperty.NAME;
import static ua.com.fielden.platform.web_api.validators.UserDefinedCalculatedPropertyNameValidator.ERR_PROPERTY_OF_BASE_TYPE;

/// Tests for the validation of [UserDefinedCalculatedProperty].
///
public class UserDefinedCalculatedPropertyTest extends AbstractDaoTestCase {

    @Test
    public void name_cannot_be_that_of_a_property_of_the_base_type() {
        final var entityType = save(new_composite(UserDefinedEnhancedEntityType.class, "MyVehicle", getUser()).setBaseType(TgVehicle.class.getName()));

        assertThat(isPropertyPresent(TgVehicle.class, "price")).as("TgVehicle has property price.").isTrue();
        final var propertyOfBaseType = new_composite(UserDefinedCalculatedProperty.class, entityType, "price");
        assertThat(propertyOfBaseType.getProperty(NAME).getFirstFailure())
                .hasMessage(ERR_PROPERTY_OF_BASE_TYPE.formatted("price", "TgVehicle"));

        // A positive control: a name that the base type does not have is accepted.
        assertThat(isPropertyPresent(TgVehicle.class, "myNumber")).as("TgVehicle has no property myNumber.").isFalse();
        final var newProperty = new_composite(UserDefinedCalculatedProperty.class, entityType, "myNumber");
        assertThat(newProperty.getProperty(NAME).isValid()).isTrue();
        assertThat(newProperty.getName()).isEqualTo("myNumber");
    }

}
