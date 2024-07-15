package ua.com.fielden.platform.serialisation.api;

import java.util.List;

/**
 * A contract that should be implemented in order to provide serialiser implementations with classes to be registered for serialisation.
 * 
 * @author TG Team
 * 
 */
public interface ISerialisationClassProvider {
    List<Class<?>> classes();
}