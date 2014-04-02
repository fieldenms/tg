package ua.com.fielden.platform.ioc;

import java.util.HashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.ioc.IModuleWithInjector;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * A factory for instantiation of Guice injector with correctly initialised TG applications modules, which support contract {@link IModuleWithInjector}.
 * 
 * @author TG Team
 * 
 */
public final class ApplicationInjectorFactory {

    private Injector injector;

    private final Set<Module> modules = new HashSet<Module>();

    public ApplicationInjectorFactory add(final Module... otherModules) {
        if (injector != null) {
            throw new IllegalStateException("Injector has already been created. No module modification is permitted.");
        }

        for (final Module module : otherModules) {
            this.modules.add(module);
        }

        return this;
    }

    public Injector getInjector() {
        if (injector != null) {
            return injector;
        }

        injector = Guice.createInjector(modules.toArray(new Module[] {}));

        for (final Module module : modules) {
            if (module instanceof IModuleWithInjector) {
                ((IModuleWithInjector) module).setInjector(injector);
            }
        }

        return injector;
    }

}
