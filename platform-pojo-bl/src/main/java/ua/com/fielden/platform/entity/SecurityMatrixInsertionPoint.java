package ua.com.fielden.platform.entity;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.security.user.UserRole;

@EntityTitle(value = "Security Matrix", desc = "A tool to manage user role authorisations.")
@KeyType(NoKey.class)
@KeyTitle("Security Matrix")
@CompanionObject(ISecurityMatrixInsertionPoint.class)
public class SecurityMatrixInsertionPoint extends AbstractFunctionalEntityWithCentreContext<NoKey> {

    @IsProperty
    @Title("Security token filter")
    private String tokenFilter;

    @IsProperty
    @Title(value = "User role filter", desc = "Desc")
    private String roleFilter;

    @IsProperty(SecurityTokenTreeNodeEntity.class)
    @Title(value = "Tokens", desc = "Security Tokens")
    private final List<SecurityTokenTreeNodeEntity> tokens = new ArrayList<>();

    @IsProperty(UserRole.class)
    @Title(value = "User roles", desc = "Available user roles")
    private final List<UserRole> userRoles = new ArrayList<>();

    @IsProperty(Object.class)
    @Title(value = "Token - Role association", desc = "Desc")
    private final Map<String, List<Long>> tokenRoleMap = new HashMap<>();

    @IsProperty
    @Title(value = "Was Calculated", desc = "Desc")
    private boolean calculated = false;

    protected SecurityMatrixInsertionPoint() {
        setKey(NO_KEY);
    }

    @Observable
    public SecurityMatrixInsertionPoint setCalculated(final boolean calculated) {
        this.calculated = calculated;
        return this;
    }

    public boolean isCalculated() {
        return calculated;
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
    public SecurityMatrixInsertionPoint setRoleFilter(final String roleFilter) {
        this.roleFilter = roleFilter;
        return this;
    }

    public String getRoleFilter() {
        return roleFilter;
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
