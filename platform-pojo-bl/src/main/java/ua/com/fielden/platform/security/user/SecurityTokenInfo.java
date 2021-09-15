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
 * An entity that represents a security token, which is predominantly used for data marshaling and use at the client side. 
 * 
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Class Name", desc = "The Class Name of this security token.")
@CompanionObject(SecurityTokenInfoCo.class)
@DescTitle(value = "Description", desc = "Description of this security token")
public class SecurityTokenInfo extends AbstractEntity<String> {
    
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
    
    @Override
    @Observable
    public SecurityTokenInfo setKey(final String key) {
        return (SecurityTokenInfo) super.setKey(key);
    }
}