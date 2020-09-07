package ua.com.fielden.platform.security.tokens;

/**
 * Various standard templates for titles and descriptions of security tokens. 
 *
 * @author TG Team
 *
 */
public enum Template {
    
    SAVE_NEW ("%s_CanSaveNew_Token", "%s Can Save New", "Authorises saving of new data."),
    SAVE_MODIFIED ("%s_CanSaveModified_Token", "%s Can Save Modified", "Authorises saving of modified data."),
    SAVE ("%s_CanSave_Token", "%s Can Save", "Authorises saving of new or modified data."),
    DELETE ("%s_CanDelete_Token", "%s Can Delete", "Authorises deletion of data."),
    MODIFY ("%s_CanModify_%s_Token", "%s Can Modify property [%s]", "Authorises modification of the specified property."),
    EXECUTE ("%s_CanExecute_Token", "%s Can Execute", "Authorises action execution."),
    MASTER_OPEN ("%s_CanOpen_Token", "%s Can Open", "Authorises opening of a compound master."),
    MASTER_MENU_ITEM_ACCESS ("%s_CanAccess_Token", "%s Can Access", "Authorises access to a menu item of a compound master.");
    
    private final String forClassName;
    private final String forTitle;
    private final String forDesc;

    public final String forClassName() {
        return forClassName;
    }

    public final String forTitle() {
        return forTitle;
    }

    public final String forDesc() {
        return forDesc;
    }

    private Template(final String forClassName, final String forTitle, final String forDesc) {
        this.forClassName = forClassName;
        this.forTitle = forTitle;
        this.forDesc = forDesc;
    }
}
