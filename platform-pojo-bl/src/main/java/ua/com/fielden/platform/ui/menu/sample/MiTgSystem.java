package ua.com.fielden.platform.ui.menu.sample;

import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.sample.domain.TgSystem;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;

/**
 * Menu item type for {@link TgSystem} centre.
 * 
 * @author TG Team
 *
 */
@EntityType(TgSystem.class)
public class MiTgSystem extends MiWithConfigurationSupport<TgSystem> {
    
}