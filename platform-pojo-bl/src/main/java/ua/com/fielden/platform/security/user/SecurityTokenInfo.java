package ua.com.fielden.platform.security.user;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/** 
 * Master entity object.
 * 
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Class Name", desc = "The Class Name of this security token.")
@CompanionObject(ISecurityTokenInfo.class)
@DescTitle(value = "Description", desc = "Description of this security token")
public class SecurityTokenInfo extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;
    
    @IsProperty
    @Title(value = "Title", desc = "Title of this security token")
    private String title;

    @Observable
    public SecurityTokenInfo setTitle(final String title) {
        this.title = title;
        return this;
    }

    public String getTitle() {
        return title;
    }
}