package ua.com.fielden.platform.equery.interfaces;

import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.EntityAggregates;
import ua.com.fielden.platform.equery.ModelResult;
import ua.com.fielden.platform.equery.ReturnedModelResult;

public interface IQueryOrderedModel<T extends AbstractEntity> {

    /**
     * Gets class of the requested entity.
     *
     * @return
     */
    Class<T> getType();

    /**
     * Sets value(s) for the parameter.
     *
     * @param paramName
     * @param values
     */
    void setParamValue(final String paramName, final Object... values);

    /**
     * Gets value(s) of the parameter.
     *
     * @return
     */
    Object getParamValue(String paramName);

    /**
     * Generates new model to get the total count of the requested in the current model entities.
     *
     * @return
     */
    IQueryModel<EntityAggregates> getCountModel();


    /**
     * Enhances model with user data filtering.
     *
     * @param filter
     * @param userName
     * @return
     */
    IQueryOrderedModel<T> enhanceWith(IFilter filter, String userName);


//    /**
//     * Decomposes the model to unordered model and its ordering.
//     *
//     * @return
//     */
//    Pair<IQueryModel<T>, List<Pair<String, Ordering>>> decomposeToModelAndOrdering();

    /**
     * Returns existing query model but parameterised with {@link AbstractEntity}. This is required for fetching via fetch-models.
     *
     * @return
     */
    IQueryOrderedModel<AbstractEntity> getModelWithAbstractEntities();

    ReturnedModelResult getFinalModelResult(IMappingExtractor mappingExtractor);

    ModelResult getModelResult(IMappingExtractor mappingExtractor);

    void setLightweight(boolean lightweight);

    boolean isLightweight();

    List<String> getYieldedPropsNames();
}
