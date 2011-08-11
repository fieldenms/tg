package ua.com.fielden.platform.csv;


public interface IParserCreator<T extends IParser> {
    T createParser(final String line) throws Exception;
}
