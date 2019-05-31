package ua.com.fielden.platform.web.resources.webui;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.isEmpty;

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
    private static final Logger LOGGER = Logger.getLogger(FileResource.class);

    private final List<String> resourcePaths;
    private final ISourceController sourceController;
    private final RestServerUtil serverRestUtil;

    /**
     * Creates an instance of {@link FileResource} with custom resource paths.
     *
     * @param resourcePaths
     * @param context
     * @param request
     * @param response
     */
    public FileResource(final RestServerUtil serverRestUtil, final ISourceController sourceController, final List<String> resourcePaths, final IDeviceProvider deviceProvider, final Context context, final Request request, final Response response) {
        super(context, request, response, deviceProvider);
        this.resourcePaths = resourcePaths;
        this.sourceController = sourceController;
        this.serverRestUtil = serverRestUtil;
    }

    /**
     * Invoked on GET request from client.
     */
    @Get
    public Representation load() {
        final String originalPath = getReference().getRemainingPart();
        final String extension = getReference().getExtensions();

//        if (isEmpty(extension)) {
//            LOGGER.warn(format("The request tried to obtain a file resource with empty extension ([%s] + [%s]), which is not supported.", originalPath, extension));
//            return null;
//        }

        final String filePath = generateFileName(resourcePaths, originalPath, extension);
        if (isEmpty(filePath)) {
            LOGGER.warn(format("The requested file resource ([%s] + [%s]) wasn't found.", originalPath, extension));
            return null;
        } else {
            final MediaType mediaType = determineMediaType(extension);
            if (MediaType.TEXT_HTML.equals(mediaType)) {
                final String source = sourceController.loadSourceWithFilePath(filePath);
                if (source != null) {
                    final byte[] bytes = source.getBytes(Charsets.UTF_8);
                    try {
                        serverRestUtil.setHeaderEntry(getResponse(), HttpHeaders.INFO, Checksum.sha1(bytes));
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                    return RestServerUtil.encodedRepresentation(new ByteArrayInputStream(bytes), mediaType);
                } else {
                    return null;
                }
            } else {
                final InputStream stream = sourceController.loadStreamWithFilePath(filePath);
                if (stream != null) {
                    final Representation encodedRepresentation = RestServerUtil.encodedRepresentation(stream, mediaType);
                    LOGGER.debug(format("File resource [%s] generated.", originalPath));
                    return encodedRepresentation;
                } else {
                    return null;
                }
            }
        }
    }

    /**
     * Searches for the file resource among resource paths starting from the last one path and generates full file path by concatenating resource path and relative file path.
     * @param extension
     *            - the file resource extension
     *
     * @param filePath
     *            - the relative file path for which full file path must be generated.
     * @return
     */
    public static String generateFileName(final List<String> resourcePaths, final String path, final String extension) {
        // this is a preventive stuff: if the server receives additional link parameters -- JUST IGNORE THEM. Was used to run
        // appropriately Mocha / Chai tests for Polymer web components. See http://localhost:8091/resources/polymer/runner.html for results.
        final String filePath = path.contains("?") ? path.substring(0, path.indexOf('?')) : path;
        final String filePathWithExtension = filePath;

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
            return MediaType.IMAGE_PNG;
        case "js":
        case "json":
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