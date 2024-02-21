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

}
