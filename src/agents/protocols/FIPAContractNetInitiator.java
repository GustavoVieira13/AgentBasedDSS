package agents.protocols;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import agents.LineAgent;
import entities.NegotiationCriteria;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import supportClasses.LineData;
import supportClasses.LineOutputJson;
import supportClasses.SelectedWorkers;

public class FIPAContractNetInitiator extends ContractNetInitiator {

	private static final long serialVersionUID = 1L;
	private int nResponders;
	private LineData lineData;
	private NegotiationCriteria criteria;
	private static List<SelectedWorkers> selectedWorkers = new ArrayList<>();
	private ArrayList<ACLMessage> proposals = new ArrayList<>();

	public FIPAContractNetInitiator(Agent a, ACLMessage cfp, NegotiationCriteria criteria, LineData lineData) {
		super(a, cfp);
		this.lineData = lineData;
		this.criteria = criteria;
	}

	@SuppressWarnings("rawtypes")
	protected void handlePropose(ACLMessage propose, Vector v) {
//		System.out.println("Agent " + propose.getSender().getName() + " proposed " + propose.getContent());
	}

	protected void handleRefuse(ACLMessage refuse) {
//		System.out.println("Agent " + refuse.getSender().getName() + " refused");
	}

	protected void handleFailure(ACLMessage failure) {
		if (failure.getSender().equals(myAgent.getAMS())) {
			// Failure notification from the JADE runtime: the receiver does not exist
//			System.out.println("Responder does not exist");
		} else {
//			System.out.println("Agent " + failure.getSender().getName() + " failed");
		}
		// Immediate failure --> we will not receive a response from this agent
		nResponders--;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void handleAllResponses(Vector responses, Vector acceptances) {
		if (responses.size() < nResponders) {
			// Some responder didn't reply within the specified timeout
//			System.out.println("Timeout expired: missing " + (nResponders - responses.size()) + " responses");
		}

//		store proposals in a list
		for (Object response : responses) {
			ACLMessage resp = (ACLMessage) response;
			if (resp.getPerformative() == ACLMessage.PROPOSE) {
				proposals.add(resp);
			}
		}
		selectedWorkers = new ArrayList<>();

		ObjectMapper objectMapper = new ObjectMapper();

//		sort
		Collections.sort(proposals, (ACLMessage a, ACLMessage b) -> {
			JsonNode rootNode_a = null;
			JsonNode rootNode_b = null;
			try {
				rootNode_a = objectMapper.readTree(a.getContent());
				rootNode_b = objectMapper.readTree(b.getContent());

			} catch (IOException e) {
				e.printStackTrace();
			}

			double valueA = rootNode_a.get("Proposal").get("Value").asDouble();
			double valueB = rootNode_b.get("Proposal").get("Value").asDouble();
//			return Double.compare(valueB, valueA);

			int valueComparison = Double.compare(valueB, valueA);

			if (valueComparison == 0) {
				valueA = rootNode_a.get("WorkerInfo").get("JobRotation").asDouble();
				valueB = rootNode_b.get("WorkerInfo").get("JobRotation").asDouble();

				int valueComparison2 = Double.compare(valueA, valueB);

				if (valueComparison2 == 0) {
					valueA = rootNode_a.get("WorkerInfo").get("ExperienceOnTheLine").asDouble();
					valueB = rootNode_b.get("WorkerInfo").get("ExperienceOnTheLine").asDouble();
					return valueComparison2 = Double.compare(valueA, valueB);
				}

				return valueComparison2;
			}

			return valueComparison;

		});

//		System.out.println(proposals.get(0).getContent());

		generateRankingFile();

// 		send accept proposals
		if (criteria.equals(NegotiationCriteria.NO_CRITERIA)) {
			System.out.println("-----------------NO_CRITERIA-----------------");
			if (proposals.size() >= lineData.getWorkersRequired()) {
				System.out.println("Proposals received: " + proposals.size());

				for (int i = 0; i < lineData.getWorkersRequired(); i++) {

					try {
						JsonNode rootNode = objectMapper.readTree(proposals.get(i).getContent());
						ACLMessage accept = proposals.get(i).createReply();
						System.out.println("Accepting proposal (wait watchdog confirmation): "
								+ proposals.get(i).getSender().getLocalName() + " that scored "
								+ rootNode.get("Proposal").get("Value").asDouble());
						accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);

						int workerId = rootNode.get("WorkerInfo").get("Id").asInt();
						boolean availability = rootNode.get("WorkerInfo").get("Availability").asBoolean();
						boolean medicalCondition = rootNode.get("WorkerInfo").get("MedicalCondition").asBoolean();
						boolean uteExperience = rootNode.get("WorkerInfo").get("UTEExperience").asBoolean();
						double workerResilience = rootNode.get("WorkerInfo").get("WorkerResilience").asDouble();
						double workerPreference = rootNode.get("WorkerInfo").get("WorkerPreference").asDouble();
						String gender = rootNode.get("WorkerInfo").get("Gender").asText();
						int age = rootNode.get("WorkerInfo").get("Age").asInt();
						double jobRotation = rootNode.get("WorkerInfo").get("JobRotation").asDouble();
						int experienceOnTheLine = rootNode.get("WorkerInfo").get("ExperienceOnTheLine").asInt();
						double proposalValue = rootNode.get("Proposal").get("Value").asDouble();

						selectedWorkers.add(new SelectedWorkers(workerId, availability, medicalCondition, uteExperience,
								workerResilience, workerPreference, gender, age, jobRotation, experienceOnTheLine,
								proposalValue));
						acceptances.addElement(accept);
					} catch (JsonMappingException e) {
						e.printStackTrace();
					} catch (JsonProcessingException e) {
						e.printStackTrace();
					}
				}
			}

//			send reject proposal
			for (int i = lineData.getWorkersRequired(); i < proposals.size(); i++) {
				ACLMessage reject = proposals.get(i).createReply();
				reject.setPerformative(ACLMessage.REJECT_PROPOSAL);
				acceptances.addElement(reject);
			}

//			structure json with results
			LineOutputJson lineOutputJson = new LineOutputJson(lineData.getLineId(), lineData.getWorkersRequired(),
					selectedWorkers);
			ObjectMapper mapper = new ObjectMapper();
			String json = null;
			try {
				json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(lineOutputJson);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}

			generateResultFile(json);

//			send results to the watchdog agent
			if (((LineAgent) myAgent).isAux() == true) {
				((LineAgent) myAgent).setAux(false);
//				((LineAgent) myAgent).sendMessage(ACLMessage.INFORM, "watchdogAgent", "ResponderProtocolWatchdogAgent",
//						json);
			}

			((LineAgent) myAgent).sendMessage(ACLMessage.INFORM, "watchdogAgent", "ResponderProtocolWatchdogAgent",
					json);

		} else if (criteria.equals(NegotiationCriteria.AGE)) {
			System.out.println("-----------------AGE-----------------");
			if (proposals.size() >= lineData.getWorkersRequired()) {
				System.out.println("Proposals received: " + proposals.size());

				int n = lineData.getWorkersRequired();
				double maxAverageAge = 35.0;
				double[] maxTotalProposalValue = { 0.0 };
				ArrayList<SelectedWorkers> workerList = new ArrayList<>();
				ArrayList<SelectedWorkers> bestSolution = new ArrayList<>();
				ArrayList<SelectedWorkers> currentSelection = new ArrayList<>();

				for (int i = 0; i < proposals.size(); i++) {
					JsonNode rootNode;
					try {
						rootNode = objectMapper.readTree(proposals.get(i).getContent());
						int workerId = rootNode.get("WorkerInfo").get("Id").asInt();
						boolean availability = rootNode.get("WorkerInfo").get("Availability").asBoolean();
						boolean medicalCondition = rootNode.get("WorkerInfo").get("MedicalCondition").asBoolean();
						boolean uteExperience = rootNode.get("WorkerInfo").get("UTEExperience").asBoolean();
						double workerResilience = rootNode.get("WorkerInfo").get("WorkerResilience").asDouble();
						double workerPreference = rootNode.get("WorkerInfo").get("WorkerPreference").asDouble();
						String gender = rootNode.get("WorkerInfo").get("Gender").asText();
						double jobRotation = rootNode.get("WorkerInfo").get("JobRotation").asDouble();
						int experienceOnTheLine = rootNode.get("WorkerInfo").get("ExperienceOnTheLine").asInt();
						int age = rootNode.get("WorkerInfo").get("Age").asInt();
						double proposalValue = rootNode.get("Proposal").get("Value").asDouble();

						workerList.add(new SelectedWorkers(workerId, availability, medicalCondition, uteExperience,
								workerResilience, workerPreference, gender, age, jobRotation, experienceOnTheLine,
								proposalValue));
					} catch (JsonProcessingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				findBestSelectionAge(workerList, n, maxAverageAge, 0, currentSelection, bestSolution,
						maxTotalProposalValue, n / 2);

				for (int i = 0; i < proposals.size(); i++) {
					JsonNode rootNode;
					try {
						rootNode = objectMapper.readTree(proposals.get(i).getContent());
						int workerId = rootNode.get("WorkerInfo").get("Id").asInt();
						for (int j = 0; j < bestSolution.size(); j++) {
							if (workerId == (bestSolution.get(j).getWorkerId())) {
								ACLMessage accept = proposals.get(i).createReply();
								System.out.println("Accepting proposal (wait watchdog confirmation): "
										+ proposals.get(i).getSender().getLocalName() + " that scored "
										+ rootNode.get("Proposal").get("Value").asDouble());
//								proposals.remove(i);
								accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
								acceptances.addElement(accept);
							} else {
								ACLMessage reject = proposals.get(i).createReply();
								reject.setPerformative(ACLMessage.REJECT_PROPOSAL);
								acceptances.addElement(reject);
							}
						}

					} catch (JsonProcessingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				selectedWorkers = bestSolution;

//				structure json with results
				LineOutputJson lineOutputJson = new LineOutputJson(lineData.getLineId(), lineData.getWorkersRequired(),
						selectedWorkers);
				ObjectMapper mapper = new ObjectMapper();
				String json = null;
				try {
					json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(lineOutputJson);
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}

				generateResultFile(json);

//					send results to the watchdog agent
				if (((LineAgent) myAgent).isAux() == true) {
					((LineAgent) myAgent).setAux(false);
//						((LineAgent) myAgent).sendMessage(ACLMessage.INFORM, "watchdogAgent", "ResponderProtocolWatchdogAgent",
//								json);
				}

				((LineAgent) myAgent).sendMessage(ACLMessage.INFORM, "watchdogAgent", "ResponderProtocolWatchdogAgent",
						json);

			}

		} else if (criteria.equals(NegotiationCriteria.GENDER)) {
			System.out.println("-----------------GENDER-----------------");
			if (proposals.size() >= lineData.getWorkersRequired()) {
				int n = lineData.getWorkersRequired();
				ArrayList<SelectedWorkers> workerList = new ArrayList<>();

				for (int i = 0; i < proposals.size(); i++) {
					JsonNode rootNode;
					try {
						rootNode = objectMapper.readTree(proposals.get(i).getContent());
						int workerId = rootNode.get("WorkerInfo").get("Id").asInt();
						boolean availability = rootNode.get("WorkerInfo").get("Availability").asBoolean();
						boolean medicalCondition = rootNode.get("WorkerInfo").get("MedicalCondition").asBoolean();
						boolean uteExperience = rootNode.get("WorkerInfo").get("UTEExperience").asBoolean();
						double workerResilience = rootNode.get("WorkerInfo").get("WorkerResilience").asDouble();
						double workerPreference = rootNode.get("WorkerInfo").get("WorkerPreference").asDouble();
						String gender = rootNode.get("WorkerInfo").get("Gender").asText();
						double jobRotation = rootNode.get("WorkerInfo").get("JobRotation").asDouble();
						int experienceOnTheLine = rootNode.get("WorkerInfo").get("ExperienceOnTheLine").asInt();
						int age = rootNode.get("WorkerInfo").get("Age").asInt();
						double proposalValue = rootNode.get("Proposal").get("Value").asDouble();

						workerList.add(new SelectedWorkers(workerId, availability, medicalCondition, uteExperience,
								workerResilience, workerPreference, gender, age, jobRotation, experienceOnTheLine,
								proposalValue));
					} catch (JsonProcessingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				ArrayList<SelectedWorkers> males = new ArrayList<>();
				ArrayList<SelectedWorkers> females = new ArrayList<>();

				for (SelectedWorkers worker : workerList) {
					if (worker.getGender().equals("Male")) {
						males.add(worker);
					} else {
						females.add(worker);
					}
				}

//	        males.sort((a, b) -> Double.compare(b.getProposalValue(), a.getProposalValue()));
//	        females.sort((a, b) -> Double.compare(b.getProposalValue(), a.getProposalValue()));

				ArrayList<SelectedWorkers> selectedWorkers = new ArrayList<>();

				int numFemales = n / 2;
				int numMales = n - numFemales;

				for (int i = 0; i < numFemales; i++) {
					selectedWorkers.add(females.get(i));
				}

				for (int i = 0; i < numMales; i++) {
					selectedWorkers.add(males.get(i));
				}

//				System.out.println("Selected Workers:");
//				for (SelectedWorkers worker : selectedWorkers) {
//					System.out.println(worker.toString());
//				}
				for (int i = 0; i < proposals.size(); i++) {
					JsonNode rootNode;
					try {
						rootNode = objectMapper.readTree(proposals.get(i).getContent());
						int workerId = rootNode.get("WorkerInfo").get("Id").asInt();
						for (int j = 0; j < selectedWorkers.size(); j++) {
							if (workerId == (selectedWorkers.get(j).getWorkerId())) {
								ACLMessage accept = proposals.get(i).createReply();
								System.out.println("Accepting proposal (wait watchdog confirmation): "
										+ proposals.get(i).getSender().getLocalName() + " that scored "
										+ rootNode.get("Proposal").get("Value").asDouble());
//								proposals.remove(i);
								accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
								acceptances.addElement(accept);
							} else {
								ACLMessage reject = proposals.get(i).createReply();
								reject.setPerformative(ACLMessage.REJECT_PROPOSAL);
								acceptances.addElement(reject);
							}
						}

					} catch (JsonProcessingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

//				structure json with results
				LineOutputJson lineOutputJson = new LineOutputJson(lineData.getLineId(), lineData.getWorkersRequired(),
						selectedWorkers);
				ObjectMapper mapper = new ObjectMapper();
				String json = null;
				try {
					json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(lineOutputJson);
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}

				generateResultFile(json);

//					send results to the watchdog agent
				if (((LineAgent) myAgent).isAux() == true) {
					((LineAgent) myAgent).setAux(false);
//						((LineAgent) myAgent).sendMessage(ACLMessage.INFORM, "watchdogAgent", "ResponderProtocolWatchdogAgent",
//								json);
				}

				((LineAgent) myAgent).sendMessage(ACLMessage.INFORM, "watchdogAgent", "ResponderProtocolWatchdogAgent",
						json);
			}
		}
	}

	protected void handleInform(ACLMessage inform) {
//		System.out.println("Agent " + inform.getSender().getName() + " successfully performed the requested action");
	}

	private void generateRankingFile() {
		String text = "RANKING - " + myAgent.getLocalName() + "\n";
		for (ACLMessage proposal : proposals) {
			text += proposal.getContent() + "\n";
		}
		String filePath;
//		if (((LineAgent) myAgent).isAux() == true) {
		filePath = "output/" + myAgent.getLocalName() + "_ranking_proposals.txt";
//		} else {
//			filePath = myAgent.getLocalName() + "_ranking_proposal_2.txt";
//		}

		try {
			FileWriter writer = new FileWriter(filePath);
			writer.write(text);
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void generateResultFile(String json) {
		String filePath;
		if (((LineAgent) myAgent).isAux() == true) {
			filePath = "output/" + myAgent.getLocalName() + "__results_1.json";
		} else {
			filePath = "output/" + myAgent.getLocalName() + "__results_2.json";
		}
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
			writer.write(json);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void findBestSelectionAge(List<SelectedWorkers> workers, int n, double maxAverageAge, int start,
			List<SelectedWorkers> currentSelection, List<SelectedWorkers> bestSolution, double[] maxTotalProposalValue,
			int minWomenRequired) {

		if (currentSelection.size() == n) {
			long femaleCount = currentSelection.stream().filter(worker -> worker.getGender().equals("Female")).count();

			if (femaleCount >= minWomenRequired) {
				double totalProposalValue = currentSelection.stream().mapToDouble(SelectedWorkers::getProposalValue)
						.sum();
				double averageAge = currentSelection.stream().mapToInt(SelectedWorkers::getAge).average().orElse(0.0);

				if (averageAge <= maxAverageAge && totalProposalValue > maxTotalProposalValue[0]) {
					bestSolution.clear();
					bestSolution.addAll(currentSelection);
					maxTotalProposalValue[0] = totalProposalValue;
				}
			}
			return;
		}
		for (int i = start; i < workers.size(); i++) {
			currentSelection.add(workers.get(i));
			findBestSelectionAge(workers, n, maxAverageAge, i + 1, currentSelection, bestSolution,
					maxTotalProposalValue, minWomenRequired);
			currentSelection.remove(currentSelection.size() - 1);
		}
	}

	public static List<SelectedWorkers> getSelectedWorkers() {
		return selectedWorkers;
	}

}