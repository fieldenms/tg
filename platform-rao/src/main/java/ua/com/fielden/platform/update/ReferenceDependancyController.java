package ua.com.fielden.platform.update;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Map;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Status;

import ua.com.fielden.platform.cypher.Checksum;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.utils.Pair;

import com.google.inject.Inject;

/**
 * A default implementation for {@link IReferenceDependancyController}.
 *
 * @author TG Team
 *
 */
public class ReferenceDependancyController implements IReferenceDependancyController {
    private static final int READ_BLOCK = 8192;

    private final RestClientUtil util;

    @Inject
    public ReferenceDependancyController(final RestClientUtil util) {
	this.util = util;
    }

    @Override
    public Map<String, Pair<String, Long>> dependencyInfo() {
	final String uri = util.getSystemUri() + "/users/" + util.getUsername() + "/update";
	final Pair<Response, Result> pair = util.process(new Request(Method.GET, uri));

	final Result result = pair.getValue();
	if (!result.isSuccessful()) {
	    throw result;
	}

	return result.getInstance(Map.class);
    }

    @Override
    public byte[] download(final String dependencyFileName, final String expectedCehcksum, final IDownloadProgress progress) {
	final String uri = util.getSystemUri() + "/users/" + util.getUsername() + "/dependencies/" + dependencyFileName;
	final Response response = util.send(new Request(Method.GET, uri));
	if (!Status.SUCCESS_OK.equals(response.getStatus())) {
	    throw new IllegalStateException(response.getStatus().toString());
	}
	try {
	    final InputStream content = response.getEntity().getStream();
	    try {

		final ReadableByteChannel bc = Channels.newChannel(content);
		ByteBuffer bb = ByteBuffer.allocate(READ_BLOCK);
		while (bc.read(bb) != -1) { // read the data while it lasts in chunks defined by READ_BLOCK
		    bb = resizeBuffer(bb); //resize the buffer if required to fit the data on the next read
		    progress.update(bb.position());
		}
		bb.flip();
		progress.update(bb.limit());

		// get the data from the buffer
		final byte[] data = new byte[bb.limit()];
		bb.get(data);
		bb.clear();

		// calculate the checksum for the downloaded data and compare it with the expected one
		final String checksum = Checksum.sha1(data);
		if (!checksum.equals(expectedCehcksum)) {
		    throw new EChecksumMismatch(expectedCehcksum, checksum);
		}

		return data;
	    } finally {
		try {
		    content.close();
		} catch (final IOException ex) {
		}
	    }
	} catch (final EChecksumMismatch ex) {
	    throw ex;
	} catch (final Result ex) {
	    throw ex;
	} catch (final Exception ex) {
	    throw new Result(ex);
	}
    }

    private static ByteBuffer resizeBuffer(final ByteBuffer in) {
	if (in.remaining() < READ_BLOCK) {
	    // create new buffer with double capacity
	    final ByteBuffer result = ByteBuffer.allocate(in.capacity() * 2);
	    // flip the in buffer in preparation for it to be copied into the newly created larger buffer
	    in.flip();
	    // copy the in buffer into new buffer
	    result.put(in);
	    return result;
	}
	return in;
    }

}
