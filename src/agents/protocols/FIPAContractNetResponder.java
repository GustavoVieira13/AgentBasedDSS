package agents.protocols;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import supportClasses.WorkerData;

//Class extending JADE's ContractNetResponder
public class FIPAContractNetResponder extends ContractNetResponder {

	private static final long serialVersionUID = 1L;
	private WorkerData workerData;
	
	private double workerPreferenceLine; // Worker preference for the specific line
	private double jobRotation; // Stores the job rotation factor for the worker on a specific line
	private int experienceOnTheLine; // Stores the worker’s prior experience on the line
	private double price; // Score determined by the worker's resilience and preference

	// Constructor chaining to the ContractNetResponder, passing in the agent and template
	public FIPAContractNetResponder(Agent a, MessageTemplate mt, WorkerData workerData) {
		super(a, mt);
		this.workerData = workerData; // Assign the worker data for use in evaluations
	}

	// Called when a CFP message is received
	protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
//		System.out.println("Agent " + myAgent.getLocalName() + ": CFP received from " + cfp.getSender().getName()
//				+ ". Action is " + cfp.getContent());

		// Evaluate the CFP content against a specific criterion
		if (criterion_1(cfp)) {
			// Convert the worker’s proposal data to JSON
			String proposal = structureJSONRespose();
//			System.out.println("Agent " + myAgent.getLocalName() + ": Proposing " + proposal);
			ACLMessage propose = cfp.createReply();
			propose.setPerformative(ACLMessage.PROPOSE);
			propose.setContent(proposal);
			return propose;
		} else {
			// Throw a RefuseException if the worker does not meet the criteria
			throw new RefuseException("evaluation-failed");
		}

	}

	@Override
	// Called if the proposal is accepted by the initiator
	protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept)
			throws FailureException {
//		System.out.println("Agent " + myAgent.getLocalName() + ": Proposal accepted");
		ACLMessage inform = accept.createReply(); // reply with an inform that the proposal was accepted
		// Creates an INFORM reply to confirm acceptance
		inform.setPerformative(ACLMessage.INFORM);
//		((WorkerAgent) myAgent).deregister(myAgent);

		return inform;
	}

	// Called if the proposal is rejected by the initiator
	protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
//		System.out.println("Agent " + myAgent.getLocalName() + ": Proposal rejected");
	}

	// Specific criteria for evaluating if the worker can propose
	private boolean criterion_1(ACLMessage cfp) {
		// Check worker's basic conditions: availability and medical condition
		if (workerData.isAvailability() && workerData.isMedicalCondition()) {
			for (int i = 0; i < workerData.getWorkerPreference().size(); i++) {
				// Match the line ID in the CFP with the worker's preference array
				if (workerData.getWorkerPreference().get(i).getLineId().equals(cfp.getContent())) {
					// Additional checks on worker’s UTE experience or resilience
					if (workerData.isUteExperience() || workerData.getWorkerResilience() > 0.5) {
						workerPreferenceLine = workerData.getWorkerPreference().get(i).getValue();
						// Calculate a score using the worker’s resilience and preference
						price = 0.65 * workerData.getWorkerResilience() + 0.35 * workerPreferenceLine;
						// Retrieve job rotation and line experience metrics
						jobRotation = workerData.getJobRotation().get(i).getValue();
						experienceOnTheLine = (int) workerData.getExperienceOnTheLine().get(i).getValue();
						
//						structureJSONRespose();
						return true;
					} else {
						return false;
					}
				}
			}

		} else {
			return false;
		}
		return false;
	}

//	Just for test
	private boolean criterion_test(ACLMessage cfp) {

		for (int i = 0; i < workerData.getWorkerPreference().size(); i++) {
			if (workerData.getWorkerPreference().get(i).getLineId().equals(cfp.getContent())) {
				workerPreferenceLine = workerData.getWorkerPreference().get(i).getValue();
				price = 0.65 * workerData.getWorkerResilience() + 0.35 * workerPreferenceLine;
//				
				jobRotation = workerData.getJobRotation().get(i).getValue();
				experienceOnTheLine = (int) workerData.getExperienceOnTheLine().get(i).getValue();
//				
//				structureJSONRespose();
				return true;
			}
		}
		return false;
	}

	// Builds a JSON string containing the worker's data and proposal
	private String structureJSONRespose() {
		String jsonString = null;
		try {
			Map<String, Object> workerDataMap = new HashMap<>();
			workerDataMap.put("Id", workerData.getWorkerId());
			workerDataMap.put("Availability", workerData.isAvailability());
			workerDataMap.put("MedicalCondition", workerData.isMedicalCondition());
			workerDataMap.put("UTEExperience", workerData.isUteExperience());
			workerDataMap.put("WorkerResilience", workerData.getWorkerResilience());
			workerDataMap.put("Gender", workerData.getGender());
			workerDataMap.put("Age", workerData.getAge());
			workerDataMap.put("WorkerPreference", workerPreferenceLine);
			workerDataMap.put("JobRotation", jobRotation);
			workerDataMap.put("ExperienceOnTheLine", experienceOnTheLine);

			Map<String, Object> proposalMap = new HashMap<>();
			proposalMap.put("Value", price); // Store the calculated “price”/score in a separate proposal field

			Map<String, Object> finalMap = new HashMap<>();
			finalMap.put("WorkerInfo", workerDataMap);
			finalMap.put("Proposal", proposalMap);

			ObjectMapper objectMapper = new ObjectMapper();
			jsonString = objectMapper.writeValueAsString(finalMap); // Convert the combined map to a JSON string

//			System.out.println(jsonString);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonString;
	}

}
