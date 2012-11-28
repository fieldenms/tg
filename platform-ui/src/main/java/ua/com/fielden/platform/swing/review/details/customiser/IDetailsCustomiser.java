package ua.com.fielden.platform.swing.review.details.customiser;

import ua.com.fielden.platform.swing.review.details.IDetails;

/**
 * The contract that enables analysis' details customisation.
 *
 * @author TG Team
 *
 * @param <A>
 */
public interface IDetailsCustomiser {

    /**
     * Returns the details contract provided by this analysis customiser.
     *
     * @param detailsParamType
     * @return
     */
    <DT> IDetails<DT> getDetails(Class<DT> detailsParamType);

}
