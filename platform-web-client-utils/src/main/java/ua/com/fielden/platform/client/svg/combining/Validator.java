package ua.com.fielden.platform.client.svg.combining;


import static java.nio.file.Files.notExists;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.File;

public class Validator {

    public void validate(final String file) {
        if (isEmpty(file) || notExists(new File(file).toPath())) {
            throw new IllegalArgumentException("Src or dest file does not exist!");
        }
    }

}
