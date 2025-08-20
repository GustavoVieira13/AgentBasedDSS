package agents;

import java.util.Date;
import java.util.UUID;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

//Abstract class extending JADE's base Agent functionality
abstract class GenericAgent extends Agent {

	private static final long serialVersionUID = -5425154723851575801L; // Unique identifier for serialization
	protected String typeAgent; // Field to store the agent's type
	protected boolean state; // Field to track the agent's operational state

	// Method to register a service in the JADE Directory Facilitator (DF)
	protected void serviceRegister(Agent agent, String serviceName) {
		try {
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(agent.getAID());
			ServiceDescription sd = new ServiceDescription();
			sd.setType(serviceName);
			sd.setName(serviceName);
			dfd.addServices(sd);

			DFService.register(agent, dfd);
//			System.out.println(agent.getLocalName() + " registered service: " + serviceName);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}

	// Method to search for agents providing a particular service
	public DFAgentDescription[] serviceSearch(Agent agent, String serviceName) {
		DFAgentDescription[] result = null;
		DFAgentDescription df = new DFAgentDescription();
		ServiceDescription service = new ServiceDescription();
		service.setType(serviceName);
		service.setName(serviceName);
		df.addServices(service);
		try {
			result = DFService.search(agent, getDefaultDF(), df);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		return result;
	}

	// Method to deregister an agent from the DF
	public void deregister(Agent agent) {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		try {
			DFService.deregister(agent, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
	}

	// Utility method to send an ACL message with specified parameters
	public void sendMessage(int performative, String receiver, String protocol, String content) {
		ACLMessage msg = new ACLMessage(performative);
		msg.addReceiver(new AID(receiver, AID.ISLOCALNAME));
		msg.setProtocol(protocol);
		msg.setConversationId(UUID.randomUUID().toString());
		msg.setReplyByDate(new Date(System.currentTimeMillis() + 5000));
		msg.setContent(content);
		send(msg);
	}
}
