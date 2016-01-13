package ua.com.fielden.platform.web.resources;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;

import javax.activation.MimetypesFileTypeMap;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

import com.google.inject.Injector;

/**
 * This resource should be used for uploading files.
 * 
 * Unlike {@link AttachmentTypeResource} it does not save or associated the uploaded file with any entity. Instead it passes that file into
 * 
 * @author TG Team
 * 
 */
public class FileUploadResource extends ServerResource {

    public FileUploadResource(final Injector injector, final Context context, final Request request, final Response response) {
        init(context, request, response);
    }

    /**
     * Receives a file 
     * 
     */
    @Put
    public Representation receiveFile(final Representation entity) throws Exception {
        if (entity != null) { // && MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(), true)
            // Create a factory for disk-based file items

            // Request is parsed by the handler which generates a list of FileItems
            StringBuilder sb = new StringBuilder("media type: ");
            sb.append(entity.getMediaType()).append("\n");
            sb.append("file size : ");
            sb.append(entity.getSize()).append("\n");

            
            final InputStream stream = entity.getStream();

            
            final BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            sb.append("\n\n");

            System.out.println(sb.toString());
            Thread.currentThread().sleep(5000);
            //result = new StringRepresentation(sb.toString(), MediaType.TEXT_PLAIN);
        }

        return new StringRepresentation("no result");
    }
}
