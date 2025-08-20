package agents.protocols;

import java.util.Date;
import java.util.UUID;

import agents.LineAgent;
import entities.NegotiationCriteria;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import supportClasses.SelectedWorkers;

public class ResponderProtocolLineAgent extends CyclicBehaviour {

	private static final long serialVersionUID = -2557458862288774560L;

	public ResponderProtocolLineAgent(Agent a) {
		super(a);
	}

	@Override
	public void action() {

		MessageTemplate template = MessageTemplate.MatchProtocol("ResponderProtocolLineAgent");

		ACLMessage msg = myAgent.receive(template);
		if (msg != null) {
			if (msg.getPerformative() == ACLMessage.INFORM) {
				System.out.println(myAgent.getLocalName() + ": " + msg.getContent());

				if (msg.getContent().equals("criteria_satisfied")) {
//					System.out.println(FIPAContractNetInitiator.getSelectedWorkers());

					for (SelectedWorkers worker : FIPAContractNetInitiator.getSelectedWorkers()) {
//						System.out.println(worker.getWorkerId());
						msg = new ACLMessage(ACLMessage.INFORM);
						msg.addReceiver(new AID("WorkerAgent" + worker.getWorkerId(), AID.ISLOCALNAME));
						msg.setProtocol("ResponderProtocolWorkerAgent");
						msg.setConversationId(UUID.randomUUID().toString());
						msg.setReplyByDate(new Date(System.currentTimeMillis() + 5000));
						msg.setContent("desregisterDF");
						myAgent.send(msg);
					}

				} else if (msg.getContent().equals("criteria_age_not_satisfied")) {
//					Renegotiation
					System.out.println(myAgent.getLocalName() + ": starting renegotiation");

					((LineAgent) myAgent).negotiation(NegotiationCriteria.AGE);
				}

				else if (msg.getContent().equals("criteria_gender_not_satisfied")) {
//					Renegotiation
					System.out.println(myAgent.getLocalName() + ": starting renegotiation");

					((LineAgent) myAgent).negotiation(NegotiationCriteria.GENDER);
				}
			}
		}
		block();
	}
}