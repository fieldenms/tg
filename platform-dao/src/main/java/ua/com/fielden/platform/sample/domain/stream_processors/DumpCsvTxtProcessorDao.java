package ua.com.fielden.platform.sample.domain.stream_processors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

/** 
 * DAO implementation for companion object {@link IDumpCsvTxtProcessor}.
 * 
 * @author Developers
 *
 */
@EntityType(DumpCsvTxtProcessor.class)
public class DumpCsvTxtProcessorDao extends CommonEntityDao<DumpCsvTxtProcessor> implements IDumpCsvTxtProcessor {
    
    @Inject
    public DumpCsvTxtProcessorDao(final IFilter filter) {
        super(filter);
    }
    
    @Override
    public DumpCsvTxtProcessor save(final DumpCsvTxtProcessor entity) {
        
        if (entity.getInputStream() == null) {
            throw Result.failure("Input stream was not provided.");
        }
        
        // lets read the data from file line by line
        final BufferedReader br = new BufferedReader(new InputStreamReader(entity.getInputStream()));
        final List<String> data = new ArrayList<>();
        String line = null;
        try {
            while ((line = br.readLine()) != null) {
                data.add(line);
            }
        } catch (IOException e) {
            throw Result.failure(e);
        }

        // now let's emulate data processing and report progress update if a corresponding subject was provided
        int prc = -1;
        final double total = data.size();
        final Random rnd = new Random();
        for (int index = 0; index < data.size(); index++) {
            try {
                // let's pretend something is being computed by sleeping the thread
                final String entry = data.get(index);
                Thread.sleep(rnd.nextInt(2));
            } catch (InterruptedException e) {
            }
            
            if (entity.getEventSourceSubject().isPresent()) {
                // in practice there is no need to report every percent completed
                // instead a time based stepping function could have been used
                final double x = index;
                final int currPrc = (int) (x / total * 100.0);
                if (currPrc > prc || currPrc >= 100) {
                    prc = currPrc;
                    entity.getEventSourceSubject().get().publish(prc);
                }
            }
        }
        
        // make sure we report 100% completion
        if (entity.getEventSourceSubject().isPresent()) {
            entity.getEventSourceSubject().get().publish(100);
        }

        
        return entity.setNoOfProcessedLines(Integer.valueOf(data.size()));
    }

}