package ua.com.fielden.platform.file_reports;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRRewindableDataSource;
import net.sf.jasperreports.engine.JRStyle;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.utils.ResourceLoader;

public abstract class AbstractPdfReportFactory implements IReportFactory {

    /**
     * Provides default implementation of report generation in the form of pdf file binary representation
     */
    @Override
    public byte[] createReport(final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> query, final IEntityAggregatesDao aggregatesDao, final Map<String, Object> allParams)
	    throws Exception {

	// obtaining necessary for report data
	final List<EntityAggregates> reportData = aggregatesDao.getAllEntities(query);

	// compiling report template file
	String reportTemplatePath = null;
	try {
	    reportTemplatePath = ResourceLoader.getURL(reportTemplatePath()).getFile();
	} catch (final Exception e) {
	    reportTemplatePath = reportTemplatePath();
	}

	final JasperReport report = JasperCompileManager.compileReport(reportTemplatePath);
	addPdfFontsToStyles(report.getStyles());
	// filling report template with data
	final JRRewindableDataSource jrs = new EntityAggregatesReportSource(reportData.toArray(new EntityAggregates[] {}), reportProps());
	final JasperPrint print = JasperFillManager.fillReport(report, prepareReportParamsValues(allParams, report.getParameters()), jrs);

	// exporting to PDF file as byte array
	final JRPdfExporter exporter = new JRPdfExporter();
	exporter.setParameter(JRExporterParameter.CHARACTER_ENCODING, "UTF-8");

	final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
	exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);
	exporter.exportReport();

	return outputStream.toByteArray();
    }

    private void addPdfFontsToStyles(final JRStyle[] styles) {
	if (styles != null) {
	    for (final JRStyle style : styles) {
		if (style.getName().equals("reportStyle")) {
		    style.setPdfFontName("fonts/times.ttf");
		    style.setBlankWhenNull(true);
		}

		if (style.getName().equals("reportBoldStyle")) {
		    style.setPdfFontName("fonts/timesbd.ttf");
		    style.setBlankWhenNull(true);
		}

	    }
	}
    }

    /**
     * Extracts values for report parameters from all (report & query) parameters values map.
     *
     * @param allParams
     * @param reportParams
     * @return
     */
    private Map<String, Object> prepareReportParamsValues(final Map<String, Object> allParams, final JRParameter[] reportParams) {
	final Map<String, Object> result = new HashMap<String, Object>();
	for (final JRParameter jrParameter : reportParams) {
	    if (allParams.containsKey(jrParameter.getName())) {
		result.put(jrParameter.getName(), allParams.get(jrParameter.getName()));
	    }
	}
	return result;
    }

    /**
     * Should provide report template path.
     *
     * @return
     */
    protected abstract String reportTemplatePath();

    /**
     * Should provide report data source properties and their types.
     *
     * @return
     */
    protected abstract Map<String, Class> reportProps();
}
