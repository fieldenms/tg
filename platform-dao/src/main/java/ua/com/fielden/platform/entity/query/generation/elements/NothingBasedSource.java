package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.utils.Pair;

public class NothingBasedSource extends AbstractSource {

    public NothingBasedSource(final DomainMetadataAnalyser domainMetadataAnalyser) {
        super(null, domainMetadataAnalyser);
    }

    @Override
    public boolean generated() {
        return false;
    }

    @Override
    public Class<? extends AbstractEntity<?>> sourceType() {
        return EntityAggregates.class;
    }

    @Override
    public String sql() {
        if (dbVersion == DbVersion.ORACLE) {
            return " DUAL ";
        } else {
            return " ";
        }
    }

    @Override
    public List<EntValue> getValues() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        if (dbVersion == DbVersion.ORACLE) {
            return " DUAL ";
        } else {
            return "";
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof NothingBasedSource)) {
            return false;
        }
        return true;
    }

    @Override
    public void populateSourceItems(boolean parentLeftJoinLegacy) {
        // TODO Auto-generated method stub
    }

    @Override
    protected Pair<PurePropInfo, PurePropInfo> lookForProp(String dotNotatedPropName) {
        // TODO Auto-generated method stub
        return null;
    }
}