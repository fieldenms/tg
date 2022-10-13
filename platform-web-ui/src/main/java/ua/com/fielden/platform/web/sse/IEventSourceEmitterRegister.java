package ua.com.fielden.platform.web.sse;

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
     * Registers an {@link IEventSourceEmitter} instance for a new SSE client, identified by {@code user} and {@code sseUid}.
     *
     * @param user
     * @param sseUid
     * @param emitter
     * @return successful result if a an emitter was registered
     */
    Result registerEmitter(final User user, final String sseUid, final IEventSourceEmitter emitter);

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