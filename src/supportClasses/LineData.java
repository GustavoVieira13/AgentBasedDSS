package supportClasses;

// The LineData class encapsulates information about a production line
public class LineData {

	private String lineId; // Unique identifier for the production line
	private String geometry; // Geometry of the line
	private boolean productionPriority; // Flag indicating if this line has production priority
	private int dueDate; // Due date associated with production on this line
	private int workersRequired; // Number of workers required on this line

	// Constructs a LineData instance with specified attributes
	public LineData(String lineId, String geometry, boolean productionPriority, int dueDate, int workersRequired) {
		this.lineId = lineId;
		this.geometry = geometry;
		this.productionPriority = productionPriority;
		this.dueDate = dueDate;
		this.workersRequired = workersRequired;
	}

	public String getLineId() {
		return lineId;
	}

	public String getGeometry() {
		return geometry;
	}

	public boolean isProductionPriority() {
		return productionPriority;
	}

	public int getDueDate() {
		return dueDate;
	}

	public int getWorkersRequired() {
		return workersRequired;
	}

	// Provides a string representation of the line's information
	@Override
	public String toString() {
		return "LineInfo [lineId=" + lineId + ", geometry=" + geometry + ", productionPriority=" + productionPriority
				+ ", dueDate=" + dueDate + ", workersRequired=" + workersRequired + "]";
	}

}
