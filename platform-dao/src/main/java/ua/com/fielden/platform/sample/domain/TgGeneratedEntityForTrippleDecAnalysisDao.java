package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.Money;
/**
 * DAO implementation for companion object {@link ITgGeneratedEntity}.
 *
 * @author TG Team
 *
 */
@EntityType(TgGeneratedEntityForTrippleDecAnalysis.class)
public class TgGeneratedEntityForTrippleDecAnalysisDao extends CommonEntityDao<TgGeneratedEntityForTrippleDecAnalysis> implements ITgGeneratedEntityForTrippleDecAnalysis {

    private static final int maxRandNumber = 10000;

    @Inject
    public TgGeneratedEntityForTrippleDecAnalysisDao(final IFilter filter) {
        super(filter);
    }

    @Override
    public Result gen(final Class<TgGeneratedEntityForTrippleDecAnalysis> type, final Map<String, Optional<?>> params) {
        final ITgGeneratedEntityForTrippleDecAnalysis coGenerator = co$(TgGeneratedEntityForTrippleDecAnalysis.class);
        final IUser coUser = co(User.class);
        final User currUser = coUser.getUser();
        final Random rand = new Random();
        final Integer entityCount  = params.get("tgGeneratedEntityForTrippleDecAnalysis_entityCount").map(num -> (Integer)num).orElse(Integer.valueOf(45));
        removeAllGeneratedEntities(coGenerator, currUser);
        for (int entityCounter = 0; entityCounter < entityCount.intValue(); entityCounter++) {
            createAndSaveGeneratedEntity(coGenerator, rand, entityCounter);
        }
        return Result.successful("Ok");
    }

    private void removeAllGeneratedEntities(final ITgGeneratedEntityForTrippleDecAnalysis coGenerator, final User currUser) {
        coGenerator.batchDelete(select(TgGeneratedEntityForTrippleDecAnalysis.class).where().prop("createdBy").eq().val(currUser).model());
    }

    private void createAndSaveGeneratedEntity(final ITgGeneratedEntityForTrippleDecAnalysis coGenerator, final Random rand, final int entityCounter) {
        final TgGeneratedEntityForTrippleDecAnalysis generatedEntity = coGenerator.new_()
                .setGroup("group " + entityCounter)
                .setCost(Money.of(String.valueOf(rand.nextDouble() * maxRandNumber)))
                .setCount(Integer.valueOf(rand.nextInt(maxRandNumber + 1)))
                .setHours(new BigDecimal(rand.nextDouble() * maxRandNumber).setScale(2, RoundingMode.HALF_UP))
                .setDesc("group " + entityCounter + " description");
        coGenerator.save(generatedEntity);
    }

    @Override
    @SessionRequired
    public int batchDelete(final EntityResultQueryModel<TgGeneratedEntityForTrippleDecAnalysis> model) {
        return defaultBatchDelete(model);
    }

    @Override
    @SessionRequired
    public int batchDelete(final Collection<Long> entitiesIds) {
        return defaultBatchDelete(entitiesIds);
    }

    @Override
    @SessionRequired
    public int batchDelete(final List<TgGeneratedEntityForTrippleDecAnalysis> entities) {
        return defaultBatchDelete(entities);
    }

    @Override
    protected IFetchProvider<TgGeneratedEntityForTrippleDecAnalysis> createFetchProvider() {
        return super.createFetchProvider().with("createdBy", "createdDate", "group", "count", "cost", "hours");
    }
}