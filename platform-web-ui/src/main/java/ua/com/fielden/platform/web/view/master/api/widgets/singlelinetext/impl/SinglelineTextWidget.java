package ua.com.fielden.platform.web.view.master.api.widgets.singlelinetext.impl;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static ua.com.fielden.platform.web.centre.WebApiUtils.webComponent;

import java.util.Map;
import java.util.Optional;

import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

/**
 * The implementation box for singleline text widgets.
 *
 * @author TG Team
 *
 */
public class SinglelineTextWidget extends AbstractWidget {
    private Optional<Integer> autoCommitMillisOpt = empty();

    /**
     * Creates {@link SinglelineTextWidget} from <code>entityType</code> type and <code>propertyName</code>.
     *
     * @param titleDesc
     * @param propertyName
     */
    public SinglelineTextWidget(final Pair<String, String> titleDesc, final String propertyName) {
        super(webComponent("editors/tg-singleline-text-editor"), titleDesc, propertyName);
    }

    public void autoCommit(final int millis) {
        autoCommitMillisOpt = of(millis);
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final Map<String, Object> customAttr = super.createCustomAttributes();
        autoCommitMillisOpt.ifPresent(millis -> customAttr.put("auto-commit-millis", millis));
        return customAttr;
    }

}