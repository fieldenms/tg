package ua.com.fielden.platform.web.view.master.api.widgets.collectional.impl;

import java.util.Map;

import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

/**
 *
 * This is a wrapper for <code>tg-collectional-editor</code> that 'edits' small collection of entities (collectional property) on Entity Master.
 * <p>
 * The editor for such collection should be embedded into functional action master with explicit specification of <code>chosenIdsPropertyName</code>, <code>addedIdsPropertyName</code> and <code>removedIdsPropertyName</code>.
 * <p>
 * The propertyName'd property will contain <b>all entities</b> when going to the client and will be empty when returning to the server.<br>
 * Property with <code>chosenIdsPropertyName</code> will hold<br>
 *  1) when sending to client -- all chosen entity ids<br>
 *  2) when sending to server -- all chosen entity ids (including added ones and those ones that have remained chosen).<br>
 * <p>
 * Property with <code>addedIdsPropertyName</code> will hold<br>
 *  1) when sending to client -- empty<br>
 *  2) when sending to server -- all added entity ids.<br>
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
    /**
     * The name of 'addedIds' property, that represents the container with unordered list of added entity ids.
     */
    /**
     * The name of 'removedIds' property, that represents the container with unordered list of removed entity ids.
     */

    public CollectionalEditorWidget(final Pair<String, String> titleAndDesc, final String propertyName) {
        super("editors/tg-collectional-editor", titleAndDesc, propertyName);
    }
}
