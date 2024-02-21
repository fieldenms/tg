package fielden.platform.bnf;

public sealed interface Symbol extends Term permits Variable, Terminal {

    String name();

}
