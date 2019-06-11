package ua.com.fielden.platform.web.resources.webui;

import static com.google.common.base.Charsets.UTF_8;
import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.restlet.data.MediaType.ALL;
import static org.restlet.data.MediaType.IMAGE_PNG;
import static ua.com.fielden.platform.cypher.Checksum.sha1;
import static ua.com.fielden.platform.web.resources.RestServerUtil.encodedRepresentation;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import com.google.common.base.Charsets;

import ua.com.fielden.platform.cypher.Checksum;
import ua.com.fielden.platform.roa.HttpHeaders;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.app.ISourceController;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * Web server resource that searches for file resource among resource paths and returns it to client.
 *
 * @author TG Team
 *
 */
public class FileResource extends AbstractWebResource {
    private final List<String> resourcePaths;
    private final ISourceController sourceController;

    /**
     * Creates an instance of {@link FileResource} with custom resource paths.
     *
     * @param resourcePaths
     * @param context
     * @param request
     * @param response
     */
    public FileResource(final ISourceController sourceController, final List<String> resourcePaths, final IDeviceProvider deviceProvider, final Context context, final Request request, final Response response) {
        super(context, request, response, deviceProvider);
        this.resourcePaths = resourcePaths;
        this.sourceController = sourceController;
    }

    /**
     * Invoked on GET request from client.
     */
    @Get
    public Representation load() {
        final String extension = getReference().getExtensions();
        final MediaType mediaType = determineMediaType(extension);
        if (IMAGE_PNG.equals(mediaType) || ALL.equals(mediaType)) {
            final String filePath = generateFileName(resourcePaths, getReference().getRemainingPart());
            final InputStream stream = sourceController.loadStreamWithFilePath(filePath);
            if (stream != null) {
                return encodedRepresentation(stream, mediaType);
            } else {
                return null;
            }
        } else {
            final String path = getReference().getPath();
            final String remainingPart = getReference().getRemainingPart();
            return createRepresentation(sourceController, mediaType, path, remainingPart);
        }
    }

    public static Representation createRepresentation(final ISourceController sourceController, final MediaType mediaType, final String path, final String remainingPart) {
        final String source = sourceController.loadSource(path);
        if (source != null) {
            final byte[] bytes = source.getBytes(UTF_8);
            final byte[] result;
            if (remainingPart.contains("?") && remainingPart.substring(remainingPart.indexOf('?')).contains("checksum=true")) {
                try {
                    final String sha = sha1(bytes);
                    //System.out.println("remainingPart = " + remainingPart + " sha1 = " + sha);
                    result = sha.getBytes(UTF_8);
                } catch (final Exception e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                result = bytes;
            }
            return encodedRepresentation(new ByteArrayInputStream(result), mediaType);
        }
        return null;
    }

    /**
     * Searches for the file resource among resource paths starting from the last one path and generates full file path by concatenating resource path and relative file path.
     * 
     * @param filePath - the relative file path for which full file path must be generated.
     * @return
     */
    public static String generateFileName(final List<String> resourcePaths, final String path) {
        // this is a preventive stuff: if the server receives additional link parameters -- JUST IGNORE THEM. Was used to run
        // appropriately Mocha / Chai tests for Polymer web components. See http://localhost:8091/resources/polymer/runner.html for results.
        final String filePathWithExtension = path.contains("?") ? path.substring(0, path.indexOf('?')) : path;
        
        for (int pathIndex = 0; pathIndex < resourcePaths.size(); pathIndex++) {
            final String prepender = resourcePaths.get(pathIndex);
            if (ResourceLoader.exist(prepender + filePathWithExtension)) {
                return prepender + filePathWithExtension;
            }
        }
        return null;
    }

    /**
     * Determines the media type of the file to return to the client. The determination process is based on file extension.
     *
     * @param extension
     *            - the file extension that is used to determine media type.
     * @return
     */
    private static MediaType determineMediaType(final String extension) {
        switch (extension.substring(extension.lastIndexOf(".") + 1)) {
        case "png":
            //return MediaType.IMAGE_PNG;
        case "js":
        case "json":
        case "webmanifest":
        case "":
            return MediaType.TEXT_JAVASCRIPT;
        case "html":
            return MediaType.TEXT_HTML;
        case "css":
            return MediaType.TEXT_CSS;
        case "svg":
            return MediaType.IMAGE_SVG;
        default:
            return MediaType.ALL;
        }
    }
}