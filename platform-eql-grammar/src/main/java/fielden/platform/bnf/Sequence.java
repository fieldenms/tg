package fielden.platform.bnf;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

public final class Sequence implements List<Term>, Term {

    private final List<Term> terms;
    private final TermMetadata metadata;

    public Sequence(Collection<? extends Term> terms, TermMetadata metadata) {
        this.terms = List.copyOf(terms);
        this.metadata = metadata;
    }

    public Sequence(Collection<? extends Term> terms) {
        this(terms, TermMetadata.EMPTY_METADATA);
    }

    public Sequence(Term... terms) {
        this(Arrays.asList(terms), TermMetadata.EMPTY_METADATA);
    }

    @Override
    public Term normalize() {
        return new Sequence(terms);
    }

    @Override
    public <V> Sequence annotate(final TermMetadata.Key<V> key, final V value) {
        return new Sequence(terms, TermMetadata.merge(metadata, key, value));
    }

    public Sequence map(final Function<? super Term, ? extends Term> mapper) {
        return new Sequence(terms.stream().map(mapper).toList());
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
        return terms.equals(o);
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
