package ua.com.fielden.platform.ioc;

import java.util.HashSet;
import java.util.Set;

import com.google.inject.*;

import com.google.inject.Module;
import ua.com.fielden.platform.basic.config.Workflows;

/**
 * A factory for instantiation of Guice injector with correctly initialised TG applications modules, which support contract {@link IModuleWithInjector}.
 * <p>
 * Guice instantiation stage is determined by the constructor argument {@code workflow}.
 * This governs how Guice instantiates singletons (refer <a href="https://github.com/google/guice/wiki/Scopes#eager-singletons">Eager Singletons</a> for more details).
 * The default value is {@code Workflows#development}, mapping to {@link Stage#DEVELOPMENT}.
 *
 * @author TG Team
 *
 */
public final class ApplicationInjectorFactory {

    private final Workflows workflow;
    private Injector injector;
    private final Set<Module> modules;

    public ApplicationInjectorFactory(final Workflows workflow) {
        this.workflow = workflow;
        this.modules = new HashSet<>();

        modules.add(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Workflows.class).toInstance(workflow);
            }
        });
    }
    
    public ApplicationInjectorFactory() {
        this(Workflows.development);
    }
    
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

        final Stage stage = Workflows.deployment == workflow || Workflows.vulcanizing == workflow ? Stage.PRODUCTION : Stage.DEVELOPMENT;
        injector = Guice.createInjector(stage, modules.toArray(new Module[] {}));

        for (final Module module : modules) {
            if (module instanceof IModuleWithInjector moduleWithInjector) {
                moduleWithInjector.setInjector(injector);
            }
        }

        return injector;
    }

}
