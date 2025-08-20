package agents;

import java.util.Date;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import agents.protocols.FIPAContractNetInitiator;
import agents.protocols.ResponderProtocolLineAgent;
import agents.support.MQTT;
import entities.NegotiationCriteria;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import supportClasses.LineData;

public class LineAgent extends GenericAgent {

	private static final long serialVersionUID = -6241675262752614992L;
	private LineData lineAgentData;
	private boolean aux = true;

	protected void setup() {

		Object[] args = getArguments();
		lineAgentData = (LineData) args[0];
		System.out.println("-----------------------------------------------------------------------");
		System.out.println("Hello! I'm " + getLocalName());
//		System.out.println(lineAgentData);

		MQTT mqtt = new MQTT(getLocalName());
		mqtt.mqttConnection();
		mqtt.subscribe("agentBasedDSS/selectLine");

		mqtt.sampleClient.setCallback(new MqttCallback() {

			public void connectionLost(Throwable cause) {
			}

			public void deliveryComplete(IMqttDeliveryToken arg0) {
			}

			public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
//				System.out.println(getLocalName() + ": " + arg1.toString());
				if (arg1.toString().equals(getLocalName())) {
					responder();
					negotiation(NegotiationCriteria.NO_CRITERIA);
				}

			}
		});

//		addBehaviour(new ResponderProtocolLineAgent(this));
//		negotiation(NegotiationCriteria.NO_CRITERIA);
	}

	public void responder() {
		addBehaviour(new ResponderProtocolLineAgent(this));
	}

	public void negotiation(NegotiationCriteria criteria) {
		DFAgentDescription[] result = serviceSearch(this, "workforce");
		System.out.println("Available services: " + result.length);
		ACLMessage msg = new ACLMessage(ACLMessage.CFP);
		for (int i = 0; i < result.length; i++) {
			msg.addReceiver(new AID(result[i].getName().getLocalName(), AID.ISLOCALNAME));
			msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
			msg.setContent(lineAgentData.getLineId());
			msg.setReplyByDate(new Date(System.currentTimeMillis() + 2000));
		}
		addBehaviour(new FIPAContractNetInitiator(this, msg, criteria, lineAgentData));

	}

	public boolean isAux() {
		return aux;
	}

	public void setAux(boolean aux) {
		this.aux = aux;
	}
}