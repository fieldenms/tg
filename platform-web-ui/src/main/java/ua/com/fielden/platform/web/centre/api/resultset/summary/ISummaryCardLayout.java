package ua.com.fielden.platform.web.centre.api.resultset.summary;

import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.insertion_points.IInsertionPoints;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;

/**
 * A contract for defining summary card layout for different devices.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ISummaryCardLayout<T extends AbstractEntity<?>> extends IInsertionPoints<T> {
    ISummaryCardLayout<T> setSummaryCardLayoutFor(final Device device, final Optional<Orientation> orientation, final String flexString);
}
