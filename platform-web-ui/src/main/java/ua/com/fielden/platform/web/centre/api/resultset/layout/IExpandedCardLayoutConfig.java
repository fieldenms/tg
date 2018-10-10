package ua.com.fielden.platform.web.centre.api.resultset.layout;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract for specifying card-based layout for an Entity Centre resultset.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IExpandedCardLayoutConfig<T extends AbstractEntity<?>> extends ICollapsedCardLayoutConfig<T> {
    /**
     * A method to specify a layout for the expanded part of the card-based representation.
     * This layout complements the default collapsed card layout, and should not be thought of as a replacement on that layout.
     * In other words, this layout governs the card section that gets visible once the user expands the collapsed card.
     *
     * @param flexString
     * @return
     */
    ICollapsedCardLayoutConfig<T> withExpansionLayout(final String flexString);
}