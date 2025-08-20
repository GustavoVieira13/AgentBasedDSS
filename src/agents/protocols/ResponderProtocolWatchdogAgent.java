package agents.protocols;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.fasterxml.jackson.databind.ObjectMapper;

import agents.support.MQTT;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import supportClasses.LineDataJson;
import supportClasses.WorkerDataJson;

public class ResponderProtocolWatchdogAgent extends CyclicBehaviour {

	private static final long serialVersionUID = 5696910106673523843L;
	MQTT mqtt;
	private String topic;
	private boolean aux = false;
	private double score = 0;

	public ResponderProtocolWatchdogAgent(Agent a, MQTT mqtt) {
		super(a);
		this.mqtt = mqtt;
	}

	@Override
	public void action() {

		MessageTemplate template = MessageTemplate.MatchProtocol("ResponderProtocolWatchdogAgent");

		ACLMessage msg = myAgent.receive(template);
		if (msg != null) {

			if (msg.getPerformative() == ACLMessage.FAILURE) {
				if (msg.getContent().equals("no_workers")) {
					topic = "agentBasedDSS/" + msg.getSender().getLocalName() + "/Renegotiation/criteria";
					mqtt.publish(topic, "no_workers");
				}
			}

			if (msg.getPerformative() == ACLMessage.INFORM) {
				System.out.println(myAgent.getLocalName() + ": " + msg.getContent());

				if (!aux) {
					mqtt.publish("agentBasedDSS/" + msg.getSender().getLocalName() + "/firstNegotiation/result",
							msg.getContent());
					topic = "agentBasedDSS/" + msg.getSender().getLocalName() + "/firstNegotiation/criteria";
				}

				if (aux) {
					mqtt.publish("agentBasedDSS/" + msg.getSender().getLocalName() + "/Renegotiation/result",
							msg.getContent());
					topic = "agentBasedDSS/" + msg.getSender().getLocalName() + "/Renegotiation/criteria";
				}

				aux = !aux;

//				Select criteria
				if (!checkAgeCriterion(msg.getContent())) {
					ACLMessage reply = msg.createReply();
					reply.setProtocol("ResponderProtocolLineAgent");
					reply.setPerformative(ACLMessage.INFORM);
					reply.setContent("criteria_satisfied");
					myAgent.send(reply);
					mqtt.publish(topic, "criteria_age_satisfied");

				} else {
					mqtt.publish(topic, "criteria_age_not_satisfied");
				}

				if (!checkGenderCriterion(msg.getContent())) {
					mqtt.publish(topic, "criteria_gender_satisfied");

				} else {
					mqtt.publish(topic, "criteria_gender_not_satisfied");
				}

				mqtt.sampleClient.setCallback(new MqttCallback() {

					public void connectionLost(Throwable cause) {
					}

					public void deliveryComplete(IMqttDeliveryToken arg0) {
					}

					public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
//						System.out.println(myAgent.getLocalName() + ": " + arg1.toString());
						if (arg1.toString().equals("Age")) {
							ACLMessage reply = msg.createReply();
							reply.setProtocol("ResponderProtocolLineAgent");
							reply.setPerformative(ACLMessage.INFORM);
							reply.setContent("criteria_age_not_satisfied");
							myAgent.send(reply);

						} else if (arg1.toString().equals("Gender")) {
							ACLMessage reply = msg.createReply();
							reply.setProtocol("ResponderProtocolLineAgent");
							reply.setPerformative(ACLMessage.INFORM);
							reply.setContent("criteria_gender_not_satisfied");
							myAgent.send(reply);

						} else if (arg1.toString().equals("None")) {
							aux = !aux;

							ACLMessage reply = msg.createReply();
							reply.setProtocol("ResponderProtocolLineAgent");
							reply.setPerformative(ACLMessage.INFORM);
							reply.setContent("criteria_satisfied");
							myAgent.send(reply);
						}

					}
				});

			}
		}

		block();

	}

	private boolean checkGenderCriterion(String msg) {
		String jsonString = msg;
		int femaleCount = 0;
		int numberOfWorkers = 0;
		score = 0;

		ObjectMapper mapper = new ObjectMapper();
		try {
			LineDataJson line = mapper.readValue(jsonString, LineDataJson.class);

			numberOfWorkers = line.getWorkers().size();
			femaleCount = 0;

			for (WorkerDataJson worker : line.getWorkers()) {
				if ("Female".equalsIgnoreCase(worker.getGender())) {
					femaleCount++;
				}
			}

			System.out.println("Female workers: " + femaleCount + " | " + "Total workers: " + numberOfWorkers);

//			for (WorkerDataJson worker : line.getWorkers()) {
//				score += worker.getProposalValue();
//			}
//			System.out.println("Score: " + score);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (femaleCount >= numberOfWorkers / 2.0) {
			System.out.println("At least half of the workers are female.");
			return false;
		} else {
			System.out.println("Less than half of the workers are female.");
//			System.out.println("ASK FOR RENEGOTIATION - GENDER CRITERION (TO BE IMPLEMENTED)");
			return true;
		}
	}

	private boolean checkAgeCriterion(String msg) {
		String jsonString = msg;
		double averageAge = 0;
		score = 0;
		ObjectMapper mapper = new ObjectMapper();
		try {
			LineDataJson line = mapper.readValue(jsonString, LineDataJson.class);
			double totalAge = 0;
			int numberOfWorkers = line.getWorkers().size();

			for (WorkerDataJson worker : line.getWorkers()) {
				totalAge += worker.getAge();
			}

			for (WorkerDataJson worker : line.getWorkers()) {
				score += worker.getProposalValue();
			}
			System.out.println("Score: " + score);
			averageAge = totalAge / numberOfWorkers;
			System.out.println("Average age: " + averageAge);

		} catch (Exception e) {
			e.printStackTrace();
		}
		if (averageAge <= 35) {
			System.out.println("The average age is below or equal to 35 years.");
			return false;
		} else {
			System.out.println("The average age is above 35 years.");
//			System.out.println("ASK FOR RENEGOTIATION - AGE CRITERION");
			return true;
		}
	}
}