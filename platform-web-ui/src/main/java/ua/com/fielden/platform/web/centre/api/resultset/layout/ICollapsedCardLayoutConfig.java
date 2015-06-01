package ua.com.fielden.platform.web.centre.api.resultset.layout;

import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder3PrimaryAction;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;

/**
 * A contract for specifying card-based layout for an Entity Centre resultset.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ICollapsedCardLayoutConfig<T extends AbstractEntity<?>> extends IResultSetBuilder3PrimaryAction<T> {
    /**
     * A method to specify a card layout for its collapsed state.
     * This layout would be used as a default card-based representation of each entity in the resultset for the specified device.
     *
     * @param device
     * @param orientation
     * @param flexString
     * @return
     */
    IExpandedCardLayoutConfig<T> setCollapsedCardLayoutFor(final Device device, final Optional<Orientation> orientation, final String flexString);
}