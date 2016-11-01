package ua.com.fielden.platform.sample.domain;

import java.util.List;
import java.util.Map;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.data.generator.IGenerator;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;

public class TgGeneratedEntityGenerator implements IGenerator<TgGeneratedEntity> {
    private final ITgGeneratedEntity co;
    private final EntityFactory factory;
    
    @Inject
    public TgGeneratedEntityGenerator(final ITgGeneratedEntity co, final EntityFactory factory) {
        this.co = co;
        this.factory = factory;
    }

    @Override
    @SessionRequired
    public Result gen(final Class<TgGeneratedEntity> type, final Map<String, Object> params) {
        System.out.println("YAY");
        final List<String> selectionCriterion = (List<String>) params.get("tgGeneratedEntity_");
        if (!selectionCriterion.isEmpty()) {
            final TgGeneratedEntity newEntity = factory.newByKey(TgGeneratedEntity.class, selectionCriterion.get(0));
            co.save(newEntity);
        }
        
        return Result.successful("ok");
    }

}
