package ua.com.fielden.platform.eql.metadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.eql.metadata.Table.ColumnData;

public class PersistedEntity {
    private final String className;
    private final String suggestedTableName;
    private final List<FinalProperty> finalProps = new ArrayList<>();

    public List<FinalProperty> getFinalProps() {
        return finalProps;
    }

    public PersistedEntity(String className, String suggestedTableName) {
        this.className = className;
        this.suggestedTableName = suggestedTableName;
    }
    
    private String nameClause() {
        return (StringUtils.isNotBlank(suggestedTableName) ? suggestedTableName : className.split("\\.")[className.split("\\.").length - 1].toUpperCase() + "_");
    }
    
    public Table produceTable() {
        List<ColumnData> columnsData = new ArrayList<>();
        for (FinalProperty fp : finalProps) {
            columnsData.add(new ColumnData(fp.nameClause(), fp.isNullable(), fp.getSqlType(), fp.getLength(), fp.getScale()));
        }
        
        return new Table(nameClause(), columnsData);
    }
}