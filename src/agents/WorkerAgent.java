package agents;

import agents.protocols.FIPAContractNetResponder;
import agents.protocols.ResponderProtocolWorkerAgent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import supportClasses.WorkerData;

public class WorkerAgent extends GenericAgent {
// WorkerAgent class extending GenericAgent for JADE functionalities

	private static final long serialVersionUID = -6805840867742500327L; // Serial version UID for object serialization
	private WorkerData workerAgentData; // Holds specific data for the worker agent

	protected void setup() {

		Object[] args = getArguments(); // Retrieve passed-in arguments when the agent is created
		workerAgentData = (WorkerData) args[0]; // Cast the first argument to WorkerData

		System.out.println("Hello! I'm " + getLocalName());
//		System.out.println(workerAgentData);

		serviceRegister(this, "workforce"); // Register this agent’s service under the 'workforce' type
		
		addBehaviour(new ResponderProtocolWorkerAgent(this)); 

		// Create a template matching Contract Net protocol requests with CFP performative
		MessageTemplate template = MessageTemplate.and( 
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
				MessageTemplate.MatchPerformative(ACLMessage.CFP));
		addBehaviour(new FIPAContractNetResponder(this, template, workerAgentData));
		// Add the Contract Net Responder behavior with this agent’s worker data
	}

}
