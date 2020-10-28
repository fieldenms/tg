package ua.com.fielden.platform.gis.gps.actors;

import static org.apache.commons.lang3.RegExUtils.replaceAll;
import static ua.com.fielden.platform.gis.gps.actors.Transliterator.transliterate;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Log4JLoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.Period;

import com.google.inject.Injector;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.gis.gps.AbstractAvlMachine;
import ua.com.fielden.platform.gis.gps.AbstractAvlMachineModuleTemporalAssociation;
import ua.com.fielden.platform.gis.gps.AbstractAvlMessage;
import ua.com.fielden.platform.gis.gps.AbstractAvlModule;
import ua.com.fielden.platform.gis.gps.AvlData;
import ua.com.fielden.platform.gis.gps.IModuleLookup;
import ua.com.fielden.platform.gis.gps.Option;
import ua.com.fielden.platform.gis.gps.factory.DefaultGpsHandlerFactory;
import ua.com.fielden.platform.gis.gps.server.ServerTeltonika;
import ua.com.fielden.platform.utils.Pair;

/**
 * A container for all actors that maintains messages.
 * 
 * @author TG Team
 * 
 */
public abstract class AbstractActors<MESSAGE extends AbstractAvlMessage, MACHINE extends AbstractAvlMachine<MESSAGE>, MODULE extends AbstractAvlModule, ASSOCIATION extends AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE>, MACHINE_ACTOR extends AbstractAvlMachineActor<MESSAGE, MACHINE>, MODULE_ACTOR extends AbstractAvlModuleActor<MESSAGE, MACHINE, MODULE, ASSOCIATION>> implements IModuleLookup<MODULE> {
    private final static Logger logger = Logger.getLogger(AbstractActors.class);

    private final ActorSystem system;
    // an actors that represent machine processors, that contain last messages
    private final Map<Long, ActorRef> machineActors;
    private final ActorRef machinesCounter;
    // an actors that represent module processors
    private final Map<String, Pair<MODULE, ActorRef>> moduleActors; // by IMEI
    private final ActorRef modulesCounter;

    private final Map<MACHINE, MESSAGE> machinesWithLastMessages;
    private final Map<MODULE, List<ASSOCIATION>> modulesWithAssociations;
    private final String gpsHost;
    private final Integer gpsPort;
    private final Injector injector;

    /**
     * Creates an actor system responsible for processing messages and getting efficiently a state from it (e.g. last machine message).
     * 
     */
    public AbstractActors(final Injector injector, final Map<MACHINE, MESSAGE> machinesWithLastMessages, final Map<MODULE, List<ASSOCIATION>> modulesWithAssociations, final String gpsHost, final Integer gpsPort) {
        this.gpsHost = gpsHost;
        this.gpsPort = gpsPort;
        this.injector = injector;

        this.system = ActorSystem.create("actors");

        this.machinesWithLastMessages = new HashMap<MACHINE, MESSAGE>(machinesWithLastMessages);
        machinesCounter = MachinesCounterActor.create(system, keys(this.machinesWithLastMessages.keySet()), this);

        this.modulesWithAssociations = new HashMap<MODULE, List<ASSOCIATION>>(modulesWithAssociations);
        modulesCounter = ModulesCounterActor.create(system, keys(this.modulesWithAssociations.keySet()), this);

        this.machineActors = new HashMap<>();
        this.moduleActors = new ConcurrentHashMap<>(); // needed thread-safe map not to produce conflicts by dataReceived() and promoteChangedModule()
    }

    private static <T extends AbstractEntity<String>> Set<String> keys(final Set<T> keySet) {
        final Set<String> keys = new LinkedHashSet<String>();
        for (final T entity : keySet) {
            keys.add(entity.getKey());
        }
        return keys;
    }

    /**
     * Starts all actors based on a state of initialised actor system.
     * 
     * @param injector
     * @return
     */
    public AbstractActors<MESSAGE, MACHINE, MODULE, ASSOCIATION, MACHINE_ACTOR, MODULE_ACTOR> startActorSystem() {
        logger.info("\tModule actors starting...");
        for (final Entry<MODULE, List<ASSOCIATION>> moduleAssociations : this.modulesWithAssociations.entrySet()) {
            registerAndStartModuleActor(moduleAssociations.getKey(), moduleAssociations.getValue());
        }
        return this;
    }

