package ua.com.fielden.platform.serialisation.api.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;

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
        return Collections.unmodifiableList(types);
    }

}
