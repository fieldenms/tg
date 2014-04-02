package ua.com.fielden.platform.swing.review.details;

import ua.com.fielden.platform.swing.analysis.DetailsFrame;
import ua.com.fielden.platform.swing.view.ICloseHook;

/**
 * Details contract.
 * 
 * @author TG Team
 * 
 * @param <T>
 */
public interface IDetails<T> {

    /**
     * Creates the details frame for the specified parameter.
     * 
     * @param detailsParam
     */
    DetailsFrame createDetailsView(T detailsParam, ICloseHook<DetailsFrame> closeHook);

}
