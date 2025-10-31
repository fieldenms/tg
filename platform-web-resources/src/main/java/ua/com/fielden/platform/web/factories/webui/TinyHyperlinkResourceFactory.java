package ua.com.fielden.platform.web.factories.webui;

import com.google.inject.Injector;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.ICentreConfigSharingModel;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.TinyHyperlinkResource;

/// A factory for [TinyHyperlinkResource].
///
public class TinyHyperlinkResourceFactory extends Restlet {

    public static final String ENTITY_ID = "entity-id";

    private final IWebUiConfig webUiConfig;
    private final RestServerUtil restUtil;
    private final EntityFactory factory;
    private final ICriteriaGenerator critGenerator;
    private final ICompanionObjectFinder coFinder;
    private final IUserProvider userProvider;
    private final IDeviceProvider deviceProvider;
    private final IDates dates;
    private final ICentreConfigSharingModel sharingModel;
    private final ISerialiser serialiser;

    public TinyHyperlinkResourceFactory(final IWebUiConfig webUiConfig, final Injector injector) {
        this.webUiConfig = webUiConfig;
        this.restUtil = injector.getInstance(RestServerUtil.class);
        this.factory = injector.getInstance(EntityFactory.class);
        this.critGenerator = injector.getInstance(ICriteriaGenerator.class);
        this.coFinder = injector.getInstance(ICompanionObjectFinder.class);
        this.serialiser = injector.getInstance(ISerialiser.class);
        this.userProvider = injector.getInstance(IUserProvider.class);
        this.deviceProvider = injector.getInstance(IDeviceProvider.class);
        this.dates = injector.getInstance(IDates.class);
        this.sharingModel = injector.getInstance(ICentreConfigSharingModel.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET == request.getMethod()) {
            // TODO Error handling.
            final var id = Long.parseLong(request.getAttributes().get(ENTITY_ID).toString());

            new TinyHyperlinkResource(
                    factory,
                    restUtil,
                    critGenerator,
                    coFinder,
                    serialiser,
                    webUiConfig,
                    userProvider,
                    deviceProvider,
                    dates,
                    sharingModel,
                    getContext(),
                    request,
                    response,
                    id
            ).handle();
        }
    }
}
