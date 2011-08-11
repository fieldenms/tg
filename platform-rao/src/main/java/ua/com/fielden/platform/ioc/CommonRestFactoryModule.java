package ua.com.fielden.platform.ioc;

import ua.com.fielden.platform.file_reports.IReportDao;
import ua.com.fielden.platform.file_reports.ReportRao;
import ua.com.fielden.platform.rao.RestClientUtil;

/**
 * Module for REST clients, which provides all essential binding such as lazy loading proxy intercepter and meta-property factory.
 *
 * @author TG Team
 *
 */
public class CommonRestFactoryModule extends RestPropertyFactoryModule {

    public CommonRestFactoryModule(final RestClientUtil restUtil) {
	super(restUtil);
	entityFactory.setModule(this);
    }

    @Override
    protected void configure() {
	super.configure();

	bind(IReportDao.class).toInstance(new ReportRao(restUtil));
    }
}
