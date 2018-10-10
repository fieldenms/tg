package ua.com.fielden.platform.dao.concurrency;

import static java.lang.String.format;
import static org.junit.Assert.assertNotEquals;

import org.hibernate.Session;
import org.junit.Test;

import ua.com.fielden.platform.dao.EntityWithMoneyDao;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.Pair;

public class ConcurrentAccessToCompanionObjectsTest extends AbstractDaoTestCase {

    private volatile Pair<Session, Session> res = null;
    
    @Test
    public void concurrently_accessed_DAO_corrupt_sessions() throws Exception {
        final EntityWithMoneyDao co = co$(EntityWithMoney.class);
        
        
        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    res = co.getSessionWithDelay(200);
                    System.out.println(format("first started with [%s] -- ended with [%s]", System.identityHashCode(res.getKey()), System.identityHashCode(res.getValue())));
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
            }
            
        };
        
        final Runnable runnable1 = new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(50);
                    final Pair<Session, Session> p = co.getSessionWithDelay(1);
                    System.out.println(format("second started with [%s] -- ended with [%s]", System.identityHashCode(p.getKey()), System.identityHashCode(p.getValue())));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
            }
            
        };

        
        final Thread thread1 = new Thread(runnable);
        final Thread thread2 = new Thread(runnable1);
        
        thread1.start();
        thread2.start();
        
        thread1.join();
        thread2.join();
        
        assertNotEquals(System.identityHashCode(res.getKey()), System.identityHashCode(res.getValue()));
    }
}