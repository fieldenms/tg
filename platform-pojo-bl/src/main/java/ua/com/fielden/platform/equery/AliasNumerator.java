package ua.com.fielden.platform.equery;

public class AliasNumerator {
    private int count = 0;

    public int getNextNumber() {
	count = count + 1;
	return count;
    }

}
