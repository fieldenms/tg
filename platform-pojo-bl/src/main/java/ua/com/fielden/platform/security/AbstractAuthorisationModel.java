package ua.com.fielden.platform.security;

/// Provides an implementation of the start/stop re-entrancy state used by `AuthorisationInterceptor` to support nested authorisation scopes.
///
/// Serves as a basis for implementing other authorisation models.
///
/// The started state is deliberately held in a `ThreadLocal`.
/// Concrete models (such as `ServerAuthorisationModel`) are typically bound as `@Singleton`, so a single instance is shared across all request threads.
/// The state tracked here is per-call-stack — it exists only to suppress re-entrant authorisation checks within a single thread's call stack — and must therefore be confined to the executing thread.
/// Holding it in a plain instance field on a shared singleton would let one thread's in-progress authorisation
/// (during which `started` is `true` for the full duration of the protected call, including the downstream operation)
/// suppress the checks of every other concurrent thread, allowing an unauthorised call to proceed.
/// That is an authorisation bypass, so do not replace this `ThreadLocal` with a plain field.
///
public abstract class AbstractAuthorisationModel implements IAuthorisationModel {

    private final ThreadLocal<Boolean> started = ThreadLocal.withInitial(() -> false);

    @Override
    public boolean isStarted() {
        return started.get();
    }

    @Override
    public void start() {
        started.set(true);
    }

    @Override
    public void stop() {
        started.remove();
    }

}
