package ua.com.fielden.platform.entity.query.generation.elements;

import static ua.com.fielden.platform.entity.query.DbVersion.ORACLE;

import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadataAnalyser;
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
    	return null;
    }

    @Override
    public String sql() {
        return dbVersion == ORACLE ? " DUAL " : " ";
    }

    @Override
    public List<EntValue> getValues() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return sql();
    }

    @Override
    public void populateSourceItems(boolean parentLeftJoinLegacy) {
        // there is nothing to populate
    }

    @Override
    protected Pair<PurePropInfo, PurePropInfo> lookForProp(String dotNotatedPropName) {
        return null;
    }
}