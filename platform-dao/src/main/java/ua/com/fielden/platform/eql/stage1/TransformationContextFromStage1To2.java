package ua.com.fielden.platform.eql.stage1;

import com.google.common.collect.ImmutableList;
import ua.com.fielden.platform.eql.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.meta.QuerySourceInfoProvider;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.ImmutableListUtils;

import java.util.List;

// TODO Decouple injected dependencies from state.

/// Context for stage 1->2 transformation.
///
/// @param sourcesStack  a stack of sources; the first element is the top and corresponds to the directly enclosing query.
///     E.g., `select(X).join(Y).where().model(select(Z)...)` produces `[[Z], [X, Y]]`.
///     This stack is used for property resolution -- determining the source that a property belongs to.
///
public record TransformationContextFromStage1To2 (
        QuerySourceInfoProvider querySourceInfoProvider,
        IDomainMetadata domainMetadata,
        List<List<ISource2<?>>> sourcesStack)
{

    /// Creates a context with `sources` as the sources stack.
    ///
    public static TransformationContextFromStage1To2 mkContext(
            final QuerySourceInfoProvider querySourceInfoProvider,
            final IDomainMetadata domainMetadata,
            final List<List<ISource2<?>>> sources)
    {
        return new TransformationContextFromStage1To2(querySourceInfoProvider, domainMetadata, ImmutableList.copyOf(sources));
    }

    /// Creates an empty context.
    ///
    public static TransformationContextFromStage1To2 mkContext(
            final QuerySourceInfoProvider querySourceInfoProvider,
            final IDomainMetadata domainMetadata)
    {
        return mkContext(querySourceInfoProvider, domainMetadata, List.of());
    }

    /// Pushes the given source on top of the sources stack: `newStack = [s] + oldStack`.
    ///
    public TransformationContextFromStage1To2 pushSource(final ISource2<?> s) {
        return pushSources(ImmutableList.of(s));
    }

    /// Pushes the given sources on top of the sources stack: `newStack = ss + oldStack`.
    ///
    public TransformationContextFromStage1To2 pushSources(final Iterable<ISource2<?>> ss) {
        final var newSources = ImmutableListUtils.prepend(ImmutableList.copyOf(ss), sourcesStack);
        return new TransformationContextFromStage1To2(querySourceInfoProvider, domainMetadata, newSources);
    }

    /// Overwrites the sources stack.
    ///
    public TransformationContextFromStage1To2 setSourcesStack(final Iterable<List<ISource2<?>>> stack) {
        return new TransformationContextFromStage1To2(querySourceInfoProvider, domainMetadata, ImmutableList.copyOf(stack));
    }

    /// Returns the sources on top of the stack.
    ///
    public List<ISource2<?>> peekSources() {
        if (sourcesStack.isEmpty()) {
            throw new EqlStage1ProcessingException("The sources stack is empty.");
        }
        return sourcesStack.getFirst();
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();

        int level = sourcesStack.size();

        sb.append("TransformationContext1 " + System.identityHashCode(this) + ":\n");
        for (final List<ISource2<?>> levelSources : sourcesStack) {
            sb.append("  Level: " + level + (level == sourcesStack.size() ? " (innermost)" : (level == 1 ? " (outermost)" : "")) + "\n");
            level = level - 1;
            for (final ISource2<?> src : levelSources) {
                sb.append("    " + src.sourceType().getSimpleName() + (src.alias() != null ? " (" + src.alias() + ")" : "") + " -- " + src.getClass().getSimpleName() +  "\n");
            }
        }

        return sb.toString();
    }
}
