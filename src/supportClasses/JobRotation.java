package supportClasses;

//The JobRotation class holds information about how effectively a worker rotates jobs on a specific production line
public class JobRotation {

	private String LineId; // Identifier for the production line
	private double value; // Numeric value indicating the extent of job rotation on this line

	// Constructor to initialize the line identifier and job rotation value
	public JobRotation(String lineId, double value) {
		LineId = lineId;
		this.value = value;
	}

	// Returns the identifier for the production line
	public String getLineId() {
		return LineId;
	}

	// Returns the job rotation value for this line
	public double getValue() {
		return value;
	}

	// Provides a string representation of the JobRotation object
	@Override
	public String toString() {
		return "JobRotation [LineId=" + LineId + ", value=" + value + "]";
	}

}
