package ua.com.fielden.platform.security;

/**
 * Provides implementation for start/stop authorisation functionality.
 * <p>
 * Serves as a basis for implementing other authorisation models.
 *
 * @author TG Team
 *
 */
public abstract class AbstractAuthorisationModel implements IAuthorisationModel {

    private boolean started = false;

    @Override
    public boolean isStarted() {
	return started;
    }

    @Override
    public void start() {
	started = true;
    }

    @Override
    public void stop() {
	started = false;
    }
}
