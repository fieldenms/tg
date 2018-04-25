package ua.com.fielden.platform.entity;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.security.user.UserRole;

@KeyType(NoKey.class)
@KeyTitle("Security Matrix Insertion Point")
@CompanionObject(ISecurityMatrixInsertionPoint.class)
public class SecurityMatrixInsertionPoint extends AbstractFunctionalEntityWithCentreContext<NoKey> {

    @IsProperty(SecurityTokenTreeNodeEntity.class)
    @Title(value = "Tokens", desc = "Security Tokens")
    private List<SecurityTokenTreeNodeEntity> tokens = new ArrayList<>();

    @IsProperty(UserRole.class)
    @Title(value = "User roles", desc = "Available user roles")
    private List<UserRole> userRoles = new ArrayList<>();

    protected SecurityMatrixInsertionPoint() {
        setKey(NO_KEY);
    }

    @Observable
    protected SecurityMatrixInsertionPoint setUserRoles(final List<UserRole> userRoles) {
        this.userRoles.clear();
        this.userRoles.addAll(userRoles);
        return this;
    }

    public List<UserRole> getUserRoles() {
        return Collections.unmodifiableList(userRoles);
    }

    @Observable
    public SecurityMatrixInsertionPoint setTokens(final List<SecurityTokenTreeNodeEntity> tokens) {
        this.tokens.clear();
        this.tokens.addAll(tokens);
        return this;
    }

    public List<SecurityTokenTreeNodeEntity> getTokens() {
        return Collections.unmodifiableList(tokens);
    }
}
