package ua.com.fielden.platform.web.sse;

import java.util.function.Supplier;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.User;

/**
 * A contract to manage emitters for SSE clients.
 *
 * @author TG Team
 *
 */
public interface IEventSourceEmitterRegister {

    /**
     * Registers an {@link IEventSourceEmitter} instance for a new SSE client, identified by {@code user} and {@code sseUid}, but only if it was not registered already.
     * {@code emitterFactory} abstract out the logic for constructing a new instance of the emitter that needs to be registered.
     *
     * @param user
     * @param sseUid
     * @param emitterFactory
     * @return successful result if a an emitter was registered
     */
    Result registerEmitter(final User user, final String sseUid, final Supplier<IEventSourceEmitter> emitterFactory);

    /**
     * Returns an {@link IEventSourceEmitter} instance, associated with {@code user} and {@code sseUid}.
     *
     * @param user
     * @param sseUid
     * @return
     */
    IEventSourceEmitter getEmitter(final User user, final String sseUid);

    /**
     * Closes and deregisters an {@link IEventSourceEmitter} instance, associated with {@code user} and {@code sseUid}.
     *
     * @param user
     * @param sseUid
     */
    void deregisterEmitter(final User user, final String sseUid);

}