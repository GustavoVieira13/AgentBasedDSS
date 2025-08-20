package supportClasses;

// Class representing a worker's preference value for a specific line
public class WorkerPreference {

	private String LineId; // Identifier of the production line
	private double value; // Numeric preference score the worker assigns to this line

	// Constructor to initialize the line ID and the preference value
	public WorkerPreference(String lineId, double value) {
		this.LineId = lineId;
		this.value = value;
	}

	public String getLineId() {
		return LineId;
	}

	public double getValue() {
		return value;
	}

	// String representation of the worker’s preference for debugging/inspection
	@Override
	public String toString() {
		return "WorkerPreference [LineId=" + LineId + ", value=" + value + "]";
	}

}
