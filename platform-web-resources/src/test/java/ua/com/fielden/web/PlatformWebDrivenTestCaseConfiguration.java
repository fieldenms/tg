package ua.com.fielden.web;

import org.restlet.data.Protocol;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.ProvidedSerialisationClassProvider;
import ua.com.fielden.platform.serialisation.json.TgObjectMapper;
import ua.com.fielden.platform.test.IDbDrivenTestCaseConfiguration;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.test.CommonRestFactoryModuleForTestingPurposes;
import ua.com.fielden.platform.web.test.IWebDrivenTestCaseConfiguration;
import ua.com.fielden.web.entities.InspectedEntity;

import com.google.inject.Injector;

/**
 * A platform specific implementation for web-driven test configuration.
 *
 * @author TG Team
 *
 */
public class PlatformWebDrivenTestCaseConfiguration implements IWebDrivenTestCaseConfiguration {
    private final RestClientUtil restClientUtil;
    private final Injector injector;
    private final EntityFactory entityFactory;
    private final ISerialiser clientSerialiser;
    private RestServerUtil restServerUtil;

    private ISerialisationClassProvider serialisationClassProvider = new ProvidedSerialisationClassProvider(new Class[] { InspectedEntity.class });

    public PlatformWebDrivenTestCaseConfiguration() {
        restClientUtil = new RestClientUtil(Protocol.HTTP, "localhost", PORT, "v1", "test");
        injector = new ApplicationInjectorFactory().add(new CommonRestFactoryModuleForTestingPurposes(restClientUtil, serialisationClassProvider)).getInjector();
        entityFactory = injector.getInstance(EntityFactory.class);
        clientSerialiser = injector.getInstance(ISerialiser.class);
        restClientUtil.initSerialiser(clientSerialiser);
    }

    @Override
    public EntityFactory entityFactory() {
        return entityFactory;
    }

    @Override
    public Injector injector() {
        return injector;
    }

    @Override
    public RestClientUtil restClientUtil() {
        return restClientUtil;
    }

    @Override
    public RestServerUtil restServerUtil() {
        return restServerUtil;
    }

    @Override
    public void setDbDrivenTestConfiguration(final IDbDrivenTestCaseConfiguration config) {
        restServerUtil = new RestServerUtil(config.getInjector().getInstance(ISerialiser.class), config.getInjector().getInstance(TgObjectMapper.class));
    }

}
