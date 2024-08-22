package ua.com.fielden.platform.serialisation.api;

import com.google.inject.ImplementedBy;
import ua.com.fielden.platform.serialisation.api.impl.DefaultSerialisationClassProvider;

import java.util.List;

/**
 * A contract that should be implemented in order to provide serialiser implementations with classes to be registered for serialisation.
 * 
 * @author TG Team
 * 
 */
@ImplementedBy(DefaultSerialisationClassProvider.class)
public interface ISerialisationClassProvider {
    List<Class<?>> classes();
}
