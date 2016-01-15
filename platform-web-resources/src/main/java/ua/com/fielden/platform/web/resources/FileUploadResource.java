package ua.com.fielden.platform.web.resources;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;

import javax.activation.MimetypesFileTypeMap;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
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

    private final long sizeLimit = 10 * 1024 * 1024; // Kilobytes

    public FileUploadResource(final Injector injector, final Context context, final Request request, final Response response) {
        init(context, request, response);
    }

    /**
     * Receives a file 
     * 
     */
    @Put
    public Representation receiveFile(final Representation entity) throws Exception {
        final Representation response;
        if (entity == null) {
            response = new StringRepresentation("There is nothing to process");
        } else { // && MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(), true)
            // Create a factory for disk-based file items

            // Request is parsed by the handler which generates a list of FileItems
            StringBuilder sb = new StringBuilder("media type: ");
            sb.append(entity.getMediaType()).append("\n");
            sb.append("file size : ");
            sb.append(entity.getSize()).append("\n");

            if (entity.getSize() == -1) {
                getResponse().setStatus(Status.CLIENT_ERROR_LENGTH_REQUIRED);
                response = new StringRepresentation("File size is required.");
            } else if (entity.getSize() > sizeLimit) {
                getResponse().setStatus(Status.CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE);
                response = new StringRepresentation("File is too large.");
            } else {
                final InputStream stream = entity.getStream();
    
                
                final BufferedReader br = new BufferedReader(new InputStreamReader(stream));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                sb.append("\n\n");
    
                System.out.println(sb.toString());
                Thread.currentThread().sleep(5000);

                response = new StringRepresentation("Processed the file.");
            }
        }

        return response;
        
    }
}
