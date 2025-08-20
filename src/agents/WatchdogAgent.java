package agents;

import agents.protocols.ResponderProtocolWatchdogAgent;
import agents.support.MQTT;

//WatchdogAgent class extending the generic agent
public class WatchdogAgent extends GenericAgent {

	// Unique identifier for object serialization
	private static final long serialVersionUID = 5125789305370335316L;

	protected void setup() {
		
		System.out.println("Hello! I'm " + getLocalName());
		MQTT mqtt = new MQTT(getLocalName()); // Instantiate an MQTT client, named by the agent's local name
		mqtt.mqttConnection(); // Establish the MQTT connection
		mqtt.subscribe("agentBasedDSS/renegotiation"); // Subscribe to a specific MQTT topic for receiving messages
		addBehaviour(new ResponderProtocolWatchdogAgent(this,mqtt)); // Add the watchdog's responder behavior, passing in the MQTT client

	}
}
