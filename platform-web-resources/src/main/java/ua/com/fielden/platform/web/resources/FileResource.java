package ua.com.fielden.platform.web.resources;

import java.io.FileNotFoundException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Encoding;
import org.restlet.data.MediaType;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.utils.ResourceLoader;

public class FileResource extends ServerResource {

    private final List<String> resourcePaths;

    public FileResource(final List<String> resourcePaths, final Context context, final Request request, final Response response) {
        init(context, request, response);
	this.resourcePaths = resourcePaths;
    }

    @Override
    protected Representation get() throws ResourceException {
        try {
            final String filePath = generateFileName(getReference().getRemainingPart());
            final String extension = getReference().getExtensions();
            if (StringUtils.isEmpty(filePath)) {
        	throw new FileNotFoundException("The requested resource wasn't faound.");
            } else {
        	return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(ResourceLoader.getStream(filePath), determineMediaType(extension)));
            }
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String generateFileName(final String filePath) {
	for (int pathIndex =0;pathIndex < resourcePaths.size(); pathIndex++) {
	    if (ResourceLoader.exist(resourcePaths.get(pathIndex) + filePath)) {
		return resourcePaths.get(pathIndex) + filePath;
	    }
	}
        return null;
    }

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
