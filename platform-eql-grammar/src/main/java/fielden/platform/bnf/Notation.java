package fielden.platform.bnf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static fielden.platform.bnf.Sequence.seqOrTerm;

public sealed interface Notation extends Term permits Alternation, Quantifier {

    static OneOrMore repeat1(final Term term) {
        return new OneOrMore(term);
    }

    static OneOrMore repeat1(final Term term, final Term... terms) {
        var list = new ArrayList<Term>(1 + terms.length);
        list.add(term);
        Collections.addAll(list, terms);
        return new OneOrMore(seqOrTerm(list));
    }

    static ZeroOrMore repeat(final Term term) {
        return new ZeroOrMore(term);
    }

    static ZeroOrMore repeat(final Term term, final Term... terms) {
        var list = new ArrayList<Term>(1 + terms.length);
        list.add(term);
        Collections.addAll(list, terms);
        return new ZeroOrMore(seqOrTerm(list));
    }

    static Optional opt(final Term term) {
        return new Optional(term);
    }

    static Optional opt(final Term term, final Term... terms) {
        var list = new ArrayList<Term>(1 + terms.length);
        list.add(term);
        Collections.addAll(list, terms);
        return new Optional(seqOrTerm(list));
    }

    static Alternation oneOf(final Term term, final Term... terms) {
        var list = new ArrayList<Sequence>(1 + terms.length);
        list.add(Sequence.of(term));
        Arrays.stream(terms).map(Sequence::of).forEach(list::add);
        return new Alternation(list);
    }

}
