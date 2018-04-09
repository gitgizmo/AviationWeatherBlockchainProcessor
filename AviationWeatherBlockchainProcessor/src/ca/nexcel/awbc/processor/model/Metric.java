package ca.nexcel.awbc.processor.model;

/**
 * A class that contains publication metrics
 * 
 * @author George Franciscus
 *
 */
public class Metric {
	
	private int successCount = 0;
	private int failureCount = 0;
	private int attemptCount = 0;
	
	public Metric(int successCount, int failureCount, int attemptCount) {
		this.successCount = successCount;
		this.failureCount = failureCount;
		this.attemptCount = attemptCount;
	}

	public void addToSuccessCount(int count) {
		this.successCount += count;
	}
	
	public void addToFailureCount(int count) {
		this.failureCount += count;
	}
	
	public void addToAttemptCount(int count) {
		this.attemptCount += count;
	}

	public int getSuccessCount() {
		return successCount;
	}

	public int getFailureCount() {
		return failureCount;
	}


	public int getAttemptCount() {
		return attemptCount;
	}
}
