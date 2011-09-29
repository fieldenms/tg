package ua.com.fielden.platform.swing.review.report.centre;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.IDomainTreeManager.IDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.report.ReportMode;

/**
 * Model for entity centre. This model allows one to configure and view report.
 * 
 * @author TG Team
 *
 * @param <DTME>
 * @param <T>
 * @param <DAO>
 */
public class CentreConfigurationModel<DTME extends IDomainTreeManagerAndEnhancer, T extends AbstractEntity, DAO extends IEntityDao<T>> {

    /**
     * The associated {@link IDomainTreeManagerAndEnhancer} instance.
     */
    private final DTME dtme;

    /**
     * Determines the current report's mode. There are two possible report modes: WIZARD, REPORT.
     */
    private ReportMode mode;

    /**
     * Initiates this CentreConfigurationModel with instance of {@link IDomainTreeManagerAndEnhancer}.
     * 
     * @param dtme
     */
    public CentreConfigurationModel(final DTME dtme){
	this.dtme = dtme;
    }

    /**
     * Returns the {@link IDomainTreeManagerAndEnhancer} instance associated with this centre configuration model.
     * 
     * @return
     */
    public DTME dtme(){
	return dtme;
    }

    /**
     * Returns value that indicates the current centre's mode: WIZARD or REPORT.
     * 
     * @return
     */
    public ReportMode getMode() {
	return mode;
    }

}
