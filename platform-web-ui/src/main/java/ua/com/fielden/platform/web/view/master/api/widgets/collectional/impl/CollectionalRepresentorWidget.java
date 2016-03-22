package ua.com.fielden.platform.web.view.master.api.widgets.collectional.impl;

import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

/**
 *
 * This is a wrapper for <code>tg-collectional-representor</code> that 'represents' small collection of entities (collectional property) on Entity Master -- no editing is available.
 * <p>
 * The editor for such collection will be actually embedded into property action master through the use of 'tg-collectional-editor' (CollectionalWidget).
 *
 * @author TG Team
 *
 */
public class CollectionalRepresentorWidget extends AbstractWidget {

    public CollectionalRepresentorWidget(final Pair<String, String> titleAndDesc, final String propertyName) {
        super("editors/tg-collectional-representor", titleAndDesc, propertyName);
    }
}
