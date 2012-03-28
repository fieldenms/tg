package ua.com.fielden.snappy;

public class Result {
    public enum ResultState {
	FAILED, SUCCESSED, ERROR
    }

    private final ResultState resultState;

    public Result(final ResultState resultState) {
	this.resultState = resultState;
    }

    public ResultState state() {
	return resultState;
    }

}
