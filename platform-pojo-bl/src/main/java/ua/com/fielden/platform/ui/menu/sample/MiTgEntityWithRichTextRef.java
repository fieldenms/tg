package ua.com.fielden.platform.ui.menu.sample;

import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.sample.domain.TgEntityWithRichTextRef;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;

/**
 * Main menu item representing an entity centre for {@link TgEntityWithRichTextRef}.
 *
 * @author TG Team
 */
@EntityType(TgEntityWithRichTextRef.class)
public class MiTgEntityWithRichTextRef extends MiWithConfigurationSupport<TgEntityWithRichTextRef> {
}
