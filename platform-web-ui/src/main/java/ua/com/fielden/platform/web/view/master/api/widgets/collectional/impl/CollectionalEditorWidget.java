package ua.com.fielden.platform.web.view.master.api.widgets.collectional.impl;

import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

/**
 *
 * This is a wrapper for <code>tg-collectional-editor</code> that 'edits' small collection of entities (collectional property) on Entity Master.
 * <p>
 * The editor for such collection should be embedded into property action master with explicit specification of <code>chosenNumbersPropertyName</code>.
 * <p>
 * The propertyName'd property will contain <b>all entities</b> when going to the client and only chosen entities when returning to the server
 * (this is potentially an optimisation step, and could be skipped on start, especially in a light of the fact that managing the conflicts on server
 * could be easier when sending <b>all entities</b> back to the server).
 *
 * @author TG Team
 *
 */
public class CollectionalEditorWidget extends AbstractWidget {
    /**
     * The name of 'chosenNumbers' property, that represents the container with ordered list of chosen entity numbers, which corresponds to 
     * list of all entities, that reside in propertyName'd property.
     */
    private final String chosenNumbersPropertyName;

    public CollectionalEditorWidget(final Pair<String, String> titleAndDesc, final String propertyName, final String chosenNumbersPropertyName) {
        super("editors/tg-collectional-editor", titleAndDesc, propertyName);
        this.chosenNumbersPropertyName = chosenNumbersPropertyName;
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final Map<String, Object> attrs = super.createCustomAttributes();
        attrs.put("chosen-numbers-property-name", this.chosenNumbersPropertyName);
        return attrs;
    };

}
