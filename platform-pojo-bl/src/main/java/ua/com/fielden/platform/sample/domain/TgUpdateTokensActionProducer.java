package ua.com.fielden.platform.sample.domain;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.dao.AbstractFunctionalEntityProducerForCollectionModification;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.security.provider.SecurityTokenNode;
import ua.com.fielden.platform.security.provider.SecurityTokenProvider;
import ua.com.fielden.platform.security.tokens.AlwaysAccessibleToken;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * A producer for new instances of entity {@link TgUpdateTokensAction}.
 *
 * @author TG Team
 *
 */
public class TgUpdateTokensActionProducer extends AbstractFunctionalEntityProducerForCollectionModification<UserRole, TgUpdateTokensAction> implements IEntityProducer<TgUpdateTokensAction> {
    private final Logger logger = Logger.getLogger(getClass());
    private final IUserRoleDao coUserRole;
    private final IUser coUser;
    private final SecurityTokenProvider securityTokenProvider;
    
    @Inject
    public TgUpdateTokensActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder, final IUserRoleDao coUserRole, final IUser coUser, final IApplicationSettings applicationSettings) throws Exception {
        super(factory, TgUpdateTokensAction.class, companionFinder);
        this.coUserRole = coUserRole;
        this.coUser = coUser;
        this.securityTokenProvider = new SecurityTokenProvider(applicationSettings.pathToSecurityTokens(), applicationSettings.securityTokensPackageName());
    }
    
    @Override
    protected TgUpdateTokensAction provideCurrentlyAssociatedValues(final TgUpdateTokensAction entity, final UserRole masterEntity) {
        // synchronise security tokens classes with persistent entities
        final SortedSet<SecurityTokenNode> topLevelTokens = securityTokenProvider.getTopLevelSecurityTokenNodes();
        for (final SecurityTokenNode node : topLevelTokens) {
            logger.error("node == " + node.getLongDesc());
        }
        final String dtr = AlwaysAccessibleToken.class.getName();
        logger.error("new BigInteger(dtr.getBytes()) == " + new BigInteger(dtr.getBytes()));
        logger.error("new BigInteger(dtr.getBytes()).longValue() == " + new BigInteger(dtr.getBytes()).longValue());
        
        
        final List<TgSecurityToken> allAvailableTokens = new ArrayList<>();// coUserRole.findAll();
        allAvailableTokens.add(factory().newEntity(TgSecurityToken.class, null, AlwaysAccessibleToken.class.getName(), "Controls permission to select and review accidents.").setTitle("Accident Review"));
        allAvailableTokens.add(factory().newEntity(TgSecurityToken.class, null, AlwaysAccessibleToken.class.getName() + "2", "2 Controls permission to select and review accidents.").setTitle("Accident Review 2"));
        allAvailableTokens.add(factory().newEntity(TgSecurityToken.class, null, AlwaysAccessibleToken.class.getName() + "3", "3 Controls permission to select and review accidents.").setTitle("Accident Review 3"));
        
        final Set<TgSecurityToken> tokens = new LinkedHashSet<>(allAvailableTokens);
        entity.setTokens(tokens);
//        
//        final Set<Long> chosenRoleIds = new LinkedHashSet<>();
//        for (final UserAndRoleAssociation association: masterEntity.getRoles()) {
//            chosenRoleIds.add(association.getUserRole().getId());
//        }
//        entity.setChosenIds(chosenRoleIds);
        return entity;
    }
    
    @Override
    protected AbstractEntity<?> getMasterEntityFromContext(final CentreContext<?, ?> context) {
        // this producer is suitable for property actions on User master and for actions on User centre
        return context.getMasterEntity() == null ? context.getCurrEntity() : context.getMasterEntity();
    }

    @Override
    protected fetch<UserRole> fetchModelForMasterEntity() {
        return coUserRole.getFetchProvider().fetchModel();
    }
}