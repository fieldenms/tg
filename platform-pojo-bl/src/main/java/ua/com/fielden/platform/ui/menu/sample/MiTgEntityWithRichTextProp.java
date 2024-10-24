package ua.com.fielden.platform.ui.menu.sample;

import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.sample.domain.TgEntityWithRichTextProp;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;

/**
 * Main menu item representing an entity centre for {@link TgEntityWithRichTextProp}.
 *
 * @author TG Team
 */
@EntityType(TgEntityWithRichTextProp.class)
public class MiTgEntityWithRichTextProp extends MiWithConfigurationSupport<TgEntityWithRichTextProp> {
}
