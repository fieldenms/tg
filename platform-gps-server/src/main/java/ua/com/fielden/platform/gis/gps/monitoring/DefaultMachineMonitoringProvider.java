package ua.com.fielden.platform.gis.gps.monitoring;

import java.util.Date;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.gis.gps.AbstractAvlMachine;
import ua.com.fielden.platform.gis.gps.AbstractAvlMessage;
import ua.com.fielden.platform.gis.gps.actors.AbstractActors;
import ua.com.fielden.platform.gis.gps.actors.AbstractAvlMachineActor;


/**
 * A default "carrier" implementation for {@link IMachineMonitoringProvider}.
 *
 * @author TG Team
 *
 */
public class DefaultMachineMonitoringProvider<T extends AbstractAvlMessage, M extends AbstractAvlMachine<T>, N extends AbstractAvlMachineActor<T, M>> implements IMachineMonitoringProvider<T, M, N> {
    private AbstractActors<T, M, N> actors;

    public void setActors(final AbstractActors<T, M, N> actors) {
	this.actors = actors;
    }

    public AbstractActors<T, M, N> getActors() {
	return actors;
    }

    @Override
    public Map<Long, List<T>> getLastMessagesUpdate(final Map<Long, Date> machinesTiming) {
        return actors.getLastMessagesUpdate(machinesTiming);
    }
}
