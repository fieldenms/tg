package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.AttachmentDownloadResource;

/**
 * Factory to instantiate {@link AttachmentDownloadResource}.
 *
 * @author TG Team
 *
 */
public class AttachmentDownloadResourceFactory extends Restlet {
    private final RestServerUtil restUtil;
    private final ICompanionObjectFinder companionFinder;
    private final IDeviceProvider deviceProvider;
    private final IDates dates;

    public AttachmentDownloadResourceFactory(final Injector injector) {
        this.companionFinder = injector.getInstance(ICompanionObjectFinder.class);
        this.restUtil = injector.getInstance(RestServerUtil.class);
        this.deviceProvider = injector.getInstance(IDeviceProvider.class);
        this.dates = injector.getInstance(IDates.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            new AttachmentDownloadResource(
                    restUtil,
                    companionFinder.find(Attachment.class),
                    deviceProvider,
                    dates,
                    getContext(), request, response).handle();
        }
    }
}
