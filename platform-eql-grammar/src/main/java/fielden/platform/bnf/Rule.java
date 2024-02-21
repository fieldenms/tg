package fielden.platform.bnf;

import java.util.Collection;
import java.util.stream.Stream;

public sealed interface Rule permits Derivation, Specialization {

    Variable lhs();

    Stream<Sequence> rhs();

    default Stream<Term> rhsTerms() {
        return rhs().flatMap(Collection::stream);
    }

}
