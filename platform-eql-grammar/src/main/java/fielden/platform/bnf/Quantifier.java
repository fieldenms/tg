package fielden.platform.bnf;

import java.util.stream.Stream;

public sealed interface Quantifier extends Notation permits OneOrMore, Optional, ZeroOrMore {

    Term term();

    @Override
    default Stream<Term> flatten() {
        return term().flatten();
    }

}
