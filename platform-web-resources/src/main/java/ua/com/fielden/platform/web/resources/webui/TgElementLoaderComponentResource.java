package ua.com.fielden.platform.web.resources.webui;

import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.apache.commons.lang.StringUtils;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Encoding;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.google.common.base.Charsets;

import ua.com.fielden.platform.web.app.IPreloadedResources;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * Resource for tg-element-loader component.
 *
 * @author TG Team
 *
 * @param <T>
 * @param <DAO>
 */
public class TgElementLoaderComponentResource extends ServerResource {
    private final RestServerUtil restUtil;
    private final IPreloadedResources preloadedResources;

    public TgElementLoaderComponentResource(final IPreloadedResources preloadedResources, final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
        init(context, request, response);
        this.restUtil = restUtil;
        this.preloadedResources = preloadedResources;
    }


    /**
     * Handles sending of generated tg-element-loader to the Web UI client (GET method).
     */
    @Override
    protected Representation get() throws ResourceException {
        return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(get(preloadedResources).getBytes(Charsets.UTF_8))));
    }

    public static String get(final IPreloadedResources preloadedResources) {
        final String source = preloadedResources.getSource("/resources/element_loader/tg-element-loader.html");
        return source.replace("importedURLs = {}", generateImportUrlsFrom(preloadedResources.get()));
    }

    /**
     * Generates the string of tg-element-loader's 'importedURLs' from 'appSpecificPreloadedResources'.
     *
     * @param appSpecificPreloadedResources
     * @return
     */
    private static String generateImportUrlsFrom(final LinkedHashSet<String> appSpecificPreloadedResources) {
        final String prepender = "importedURLs = {";
        final StringBuilder sb = new StringBuilder("");
        final Iterator<String> iter = appSpecificPreloadedResources.iterator();
        while (iter.hasNext()) {
            final String next = iter.next();
            sb.append(",'" + next + "': 'imported'");
        }
        final String res = sb.toString();
        return prepender + (StringUtils.isEmpty(res) ? "" : res.substring(1)) + "}";
    }
}