    /**
     * Creates, registers and starts an actor responsible for module processing.
     * 
     * @param injector
     * @param moduleAssociations
     */
    public void registerAndStartModuleActor(final MODULE module, final List<ASSOCIATION> associations) {
        final ActorRef moduleActor = create(injector, system, module, associations, modulesCounter);
        this.moduleActors.put(module.getKey(), new Pair<>(module, moduleActor));
    }

    /**
     * Creates, registers and starts an actor responsible for machine processing.
     * 
     * @param injector
     * @param moduleAssociations
     */
    public void registerAndStartMachineActor(final MACHINE machine, final MESSAGE lastMessage) {
        this.machineActors.put(machine.getId(), create(injector, system, machine, lastMessage, machinesCounter));
    }

    /**
     * Creates module actor under a "system" supervisor.
     * 
     * @param system
     * @param hibUtil
     * @param moduleAssociations
     * @return
     */
    protected final ActorRef create(final Injector injector, final ActorSystem system, final MODULE module, final List<ASSOCIATION> associations, final ActorRef machinesCounterRef) {
        final ActorRef moduleActorRef = system.actorOf(new Props(new UntypedActorFactory() {
            private static final long serialVersionUID = -6677642334839003771L;

            @Override
            public UntypedActor create() {
                return createModuleActor(injector, module, associations, machinesCounterRef);
            }
        }), createName(module));
        return moduleActorRef;
    }

    /**
     * Creates machine actor under a "system" supervisor.
     * 
     * @param system
     * @param hibUtil
     * @param machineAndMessage
     * @return
     */
    protected final ActorRef create(final Injector injector, final ActorSystem system, final MACHINE machine, final MESSAGE lastMessage, final ActorRef machinesCounterRef) {
        final ActorRef machineActorRef = system.actorOf(new Props(new UntypedActorFactory() {
            private static final long serialVersionUID = -6677642334839003771L;

            @Override
            public UntypedActor create() {
                return createMachineActor(injector, machine, lastMessage, machinesCounterRef);
            }
        }), createName(machine));
        return machineActorRef;
    }

    /**
     * Creates an instance of concrete {@link AbstractAvlMachineActor} implementation.
     * 
     * @param injector
     * @param machineAndMessage
     * @return
     */
    protected abstract MACHINE_ACTOR createMachineActor(final Injector injector, final MACHINE machine, final MESSAGE lastMessage, final ActorRef machinesCounterRef);

    /**
     * Creates an instance of concrete {@link AbstractAvlMachineActor} implementation.
     * 
     * @param injector
     * @param moduleAssociations
     * @return
     */
    protected abstract MODULE_ACTOR createModuleActor(final Injector injector, final MODULE module, final List<ASSOCIATION> associations, final ActorRef modulesCounterRef);

    /**
     * Creates a machine actor name using a transliterated version of machine's key.
     * 
     * @param machine
     * @return
     */
    private static final Pattern SPACE_OR_SLASH = Pattern.compile("\\s|/");
    private final String createName(final MACHINE machine) {
        return "machine_" + replaceAll(transliterate(machine.getKey()), SPACE_OR_SLASH, "_").toLowerCase();
    }

    /**
     * Creates a module actor name using its IMEI.
     * 
     * @param module
     * @return
     */
    private final String createName(final MODULE module) {
        return "module_" + module.getKey();
    }

    public ActorRef getMachineActor(final Long machineId) {
        return machineActors.get(machineId);
    }

    public ActorRef getMachineActor(final MACHINE machine) {
        return getMachineActor(machine.getId());
    }

    public boolean isModuleRegistered(final String imei) {
        return moduleActors.get(imei) != null;
    }

    public ActorRef getModuleActor(final String imei) {
        return isModuleRegistered(imei) ? moduleActors.get(imei).getValue() : null;
    }

    /**
     * An API method for handling received module data.
     * 
     * @param imei
     * @param data
     */
    public void dataReceived(final String imei, final AvlData[] data) {
        final ActorRef actor = getModuleActor(imei);
        if (actor != null) { // the module is registered
            actor.tell(data, null);
        } else {
            logger.warn("The module with imei [" + imei + "] is no longer registered. " + "This is most likely caused by the changes of IMEI for the module. "
                    + "As soon as old cached message channel with old IMEI will be dead and new channel will handle login -- "
                    + "you will see the regular 'Unrecognised IMEI' message.");
        }
    }

    private final String findModuleIMEIbyId(final Long id) {
        for (final String imei : moduleActors.keySet()) {
            if (moduleActors.get(imei).getKey().getId().equals(id)) {
                return imei;
            }
        }
        return null;
    }

