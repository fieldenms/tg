package ua.com.fielden.platform.web.resources.webui;

import static com.google.common.base.Charsets.UTF_8;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.restlet.data.MediaType.ALL;
import static org.restlet.data.MediaType.IMAGE_PNG;
import static org.restlet.data.MediaType.IMAGE_SVG;
import static org.restlet.data.MediaType.TEXT_CSS;
import static org.restlet.data.MediaType.TEXT_HTML;
import static org.restlet.data.MediaType.TEXT_JAVASCRIPT;
import static ua.com.fielden.platform.web.resources.RestServerUtil.encodedRepresentation;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.app.IWebResourceLoader;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;

/**
 * Web server resource that searches for file resource among resource paths and returns it to client.
 *
 * @author TG Team
 *
 */
public class FileResource extends AbstractWebResource {
    private final List<String> resourcePaths;
    private final IWebResourceLoader webResourceLoader;

    /**
     * Creates an instance of {@link FileResource} with custom resource paths.
     *
     * @param resourcePaths
     * @param context
     * @param request
     * @param response
     */
    public FileResource(final IWebResourceLoader webResourceLoader, final List<String> resourcePaths, final IDeviceProvider deviceProvider, final IDates dates, final Context context, final Request request, final Response response) {
        super(context, request, response, deviceProvider, dates);
        this.resourcePaths = resourcePaths;
        this.webResourceLoader = webResourceLoader;
    }

    /**
     * Invoked on GET request from client.
     */
    @Get
    public Representation load() {
        final String extension = getReference().getExtensions();
        final MediaType mediaType = determineMediaType(extension);
        if (IMAGE_PNG.equals(mediaType) || ALL.equals(mediaType)) {
            return createStreamRepresentation(webResourceLoader, resourcePaths, mediaType, getReference().getPath(), getReference().getRemainingPart());
        } else {
            return createRepresentation(webResourceLoader, mediaType, getReference().getPath(), getReference().getRemainingPart(), of(getResponse()));
        }
    }
    
    /**
     * Returns checksum representation for the resource in case where checksum has been requested.
     * Otherwise, creates representation using <code>createRepresentation</code> callback.
     * 
     * @param createRepresentation
     * @param webResourceLoader
     * @param mediaType
     * @param path
     * @param remainingPart
     * @return
     */
    private static Representation returnChecksumRepresentationOr(final Supplier<Representation> createRepresentation, final IWebResourceLoader webResourceLoader, final MediaType mediaType, final String path, final String remainingPart) {
        if (remainingPart.endsWith("?checksum=true")) {
            return encodedRepresentation(new ByteArrayInputStream(webResourceLoader.checksum(path).orElse("").getBytes(UTF_8)), mediaType);
        } else {
            return createRepresentation.get();
        }
    }
    
    /**
     * Returns checksum representation for the resource in case where checksum has been requested.
     * Otherwise, creates stream-based representation for resources like images.
     *  
     * @param webResourceLoader
     * @param resourcePaths
     * @param mediaType
     * @param path
     * @param remainingPart
     * @return
     */
    private static Representation createStreamRepresentation(final IWebResourceLoader webResourceLoader, final List<String> resourcePaths, final MediaType mediaType, final String path, final String remainingPart) {
        return returnChecksumRepresentationOr(() -> {
            final String filePath = generateFileName(resourcePaths, remainingPart);
            return encodedRepresentation(webResourceLoader.loadStream(filePath), mediaType);
        }, webResourceLoader, mediaType, path, remainingPart);
    }

    /**
     * Returns checksum representation for the resource in case where checksum has been requested.
     * Otherwise, creates textual representation for text-like resources. 
     * 
     * @param webResourceLoader
     * @param mediaType
     * @param path
     * @param remainingPart
     * @param response -- optional response object to set status 404 in case of a missing resource.
     * @return
     */
    public static Representation createRepresentation(final IWebResourceLoader webResourceLoader, final MediaType mediaType, final String path, final String remainingPart, final Optional<Response> response) {
        return returnChecksumRepresentationOr(() -> {
            final Optional<String> source = webResourceLoader.loadSource(path);
            return source.map(content -> encodedRepresentation(new ByteArrayInputStream(content.getBytes(UTF_8)), mediaType))
                    .orElseGet(() -> {response.ifPresent(r -> r.setStatus(Status.CLIENT_ERROR_NOT_FOUND)); return response.isPresent() ? null : new StringRepresentation("Web UI resource is missing");});
        }, webResourceLoader, mediaType, path, remainingPart);
    }

    public static Representation createRepresentation(final IWebResourceLoader webResourceLoader, final MediaType mediaType, final String path, final String remainingPart) {
        return createRepresentation(webResourceLoader, mediaType, path, remainingPart, empty());
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
            return IMAGE_PNG;
        case "js":
        case "json":
        case "webmanifest":
        case "":
            return TEXT_JAVASCRIPT;
        case "html":
            return TEXT_HTML;
        case "css":
            return TEXT_CSS;
        case "svg":
            return IMAGE_SVG;
        default:
            return ALL;
        }
    }
}