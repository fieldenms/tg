package ua.com.fielden.platform.pagination;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.KeyType;

public class IterablePageTestCase {

    private List<Entity> oneElement = new ArrayList<Entity>() {{add(new Entity()); }};
    private List<Entity> twoElements = new ArrayList<Entity>() {{add(new Entity()); add(new Entity());}};
    private List<Entity> threeElements = new ArrayList<Entity>() {{add(new Entity()); add(new Entity()); add(new Entity());}};


    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void failed_to_chekc_has_next_for_a_single_element_page() {
	final IterablePage<Entity> page = new IterablePage<Entity>(new Page(oneElement));
	assertTrue(page.hasNext());
    }

    @Test
    public void accessing_two_single_element_pages_should_be_possible() {
	final IterablePage<Entity> page = new IterablePage<Entity>(new Page(oneElement, new Page(oneElement)));
	assertTrue(page.hasNext());
	assertNotNull(page.next());
	assertTrue(page.hasNext());
	assertNotNull(page.next());
	assertFalse(page.hasNext());
    }

    @Test
    public void invalid_number_of_iterations_when_iterating_over_an_empty_pages() {
	final IterablePage<Entity> page = new IterablePage<Entity>(new Page(new ArrayList<Entity>()));
	int count = 0;
	for (final Entity en : page) {
	    count++;
	}
	assertEquals(0, count);
    }

    @Test
    public void invalid_number_of_iterations_when_iterating_over_one_single_element_pages() {
	final IterablePage<Entity> page = new IterablePage<Entity>(new Page(oneElement));
	int count = 0;
	for (final Entity en : page) {
	    count++;
	}
	assertEquals(1, count);
    }

    @Test
    public void invalid_number_of_iterations_when_iterating_over_one_three_element_pages() {
	final IterablePage<Entity> page = new IterablePage<Entity>(new Page(threeElements));
	int count = 0;
	for (final Entity en : page) {
	    count++;
	}
	assertEquals(3, count);
    }

    @Test
    public void invalid_number_of_iterations_when_iterating_over_multiple_pages() {
	final IterablePage<Entity> page = new IterablePage<Entity>(new Page(threeElements, new Page(twoElements, new Page(threeElements))));
	int count = 0;
	for (final Entity en : page) {
	    count++;
	}
	assertEquals(8, count);
    }


    @Test(expected = UnsupportedOperationException.class)
    public void removing_of_elements_should_not_be_permitted() {
	final IterablePage<Entity> page = new IterablePage<Entity>(new Page(threeElements, new Page(twoElements, new Page(threeElements))));
	page.remove();
    }

    /////////////////////////////////////////////////
    //////////////// helper classes /////////////////
    /////////////////////////////////////////////////
    @KeyType(String.class)
    private static class Entity extends AbstractEntity<String> {
    }

    private static class Page implements IPage<Entity> {

	private final List<Entity> content = new ArrayList<>(25);
	private final IPage<Entity> next;

	public Page(final List<Entity> content, final IPage<Entity> nextPage) {
	    this.content.addAll(content);
	    this.next = nextPage;
	}

	public Page(final List<Entity> content) {
	    this(content, null);
	}

	@Override
	public int capacity() {
	    return content.size();
	}

	@Override
	public int no() {
	    throw new UnsupportedOperationException();
	}

	@Override
	public int numberOfPages() {
	    throw new UnsupportedOperationException();
	}

	@Override
	public List<Entity> data() {
	    return content;
	}

	@Override
	public boolean hasNext() {
	    return next != null;
	}

	@Override
	public boolean hasPrev() {
	    return false;
	}

	@Override
	public IPage<Entity> next() {
	    return next;
	}

	@Override
	public IPage<Entity> prev() {
	    throw new UnsupportedOperationException();
	}

	@Override
	public IPage<Entity> last() {
	    return next == null ? this : next.last();
	}

	@Override
	public IPage<Entity> first() {
	    throw new UnsupportedOperationException();
	}

	@Override
	public Entity summary() {
	    throw new UnsupportedOperationException();
	}

    }
}
