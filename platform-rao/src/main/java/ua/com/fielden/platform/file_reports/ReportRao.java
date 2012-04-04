package ua.com.fielden.platform.file_reports;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.rao.RestClientUtil;

/**
 * This {@link IReport} implementor sends request to server in order to obtain reports.
 *
 * @author TG Team
 *
 */
public class ReportRao implements IReport {

    private final RestClientUtil restUtil;

    public ReportRao(final RestClientUtil restUtil) {
	this.restUtil = restUtil;
    }

    @Override
    public byte[] getReport(final String reportName, final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> query, final Map<String, Object> params) throws IOException {
	// create request envelope containing Entity Query
	final List<Object> requestContent = new ArrayList<Object>();
	requestContent.add(reportName);
	requestContent.add(query);
	requestContent.add(params);
	final Representation envelope = restUtil.represent(requestContent);
	// send request
	final Response response = restUtil.send(new Request(Method.POST, restUtil.getReportUri(), envelope));
	if (!Status.SUCCESS_OK.equals(response.getStatus())) {
	    throw new IllegalStateException(response.getStatus().toString());
	}
	final InputStream content = response.getEntity().getStream();
	final GZIPInputStream stream = new GZIPInputStream(content);
	final ByteArrayOutputStream oStream = new ByteArrayOutputStream();
	int i = stream.read();
	while (i != -1) {
	    oStream.write(i);
	    i = stream.read();
	}
	oStream.flush();
	oStream.close();
	stream.close();

	return oStream.toByteArray();
    }

    @Override
    public void setUsername(final String username) {
	throw new UnsupportedOperationException("Setting username is not required at the client side, and this fact most likely points to a programming mistake.");
    }

    @Override
    public String getUsername() {
	throw new UnsupportedOperationException("Getting username is not required at the client side, and this fact most likely points to a programming mistake.");
    }
}
