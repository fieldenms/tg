package ua.com.fielden.platform.gis.gps.actors;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Log4JLoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.Period;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import ua.com.fielden.platform.gis.gps.AbstractAvlMachine;
import ua.com.fielden.platform.gis.gps.AbstractAvlMessage;
import ua.com.fielden.platform.gis.gps.AvlData;
import ua.com.fielden.platform.gis.gps.factory.DefaultGpsHandlerFactory;
import ua.com.fielden.platform.gis.gps.server.ServerTeltonika;
import ua.com.fielden.platform.utils.Pair;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import akka.pattern.Patterns;
import akka.util.Timeout;

import com.google.inject.Injector;


/**
 * A container for all actors that maintains messages.
 *
 * @author TG Team
 *
 */
public abstract class AbstractActors<T extends AbstractAvlMessage, M extends AbstractAvlMachine<T>, N extends AbstractAvlMachineActor<T, M>> {
    private final Logger logger = Logger.getLogger(AbstractActors.class);

    private final ActorSystem system;
    // an actors that represent machine processors, that contain last messages
    private final Map<Long, ActorRef> machineActors;
    private final ActorRef machinesCounter;

    private final Map<String, Pair<M, T>> cache;
    private final String gpsHost;
    private final Integer gpsPort;

    /**
     * Creates an actor system responsible for processing messages and getting efficiently a state from it (e.g. last machine message).
     *
     * @param machines -- a current machines in a system
     *
     * TODO IMPORTANT: creating of a new machine is not supported yet in server runtime.
     *
     */
    public AbstractActors(final Injector injector, final Map<String, Pair<M, T>> cache, final String gpsHost, final Integer gpsPort) {
	this.gpsHost = gpsHost;
	this.gpsPort = gpsPort;

	this.system = ActorSystem.create("machine-actors");

	this.cache = new HashMap<String, Pair<M, T>>(cache);
	machinesCounter = MachinesCounterActor.create(system, this.cache.size(), this);

	this.machineActors = new HashMap<>();
    }

    /**
     * Starts all machine actors based on a state of initialised actor system.
     *
     * @param injector
     * @return
     */
    public AbstractActors<T, M, N> startMachines(final Injector injector) {
	logger.info("\tMachine actors starting...");
	for (final Pair<M, T> machineAndMessage : this.cache.values()) {
	    this.machineActors.put(machineAndMessage.getKey().getId(), create(injector, system, machineAndMessage, machinesCounter));
	}
	return this;
    }

    /**
     * Creates machine actor under a "system" supervisor.
     *
     * @param system
     * @param hibUtil
     * @param machine
     * @return
     */
    protected final ActorRef create(final Injector injector, final ActorSystem system, final Pair<M, T> machineAndMessage, final ActorRef machinesCounterRef) {
	final ActorRef machineActorRef = system.actorOf(new Props(new UntypedActorFactory() {
	    private static final long serialVersionUID = -6677642334839003771L;

	    @Override
	    public UntypedActor create() {
		return createMachineActor(injector, machineAndMessage, machinesCounterRef);
	    }
	}), createName(machineAndMessage.getKey()));
	return machineActorRef;
    }

    /**
     * Creates an instance of concrete {@link AbstractAvlMachineActor} implementation.
     *
     * @param injector
     * @param machine
     * @return
     */
    protected abstract N createMachineActor(final Injector injector, final Pair<M, T> machineAndMessage, final ActorRef machinesCounterRef);

    /**
     * Creates a machine actor name using a transliterated version of machine's key.
     *
     * @param machine
     * @return
     */
    private final String createName(final M machine) {
	return "machine_" + Transliterator.transliterate(machine.getKey()).replaceAll(" ", "_").toLowerCase();
    }

    public ActorRef getMachineActor(final Long machineId) {
	return machineActors.get(machineId);
    }

    protected ActorRef getMachineActor(final M machine) {
	return getMachineActor(machine.getId());
    }

    /**
     * An API method for handling received machine data.
     *
     * @param machine
     * @param data
     */
    public void dataReceived(final M machine, final AvlData[] data) {
	getMachineActor(machine).tell(data, null);
    }

    /**
     * An API method for handling received machine data.
     *
     * @param machine
     * @param data
     */
    public Map<Long, List<T>> getLastMessagesUpdate(final Map<Long, Date> machinesTiming) {
	final DateTime st = new DateTime();
	final Timeout timeout = new Timeout(Duration.create(50000, "seconds"));
	// TODO use several (or even one) existing LastMessageRetrieverActors? just not to create new ones every time
	final Future<Object> future = Patterns.ask(LastMessageRetrieverActor.create(system, machineActors), new MachinesTiming(machinesTiming), timeout);
	try {
	    final Map<Long, List<T>> result = (Map<Long, List<T>>) Await.result(future, timeout.duration());
	    final Period p = new Period(st, new DateTime());
	    logger.info("Last messages for " + machinesTiming.size() + " machines retrieved in " + (p.getHours() == 0 ? "" : p.getHours() + " h ") + (p.getMinutes() == 0 ? "" : p.getMinutes() + " m ") + p.getSeconds() + " s " + p.getMillis() + " ms");
	    return result;
	} catch (final Exception e) {
	    logger.error(e);
	    throw new IllegalStateException(e);
	}
    }

    public Map<String, Pair<M, T>> getCache() {
	return cache;
    }

    /**
     * Performs some custom action after the actors has been started.
     */
    protected void actorsStartedPostAction() {
	startNettyGpsServer();
    }

    /**
     * Starts Netty Gps server.
     */
    protected void startNettyGpsServer() {
	//////// start netty-based GPS server
	InternalLoggerFactory.setDefaultFactory(new Log4JLoggerFactory());
	final ChannelGroup allChannels = new DefaultChannelGroup("gps-server");
	final ConcurrentHashMap<String, Channel> existingConnections = new ConcurrentHashMap<>();
	final ServerTeltonika serverTeltonika = new ServerTeltonika(gpsHost, gpsPort, existingConnections, allChannels, new DefaultGpsHandlerFactory<T, M, N>(existingConnections, allChannels, this)) {
	    @Override
	    public void run() {
	        super.run();

	        nettyServerStartedPostAction();
	    }
	};
	new Thread(serverTeltonika).start();

	Runtime.getRuntime().addShutdownHook(new Thread(){
	    @Override
	    public void run() {
		serverTeltonika.shutdown();
	    }
	});
    }

    protected void nettyServerStartedPostAction() {
    }

    protected ActorSystem getSystem() {
	return system;
    }

    protected Logger getLogger() {
	return logger;
    }
}
