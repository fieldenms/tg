package ua.com.fielden.platform.web.factories;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import ua.com.fielden.platform.attachment.IAttachment;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.web.resources.AttachmentDownloadResource;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * This is {@link Restlet} implementation for instantiating {@link AttachmentDownloadResource}.
 *
 * @author TG Team
 *
 */
public class AttachmentDownloadResourceFactory extends Restlet {
    private final Injector injector;
    private final RestServerUtil restUtil;
    private final String location;

    /**
     * Instances of DAO and factory should be thread-safe as they are used by multiple instances of resources serving concurrent requests.
     *
     */
    public AttachmentDownloadResourceFactory(final String location, final Injector injector) {
        this.injector = injector;
        this.restUtil = injector.getInstance(RestServerUtil.class);
        this.location = location;
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET == request.getMethod()) {
            final String username = (String) request.getAttributes().get("username");
            injector.getInstance(IUserProvider.class).setUsername(username, injector.getInstance(IUser.class));
            new AttachmentDownloadResource(location, injector.getInstance(IAttachment.class), restUtil, getContext(), request, response).handle();
        }
    }
}
