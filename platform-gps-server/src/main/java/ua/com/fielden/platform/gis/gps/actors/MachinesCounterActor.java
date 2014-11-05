package ua.com.fielden.platform.gis.gps.actors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;

/**
 * This actors counts started machine actors and provides necessary events after all actors have been started.
 *
 */
public class MachinesCounterActor extends UntypedActor {
    private final Logger logger = Logger.getLogger(MachinesCounterActor.class);

    private final Set<String> notStartedMachinesKeys, machinesKeys;
    private final AbstractActors<?, ?, ?, ?, ?, ?, ?> actors;
    private int startedMachinesCount;

    public MachinesCounterActor(final Set<String> machinesKeys, final AbstractActors<?, ?, ?, ?, ?, ?, ?> actors) {
        this.notStartedMachinesKeys = new LinkedHashSet<>(machinesKeys);
        this.machinesKeys = new LinkedHashSet<>(machinesKeys);
        this.startedMachinesCount = 0;
        this.actors = actors;
    }

    /**
     * Creates an actor that counts started machine actors.
     *
     * @param system
     * @param machinesCount
     * @return
     */
    public static ActorRef create(final ActorSystem system, final Set<String> machinesKeys, final AbstractActors<?, ?, ?, ?, ?, ?, ?> actors) {
        final ActorRef machinesCounterRef = system.actorOf(new Props(new UntypedActorFactory() {
            private static final long serialVersionUID = -6677642334839003771L;

            @Override
            public UntypedActor create() {
                return new MachinesCounterActor(machinesKeys, actors);
            }
        }), "machines_counter_actor");
        return machinesCounterRef;
    }

    @Override
    public void onReceive(final Object data) throws Exception {
        if (data instanceof MachineActorStarted) {
            final MachineActorStarted info = (MachineActorStarted) data;
            if (!notStartedMachinesKeys.isEmpty()) {
                // still starting is not completed
                if (notStartedMachinesKeys.contains(info.getKey())) {
                    notStartedMachinesKeys.remove(info.getKey());
                    startedMachinesCount++;

                    logger.info("\t\t" + startedMachinesCount + " / " + machinesKeys.size() + " [" + info.getKey() + "] => " + notStartedMachinesKeys.size() + " machines left [" + some(notStartedMachinesKeys) + "]");
                    if (notStartedMachinesKeys.isEmpty()) {
                        logger.info("\tMachine actors started.");

                        this.actors.machineActorsStartedPostAction();
                    }
                } else {
                    logger.error("Unrecognizable machine (" + info.getKey() + ") has been obtained.");
                    unhandled(data);
                }
            } else {
                if (!machinesKeys.contains(info.getKey())) {
                    startedMachinesCount++;

                    machinesKeys.add(info.getKey());
                    logger.info("\t\t" + startedMachinesCount + " / " + machinesKeys.size() + " [" + info.getKey() + " -- " + info.getDesc() + "] => Additional machine actor has been created after registration of new machine.");
                } else {
                    logger.warn("\t\t" + startedMachinesCount + " / " + machinesKeys.size() + " [" + info.getKey() + " -- " + info.getDesc() + "] => Existing machine actor has been died and restarted.");
                }
            }
        } else {
            logger.error("Unrecognizable message (" + data + ") has been obtained.");
            unhandled(data);
        }
    }

    private static final int n = 2;

    public static String some(final Set<String> keys) {
        final List<String> someKeys = new ArrayList<>(keys);
        return someKeys.size() <= n ? toString(someKeys) : toString(someKeys.subList(0, n)) + "...";
    }

    private static String toString(final List<String> someKeys) {
        final StringBuilder sb = new StringBuilder();
        final Iterator<String> iter = someKeys.iterator();
        if (iter.hasNext()) {
            sb.append(iter.next());
        }
        while (iter.hasNext()) {
            sb.append(", " + iter.next());
        }
        return sb.toString();
    }
}
