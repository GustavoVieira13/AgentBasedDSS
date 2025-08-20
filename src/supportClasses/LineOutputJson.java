package supportClasses;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "LineId", "WorkersRequired", "Workers" }) // Specifies the order of fields when serialized to JSON
public class LineOutputJson {
	private String LineId; // Identifier of the production line
	private int WorkersRequired; // Number of workers required by the line
	List<SelectedWorkers> Workers; // List of workers selected for this line

	// Constructor to initialize line details and selected workers
	public LineOutputJson(String lineId, int workersRequired, List<SelectedWorkers> workers) {
		this.LineId = lineId;
		this.WorkersRequired = workersRequired;
		this.Workers = workers;
	}

	// Getter for the line identifier
	public String getLineId() {
		return LineId;
	}

	// Getter for the required number of workers
	public int getWorkersRequired() {
		return WorkersRequired;
	}

	// Getter for the list of selected workers
	public List<SelectedWorkers> getWorkers() {
		return Workers;
	}

}
