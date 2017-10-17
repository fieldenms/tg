package ua.com.fielden.platform.web.centre;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * This utility class contains the methods that are shared across {@link CentreResource} and {@link CriteriaResource}.
 *
 * @author TG Team
 *
 */
public class CentreUtils<T extends AbstractEntity<?>> extends CentreUpdater {
    private final static Logger logger = Logger.getLogger(CentreUtils.class);
    
    /** Protected default constructor to prevent instantiation. */
    protected CentreUtils() {
    }
    
    /**
     * Returns <code>true</code> if the centre is changed (and thus can be saved / discarded) from client application perspective, <code>false</code> otherwise.
     *
     * @param updatedFreshCentre -- updated instance of 'fresh' centre
     * @param updatedSavedCentre -- updated instance of 'saved' centre
     * @return
     */
    public static boolean isFreshCentreChanged(final ICentreDomainTreeManagerAndEnhancer updatedFreshCentre, final ICentreDomainTreeManagerAndEnhancer updatedSavedCentre) {
        final boolean isCentreChanged = !EntityUtils.equalsEx(updatedFreshCentre, updatedSavedCentre);
        logger.debug("isCentreChanged == " + isCentreChanged);
        return isCentreChanged;
    }
}
