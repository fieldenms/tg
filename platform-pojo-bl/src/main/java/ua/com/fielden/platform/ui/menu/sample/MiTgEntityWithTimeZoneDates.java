package ua.com.fielden.platform.ui.menu.sample;

import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.sample.domain.TgEntityWithTimeZoneDates;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
/**
 * Main menu item representing an entity centre for {@link TgEntityWithTimeZoneDates}.
 *
 * @author Developers
 *
 */
@EntityType(TgEntityWithTimeZoneDates.class)
public class MiTgEntityWithTimeZoneDates extends MiWithConfigurationSupport<TgEntityWithTimeZoneDates> {

}
