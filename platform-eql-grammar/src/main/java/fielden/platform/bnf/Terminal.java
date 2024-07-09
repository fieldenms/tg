package fielden.platform.bnf;

import java.util.Arrays;
import java.util.List;

public non-sealed interface Terminal extends Symbol {

    default Token with(List<? extends Parameter> parameters) {
        return new Token(this, parameters);
    }

    default Token with(Class<?>... parameters) {
        return with(Arrays.stream(parameters).map(NormalParameter::new).toList());
    }

    default Token rest(Class<?> varArityParameter) {
        return with(List.of(new VarArityParameter(varArityParameter)));
    }

    @Override
    default Terminal normalize() {
        return this;
    }

    @Override
    default <V> Terminal annotate(TermMetadata.Key<V> key, V value) {
        final var newMetadata = TermMetadata.merge(metadata(), key, value);
        final String name = name();
        final Terminal normal = normalize();

        return new Terminal() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public Terminal normalize() {
                return normal;
            }

            @Override
            public TermMetadata metadata() {
                return newMetadata;
            }
        };
    }

}
