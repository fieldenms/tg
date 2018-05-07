package ua.com.fielden.platform.entity;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @IsProperty
    @Title("Security token filter")
    private String tokenFilter;

    @IsProperty(SecurityTokenTreeNodeEntity.class)
    @Title(value = "Tokens", desc = "Security Tokens")
    private List<SecurityTokenTreeNodeEntity> tokens = new ArrayList<>();

    @IsProperty(UserRole.class)
    @Title(value = "User roles", desc = "Available user roles")
    private List<UserRole> userRoles = new ArrayList<>();

    @IsProperty(Object.class)
    @Title(value = "Token - Role association", desc = "Desc")
    private Map<String, List<Long>> tokenRoleMap = new HashMap<>();

    protected SecurityMatrixInsertionPoint() {
        setKey(NO_KEY);
    }

    @Observable
    public SecurityMatrixInsertionPoint setTokenFilter(final String tokenFilter) {
        this.tokenFilter = tokenFilter;
        return this;
    }

    public String getTokenFilter() {
        return tokenFilter;
    }

    @Observable
    protected SecurityMatrixInsertionPoint setUserRoles(final List<UserRole> userRoles) {
        this.userRoles.clear();
        this.userRoles.addAll(userRoles);
        return this;
    }

    @Observable
    public SecurityMatrixInsertionPoint setTokenRoleMap(final Map<String, List<Long>> tokenRoleMap) {
        this.tokenRoleMap.clear();
        this.tokenRoleMap.putAll(tokenRoleMap);
        return this;
    }

    public Map<String, List<Long>> getTokenRoleMap() {
        return Collections.unmodifiableMap(tokenRoleMap);
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
