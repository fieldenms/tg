package ua.com.fielden.platform.web.sse;

/**
 * A contract to manage clients for SSE.
 *
 * @author TG Team
 *
 */
public interface IEventSourceEmitterRegister {

    /**
     * Registers new client represented with {@link IEventSourceEmitter} instance.
     *
     * @param uid
     * @param emitter
     * @return
     */
    IEventSourceEmitterRegister registerEmitter(final String uid, final IEventSourceEmitter emitter);

    /**
     * Returns {@link IEventSourceEmitter} instance by client UID.
     *
     * @param uid
     * @return
     */
    IEventSourceEmitter getEmitter(final String uid);

    /**
     * Closes the {@link IEventSourceEmitter} that was registered with specified UID and removes it from the register.
     *
     * @param uid
     * @return
     */
    boolean deregisterEmitter(final String uid);

}