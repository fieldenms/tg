package fielden.platform.bnf;

public sealed interface Rule permits Derivation, Specialization {

    Variable lhs();

    Alternation rhs();

}
