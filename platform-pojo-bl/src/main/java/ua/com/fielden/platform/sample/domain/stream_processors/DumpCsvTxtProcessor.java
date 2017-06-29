package ua.com.fielden.platform.sample.domain.stream_processors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;

import ua.com.fielden.platform.cypher.HexString;
import ua.com.fielden.platform.entity.AbstractEntityWithInputStream;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.error.Result;

/** 
 * An demo entity that both provides an input stream for processing and represent the result of that processing as implemented in its companion's method <code>save</code>.
 * <p>
 * An important part of this entity is that it extends {@link AbstractEntityWithInputStream}.
 * 
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(IDumpCsvTxtProcessor.class)
public class DumpCsvTxtProcessor extends AbstractEntityWithInputStream<String> {

    @IsProperty
    @MapTo
    @Title(value = "Title", desc = "Desc")
    private Integer noOfProcessedLines;

    @Observable
    public DumpCsvTxtProcessor setNoOfProcessedLines(final Integer noOfProcessedLines) {
        this.noOfProcessedLines = noOfProcessedLines;
        return this;
    }

    public Integer getNoOfProcessedLines() {
        return noOfProcessedLines;
    }
    
    public static void main(String[] args) throws Exception {
        final MessageDigest md = MessageDigest.getInstance("SHA1");
        
        try (final InputStream is = Files.newInputStream(Paths.get("../pom.xml"));
             final DigestInputStream dis = new DigestInputStream(is, md); // decorator
             final BufferedReader br = new BufferedReader(new InputStreamReader(dis));) 
        {
          /* Read decorated stream (dis) to EOF as normal... */
            String line = null;
            try {
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                throw Result.failure(e);
            }
        }
        byte[] digest = md.digest();
        final String hash = HexString.bufferToHex(digest, 0, digest.length);
        System.out.println(hash);
        //AD35A51B8C8658E0ACB1DFCF5A11923BE8B05DD4
        //95B3FF9137053279C8B1ECE96F817BA0129F614C
    }
}
