package ua.com.fielden.platform.file_reports;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Deflater;

import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.dao.UsernameSetterMixin;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.serialisation.GZipOutputStreamEx;

import com.google.inject.Inject;

/**
 * Implementation of {@link IReport} interface, which uses a set of registered {@link IReportFactory}s associated with report type to provide reports.
 *
 * @author TG Team
 *
 */
public class ReportDao implements IReport {

    private String username;

    private final Map<String, IReportFactory> reportFactories = new HashMap<String, IReportFactory>();

    private final IEntityAggregatesDao aggregatesDao;

    @Inject
    public ReportDao(final IEntityAggregatesDao aggregatesDao) {
	this.aggregatesDao = aggregatesDao;
    }

    /**
     * Registers {@link IReportFactory} for reports with the specified report type.
     *
     * @param reportType
     * @param reportFactory
     * @return
     */
    public <ReportParams extends AbstractEntity> ReportDao addReportFactory(final String reportType, final IReportFactory reportFactory) {
	reportFactories.put(reportType, reportFactory);
	return this;
    }

    @Override
    public final void setUsername(final String username) {
	try {
	    UsernameSetterMixin.setUsername(username, this, Finder.findFieldByName(getClass(), "username"));
	} catch (final Exception e) {
	    throw new IllegalStateException(e);
	}
    }

    @Override
    public final String getUsername() {
	return username;
    }

    @Override
    public byte[] getReport(final String reportType, final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> query, final Map<String, Object> allParams) throws Exception {
	final IReportFactory reportFactory = reportFactories.get(reportType);
	if (reportFactory == null) {
	    return null;
	} else {
	    // zipping pdf report
	    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    final GZipOutputStreamEx zipperStream = new GZipOutputStreamEx(outputStream, Deflater.BEST_COMPRESSION);
	    final byte[] pdfData = reportFactory.createReport(query, aggregatesDao, allParams);
	    zipperStream.write(pdfData);
	    zipperStream.close();

	    return outputStream.toByteArray();
	}

    }
}