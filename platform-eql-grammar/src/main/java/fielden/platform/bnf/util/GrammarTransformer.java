package fielden.platform.bnf.util;

import fielden.platform.bnf.BNF;

import java.util.function.Function;

/**
 * Transforms a grammar into another grammar.
 */
@FunctionalInterface
public interface GrammarTransformer extends Function<BNF, BNF> {}
