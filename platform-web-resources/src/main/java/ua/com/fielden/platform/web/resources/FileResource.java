package ua.com.fielden.platform.web.resources;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.utils.ResourceLoader;

/**
 * Web server resource that  searches for file resource among resource paths and returns it to client.
 *
 * @author TG Team
 *
 */
public class FileResource extends ServerResource {

    private final List<String> resourcePaths;

    /**
     * Creates an instance of {@link FileResource} with custom resource paths.
     *
     * @param resourcePaths
     * @param context
     * @param request
     * @param response
     */
    public FileResource(final List<String> resourcePaths, final Context context, final Request request, final Response response) {
        init(context, request, response);
	this.resourcePaths = resourcePaths;
    }

    /**
     * Invoked on GET request from client.
     */
    @Override
    protected Representation get() throws ResourceException {
        try {
            final String filePath = generateFileName(getReference().getRemainingPart());
            final String extension = getReference().getExtensions();
            if (StringUtils.isEmpty(filePath)) {
        	throw new FileNotFoundException("The requested resource (" + getReference().getRemainingPart() + " + " + extension + ") wasn't found.");
            } else {
        	final InputStream stream = ResourceLoader.getStream(filePath);
		final MediaType mediaType = determineMediaType(extension);
		return RestServerUtil.encodedRepresentation(stream, mediaType);
            }
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Searches for the file resource among resource paths starting from the last one path and generates full file path by concatenating resource path and relative file path.
     *
     * @param filePath - the relative file path for which full file path must be generated.
     * @return
     */
    private String generateFileName(final String filePath) {
	for (int pathIndex =0;pathIndex < resourcePaths.size(); pathIndex++) {
	    if (ResourceLoader.exist(resourcePaths.get(pathIndex) + filePath)) {
		return resourcePaths.get(pathIndex) + filePath;
	    }
	}
        return null;
    }

    /**
     * Determines the media type of the file to return to the client. The determination process is based on file extension.
     *
     * @param extension - the file extension that is used to determine media type.
     * @return
     */
    private MediaType determineMediaType(final String extension) {
	switch (extension) {
	case "png":
	    return MediaType.IMAGE_PNG;
	case "js":
	case "json":
	    return MediaType.TEXT_JAVASCRIPT;
	case "html":
	    return MediaType.TEXT_HTML;
	case "css":
	    return MediaType.TEXT_CSS;
	default: return MediaType.ALL;
	}
    }

//	private static String compress(final String str) throws IOException {
//		if (str == null || str.length() == 0) {
//			return str;
//		}
//		ByteArrayOutputStream out = new ByteArrayOutputStream();
//		GZIPOutputStream gzip = new GZIPOutputStream(out);
//		gzip.write(str.getBytes());
//		gzip.close();
//		String outStr = out.toString("UTF-8");
//		return outStr;
//	}
}
