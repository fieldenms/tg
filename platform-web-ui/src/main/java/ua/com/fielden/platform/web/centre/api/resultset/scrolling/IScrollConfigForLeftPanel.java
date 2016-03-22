package ua.com.fielden.platform.web.centre.api.resultset.scrolling;

/**
 * A convinient API for configuring scrolling behaviour of the entity centre.
 *
 * @author TG Team
 *
 */
public interface IScrollConfigForLeftPanel extends IScrollConfigSecondaryActions {

    IScrollConfigSecondaryActions withFixedCheckboxes();

    IScrollConfigSecondaryActions withFixedCheckboxesAndPrimaryActions();

    /**
     * Set the number of fixed first columns
     *
     * @param numberOfProps
     * @return
     */
    IScrollConfigSecondaryActions withFixedCheckboxesPrimaryActionsAndFirstProps(final int numberOfProps);
}
