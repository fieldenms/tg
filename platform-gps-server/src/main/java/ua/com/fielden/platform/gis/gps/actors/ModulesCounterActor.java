package ua.com.fielden.platform.gis.gps.actors;

import java.util.LinkedHashSet;
import java.util.Set;

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

    private final Set<String> notStartedModulesKeys, modulesKeys;
    private final AbstractActors<?, ?, ?, ?, ?, ?> actors;
    private int startedModulesCount;

    public ModulesCounterActor(final Set<String> modulesKeys, final AbstractActors<?, ?, ?, ?, ?, ?> actors) {
        this.notStartedModulesKeys = new LinkedHashSet<>(modulesKeys);
        this.modulesKeys = new LinkedHashSet<>(modulesKeys);
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
    public static ActorRef create(final ActorSystem system, final Set<String> modulesKeys, final AbstractActors<?, ?, ?, ?, ?, ?> actors) {
        final ActorRef modulesCounterRef = system.actorOf(new Props(new UntypedActorFactory() {
            private static final long serialVersionUID = -6677642334839003771L;

            public UntypedActor create() {
                return new ModulesCounterActor(modulesKeys, actors);
            }
        }), "modules_counter_actor");
        return modulesCounterRef;
    }

    @Override
    public void onReceive(final Object data) throws Exception {
        if (data instanceof ModuleActorStarted) {
            final ModuleActorStarted info = (ModuleActorStarted) data;
            if (!notStartedModulesKeys.isEmpty()) {
                // still starting is not completed
                if (notStartedModulesKeys.contains(info.getImei())) {
                    notStartedModulesKeys.remove(info.getImei());
                    startedModulesCount++;

                    logger.info("\t\t" + startedModulesCount + " / " + modulesKeys.size() + " [" + info.getImei() + "] => " + notStartedModulesKeys.size() + " modules left ["
                            + MachinesCounterActor.some(notStartedModulesKeys) + "]");
                    if (notStartedModulesKeys.isEmpty()) {
                        logger.info("\tModule actors started.");

                        this.actors.moduleActorsStartedPostAction();
                    }
                } else {
                    logger.error("Unrecognizable module (" + info.getImei() + ") has been obtained.");
                    unhandled(data);
                }
            } else {
                if (!modulesKeys.contains(info.getImei())) {
                    startedModulesCount++;

                    modulesKeys.add(info.getImei());
                    logger.info("\t\t" + startedModulesCount + " / " + modulesKeys.size() + " [" + info.getImei()
                            + "] => Additional module actor has been created after registration of new module.");
                } else {
                    logger.warn("\t\t" + startedModulesCount + " / " + modulesKeys.size() + " [" + info.getImei() + "] => Existing module actor has been died and restarted.");
                }
            }
        } else {
            logger.error("Unrecognizable message (" + data + ") has been obtained.");
            unhandled(data);
        }
    }
}
