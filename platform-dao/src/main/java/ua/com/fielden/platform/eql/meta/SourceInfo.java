package ua.com.fielden.platform.eql.meta;

import ua.com.fielden.platform.eql.s2.elements.ISource2;

public class SourceInfo {
    private final ISource2 source;
    private final EntityInfo entityInfo;
    private final boolean aliasingAllowed;
    private final String alias;

    public SourceInfo(final ISource2 source, final EntityInfo entityInfo, final boolean aliasingAllowed, final String alias) {
        this.source = source;
        this.entityInfo = entityInfo;
        this.aliasingAllowed = aliasingAllowed;
        this.alias = alias;
    }

    SourceInfo produceNewWithoutAliasing() {
        return new SourceInfo(source, entityInfo, false, alias);
    }

    public ISource2 getSource() {
        return source;
    }

    public EntityInfo getEntityInfo() {
        return entityInfo;
    }

    public boolean isAliasingAllowed() {
        return aliasingAllowed;
    }

    public String getAlias() {
        return alias;
    }
}
