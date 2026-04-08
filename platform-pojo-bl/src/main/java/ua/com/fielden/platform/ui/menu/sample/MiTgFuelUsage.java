package ua.com.fielden.platform.ui.menu.sample;

import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;

/// Main menu item representing an entity centre for [TgFuelUsage].
///
@EntityType(TgFuelUsage.class)
public class MiTgFuelUsage extends MiWithConfigurationSupport<TgFuelUsage> {
}
