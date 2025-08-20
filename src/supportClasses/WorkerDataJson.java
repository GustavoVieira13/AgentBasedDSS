package supportClasses;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WorkerDataJson {
	@JsonProperty("gender")
	private String gender;

	@JsonProperty("age")
	private int age;

	@JsonProperty("jobRotation")
	private double jobRotation;

	@JsonProperty("experienceOnTheLine")
	private int experienceOnTheLine;

	@JsonProperty("workerResilience")
	private double workerResilience;

	@JsonProperty("workerPreference")
	private double workerPreference;

	@JsonProperty("workerId")
	private int workerId;

	@JsonProperty("availability")
	private boolean availability;

	@JsonProperty("medicalCondition")
	private boolean medicalCondition;

	@JsonProperty("uteexperience")
	private boolean uteexperience;
	
	@JsonProperty("proposalValue")
	private double proposalValue;

	public double getProposalValue() {
		return proposalValue;
	}

	// Getters
	public String getGender() {
		return gender;
	}

	public int getAge() {
		return age;
	}

	public double getJobRotation() {
		return jobRotation;
	}

	public int getExperienceOnTheLine() {
		return experienceOnTheLine;
	}

	public double getWorkerResilience() {
		return workerResilience;
	}

	public double getWorkerPreference() {
		return workerPreference;
	}

	public int getWorkerId() {
		return workerId;
	}

	public boolean isAvailability() {
		return availability;
	}

	public boolean isMedicalCondition() {
		return medicalCondition;
	}

	public boolean isUteexperience() {
		return uteexperience;
	}

}