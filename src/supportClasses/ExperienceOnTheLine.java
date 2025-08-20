package supportClasses;

// Class that represents a worker's experience level on a specific line
public class ExperienceOnTheLine {

	private String LineId; // Identifier for the production line
	private int value; // Numeric value quantifying the experience on that line

	// Constructor to initialize the line ID and the experience value
	public ExperienceOnTheLine(String lineId, int value) {
		LineId = lineId;
		this.value = value;
	}

	// Getter for the line identifier
	public String getLineId() {
		return LineId;
	}

	// Getter for the experience value on this line
	public double getValue() {
		return value;
	}

	@Override
	// Method that returns a string representation of this object
	public String toString() {
		return "ExperienceOnTheLine [LineId=" + LineId + ", value=" + value + "]";
	}

}
