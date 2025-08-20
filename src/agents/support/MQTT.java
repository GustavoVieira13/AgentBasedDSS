package agents.support;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import jade.lang.acl.ACLMessage;

//Class to provide MQTT connection, publishing, and subscribing functionality
public class MQTT {

	public MqttClient sampleClient; // The Paho MQTT client instance
	public int qos; // Quality of Service level for MQTT messaging
	public String broker; // MQTT broker URI
	public String clientId; // Unique client ID for the MQTT client
	public MemoryPersistence persistence = new MemoryPersistence(); // In-memory persistence for the MQTT client session

	public MQTT(String clientId) {
		this.clientId = clientId;
	}

	// Establish an MQTT connection and return success/failure
	public boolean mqttConnection() {
		try {
			Properties props = loadProperties();
			broker = props.getProperty("broker");
			qos = Integer.parseInt(props.getProperty("qos"));
			sampleClient = new MqttClient(broker, clientId, persistence);
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			System.out.println(clientId + " connecting to broker: " + broker);
			sampleClient.connect(connOpts);
			System.out.println(clientId + " connected");
			return true;
		} catch (MqttException e) {
			e.printStackTrace();
			return false;
		}
	}

	// Load MQTT properties from an external file named "mqtt.properties"
	private static Properties loadProperties() {
		try (FileInputStream fs = new FileInputStream("./mqtt.properties")) {
			Properties props = new Properties();
			props.load(fs);
			return props;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Publish a message with the specified content to a given topic
	public void publish(String topic, String content) {
		try {
//			System.out.println("Publishing message: " + content);
			MqttMessage message = new MqttMessage(content.getBytes());
			message.setQos(qos);
			sampleClient.publish(topic, message);
//			System.out.println("Message published");

		} catch (MqttException me) {
//			System.out.println("reason " + me.getReasonCode());
//			System.out.println("msg " + me.getMessage());
//			System.out.println("loc " + me.getLocalizedMessage());
//			System.out.println("cause " + me.getCause());
//			System.out.println("excep " + me);
			me.printStackTrace();
		}
	}

	// Subscribe to a given MQTT topic
	public void subscribe(String topic) {
		try {
//			System.out.println(clientId + " subscribing topic: " + topic);
			sampleClient.subscribe(topic, qos);
		} catch (MqttException me) {
//			System.out.println("reason " + me.getReasonCode());
//			System.out.println("msg " + me.getMessage());
//			System.out.println("loc " + me.getLocalizedMessage());
//			System.out.println("cause " + me.getCause());
//			System.out.println("excep " + me);
			me.printStackTrace();
		}
	}
}
