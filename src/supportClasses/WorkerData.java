package supportClasses;

import java.util.List;

//Class containing detailed information about the worker
public class WorkerData {

	private String workerId; // Unique identifier for the worker
	private boolean availability; // Indicates if the worker is currently available
	private boolean medicalCondition; // Reflects whether the worker’s medical condition allows them to work
	private boolean uteExperience; // Whether the worker has experience with UTE
	private double workerResilience; // A numeric measure of the worker’s resilience
	private String gender; // The worker’s gender
	private int age; // The worker’s age
	private List<WorkerPreference> workerPreference; // A list representing the worker’s preferences for different lines
	private List<JobRotation> jobRotation; // A list that shows how often the worker rotates jobs on each line
	private List<ExperienceOnTheLine> experienceOnTheLine; // A list capturing the worker’s experience level on each line

	// Constructor for basic worker attributes, including a list of preferences
	public WorkerData(String workerId, boolean availability, boolean medicalCondition, boolean uteExperience,
			double workerResilience, List<WorkerPreference> workerPreference) {
		this.workerId = workerId;
		this.availability = availability;
		this.medicalCondition = medicalCondition;
		this.uteExperience = uteExperience;
		this.workerResilience = workerResilience;
		this.workerPreference = workerPreference;
	}

	// Overloaded constructor with extended attributes such as gender, age, job rotation, and experience on the line
	public WorkerData(String workerId, boolean availability, boolean medicalCondition, boolean uteExperience,
			double workerResilience, String gender, int age, List<WorkerPreference> workerPreference,
			List<JobRotation> jobRotation, List<ExperienceOnTheLine> experienceOnTheLine) {
		this.workerId = workerId;
		this.availability = availability;
		this.medicalCondition = medicalCondition;
		this.uteExperience = uteExperience;
		this.workerResilience = workerResilience;
		this.gender = gender;
		this.age = age;
		this.workerPreference = workerPreference;
		this.jobRotation = jobRotation;
		this.experienceOnTheLine = experienceOnTheLine;
	}

	public String getWorkerId() {
		return workerId;
	}

	public boolean isAvailability() {
		return availability;
	}

	public boolean isMedicalCondition() {
		return medicalCondition;
	}

	public boolean isUteExperience() {
		return uteExperience;
	}

	public double getWorkerResilience() {
		return workerResilience;
	}
	
	public String getGender() {
		return gender;
	}

	public int getAge() {
		return age;
	}

	// Getter for the worker’s preference list
	public List<WorkerPreference> getWorkerPreference() {
		return workerPreference;
	}

	// Getter for the worker’s job rotation list
	public List<JobRotation> getJobRotation() {
		return jobRotation;
	}

	// Getter for the worker’s line experience list
	public List<ExperienceOnTheLine> getExperienceOnTheLine() {
		return experienceOnTheLine;
	}

	// Provides a string representation of the worker data
	@Override
	public String toString() {
		return "WorkerData [workerId=" + workerId + ", availability=" + availability + ", medicalCondition="
				+ medicalCondition + ", uteExperience=" + uteExperience + ", workerResilience=" + workerResilience
				+ ", gender=" + gender + ", age=" + age + ", workerPreference=" + workerPreference + ", jobRotation="
				+ jobRotation + ", experienceOnTheLine=" + experienceOnTheLine + "]";
	}



}
