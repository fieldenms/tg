package ua.com.fielden.platform.web.factories.webui;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.routing.Router;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.AbstractEntityWithInputStream;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.FileProcessingResource;
import ua.com.fielden.platform.web.sse.resources.EventSourcingResourceFactory;
import ua.com.fielden.platform.web.test.eventsources.TgPersistentEntityWithPropertiesEventSrouce;

/**
 * Factory to instantiate {@link FileProcessingResource}.
 *
 * @author TG Team
 *
 */
public class FileProcessingResourceFactory<T extends AbstractEntityWithInputStream<?>> extends Restlet {
    private final Injector injector;
    private final Class<T> entityType;
    private final Function<EntityFactory, T> entityCreator;
    private final ICompanionObjectFinder companionFinder;
    private final Router router;
    
    private final long fileSizeLimitKb;
    private final Set<MediaType> types = new HashSet<>();

    public FileProcessingResourceFactory(
            final Router router,
            final Injector injector,
            final Class<T> entityType,
            final Function<EntityFactory, T> entityCreator,
            final long fileSizeLimitKb,
            final MediaType type, // at least one type is required 
            final MediaType... types) {
        this.router = router;
        this.injector = injector;
        this.entityType = entityType;
        this.entityCreator = entityCreator;
        this.companionFinder = injector.getInstance(ICompanionObjectFinder.class);
        this.fileSizeLimitKb = fileSizeLimitKb;
        this.types.add(type);
        Arrays.stream(types).forEach(t -> this.types.add(t));
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.PUT.equals(request.getMethod())) {
            new FileProcessingResource<T>(
                    router,
                    companionFinder.find(entityType),
                    injector.getInstance(EntityFactory.class), 
                    entityCreator, 
                    injector.getInstance(RestServerUtil.class), 
                    fileSizeLimitKb, 
                    types, 
                    getContext(), request, response).handle();
        }
    }
}
