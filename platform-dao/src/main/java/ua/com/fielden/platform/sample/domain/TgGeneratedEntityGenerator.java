package ua.com.fielden.platform.sample.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.data.generator.IGenerator;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;

/**
 * This is an example centre's generator for {@link TgGeneratedEntity}.
 * <p>
 * It takes some parameters from selection criteria and generates a couple of {@link TgGeneratedEntity} instances for current user (relation to current user is defined in
 * 'createdBy' property).
 * 
 * @author TG Team
 *
 */
public class TgGeneratedEntityGenerator implements IGenerator<TgGeneratedEntity> {
    private final Logger logger = Logger.getLogger(getClass());
    private final ITgGeneratedEntity co;
    private final EntityFactory factory;
    private final IUserProvider userProvider;

    @Inject
    public TgGeneratedEntityGenerator(final ITgGeneratedEntity co, final EntityFactory factory, final IUserProvider userProvider) {
        this.co = co;
        this.factory = factory;
        this.userProvider = userProvider;
    }

    @Override
    @SessionRequired
    public Result gen(final Class<TgGeneratedEntity> type, final Map<String, Optional<?>> params) {
        logger.debug("TgGeneratedEntityGenerator.gen occurs. params = " + params);
        // generate instances based on crit-only multi criterion
        final List<String> critOnlyMultiCriterion = params.get("tgGeneratedEntity_critOnlyMultiProp").map(p -> (List<String>) p).orElse(new ArrayList<>());
        for (final String part : critOnlyMultiCriterion) {
            for (int index = 0; index < 15; index++) {
                co.save(factory.newEntity(TgGeneratedEntity.class).setEntityKey(part + index));
            }
        }
        // generate instances based on crit-only single criterion
        final User critOnlySingleCriterion = (User) params.get("tgGeneratedEntity_critOnlySingleProp").map(p -> (User) p).orElse(null);
        if (critOnlySingleCriterion != null) {
            if (critOnlySingleCriterion.equals(userProvider.getUser())) {
                return Result.failure(String.format("Can not generate the instance based on current user [%s], choose another user for that.", critOnlySingleCriterion));
            }
            for (int index = 0; index < 10; index++) {
                co.save(factory.newEntity(TgGeneratedEntity.class).setEntityKey(critOnlySingleCriterion.getKey() + "_GEN" + index));
            }
        }
        return Result.successful("ok");
    }

}
