package ua.com.fielden.platform.web.resources;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

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

public class FileResource extends ServerResource {

    private final String fileName;
    private final MediaType mediaType;

    public FileResource(final String fileName, final MediaType mediaType, final Context context, final Request request, final Response response) {
        init(context, request, response);
        this.fileName = fileName;
        this.mediaType = mediaType;
    }

    @Override
    protected Representation get() throws ResourceException {
        try {
            return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new FileInputStream(fileName), mediaType));
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            return null;
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
