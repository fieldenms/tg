package ua.com.fielden.platform.swing.groupanalysis;

import java.util.List;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reportquery.IAggregatedProperty;
import ua.com.fielden.platform.reportquery.IDistributedProperty;
import ua.com.fielden.platform.swing.analysis.IAnalysisReportModel;

public abstract class GroupAnalysisReportModel<T extends AbstractEntity, DAO extends IEntityDao<T>> implements IAnalysisReportModel {

    public abstract List<IAggregatedProperty> getAvailableAggregationProperties();

    public abstract List<IDistributedProperty> getAvailableDistributionProperties();

    public abstract boolean canRestoreReportView();

}
