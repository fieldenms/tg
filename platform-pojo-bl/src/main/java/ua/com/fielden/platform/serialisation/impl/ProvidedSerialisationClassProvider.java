package ua.com.fielden.platform.serialisation.impl;

import java.util.Arrays;
import java.util.List;

/**
 * An implementation of {@link ISerialisationClassProvider}, which simply uses the provided into the constructor classes.
 * 
 * Convenient for custom usage such as demo apps and tests.
 * 
 * @author TG Team
 * 
 */
public class ProvidedSerialisationClassProvider implements ISerialisationClassProvider {

    private final List<Class<?>> types;

    public ProvidedSerialisationClassProvider(final Class<?>... classes) {
        types = Arrays.asList(classes);
    }

    @Override
    public List<Class<?>> classes() {
        return types;
    }

}
