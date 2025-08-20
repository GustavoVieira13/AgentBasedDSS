package agents.protocols;

import agents.WorkerAgent;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

//Behavior continuously checking for messages addressed to the WorkerAgent
public class ResponderProtocolWorkerAgent extends CyclicBehaviour {

	private static final long serialVersionUID = -2216986155970406716L;

	// Constructor takes the agent reference and passes it to the superclass
	public ResponderProtocolWorkerAgent(Agent a) {
		super(a);
	}

	@Override
	// Invoked repeatedly by JADE to handle incoming messages
	public void action() {

		// Create a template to match messages under the specified protocol
		MessageTemplate template = MessageTemplate.MatchProtocol("ResponderProtocolWorkerAgent");

		// Attempt to retrieve a message matching the protocol from the agent's queue
		ACLMessage msg = myAgent.receive(template);
		if (msg != null) {
			if (msg.getPerformative() == ACLMessage.INFORM) {
//				System.out.println(myAgent.getLocalName() + ": " + msg.getContent());
				// If the message instructs this worker to deregister from DF
				if (msg.getContent().equals("desregisterDF")) {
//					System.out.println(myAgent.getLocalName() + ": deregistering from df");
					// Log a confirmation message in the console
					System.out.println(myAgent.getLocalName() + ": confirmed");
					// Deregister the WorkerAgent from the Directory Facilitator
					((WorkerAgent) myAgent).deregister(myAgent);
				}
			}
		}
		// Block the behavior until a new message arrives
		block();
	}
}
