package ua.com.fielden.platform.security.tokens;

/**
 * Various standard templates for titles and descriptions of security tokens. 
 *
 * @author TG Team
 *
 */
public enum Template {
    
    SAVE("%s Can Save", "Authorises saving of new or modified data."),
    DELETE("%s Can Delete", "Authorises deletion of data."),
    MODIFY("%s Can Modify property [%s]", "Authorises modification of the specified property."),
    EXECUTE("%s Can Execute", "Authorises action execution."),
    MASTER_OPEN("%s Can Open", "Authorises opening of a compound master."),
    MASTER_MENU_ITEM_ACCESS("%s Can Access", "Authorises access to a menu item of a compound master.");
    
    public final String forTitle;
    public final String forDesc;
    
    private Template(final String forTitle, final String forDesc) {
        this.forTitle = forTitle;
        this.forDesc = forDesc;
    }
}
