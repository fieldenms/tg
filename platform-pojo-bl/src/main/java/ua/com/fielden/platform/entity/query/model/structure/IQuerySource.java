package ua.com.fielden.platform.entity.query.model.structure;

import ua.com.fielden.platform.entity.query.model.structure.QueryModelResult.ResultPropertyInfo;


public interface IQuerySource {
    IQuerySourceItem getSourceItem(String dotNotatedName /*full name or context-aware name*/);
    boolean hasReferences();

    public ResultPropertyInfo getPropInfo(String dotNotatedPropName);
    public String getSql();
}
