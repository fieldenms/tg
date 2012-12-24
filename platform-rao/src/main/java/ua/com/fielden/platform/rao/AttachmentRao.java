package ua.com.fielden.platform.rao;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Status;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.attachment.IAttachmentController;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * RAO implementation for managing attachments.
 *
 * @author TG Team
 *
 */
@EntityType(Attachment.class)
public class AttachmentRao extends CommonEntityRao<Attachment> implements IAttachmentController {

    @Inject
    public AttachmentRao(final RestClientUtil restUtil) {
	super(restUtil);

    }

    @Override
    public Attachment save(final Attachment entity) {
	// if attachment is persisted then there is no need to upload a file
	// thus, can fully rely on the standard way of persisting domain entities
	if (entity.isPersisted()) {
	    return super.save(entity);
	}

	//////////////////////////////////////////////////////////////////////////
	/////// the attachment entity is brand new and requires file upload //////
	//////////////////////////////////////////////////////////////////////////

	// first, check if attachment is valid
	final Result validationResult = entity.isValid();
	if (!validationResult.isSuccessful()) {
	    throw validationResult;
	}
	// compose multi-part request
	final HttpClient httpClient = new DefaultHttpClient();
	final MultipartEntity mpe = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
	// lets add all constituents of the request -- the order is very important
	// add both key and description to the body
	try {
	    mpe.addPart("KEY", new StringBody(entity.getKey()));
	    mpe.addPart("DESC", new StringBody(entity.getDesc()));
	} catch (final UnsupportedEncodingException e) {
	    throw new IllegalArgumentException(e);
	}
	// add file to the body
	final FileBody fileBody = new FileBody(entity.getFile(), "application/octet-stream");
	mpe.addPart("UPLOAD_FILE", fileBody);

	// create and post the request
	final HttpPost httpPost = new HttpPost(restUtil.getUri(getEntityType(), getDefaultWebResourceType()));// "http://localhost:9001/testFileUpload/"
	httpPost.setEntity(mpe);

	final InputStream envelope;
	try {
	    final HttpResponse response = httpClient.execute(httpPost);
	    System.out.println(response.getStatusLine());
	    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
		envelope = response.getEntity().getContent();
	    } else {
		throw new IllegalStateException(response.getStatusLine().toString());
	    }
	} catch (final Exception e) {
	    throw new IllegalStateException(e);
	}

	final Result result = restUtil.process(envelope);

	if (result.isSuccessful()) {
	    return getEntityType().cast(result.getInstance());
	}
	throw result;
    }

    @Override
    public byte[] download(final Attachment attachment) {
	if (!attachment.isPersisted()) {
	    throw new IllegalArgumentException("Cannot download not persisted attachment.");
	}
	// create a request URI containing page capacity and number
	final String uri = restUtil.getDownloadAttachmentUri(attachment.getId());
	// send request
	final Response response = restUtil.send(new Request(Method.GET, uri));
	if (!Status.SUCCESS_OK.equals(response.getStatus())) {
	    throw new IllegalStateException(response.getStatus().toString());
	}
	try {
	    final InputStream content = response.getEntity().getStream();
	    final ByteArrayOutputStream oStream = new ByteArrayOutputStream();
	    int i = content.read();
	    while (i != -1) {
	        oStream.write(i);
	        i = content.read();
	    }
	    oStream.flush();
	    oStream.close();
	    content.close();

	    return oStream.toByteArray();
	} catch (final IOException e) {
	    throw new Result(e);
	}
    }
}
