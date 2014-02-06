package ua.com.fielden.platform.swing.review.report.analysis.grid;

import java.util.EventListener;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;

public interface IDataPromotingListener<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends EventListener {

    void beforeDataPromoting(DataPromotingEvent<T, CDTME> event);

    void afterDataPromoting(DataPromotingEvent<T, CDTME> event);
}
