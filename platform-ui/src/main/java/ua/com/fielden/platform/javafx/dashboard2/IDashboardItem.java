package ua.com.fielden.platform.javafx.dashboard2;

import java.util.List;

import javafx.embed.swing.JFXPanel;
import ua.com.fielden.platform.dashboard.IDashboardItemResult;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.swing.model.IUmViewOwner;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder.QueryProperty;


/** A general interface for dashboard item. */
public interface IDashboardItem <RESULT extends IDashboardItemResult, UI extends JFXPanel & IDashboardItemUi<RESULT> & IUmViewOwner> {

    /** Runs a computation behind the dashboard item and displays result. */
    void runAndDisplay(final List<QueryProperty> customParameters);

    /**
     * Acknowledges the potentially changed alert information by the <code>user</code>.
     *
     * @param user
     */
    void acknowledge(final User user);

    void configure();

    void invokeErrorDetails();
    void invokeWarningDetails();
    void invokeRegularDetails();

    UI getUi();

    Class<? extends AbstractEntity<?>> mainType();
}
