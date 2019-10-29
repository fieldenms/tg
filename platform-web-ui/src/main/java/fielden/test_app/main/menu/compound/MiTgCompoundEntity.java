package fielden.test_app.main.menu.compound;

import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntity;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;

/**
 * Main menu item representing an entity centre for {@link TgCompoundEntity}.
 *
 * @author TG Team
 *
 */
@EntityType(TgCompoundEntity.class)
public class MiTgCompoundEntity extends MiWithConfigurationSupport<TgCompoundEntity> {

}