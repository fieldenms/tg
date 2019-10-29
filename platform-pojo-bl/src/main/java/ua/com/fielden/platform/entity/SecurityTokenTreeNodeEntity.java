package ua.com.fielden.platform.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;


/**
 * The entity that is used more like a transport for security tokens to the client
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle("Security token")
@CompanionObject(ISecurityTokenTreeNodeEntity.class)
@DescTitle("Security token description")
public class SecurityTokenTreeNodeEntity extends AbstractEntity<String> {

    @IsProperty
    @Title("Title")
    private String title;

    @IsProperty
    @Title(value = "Parent token", desc = "Desc")
    private SecurityTokenTreeNodeEntity parent;

    @IsProperty(SecurityTokenTreeNodeEntity.class)
    @Title(value = "Children", desc = "Desc")
    private List<SecurityTokenTreeNodeEntity> children = new ArrayList<>();

    @Observable
    public SecurityTokenTreeNodeEntity setTitle(final String title) {
        this.title = title;
        return this;
    }

    public String getTitle() {
        return title;
    }

    @Observable
    public SecurityTokenTreeNodeEntity setChildren(final Set<SecurityTokenTreeNodeEntity> children) {
        this.children.clear();
        this.children.addAll(children);
        return this;
    }

    public List<SecurityTokenTreeNodeEntity> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Observable
    public SecurityTokenTreeNodeEntity setParent(final SecurityTokenTreeNodeEntity parent) {
        this.parent = parent;
        return this;
    }

    public SecurityTokenTreeNodeEntity getParent() {
        return parent;
    }
}
