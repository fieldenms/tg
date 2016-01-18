package ua.com.fielden.platform.sample.domain.stream_processors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
        
        final BufferedReader br = new BufferedReader(new InputStreamReader(entity.getInputStream()));
        final StringBuilder sb = new StringBuilder();
        String line = null;
        int linesCount = 0;
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
                linesCount++;
            }
        } catch (IOException e) {
            throw Result.failure(e);
        }
        sb.append("\n\n");

        System.out.println(sb.toString());
        
        return entity.setNoOfProcessedLines(Integer.valueOf(linesCount));
    }

}