package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.web.resources.webui.AttachmentDownloadResource;

/**
 * Factory to instantiate {@link AttachmentDownloadResource}.
 *
 * @author TG Team
 *
 */
public class AttachmentDownloadResourceFactory extends Restlet {
    private final ICompanionObjectFinder companionFinder;

    public AttachmentDownloadResourceFactory(final Injector injector) {
        this.companionFinder = injector.getInstance(ICompanionObjectFinder.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            new AttachmentDownloadResource(
                    companionFinder.find(Attachment.class),
                    getContext(), request, response).handle();
        }
    }
}
