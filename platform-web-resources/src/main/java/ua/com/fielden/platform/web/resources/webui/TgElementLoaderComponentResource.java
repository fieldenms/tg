package ua.com.fielden.platform.web.resources.webui;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

import ua.com.fielden.platform.utils.ResourceLoader;
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
    private final static String appSpecificPreloadedResourcesSrc = ResourceLoader.getText("ua/com/fielden/platform/web/application-startup-resources.html");
    private final static List<String> appSpecificPreloadedResources = getPreloadedResources(appSpecificPreloadedResourcesSrc);

    public TgElementLoaderComponentResource(final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
        init(context, request, response);
        this.restUtil = restUtil;
    }

    /**
     * Reads the source of 'app-specific preloaded resources' file and extracts the list of top-level (root) import URIs.
     *
     * @param appSpecificPreloadedResourcesSrc
     * @return
     */
    private static List<String> getPreloadedResources(final String appSpecificPreloadedResourcesSrc) {
        final List<String> list = new ArrayList<String>();
        String curr = appSpecificPreloadedResourcesSrc;
        // TODO enhance the logic to support single quotes, whitespaces etc.?
        while (curr.indexOf("href=\"") >= 0) {
            final int startIndex = curr.indexOf("href=\"") + 6;
            final String nextCurr = curr.substring(startIndex);
            final int endIndex = nextCurr.indexOf("\"");
            final String importURI = nextCurr.substring(0, endIndex);
            list.add(importURI);
            curr = nextCurr.substring(endIndex);
        }

        return list;
    }

    /**
     * Handles sending of generated tg-element-loader to the Web UI client (GET method).
     */
    @Override
    protected Representation get() throws ResourceException {
        final String text = ResourceLoader.getText("ua/com/fielden/platform/web/element_loader/tg-element-loader.html");
        final byte[] elementLoaderComponent = text.replace("importedURLs = {}", generateImportUrlsFrom(appSpecificPreloadedResources)).getBytes(Charsets.UTF_8);
        return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(elementLoaderComponent)));
    }

    /**
     * Generates the string of tg-element-loader's 'importedURLs' from 'appSpecificPreloadedResources'.
     *
     * @param appSpecificPreloadedResources
     * @return
     */
    private String generateImportUrlsFrom(final List<String> appSpecificPreloadedResources) {
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
