package ua.com.fielden.platform.entity.query.metadata;

import jakarta.inject.Inject;
import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Optional;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.StreamUtils;

import java.util.List;

import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.reflection.AnnotationReflector.isPropertyAnnotationPresent;
import static ua.com.fielden.platform.reflection.Reflector.getKeyMemberSeparator;
import static ua.com.fielden.platform.types.Money.AMOUNT;
import static ua.com.fielden.platform.types.Money.CURRENCY;
import static ua.com.fielden.platform.utils.StreamUtils.foldLeft;

/// Generates EQL expressions for composite keys.
///
public class CompositeKeyEqlExpressionGenerator {

    static final String EMPTY_STRING = "";
    static final String ERR_NO_COMPOSITE_KEY = "Entity type [%s] does not have a composite key.";
    static final String CURRENCY_SYMBOL_AND_AMOUNT_SEPARATOR = " ";

    // TODO Reduce visibility once EQL tests use IoC.
    @Inject
    public CompositeKeyEqlExpressionGenerator(
            final IDomainMetadata domainMetadata,
            final IApplicationSettings appSettings)
    {
        this.domainMetadata = domainMetadata;
        this.appSettings = appSettings;
    }

    private final IDomainMetadata domainMetadata;
    private final IApplicationSettings appSettings;

    public ExpressionModel getKeyExpression(final Class<? extends AbstractEntity<?>> entityType) {
        final var keyMembers = domainMetadata.entityMetadataUtils().compositeKeyMembers(domainMetadata.forEntity(entityType));
        if (keyMembers.isEmpty()) {
            throw new InvalidArgumentException(ERR_NO_COMPOSITE_KEY.formatted(entityType.getSimpleName()));
        }
        return makeKeyExpression(entityType, keyMembers);
    }

    private ExpressionModel makeKeyExpression(
            final Class<? extends AbstractEntity<?>> entityType,
            final List<PropertyMetadata> keyMembers)
    {
        if (keyMembers.stream().noneMatch(km1 -> isOptional(entityType, km1.name()))) {
            return makeConcatWithoutOptional(entityType, keyMembers);
        }
        else {
            final var expr = makeConcatWithOptional(entityType, keyMembers);
            // If all are null, make the expression null.
            if (keyMembers.stream().allMatch(km1 -> isOptional(entityType, km1.name()))) {
                final var condAllNull = keyMembers.stream()
                        .map(km -> cond().prop(km.name()).isNull().model())
                        .reduce((cond1, cond2) -> cond().condition(cond1).and().condition(cond2).model())
                        // There must be at least one key member.
                        .orElseThrow();
                return expr().caseWhen().condition(condAllNull).then().val(null).otherwise().expr(expr).end().model();
            }
            else {
                return expr;
            }
        }
    }

    private boolean isOptional(final Class<? extends AbstractEntity<?>> entityType, final CharSequence property) {
        return isPropertyAnnotationPresent(Optional.class, entityType, property.toString());
    }

    private ExpressionModel makeConcatWithoutOptional(
            final Class<? extends AbstractEntity<?>> entityType,
            final List<PropertyMetadata> keyMembers)
    {
        final var separator = getKeyMemberSeparator((Class) entityType);

        return foldLeft(keyMembers.subList(1, keyMembers.size()).stream(),
                        expr().concat().expr(processKeyMember(entityType, keyMembers.getFirst())),
                        (acc, km) -> acc.with().val(separator).with().expr(processKeyMember(entityType, km)))
                .end().model();
    }

    private ExpressionModel makeConcatWithOptional(
            final Class<? extends AbstractEntity<?>> entityType,
            final List<PropertyMetadata> keyMembers)
    {
        final var separator = getKeyMemberSeparator((Class) entityType);

        return StreamUtils.enumerate(keyMembers.stream(), (km, i) -> {
            // This condition is true iff at least one of the key members preceding `km` is not null.
            // Start with a false condition to avoid a separator before the 1st key member and also when all preceding ones are null.
            final var condPrefix = foldLeft(keyMembers.subList(0, i),
                                            cond().val(1).eq().val(2).model(),
                                            (acc, prevKm) -> cond().condition(acc).or().prop(prevKm.name()).isNotNull().model());

            // Insert a separator only if the current key member is not null and `condPrefix` is true.
            final var sepExpr = expr().caseWhen().condition(condPrefix).then().val(separator).otherwise().val(EMPTY_STRING).end().model();
            return expr().caseWhen().prop(km.name()).isNotNull()
                    .then().concat().expr(sepExpr).with().expr(processKeyMember(entityType, km)).end()
                    .otherwise().val(EMPTY_STRING)
                    .end().model();
        }).reduce((expr1, expr2) -> expr().concat().expr(expr1).with().expr(expr2).end().model()).orElseThrow();
    }

    private ExpressionModel processKeyMember(final Class<? extends AbstractEntity<?>> entityType, final PropertyMetadata property) {
        return processKeyMember(entityType, property.name());
    }

    /// Creates an expression to represent a key member property as a string.
    ///
    /// ### Synchronisation of string representation with the client side
    /// The implementation of this method must ensure that the string representation matches the one used at the client side.
    /// Clients may send strings that represent composite key values for existing entities, and the server must be able to find those entities by key.
    /// Specifically, executing the following EQL query must produce the entity requested by a client.
    /// ```
    /// select(entityType).where().prop("key").eq().val(clientKeyString).model()
    /// ```
    /// Importantly, property `key`, being a composite key, will be replaced by an expression produced by this generator.
    ///
    private ExpressionModel processKeyMember(final Class<? extends AbstractEntity<?>> entityType, final String property) {
        final var pm = domainMetadata.forProperty(entityType, property);
        if (pm.type().isEntity() && !pm.type().javaType().equals(PropertyDescriptor.class)) {
            return processKeyMember(entityType, property + "." + KEY);
        }
        else if (pm.type().javaType().equals(Money.class)) {
            final ExpressionModel symbolExpr;
            // If this Money-typed property has currency, map the currency code to a symbol.
            if (domainMetadata.propertyMetadataUtils().hasSubProperty(pm, CURRENCY)
                && !appSettings.currencySymbolMap().isEmpty())
            {
                // Build a case-when that compares the currency code with each configured mapping.
                symbolExpr = foldLeft(appSettings.currencySymbolMap().entrySet().stream(),
                                      expr().caseWhen(),
                                      (acc, entry) -> acc.prop(pm.name() + "." + CURRENCY).eq().val(entry.getKey()).then().val(entry.getValue()).when())
                        // This is like "otherwise", but in a different form due to Fluent API composition.
                        .val(1).eq().val(1).then().val(appSettings.currencySymbol())
                        .end()
                        .model();
            }
            else {
                symbolExpr = expr().val(appSettings.currencySymbol()).model();
            }
            final var signExpr = expr().caseWhen().prop(property + "." + AMOUNT).lt().val(0)
                                       .then().val("-")
                                       .otherwise().val("")
                                       .end()
                                 .model();
            return expr()
                    .concat().expr(signExpr)
                    .with().expr(symbolExpr)
                    .with().val(CURRENCY_SYMBOL_AND_AMOUNT_SEPARATOR)
                    .with().prop(property + "." + AMOUNT)
                    .end()
                    .model();
        }
        else {
            // Concat with an empty string to implicitly coerce the value into a string.
            return expr().concat().val(EMPTY_STRING).with().prop(property).end().model();
        }
    }

}
