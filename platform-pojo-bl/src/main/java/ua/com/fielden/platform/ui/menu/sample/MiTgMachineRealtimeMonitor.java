package ua.com.fielden.platform.ui.menu.sample;

import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.sample.domain.TgMachine;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;

@EntityType(TgMachine.class)
public class MiTgMachineRealtimeMonitor extends MiWithConfigurationSupport<TgMachine> {
}