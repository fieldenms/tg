package ua.com.fielden.platform.basic;

import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.utils.EntityUtils.isCompositeEntity;
import static ua.com.fielden.platform.utils.EntityUtils.keyPaths;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.ConditionModel;

public class ValueMatcherUtils {
    
    private ValueMatcherUtils() {}

    public static ConditionModel createStrictSearchByKeyCriteriaModel(Class<? extends AbstractEntity<?>> entityType, final String searchString) {
        ConditionModel keyCriteria = cond().prop(KEY).iLike().val(searchString).model();

        if (isCompositeEntity((Class<? extends AbstractEntity<?>>) entityType)) {
            for (String propName : keyPaths(entityType)) {
                keyCriteria = cond().condition(keyCriteria).or().prop(propName).iLike().val(searchString).model();
            }
        }

        return keyCriteria;
    }

    public static ConditionModel createRelaxedSearchByKeyCriteriaModel(final String searchString) {
        return cond().prop(KEY).iLike().val("%" + searchString).model();
    }

}
