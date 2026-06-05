package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.*;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;


/// The entity that is used more like a transport for security tokens to the client
///
@KeyType(String.class)
@KeyTitle("Security token")
@CompanionObject(SecurityTokenTreeNodeEntityCo.class)
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
    private final Set<SecurityTokenTreeNodeEntity> children = new LinkedHashSet<>();

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

    public Set<SecurityTokenTreeNodeEntity> getChildren() {
        return Collections.unmodifiableSet(children);
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
