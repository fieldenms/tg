package fielden.platform.bnf;

import com.google.common.collect.ImmutableList;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * A sequence of terms, which is itself a term. More commonly known as <i>grouping</i>.
 */
public final class Sequence implements List<Term>, Term {

    private static final Sequence EMPTY_SEQUENCE = new Sequence(ImmutableList.of());

    private final List<Term> terms;
    private final Metadata metadata;

    private Sequence(final Collection<? extends Term> terms, Metadata metadata) {
        this.terms = ImmutableList.copyOf(terms);
        this.metadata = metadata;
    }

    private Sequence(final Collection<? extends Term> terms) {
        this(terms, Metadata.EMPTY_METADATA);
    }

    public static Sequence of(final Term... terms) {
        return of(ImmutableList.copyOf(terms));
    }

    public static Sequence of(final Collection<? extends Term> terms) {
        if (terms.isEmpty()) {
            return EMPTY_SEQUENCE;
        }
        else if (terms.size() == 1 && terms.iterator().next() instanceof Sequence seq) {
            return seq;
        }
        return new Sequence(terms);
    }

    /**
     * Given a single term, returns it, otherwise returns a sequence containing all given terms.
     */
    public static Term seqOrTerm(Term... terms) {
        return terms.length == 1 ? terms[0] : of(terms);
    }

    /**
     * Given a single term, returns it, otherwise returns a sequence containing all given terms.
     */
    public static Term seqOrTerm(Term term, Term... terms) {
        return terms.length == 0 ? term : of(ImmutableList.<Term>builder().add(term).add(terms).build());
    }

    /**
     * Given a single term, returns it, otherwise returns a sequence containing all given terms.
     */
    public static Term seqOrTerm(Collection<? extends Term> terms) {
        return terms.size() == 1 ? terms.iterator().next() : of(terms);
    }

    @Override
    public Term normalize() {
        return new Sequence(terms);
    }

    @Override
    public Metadata metadata() {
        return metadata;
    }

    @Override
    public Sequence annotate(final Metadata.Annotation annotation) {
        return new Sequence(terms, Metadata.merge(metadata, annotation));
    }

    @Override
    public Sequence recMap(final Function<? super Term, ? extends Term> mapper) {
        return new Sequence(terms.stream().map(t -> t.recMap(mapper)).collect(toImmutableList()), metadata);
    }

    @Override
    public Sequence map(final Function<? super Term, ? extends Term> mapper) {
        return new Sequence(terms.stream().map(mapper).collect(toImmutableList()), metadata);
    }

    @Override
    public Stream<Term> flatten() {
        return terms.stream().flatMap(Term::flatten);
    }

    @Override
    public int size() {
        return terms.size();
    }

    @Override
    public boolean isEmpty() {
        return terms.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        return terms.contains(o);
    }

    @Override
    public int indexOf(final Object o) {
        return terms.indexOf(o);
    }

    @Override
    public int lastIndexOf(final Object o) {
        return terms.lastIndexOf(o);
    }

    @Override
    public Object[] toArray() {
        return terms.toArray();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        return terms.toArray(a);
    }

    @Override
    public Term get(final int index) {
        return terms.get(index);
    }

    @Override
    public Term getFirst() {
        return terms.getFirst();
    }

    @Override
    public Term getLast() {
        return terms.getLast();
    }

    @Override
    public Term set(final int index, final Term element) {
        return terms.set(index, element);
    }

    @Override
    public boolean add(final Term term) {
        return terms.add(term);
    }

    @Override
    public void add(final int index, final Term element) {
        terms.add(index, element);
    }

    @Override
    public void addFirst(final Term element) {
        terms.addFirst(element);
    }

    @Override
    public void addLast(final Term element) {
        terms.addLast(element);
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof Sequence that && terms.equals(that.terms);
    }

    @Override
    public int hashCode() {
        return terms.hashCode();
    }

    @Override
    public ListIterator<Term> listIterator(final int index) {
        return terms.listIterator(index);
    }

    @Override
    public ListIterator<Term> listIterator() {
        return terms.listIterator();
    }

    @Override
    public Iterator<Term> iterator() {
        return terms.iterator();
    }

    @Override
    public List<Term> subList(final int fromIndex, final int toIndex) {
        return terms.subList(fromIndex, toIndex);
    }

    @Override
    public void forEach(final Consumer<? super Term> action) {
        terms.forEach(action);
    }

    @Override
    public Spliterator<Term> spliterator() {
        return terms.spliterator();
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return terms.containsAll(c);
    }

    @Override
    public String toString() {
        return terms.toString();
    }

    @Override
    public <T> T[] toArray(final IntFunction<T[]> generator) {
        return terms.toArray(generator);
    }

    @Override
    public Stream<Term> stream() {
        return terms.stream();
    }

    @Override
    public Stream<Term> parallelStream() {
        return terms.parallelStream();
    }

    @Override
    public List<Term> reversed() {
        return terms.reversed();
    }

    @Override
    public boolean removeIf(final Predicate<? super Term> filter) {
        throw new UnsupportedOperationException("%s is immutable!".formatted(this.getClass().getSimpleName()));
    }

    @Override
    public void replaceAll(final UnaryOperator<Term> operator) {
        throw new UnsupportedOperationException("%s is immutable!".formatted(this.getClass().getSimpleName()));
    }

    @Override
    public void sort(final Comparator<? super Term> c) {
        throw new UnsupportedOperationException("%s is immutable!".formatted(this.getClass().getSimpleName()));
    }

    @Override
    public boolean remove(final Object o) {
        throw new UnsupportedOperationException("%s is immutable!".formatted(this.getClass().getSimpleName()));
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("%s is immutable!".formatted(this.getClass().getSimpleName()));
    }

    @Override
    public boolean addAll(final Collection<? extends Term> c) {
        throw new UnsupportedOperationException("%s is immutable!".formatted(this.getClass().getSimpleName()));
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends Term> c) {
        throw new UnsupportedOperationException("%s is immutable!".formatted(this.getClass().getSimpleName()));
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        throw new UnsupportedOperationException("%s is immutable!".formatted(this.getClass().getSimpleName()));
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        throw new UnsupportedOperationException("%s is immutable!".formatted(this.getClass().getSimpleName()));
    }

    @Override
    public Term remove(final int index) {
        throw new UnsupportedOperationException("%s is immutable!".formatted(this.getClass().getSimpleName()));
    }

    @Override
    public Term removeFirst() {
        throw new UnsupportedOperationException("%s is immutable!".formatted(this.getClass().getSimpleName()));
    }

    @Override
    public Term removeLast() {
        throw new UnsupportedOperationException("%s is immutable!".formatted(this.getClass().getSimpleName()));
    }

}
