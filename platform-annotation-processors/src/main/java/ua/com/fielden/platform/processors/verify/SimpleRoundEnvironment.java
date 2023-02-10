package ua.com.fielden.platform.processors.verify;

import javax.annotation.processing.RoundEnvironment;

/**
 * Simplest implementation of {@link AbstractRoundEnvironment}.
 *
 * @author TG Team
 */
public class SimpleRoundEnvironment extends AbstractRoundEnvironment {

    public SimpleRoundEnvironment(final RoundEnvironment roundEnv) {
        super(roundEnv);
    }

}
