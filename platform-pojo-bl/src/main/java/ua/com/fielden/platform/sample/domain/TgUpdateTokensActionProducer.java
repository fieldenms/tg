package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.sun.org.apache.regexp.internal.recompile;

import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.dao.AbstractFunctionalEntityProducerForCollectionModification;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.dao.ISecurityRoleAssociationDao;
import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.security.provider.SecurityTokenNode;
import ua.com.fielden.platform.security.provider.SecurityTokenProvider;
import ua.com.fielden.platform.security.tokens.AlwaysAccessibleToken;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
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
    private final ISecurityRoleAssociationDao associationCompanion;
    
    @Inject
    public TgUpdateTokensActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder, final IUserRoleDao coUserRole, final IUser coUser, final IApplicationSettings applicationSettings, final ISecurityRoleAssociationDao associationCompanion) throws Exception {
        super(factory, TgUpdateTokensAction.class, companionFinder);
        this.coUserRole = coUserRole;
        this.coUser = coUser;
        this.securityTokenProvider = new SecurityTokenProvider(applicationSettings.pathToSecurityTokens(), applicationSettings.securityTokensPackageName());
        this.associationCompanion = associationCompanion;
    }
    
    @Override
    protected TgUpdateTokensAction provideCurrentlyAssociatedValues(final TgUpdateTokensAction entity, final UserRole masterEntity) {
        final SortedSet<SecurityTokenNode> topLevelTokens = securityTokenProvider.getTopLevelSecurityTokenNodes();
        final Set<TgSecurityToken> linearisedTokens = lineariseTokens(topLevelTokens, factory());
        logger.error("linearisedTokens == " + linearisedTokens);
        entity.setTokens(linearisedTokens);
        
        final Set<String> chosenRoleIds = new LinkedHashSet<>();
        final List<SecurityRoleAssociation> associations = associationCompanion.getAllEntities(
                from(select(SecurityRoleAssociation.class).where().prop("role").eq().val(masterEntity).model())
                .with(fetchAll(SecurityRoleAssociation.class)).model()
        );
        
        for (final SecurityRoleAssociation association: associations) {
            chosenRoleIds.add(association.getSecurityToken().getName());
        }
        entity.setChosenIds(chosenRoleIds);
        return entity;
    }
    
    private static Set<TgSecurityToken> lineariseTokens(final SortedSet<SecurityTokenNode> topLevelTokenNodes, final EntityFactory factory) {
        final Set<TgSecurityToken> setOfTokens = new LinkedHashSet<>();
        for (final SecurityTokenNode node: topLevelTokenNodes){
            setOfTokens.addAll(lineariseToken(node, factory));
        }
        return setOfTokens;
    }
    
    private static List<TgSecurityToken> lineariseToken(final SecurityTokenNode tokenNode, final EntityFactory factory) {
        final TgSecurityToken tokenEntity = factory.newEntity(TgSecurityToken.class, null, tokenNode.getToken().getName(), tokenNode.getLongDesc()).setTitle(tokenNode.getShortDesc());
        final List<TgSecurityToken> listOfTokens = new ArrayList<>();
        listOfTokens.add(tokenEntity);
        final List<SecurityTokenNode> children = tokenNode.daughters();
        for (final SecurityTokenNode child: children) {
            final List<TgSecurityToken> childList = lineariseToken(child, factory);
            listOfTokens.addAll(childList);
        }
        return listOfTokens;
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