    /**
     * Promotes changed module to the server cache.
     * 
     * @param module
     */
    public void promoteChangedModule(final MODULE module) {
        final String prevIMEI = findModuleIMEIbyId(module.getId());

        // final MODULE prevModule = moduleActors.get(prevIMEI).getKey();
        final ActorRef prevModuleActor = moduleActors.get(prevIMEI).getValue();

        prevModuleActor.tell(new ChangedModule<MODULE>(module), null);
        moduleActors.remove(prevIMEI);
        this.moduleActors.put(module.getKey(), new Pair<>(module, prevModuleActor));
    }

    /**
     * An API method for handling received machine data.
     * 
     * @param machine
     * @param data
     */
    public Map<Long, List<MESSAGE>> getLastMessagesUpdate(final Map<Long, Date> machinesTiming) {
        final DateTime st = new DateTime();
        final Timeout timeout = new Timeout(Duration.create(50000, "seconds"));
        // TODO use several (or even one) existing LastMessageRetrieverActors? just not to create new ones every time
        final Future<Object> future = Patterns.ask(LastMessageRetrieverActor.create(system, machineActors), new MachinesTiming(machinesTiming), timeout);
        try {
            final Map<Long, List<MESSAGE>> result = (Map<Long, List<MESSAGE>>) Await.result(future, timeout.duration());
            final Period p = new Period(st, new DateTime());
            logger.info("Last messages [" + result.size() + "] for " + machinesTiming.size() + " machines retrieved in " + (p.getHours() == 0 ? "" : p.getHours() + " h ")
                    + (p.getMinutes() == 0 ? "" : p.getMinutes() + " m ") + p.getSeconds() + " s " + p.getMillis() + " ms.");
            return result;
        } catch (final Exception e) {
            logger.error(e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * Performs some custom action after the actors has been started.
     */
    protected void machineActorsStartedPostAction() {
        startNettyGpsServer();
    }

    /**
     * Performs some custom action after the module actors has been started.
     */
    protected void moduleActorsStartedPostAction() {
        logger.info("\tMachine actors starting...");
        for (final Entry<MACHINE, MESSAGE> machineAndMessage : this.machinesWithLastMessages.entrySet()) {
            registerAndStartMachineActor(machineAndMessage.getKey(), machineAndMessage.getValue());
        }
    }

    /**
     * Starts Netty Gps server.
     */
    protected void startNettyGpsServer() {
        //////// start netty-based GPS server
        InternalLoggerFactory.setDefaultFactory(new Log4JLoggerFactory());
        final ChannelGroup allChannels = new DefaultChannelGroup("gps-server");
        final ConcurrentHashMap<String, Channel> existingConnections = new ConcurrentHashMap<>();
        final ServerTeltonika serverTeltonika = new ServerTeltonika(gpsHost, gpsPort, existingConnections, allChannels, new DefaultGpsHandlerFactory<MESSAGE, MACHINE, MODULE, ASSOCIATION, MACHINE_ACTOR, MODULE_ACTOR>(existingConnections, allChannels, this)) {
            @Override
            public void run() {
                super.run();

                nettyServerStartedPostAction();
            }
        };
        new Thread(serverTeltonika).start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
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

    @Override
    public Option<MODULE> get(final String imei) {
        return isModuleRegistered(imei) ? new Option<MODULE>(moduleActors.get(imei).getKey()) : new Option<MODULE>(null);
    }

    /**
     * Gets an response from actor using blocking (!). If response is an exception -> returns unsuccessful result, otherwise returns result that wraps response object.
     * 
     * @param actor
     * @param message
     * @param waitSeconds
     * @return
     */
    public static Result getResponseFromActor(final ActorRef actor, final Object message, final int waitSeconds) {
        final DateTime st = new DateTime();
        final Timeout timeout = new Timeout(Duration.create(waitSeconds, "seconds"));

        final Future<Object> future = Patterns.ask(actor, message, timeout);
        try {
            final Object result = Await.result(future, timeout.duration());
            final Period p = new Period(st, new DateTime());
            logger.info("Operation has been done in " + (p.getHours() == 0 ? "" : p.getHours() + " h ") + (p.getMinutes() == 0 ? "" : p.getMinutes() + " m ") + p.getSeconds()
                    + " s " + p.getMillis() + " ms.");

            if (result instanceof Exception) {
                final Exception ex = (Exception) result;
                logger.error(ex);
                return Result.failure((Exception) result);
            } else { // in other case -- successful
                return Result.successful(result);
            }
        } catch (final Exception ex) {
            logger.error(ex);
            return Result.failure(ex);
        }
    }
}
