package ua.com.fielden.platform.web.view.master.api.helpers.impl;

import org.junit.Test;
import ua.com.fielden.platform.entity.annotation.DateOnly;
import ua.com.fielden.platform.entity.annotation.TimeOnly;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;
import ua.com.fielden.platform.web.view.master.exceptions.EntityMasterConfigurationException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static ua.com.fielden.platform.web.view.master.api.helpers.impl.WidgetSelector.ERR_INVALID_DATEPICKER_CHOICE;

/// Tests for the choice of editors in the Entity Master DSL.
///
public class WidgetSelectorTest {

    @Test
    public void date_picker_for_property_without_DateOnly_is_rejected_with_the_property_named_as_entity_dot_property() {
        final EntityMasterConfigurationException ex = assertThrows(EntityMasterConfigurationException.class,
                () -> new SimpleMasterBuilder<TgPersistentEntityWithProperties>().forEntity(TgPersistentEntityWithProperties.class).addProp("dateProp").asDatePicker());

        assertEquals(ERR_INVALID_DATEPICKER_CHOICE.formatted(TgPersistentEntityWithProperties.class.getSimpleName(), "dateProp", DateOnly.class.getSimpleName()), ex.getMessage());
    }

    @Test
    public void time_picker_for_property_without_TimeOnly_is_rejected_with_the_property_named_as_entity_dot_property() {
        final EntityMasterConfigurationException ex = assertThrows(EntityMasterConfigurationException.class,
                () -> new SimpleMasterBuilder<TgPersistentEntityWithProperties>().forEntity(TgPersistentEntityWithProperties.class).addProp("dateProp").asTimePicker());

        assertEquals(ERR_INVALID_DATEPICKER_CHOICE.formatted(TgPersistentEntityWithProperties.class.getSimpleName(), "dateProp", TimeOnly.class.getSimpleName()), ex.getMessage());
    }

}
