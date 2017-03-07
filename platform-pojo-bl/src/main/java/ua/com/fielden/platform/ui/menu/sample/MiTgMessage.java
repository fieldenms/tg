package ua.com.fielden.platform.ui.menu.sample;

import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.sample.domain.TgMessage;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;

@EntityType(TgMessage.class)
public class MiTgMessage extends MiWithConfigurationSupport<TgMessage> {
}