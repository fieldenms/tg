package fielden.platform.bnf;

import java.util.function.Function;
import java.util.stream.Stream;

public sealed interface Symbol extends Term permits Variable, Terminal {

    String name();

    @Override
    default Stream<Term> flatten() {
        return Stream.of(this);
    }

    @Override
    default Term recMap(final Function<? super Term, ? extends Term> mapper) {
        return mapper.apply(this);
    }

    default Term map(final Function<? super Term, ? extends Term> mapper) {
        return mapper.apply(this);
    }

}
