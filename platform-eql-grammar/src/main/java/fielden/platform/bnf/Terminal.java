package fielden.platform.bnf;

import java.util.Arrays;
import java.util.List;

public non-sealed interface Terminal extends Symbol {

    default Token with(final List<? extends Parameter> parameters) {
        return new Token(this, parameters);
    }

    default Token with(final Class<?>... parameters) {
        return with(Arrays.stream(parameters).map(NormalParameter::new).toList());
    }

    default Token rest(final Class<?> varArityParameter) {
        return with(List.of(new VarArityParameter(varArityParameter)));
    }

    @Override
    default Terminal normalize() {
        return this;
    }

    @Override
    default Terminal annotate(final Metadata.Annotation annotation) {
        final var newMetadata = Metadata.merge(metadata(), annotation);
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
            public Metadata metadata() {
                return newMetadata;
            }
        };
    }

}