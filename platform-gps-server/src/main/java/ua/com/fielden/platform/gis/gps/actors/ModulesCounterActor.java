package ua.com.fielden.platform.gis.gps.actors;

import org.apache.log4j.Logger;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;

/**
 * This actors counts started modules actors and provides necessary events after all actors have been started.
 *
 */
public class ModulesCounterActor extends UntypedActor {
    private final Logger logger = Logger.getLogger(ModulesCounterActor.class);

    private final int modulesCount;
    private final AbstractActors<?, ?, ?, ?, ?, ?> actors;
    private int startedModulesCount;

    public ModulesCounterActor(final int modulesCount, final AbstractActors<?, ?, ?, ?, ?, ?> actors) {
	this.modulesCount = modulesCount;
	this.startedModulesCount = 0;
	this.actors = actors;
    }

    /**
     * Creates an actor that counts started module actors.
     *
     * @param system
     * @param modulesCount
     * @return
     */
    public static ActorRef create(final ActorSystem system, final int modulesCount, final AbstractActors<?, ?, ?, ?, ?, ?> actors) {
	final ActorRef modulesCounterRef = system.actorOf(new Props(new UntypedActorFactory() {
	    private static final long serialVersionUID = -6677642334839003771L;

	    public UntypedActor create() {
		return new ModulesCounterActor(modulesCount, actors);
	    }
	}), "modules_counter_actor");
	return modulesCounterRef;
    }

    @Override
    public void onReceive(final Object data) throws Exception {
	if (data instanceof ModuleActorStarted) {
	    final ModuleActorStarted info = (ModuleActorStarted) data;
	    startedModulesCount++;

	    logger.info("\t\t" + startedModulesCount + " / " + modulesCount + " [" + info.getImei() + "]");
	    if (startedModulesCount > modulesCount) {
		logger.info("\t\t[Additional module actor created after registration of new module].");
	    } else if (startedModulesCount == modulesCount) {
		logger.info("\tModule actors started.");

		this.actors.moduleActorsStartedPostAction();
	    }
	} else {
	    logger.error("Unrecognizable message (" + data + ") has been obtained.");
	    unhandled(data);
	}
    }
}
