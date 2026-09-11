package ua.com.fielden.platform.eql.stage1.sundries;

import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.queries.AbstractQuery1;
import ua.com.fielden.platform.eql.stage2.sundries.Yield2;
import ua.com.fielden.platform.eql.stage3.sundries.Yield3;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadataUtils.SubPropertyNaming;

import java.util.Optional;

/// A temporary solution while waiting on [tg/2675](https://github.com/fieldenms/tg/issues/2675).
///
/// It transforms any yield that targets a property whose type has a single component (e.g., `Money` with `amount` only).
/// The transformed yield has an alias that represents that single component.
/// E.g., `as("price")`, where `price : Money`, is transformed into `as("price.amount")`.
///
/// The necessity of this transformation can be explained using the case of `Money`.
/// It prevents type inference from assigning type `Money` to a yield (see [Yield3#type]), which would cause an error under
/// PostgreSQL when attempting to create an SQL-level cast (`Money` is not a native SQL type).
/// The kind of type inference mentioned is the one that runs when the yield's operand type is null, serving as a fallback
/// and using the declared type of the yield's target (e.g., `price : Money`).
///
/// One extra benefit of this transformation is that it enables the use of such yields in top-level queries,
/// while previously this has been limited to source queries only.
///
public class ExpandSingleComponentTypedYield1 {

    private final IDomainMetadata domainMetadata;

    public ExpandSingleComponentTypedYield1(final IDomainMetadata domainMetadata) {
        this.domainMetadata = domainMetadata;
    }

    /// Transforms the yield if necessary.
    /// Returns an empty optional otherwise.
    ///
    public Optional<Yield2> apply(
            final Yield1 yield,
            final TransformationContextFromStage1To2 context,
            final AbstractQuery1 query)
    {
        if (query.resultType != null && query.resultType != EntityAggregates.class && !yield.alias().isEmpty()) {
            return domainMetadata.forPropertyOpt(query.resultType, yield.alias())
                    .filter(pm -> pm.type().isComponent())
                    .map(pm -> {
                        final var subProps = domainMetadata.propertyMetadataUtils().subProperties(pm, SubPropertyNaming.PATH);
                        if (subProps.size() == 1) {
                            return new Yield1(yield.operand(), subProps.getFirst().name(), false);
                        }
                        else {
                            return null;
                        }
                    })
                    .map(y -> y.transform(context));
        }
        else {
            return Optional.empty();
        }
    }


}
