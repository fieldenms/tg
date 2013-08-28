package ua.com.fielden.platform.gis.gps.monitoring;

import java.util.Date;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.gis.gps.AbstractAvlMachine;
import ua.com.fielden.platform.gis.gps.AbstractAvlMessage;
import ua.com.fielden.platform.gis.gps.actors.AbstractAvlMachineActor;

/**
 * A contract to provide access to machine related monitoring information that gets updated asynchronously at runtime during receiving of GPS messages.
 *
 * @author TG Team
 *
 */
public interface IMachineMonitoringProvider<T extends AbstractAvlMessage, M extends AbstractAvlMachine<T>, N extends AbstractAvlMachineActor<T, M>> {
    Map<Long, List<T>> getLastMessagesUpdate(final Map<Long, Date> machinesTiming);
}
