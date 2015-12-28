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
}
