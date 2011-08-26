package ua.com.fielden.platform.entity.query.model.transformation;

import ua.com.fielden.platform.entity.query.model.structure.QueryModelResult.ResultPropertyInfo;


public interface IQuerySource {
    String alias();
    IQuerySourceItem getSourceItem(String dotNotatedName /*full name or context-aware name*/);
    String getSourceItemSql(String sourceItemName);
    boolean hasReferences();

    public ResultPropertyInfo getPropInfo(String dotNotatedPropName);
    public String getSql();
}
