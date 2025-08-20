package supportClasses;

public class SelectedWorkers {

	private int WorkerId;
	private boolean Availability;
	private boolean MedicalCondition;
	private boolean UTEExperience;
	private double WorkerResilience;
	private double WorkerPreference;
	private String gender;
	private int age;
	private double jobRotation;
	private int experienceOnTheLine;
	private double ProposalValue;

	public SelectedWorkers(int WorkerId, boolean Availability, boolean MedicalCondition, boolean UTEExperience,
			double WorkerResilience, double WorkerPreference) {

		this.WorkerId = WorkerId;
		this.Availability = Availability;
		this.MedicalCondition = MedicalCondition;
		this.UTEExperience = UTEExperience;
		this.WorkerResilience = WorkerResilience;
		this.WorkerPreference = WorkerPreference;
	}

	public SelectedWorkers(int workerId, boolean availability, boolean medicalCondition, boolean uTEExperience,
			double workerResilience, double workerPreference, String gender, int age, double jobRotation,
			int experienceOnTheLine) {
		this.WorkerId = workerId;
		this.Availability = availability;
		this.MedicalCondition = medicalCondition;
		this.UTEExperience = uTEExperience;
		this.WorkerResilience = workerResilience;
		this.WorkerPreference = workerPreference;
		this.gender = gender;
		this.age = age;
		this.jobRotation = jobRotation;
		this.experienceOnTheLine = experienceOnTheLine;
	}

	public SelectedWorkers(int workerId, boolean availability, boolean medicalCondition, boolean uTEExperience,
			double workerResilience, double workerPreference, String gender, int age, double jobRotation,
			int experienceOnTheLine, double proposalValue) {
		this.WorkerId = workerId;
		this.Availability = availability;
		this.MedicalCondition = medicalCondition;
		this.UTEExperience = uTEExperience;
		this.WorkerResilience = workerResilience;
		this.WorkerPreference = workerPreference;
		this.gender = gender;
		this.age = age;
		this.jobRotation = jobRotation;
		this.experienceOnTheLine = experienceOnTheLine;
		this.ProposalValue = proposalValue;
	}

	public String getGender() {
		return gender;
	}

	public int getAge() {
		return age;
	}

	public int getWorkerId() {
		return WorkerId;
	}

	public double getJobRotation() {
		return jobRotation;
	}

	public int getExperienceOnTheLine() {
		return experienceOnTheLine;
	}

	public boolean isAvailability() {
		return Availability;
	}

	public boolean isMedicalCondition() {
		return MedicalCondition;
	}

	public boolean isUTEExperience() {
		return UTEExperience;
	}

	public double getWorkerResilience() {
		return WorkerResilience;
	}

	public double getWorkerPreference() {
		return WorkerPreference;
	}

	@Override
	public String toString() {
		return "SelectedWorkers [WorkerId=" + WorkerId + ", Availability=" + Availability + ", MedicalCondition="
				+ MedicalCondition + ", UTEExperience=" + UTEExperience + ", WorkerResilience=" + WorkerResilience
				+ ", WorkerPreference=" + WorkerPreference + ", gender=" + gender + ", age=" + age + ", jobRotation="
				+ jobRotation + ", experienceOnTheLine=" + experienceOnTheLine + "]";
	}

	public double getProposalValue() {
		return ProposalValue;
	}

}