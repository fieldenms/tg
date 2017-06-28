package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.EntityNewAction;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

import java.math.BigDecimal;

import org.joda.time.format.DateTimeFormat;

import com.google.inject.Inject;

/**
 * A producer for new instances of entity {@link TgMessage}.
 *
 * @author TG Team
 *
 */
public class TgMessageProducer extends DefaultEntityProducerWithContext<TgMessage> {

    @Inject
    public TgMessageProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, TgMessage.class, companionFinder);
    }
    
    @Override
    protected TgMessage provideDefaultValuesForStandardNew(final TgMessage entity, final EntityNewAction masterEntity) {
        final TgMessage message = super.provideDefaultValuesForStandardNew(entity, masterEntity);
        message.setGpsTime(DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss.SSS").parseDateTime("04/03/2014 16:45:52.000").toDate());
        message.setTravelledDistance(BigDecimal.valueOf(12.8));
        message.setVectorAngle(78);
        message.setVectorSpeed(25);
        message.setX(BigDecimal.valueOf(24.005127));
        message.setY(BigDecimal.valueOf(49.852152));
        return message;
    }
}
