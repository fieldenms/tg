package ua.com.fielden.platform.entity.query.model.transformation;



public interface IQuerySource {

    /**
     * Generates sequential sql alias for given table/query taking into account its position within the query sources and also its position in masterQuery/subQuery hierarchy.
     * e.g. T1, T3, T1L2
     * @return
     */
    String alias();


    /**
     * Generates sql representation of given table/query. E.g.  EQDET or (SELECT ... FROM ... WHERE ...). For cases of queries as sources its yields should be aliased sequentially (e.g. C1, C2 ... C25) and this should be the same alias that respective IQuerySourceItems return.
     * @return
     */
    String sql();

    IQuerySource getPredecessor();

    IQuerySourceItem getSourceItem(String dotNotatedName /*full name or context-aware name*/);

//    IQuerySource getMaster();
//    boolean hasReferences();
//    public ResultPropertyInfo getPropInfo(String dotNotatedPropName);
}
