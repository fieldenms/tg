package ua.com.fielden.platform.web.view.master.api.widgets.collectional.impl;

import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

/**
 *
 * This is a wrapper for <code>tg-collectional-editor</code> that 'edits' small collection of entities (collectional property) on Entity Master.
 * <p>
 * The editor for such collection should be embedded into property action master with explicit specification of <code>chosenIdsPropertyName</code> and <code>removedIdsPropertyName</code>.
 * <p>
 * The propertyName'd property will contain <b>all entities</b> when going to the client and only chosen entity ids when returning to the server.<br>
 * Property with <code>chosenIdsPropertyName</code> will hold<br>
 *  1) when sending to client -- all chosen entity ids (key of the map entry)<br>
 *  2) when sending to server -- all chosen entity ids (key of the map entry) with a mark (value of the map entry) whether it was added (<code>true</code>) or remain selected (<code>false</code>).<br>
 * <p>
 * Property with <code>removedIdsPropertyName</code> will hold<br> 
 *  1) when sending to client -- empty<br>
 *  2) when sending to server -- all removed entity ids.<br>
 *
 * @author TG Team
 *
 */
public class CollectionalEditorWidget extends AbstractWidget {
    /**
     * The name of 'chosenIds' property, that represents the container with ordered list of chosen entity ids, which should exist in the 
     * list of all entities, that reside in propertyName'd property.
     */
    private final String chosenIdsPropertyName;
    /**
     * The name of 'removedIds' property, that represents the container with unordered list of removed entity ids.
     */
    private final String removedIdsPropertyName;

    public CollectionalEditorWidget(final Pair<String, String> titleAndDesc, final String propertyName, final String chosenIdsPropertyName, final String removedIdsPropertyName) {
        super("editors/tg-collectional-editor", titleAndDesc, propertyName);
        this.chosenIdsPropertyName = chosenIdsPropertyName;
        this.removedIdsPropertyName = removedIdsPropertyName;
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final Map<String, Object> attrs = super.createCustomAttributes();
        attrs.put("chosen-ids-property-name", this.chosenIdsPropertyName);
        attrs.put("removed-ids-property-name", this.removedIdsPropertyName);
        return attrs;
    };

}
