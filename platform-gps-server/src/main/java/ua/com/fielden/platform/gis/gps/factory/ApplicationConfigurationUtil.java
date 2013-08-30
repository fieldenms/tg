package ua.com.fielden.platform.gis.gps.factory;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Log4JLoggerFactory;

import ua.com.fielden.platform.gis.gps.AbstractAvlMachine;
import ua.com.fielden.platform.gis.gps.AbstractAvlMessage;
import ua.com.fielden.platform.gis.gps.actors.AbstractActors;
import ua.com.fielden.platform.gis.gps.actors.AbstractAvlMachineActor;
import ua.com.fielden.platform.gis.gps.monitoring.DefaultMachineMonitoringProvider;
import ua.com.fielden.platform.gis.gps.monitoring.IMachineMonitoringProvider;
import ua.com.fielden.platform.gis.gps.server.ServerTeltonika;

import com.google.inject.Injector;

/**
 * An utility class to start GPS server services (like Netty GPS server and Machine actors).
 *
 * @author TG Team
 *
 * @param <T>
 * @param <M>
 * @param <N>
 */
public abstract class ApplicationConfigurationUtil<T extends AbstractAvlMessage, M extends AbstractAvlMachine<T>, N extends AbstractAvlMachineActor<T, M>> {
    private final static Logger logger = Logger.getLogger(ApplicationConfigurationUtil.class);

    /** An utility class to start GPS server services (like Netty GPS server and Machine actors). */
    public final ServerTeltonika startGpsServices(final Properties props, final Injector injector) {
	// get all vehicles with their latest GPS message
	// the resultant map is used by both the GPS message handler for updating and CurrentMachinesState resource for read
	// thus a concurrent map is used to synchronize read/write operations
	final Map<String, M> machineCache = fetchMachinesToTrack(injector);
	// create and start all actors responsible for message handling
	final AbstractActors<T, M, N> actors = createActors(machineCache.values());

	final DefaultMachineMonitoringProvider<T, M, N> mmProvider = (DefaultMachineMonitoringProvider<T, M, N>) injector.getInstance(IMachineMonitoringProvider.class);
	mmProvider.setActors(actors);

	//////// start netty-based GPS server
	InternalLoggerFactory.setDefaultFactory(new Log4JLoggerFactory());
	final ServerTeltonika gpsServer = new ServerTeltonika(props.getProperty("gps.host"), Integer.valueOf(props.getProperty("gps.port")), new DefaultGpsHandlerFactory<T, M, N>(machineCache, actors));
	new Thread(gpsServer).start();
	return gpsServer;
    }

    /** Creates specific {@link AbstractActors} implementation. */
    protected abstract AbstractActors<T, M, N> createActors(final Collection<M> machines);

    /** Fetches all machines from the database that should be used for message processing. */
    protected abstract Map<String, M> fetchMachinesToTrack(final Injector injector);

    public static Logger getLogger() {
	return logger;
    }
}
