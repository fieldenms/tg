package ua.com.fielden.platform.file_reports;

/**
 * Interface representing factory for producing {@link ReportDao} instances.
 * 
 * @author yura
 * 
 */
public interface IReportDaoFactory {

    /**
     * This method should create new instance of {@link ReportDao} with registered {@link IReportFactory}s.
     * 
     * @return
     */
    IReport createReportDao();

}
