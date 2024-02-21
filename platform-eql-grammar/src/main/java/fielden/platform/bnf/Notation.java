package fielden.platform.bnf;

import java.util.ArrayList;
import java.util.Collections;

public sealed interface Notation extends Term permits OneOrMore, Optional, ZeroOrMore {

    Term term();

    static OneOrMore repeat1(Term term) {
        return new OneOrMore(term);
    }

    static OneOrMore repeat1(Term term, Term... terms) {
        var list = new ArrayList<Term>(1 + terms.length);
        list.add(term);
        Collections.addAll(list, terms);
        return new OneOrMore(new Sequence(list));
    }

    static ZeroOrMore repeat(Term term) {
        return new ZeroOrMore(term);
    }

    static ZeroOrMore repeat(Term term, Term... terms) {
        var list = new ArrayList<Term>(1 + terms.length);
        list.add(term);
        Collections.addAll(list, terms);
        return new ZeroOrMore(new Sequence(list));
    }

    static Optional opt(Term term) {
        return new Optional(term);
    }

    static Optional opt(Term term, Term... terms) {
        var list = new ArrayList<Term>(1 + terms.length);
        list.add(term);
        Collections.addAll(list, terms);
        return new Optional(new Sequence(list));
    }

}
