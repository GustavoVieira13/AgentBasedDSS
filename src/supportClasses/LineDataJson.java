package supportClasses;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

//Class representing a line and its associated workers, used for JSON mapping
public class LineDataJson {
	@JsonProperty("lineId")
	private String lineId; // The unique identifier of the production line

	@JsonProperty("workersRequired")
	private int workersRequired; // The required number of workers for this line

	@JsonProperty("workers")
	private List<WorkerDataJson> workers; // A list of workers assigned or selected for this line

	// Getters
	public String getLineId() { // Getter for the line identifier
		return lineId;
	}

	public int getWorkersRequired() { // Getter for the required number of workers
		return workersRequired;
	}

	public List<WorkerDataJson> getWorkers() { // Getter for the list of workers
		return workers;
	}

}
