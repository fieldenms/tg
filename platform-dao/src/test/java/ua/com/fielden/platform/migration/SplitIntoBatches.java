package ua.com.fielden.platform.migration;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import junit.framework.TestCase;

import org.hibernate.SessionFactory;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.migration.DataMigrator;
import ua.com.fielden.platform.migration.IRetriever;
import ua.com.fielden.platform.migration.MigrationRun;
import ua.com.fielden.platform.migration.dao.MigrationErrorDao;
import ua.com.fielden.platform.migration.dao.MigrationHistoryDao;
import ua.com.fielden.platform.utils.Pair;


public class SplitIntoBatches extends TestCase{

    public void testThatSplitIntoBatchesWorks() {
	final List<Pair<String, Long>> groupedItems = new ArrayList<Pair<String, Long>>();
	groupedItems.add(new Pair<String, Long>("A01", 25l));
	groupedItems.add(new Pair<String, Long>("A02", 17l));
	groupedItems.add(new Pair<String, Long>("A03", 15l));
	groupedItems.add(new Pair<String, Long>("A04", 13l));
	groupedItems.add(new Pair<String, Long>("A05", 13l));
	groupedItems.add(new Pair<String, Long>("A06", 12l));
	groupedItems.add(new Pair<String, Long>("A07", 10l));
	groupedItems.add(new Pair<String, Long>("A08", 8l));
	groupedItems.add(new Pair<String, Long>("A09", 6l));
	groupedItems.add(new Pair<String, Long>("A10", 6l));
	groupedItems.add(new Pair<String, Long>("A11", 4l));
	groupedItems.add(new Pair<String, Long>("A12", 1l));
	groupedItems.add(new Pair<String, Long>("A13", 1l));
	groupedItems.add(new Pair<String, Long>("A14", 1l));

	final PriorityQueue<Pair<Set<String>, Long>> pq = DataMigrator.splitIntoBatches(groupedItems, 4);
	final List<Pair<Set<String>, Long>> expected = new ArrayList<Pair<Set<String>, Long>>();
	expected.add(new Pair<Set<String>, Long>(new HashSet<String>(Arrays.asList(new String[]{"A09", "A13", "A05", "A04"})), 33l));
	expected.add(new Pair<Set<String>, Long>(new HashSet<String>(Arrays.asList(new String[]{"A07", "A02", "A10"})), 33l));
	expected.add(new Pair<Set<String>, Long>(new HashSet<String>(Arrays.asList(new String[]{"A08", "A01"})), 33l));
	expected.add(new Pair<Set<String>, Long>(new HashSet<String>(Arrays.asList(new String[]{"A11", "A12", "A06", "A14", "A03"})), 33l));

	assertTrue("Contains different batches of items then expected", pq.containsAll(expected));
    }

    public void testThatSplitSqlCompositionForRetieverWithOrderedSelectWorks() {
	System.out.println(DataMigrator.getSplitSql(new IRetriever() {

	    @Override
	    public String selectSql() {
		return "SELECT aa first_property_, bb second_property_ FROM CC ORDER BY 1 DESC";
	    }

	    @Override
	    public Result populateData(final SessionFactory sessionFactory, final Connection conn, final EntityFactory factory, final MigrationErrorDao errorDao, final MigrationHistoryDao histDao, final MigrationRun migrationRun, final String subset)
		    throws Exception {
		return null;
	    }

	    @Override
	    public Class type() {
		return null;
	    }

	    @Override
	    public String splitProperty() {
		return "firstProperty";
	    }

	}));
    }

    public void testThatSplitSqlCompositionForRetieverWithUnorderedSelectWorks() {
	System.out.println(DataMigrator.getSplitSql(new IRetriever() {

	    @Override
	    public String selectSql() {
		// TODO Auto-generated method stub
		return "SELECT aa first_property_, bb second_property_ FROM CC";
	    }

	    @Override
	    public Result populateData(final SessionFactory sessionFactory, final Connection conn, final EntityFactory factory, final MigrationErrorDao errorDao, final MigrationHistoryDao histDao, final MigrationRun migrationRun, final String subset)
		    throws Exception {
		return null;
	    }

	    @Override
	    public Class type() {
		return null;
	    }

	    @Override
	    public String splitProperty() {
		return "firstProperty";
	    }

	}));
    }

}
