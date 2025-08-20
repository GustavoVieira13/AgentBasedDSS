package main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import agents.support.DB;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import supportClasses.ExperienceOnTheLine;
import supportClasses.JobRotation;
import supportClasses.LineData;
import supportClasses.WorkerData;
import supportClasses.WorkerPreference;

public class AgentGenerator {

	private static ContainerController lineAgentController;
	private static ContainerController workerAgentController;
	private static ContainerController watchdogAgentController;

	private static AgentController lineAgent;
	private static AgentController workerAgent;
	private static AgentController watchdogAgent;

	private static String jsonString;

//	WorkerAgent attributes
	private static String workerId;
	private static boolean availability;
	private static boolean medicalCondition;
	private static boolean uteExperience;
	private static double workerResilience;
	private static String gender;
	private static int age;
	private static List<WorkerPreference> listWorkerPreference = new ArrayList<>();
	private static List<JobRotation> listJobRotation = new ArrayList<>();
	private static List<ExperienceOnTheLine> listExperienceOnTheLine = new ArrayList<>();
	private static String lineId; // also used for LineAgent
	private static double value;

//	LineAgent attributes	
	private static String geometry;
	private static boolean productionPriority;
	private static int dueDate;
	private static int workersRequired;

	static java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");

	public static void main(String[] args) {

		Runtime rt = Runtime.instance();

//		Create main container
		ProfileImpl profileTemplate = new ProfileImpl();
		profileTemplate.setParameter(Profile.GUI, "true");
		profileTemplate.setParameter(Profile.CONTAINER_NAME, "Main");
		profileTemplate.setParameter(Profile.MAIN, "true");
		profileTemplate.setParameter(Profile.MAIN_HOST, "localhost");
		profileTemplate.setParameter(Profile.MAIN_PORT, "1099");
		profileTemplate.setParameter(Profile.LOCAL_HOST, "localhost");
		profileTemplate.setParameter(Profile.LOCAL_PORT, "1099");
		profileTemplate.setParameter("jade_domain_df_maxresult", "1000"); // Increases the number of agent registrations

		rt.createMainContainer(profileTemplate);

//		Create container for Line Agents
		ProfileImpl profileTemplateLineAgent = new ProfileImpl();
		profileTemplateLineAgent.setParameter(Profile.GUI, "false");
		profileTemplateLineAgent.setParameter(Profile.CONTAINER_NAME, "Line Agents");
		profileTemplateLineAgent.setParameter(Profile.MAIN, "false");
		profileTemplateLineAgent.setParameter(Profile.MAIN_HOST, "localhost");
		profileTemplateLineAgent.setParameter(Profile.MAIN_PORT, "1099");
		profileTemplateLineAgent.setParameter(Profile.LOCAL_HOST, "localhost");
		profileTemplateLineAgent.setParameter(Profile.LOCAL_PORT, "1099");

		lineAgentController = rt.createAgentContainer(profileTemplateLineAgent);

//		Create container for Worker Agents
		ProfileImpl profileTemplateWorkerAgent = new ProfileImpl();
		profileTemplateWorkerAgent.setParameter(Profile.GUI, "false");
		profileTemplateWorkerAgent.setParameter(Profile.CONTAINER_NAME, "Worker Agents");
		profileTemplateWorkerAgent.setParameter(Profile.MAIN, "false");
		profileTemplateWorkerAgent.setParameter(Profile.MAIN_HOST, "localhost");
		profileTemplateWorkerAgent.setParameter(Profile.MAIN_PORT, "1099");
		profileTemplateWorkerAgent.setParameter(Profile.LOCAL_HOST, "localhost");
		profileTemplateWorkerAgent.setParameter(Profile.LOCAL_PORT, "1099");

		workerAgentController = rt.createAgentContainer(profileTemplateWorkerAgent);

//		Create container for Watchdog Agent
		ProfileImpl profileTemplateWatchdogAgent = new ProfileImpl();
		profileTemplateWatchdogAgent.setParameter(Profile.GUI, "false");
		profileTemplateWatchdogAgent.setParameter(Profile.CONTAINER_NAME, "Watchdog Agent");
		profileTemplateWatchdogAgent.setParameter(Profile.MAIN, "false");
		profileTemplateWatchdogAgent.setParameter(Profile.MAIN_HOST, "localhost");
		profileTemplateWatchdogAgent.setParameter(Profile.MAIN_PORT, "1099");
		profileTemplateWatchdogAgent.setParameter(Profile.LOCAL_HOST, "localhost");
		profileTemplateWatchdogAgent.setParameter(Profile.LOCAL_PORT, "1099");
		watchdogAgentController = rt.createAgentContainer(profileTemplateWatchdogAgent);

		jsonString = readJson("./OutputKB_Final.json");

		if (jsonString != null) {
			createWatchdogAgent();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

//			If you want to test for small samples include an integer value or null for all
//			createWorkerAgentsDB(10);
			createWorkerAgentsDB(null);

//			Old version using json file
//			createWorkerAgentsJson();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			createLineAgentsJson();
		} else {
			System.out.println("Failed to read json");
		}
	}

