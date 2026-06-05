package ua.com.fielden.platform.web.factories.webui;

import com.google.inject.Injector;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import ua.com.fielden.platform.entity.AbstractEntityWithInputStream;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.FileProcessingResource;
import ua.com.fielden.platform.web.sse.IEventSourceEmitterRegister;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/// Factory to instantiate [FileProcessingResource].
///
public class FileProcessingResourceFactory<T extends AbstractEntityWithInputStream<?>> extends Restlet {
    protected final Injector injector;
    protected final Class<T> entityType;
    protected final Function<EntityFactory, T> entityCreator;
    protected final ICompanionObjectFinder companionFinder;
    protected final IEventSourceEmitterRegister eseRegister;

    protected final long fileSizeLimitBytes;
    protected final Set<MediaType> types = new HashSet<>();
    protected final IDeviceProvider deviceProvider;
    protected final IDates dates;
    protected final IUserProvider userProvider;

    public FileProcessingResourceFactory(
            final IEventSourceEmitterRegister eseRegister,
            final Injector injector,
            final Class<T> entityType,
            final Function<EntityFactory, T> entityCreator,
            final IDeviceProvider deviceProvider,
            final IDates dates,
            final long fileSizeLimitKb,
            final MediaType type, // at least one type is required
            final MediaType... types)
    {
        this.eseRegister = eseRegister;
        this.injector = injector;
        this.userProvider = injector.getInstance(IUserProvider.class);
        this.entityType = entityType;
        this.entityCreator = entityCreator;
        this.companionFinder = injector.getInstance(ICompanionObjectFinder.class);
        this.fileSizeLimitBytes = fileSizeLimitKb * 1024;
        this.types.add(type);
        this.types.addAll(Arrays.asList(types));
        this.deviceProvider = deviceProvider;
        this.dates = dates;
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.PUT.equals(request.getMethod())) {
            new FileProcessingResource<>(
                    eseRegister,
                    userProvider,
                    companionFinder.find(entityType),
                    injector.getInstance(EntityFactory.class),
                    entityCreator,
                    injector.getInstance(RestServerUtil.class),
                    fileSizeLimitBytes,
                    types,
                    deviceProvider,
                    dates,
                    injector.getInstance(ISerialiser.class),
                    getContext(), request, response).handle();
        }
    }

}