package ua.com.fielden.platform.sample.domain.crit_gen;

import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Singleton;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;

@Singleton
public class StubSerialisationClassProvider implements ISerialisationClassProvider {

    @Override
    public List<Class<?>> classes() {
	// TODO Auto-generated method stub
	return new ArrayList<>();
    }

}