	private static String readJson(String path) {
		try {
			String jsonString = new String(Files.readAllBytes(Paths.get(path)));
			return jsonString;
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}
	}

	private static void createWorkerAgentsDB(Integer limit) {
		Connection connection = null;
		Statement statement = null;

		try {
			connection = DB.getConnection();

			statement = connection.createStatement();
			String query = String
					.format("SELECT w.Id, w.Availability, w.MedicalCondition, w.UTEExperience, w.WorkerResilience, "
							+ "w.Gender, w.Age, " + "wp.Preferences, wr.JobRotation, we.ExperienceOnTheLine "
							+ "FROM workers w " + "LEFT JOIN ( "
							+ "    SELECT WorkerId, JSON_ARRAYAGG(JSON_OBJECT('LineId', LineId, 'Value', Value)) AS Preferences "
							+ "    FROM worker_preference " + "    GROUP BY WorkerId " + ") wp ON w.Id = wp.WorkerId "
							+ "LEFT JOIN ( "
							+ "    SELECT WorkerId, JSON_ARRAYAGG(JSON_OBJECT('LineId', LineId, 'Value', Value)) AS JobRotation "
							+ "    FROM worker_job_rotation " + "    GROUP BY WorkerId " + ") wr ON w.Id = wr.WorkerId "
							+ "LEFT JOIN ( "
							+ "    SELECT WorkerId, JSON_ARRAYAGG(JSON_OBJECT('LineId', LineId, 'Value', Value)) AS ExperienceOnTheLine "
							+ "    FROM worker_experience_on_the_line " + "    GROUP BY WorkerId "
							+ ") we ON w.Id = we.WorkerId ");

			if (limit != null) {
				query += String.format(" LIMIT %d", limit);
			}

			ResultSet resultSet = statement.executeQuery(query);

			while (resultSet.next()) {
				workerId = resultSet.getString("Id");
				availability = resultSet.getBoolean("Availability");
				medicalCondition = resultSet.getBoolean("MedicalCondition");
				uteExperience = resultSet.getBoolean("UTEExperience");
				workerResilience = resultSet.getDouble("WorkerResilience");
				gender = resultSet.getString("Gender");
				age = resultSet.getInt("Age");

				String preferencesJson = resultSet.getString("Preferences");
				if (preferencesJson != null) {
					JSONArray preferencesArray = new JSONArray(preferencesJson);
					for (int i = 0; i < preferencesArray.length(); i++) {
						JSONObject preference = preferencesArray.getJSONObject(i);
						lineId = preference.getString("LineId");
						value = preference.getDouble("Value");
						WorkerPreference workerPreference = new WorkerPreference(lineId,
								(Math.round(value * 100.0) / 100.0));
						listWorkerPreference.add(workerPreference);
					}
				}

				String jobRotationJson = resultSet.getString("JobRotation");
				if (jobRotationJson != null) {
					JSONArray jobRotationArray = new JSONArray(jobRotationJson);
					for (int i = 0; i < jobRotationArray.length(); i++) {
						JSONObject rotation = jobRotationArray.getJSONObject(i);
						lineId = rotation.getString("LineId");
						value = rotation.getDouble("Value");
						JobRotation jobRotation = new JobRotation(lineId, (Math.round(value * 100.0) / 100.0));
						listJobRotation.add(jobRotation);
					}
				}

				String experienceOnTheLineJson = resultSet.getString("ExperienceOnTheLine");
				if (experienceOnTheLineJson != null) {
					JSONArray experienceArray = new JSONArray(experienceOnTheLineJson);
					for (int i = 0; i < experienceArray.length(); i++) {
						JSONObject experience = experienceArray.getJSONObject(i);
						lineId = experience.getString("LineId");
						value = experience.getInt("Value");
						ExperienceOnTheLine experienceOnTheLine = new ExperienceOnTheLine(lineId, (int) value);
						listExperienceOnTheLine.add(experienceOnTheLine);
					}
				}

				WorkerData workerData = new WorkerData(workerId, availability, medicalCondition, uteExperience,
						workerResilience, gender, age, listWorkerPreference, listJobRotation, listExperienceOnTheLine);
				Object arguments[] = new Object[1];
				arguments[0] = workerData;

				try {
					workerAgent = workerAgentController.createNewAgent("workerAgent" + workerId, "agents.WorkerAgent",
							arguments);
					workerAgent.start();
					listWorkerPreference = new ArrayList<>();
					listJobRotation = new ArrayList<>();
					listExperienceOnTheLine = new ArrayList<>();
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} catch (StaleProxyException e) {
					e.printStackTrace();
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (statement != null)
					statement.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private static void createWorkerAgentsJson() {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			JsonNode rootNode = objectMapper.readTree(jsonString);
			int n = rootNode.get("OrderInfoList").get(0).get("WorkerInfoList").size();
//			int n = 30; // Test purposes
			for (int i = 0; i < n; i++) {

				workerId = rootNode.get("OrderInfoList").get(0).get("WorkerInfoList").get(i).get("Id").asText();
				availability = Boolean.parseBoolean(
						rootNode.get("OrderInfoList").get(0).get("WorkerInfoList").get(i).get("Availability").asText());
				medicalCondition = Boolean.parseBoolean(rootNode.get("OrderInfoList").get(0).get("WorkerInfoList")
						.get(i).get("MedicalCondition").asText());
				uteExperience = Boolean.parseBoolean(rootNode.get("OrderInfoList").get(0).get("WorkerInfoList").get(i)
						.get("UTEExperience").asText());
				workerResilience = rootNode.get("OrderInfoList").get(0).get("WorkerInfoList").get(i)
						.get("WorkerResilience").asDouble();

				for (int j = 0; j < rootNode.get("OrderInfoList").get(0).get("WorkerInfoList").get(i)
						.get("WorkerPreference").size(); j++) {
					lineId = rootNode.get("OrderInfoList").get(0).get("WorkerInfoList").get(i).get("WorkerPreference")
							.get(j).get("LineId").asText();
					value = rootNode.get("OrderInfoList").get(0).get("WorkerInfoList").get(i).get("WorkerPreference")
							.get(j).get("Value").asDouble();

					WorkerPreference workerPreference = new WorkerPreference(lineId, value);
					listWorkerPreference.add(workerPreference);
				}

				WorkerData workerData = new WorkerData(workerId, availability, medicalCondition, uteExperience,
						workerResilience, listWorkerPreference);
				Object arguments[] = new Object[1];
				arguments[0] = workerData;

				try {
					workerAgent = workerAgentController.createNewAgent("workerAgent" + workerId, "agents.WorkerAgent",
							arguments);
					workerAgent.start();
					listWorkerPreference = new ArrayList<>();
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} catch (StaleProxyException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void createLineAgentsJson() {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			JsonNode rootNode = objectMapper.readTree(jsonString);
			int n = rootNode.get("OrderInfoList").size();
//			n = 1;
			for (int i = 0; i < n; i++) {
				lineId = rootNode.get("OrderInfoList").get(i).get("LineInfo").get("LineId").asText();
				geometry = rootNode.get("OrderInfoList").get(i).get("LineInfo").get("Geometry").asText();
				productionPriority = Boolean.parseBoolean(
						rootNode.get("OrderInfoList").get(i).get("LineInfo").get("ProductionPriority").asText());
				dueDate = rootNode.get("OrderInfoList").get(i).get("LineInfo").get("DueDate").asInt();
				workersRequired = rootNode.get("OrderInfoList").get(i).get("LineInfo").get("WorkersRequired").asInt();

				LineData lineData = new LineData(lineId, geometry, productionPriority, dueDate, workersRequired);
				Object arguments[] = new Object[1];
				arguments[0] = lineData;

				try {
					lineAgent = lineAgentController.createNewAgent("lineAgent" + lineId, "agents.LineAgent", arguments);
					lineAgent.start();
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} catch (StaleProxyException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error reading JSON Array");
		}
	}

	private static void createWatchdogAgent() {
		Object arguments[] = new Object[0];
		try {
			watchdogAgent = watchdogAgentController.createNewAgent("watchdogAgent", "agents.WatchdogAgent", arguments);
			watchdogAgent.start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}

}