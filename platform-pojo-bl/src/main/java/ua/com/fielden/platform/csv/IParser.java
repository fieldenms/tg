package ua.com.fielden.platform.csv;

import java.util.List;

import org.antlr.runtime.RecognitionException;

public interface IParser {
    List<String> line() throws RecognitionException;
}
