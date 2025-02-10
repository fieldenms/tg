package ua.com.fielden.platform.ui.menu.sample;

import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.sample.domain.TgNote;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;

/**
 * Main menu item representing an entity centre for {@link TgNote}.
 *
 * @author TG Team
 */
@EntityType(TgNote.class)
public class MiTgNote extends MiWithConfigurationSupport<TgNote> {
}
