package com.proHar.perfoMeasure.main.ssmsModules;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Random;

import com.proHar.perfoMeasure.main.queries.QueriesLibrary;

public class SSMSUtils {

	/*
	 * Get TestCaseName with ID - TestCaseName and TestCase_System_UserName
	 * Pass TestCaseID to Application Name
	 * will be inserted :: Pk-TestScenerio(TestScenarioID) :: FK-Application_Name(TestScenarioID)
	 * */
	public static int setTestCaseName(String testScenarioName, int appId, int UserId, int decisionFlag){

		int holdID = 0;
		//		String databaseURL = AccessDataMigrationCredentials.databaseConnectionURL;
		String insertSQL = QueriesLibrary.insertIntoTestScenario_Master;
		String updateSQL = QueriesLibrary.updateTestScenario_Master(testScenarioName, UserId);

		try (Connection connection = SSMSDataMigrationCredentials.getSSMSConnection()) {
			String status = "";
			String sqlNew = QueriesLibrary.getTestScenario_Master(testScenarioName);
			Statement statement = connection.createStatement();
			
			if (decisionFlag == 1) {
				status = "Create";
				int nRowsInserted = 0;
				PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
				preparedStatement.setString(1, testScenarioName);
				preparedStatement.setInt(2, appId);
				preparedStatement.setInt(3, UserId);
				Calendar cal = Calendar.getInstance(); 
				java.sql.Timestamp timestamp = new java.sql.Timestamp(cal.getTimeInMillis());
				preparedStatement.setTimestamp(4, timestamp);
				nRowsInserted += preparedStatement.executeUpdate();
			}
			if (decisionFlag == 2) {
				status = "Update";
				statement.executeUpdate(updateSQL);
			}
			
			// Return the TestScenario_ID
			ResultSet results = statement.executeQuery(sqlNew);
			while (results.next()) {
				int idNew = results.getInt("TS_ID");
				String ScenarioNames = results.getString("TS_Name");
				if (ScenarioNames.equals(testScenarioName)) {
					holdID = idNew;
				}
			}
			
			// Trace In History table
			TestCaseTracer(holdID, testScenarioName, appId, UserId, status);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return holdID;
	}
	
	public static void TestCaseTracer(int TS_ID,String TS_Name,int TS_AppId,int TS_RegU_ID,String TS_Status) {
		String Sql = QueriesLibrary.insertIntoTestScenario_Master_History;
		try (Connection connection = SSMSDataMigrationCredentials.getSSMSConnection()) {
			int nRowsInserted = 0;
			Statement statement = connection.createStatement();
			PreparedStatement preparedStatement = connection.prepareStatement(Sql);
			preparedStatement.setInt(1, TS_ID);
			preparedStatement.setString(2, TS_Name);
			preparedStatement.setInt(3, TS_AppId);
			preparedStatement.setInt(4, TS_RegU_ID);
			preparedStatement.setString(5, TS_Status);
			Calendar cal = Calendar.getInstance(); 
			java.sql.Timestamp timestamp = new java.sql.Timestamp(cal.getTimeInMillis());
			preparedStatement.setTimestamp(6, timestamp);
			nRowsInserted += preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Check if Application Name Exists
	 * */
	public static int hasApplication(String ApplicationName) {
		int holdID = 0;
		String sql = QueriesLibrary.AskApplicationMaster;
		try (Connection connection = SSMSDataMigrationCredentials.getSSMSConnection()) {
			String appName = ApplicationName;
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(sql);

			while (result.next()) {
				int id = result.getInt("Application_ID");
				String fullname = result.getString("Application_NAME");

				if (fullname.equals(appName)) {
					holdID = id;
				}
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}

		return holdID;
	}

	/*
	 * PUT Domain Name(Application_Name) :
	 *  Check If exists OR Insert value in ACCESS :: RETURN Application_ID
	 * */
	public static int getApplicationID(String getApplicationName, int userID){
		int holdID = 0;
		String sql = QueriesLibrary.AskApplicationMaster;
		try (Connection connection = SSMSDataMigrationCredentials.getSSMSConnection()) {
			String appName = getApplicationName;
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(sql);

			while (result.next()) {
				int id = result.getInt("Application_ID");
				String fullname = result.getString("Application_NAME");
				int user_id = result.getInt("Application_Reg_Admin_UserID");

				if (fullname.equals(appName) && userID == userID) {
					holdID = id;
				}
			}              
		} catch (Exception e) {   
			e.printStackTrace();
		}

		return holdID;
	}

	/*
	 * Get Page Name with ID - CurrentURL and App_ID
	 * will be inserted :: Pk-Application_Name(Application_Id) :: FK-Page_Name(Application_ID)
	 * */

	public static int getPagerId(String pageName){
		int holdID = 0;
		String sql = QueriesLibrary.getPages;
		String insertSQL = QueriesLibrary.setPages;

		try (Connection connection = SSMSDataMigrationCredentials.getSSMSConnection()) {
			String appName = pageName;
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(sql);
			boolean flag = false;
			while (result.next()) {
				int id = result.getInt("Page_ID");
				String fullname = result.getString("Page_Name");

				if (fullname.equals(appName)) {
					holdID = id;
					flag = true;
				}
			}
			if (flag == false) {
				int nRowsInserted = 0;
				PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
				preparedStatement.setString(1, pageName);
				nRowsInserted += preparedStatement.executeUpdate();
				String sqlNew = QueriesLibrary.getPageId(pageName);
				ResultSet results = statement.executeQuery(sqlNew);
				while (results.next()) {
					int id = results.getInt("Page_ID");
					String fullname = results.getString("Page_Name");
					if (fullname.equalsIgnoreCase(appName)) {
						holdID = id;
						flag = true;
					}
				}
			}              
		} catch (Exception e) {   
			e.printStackTrace();
		}
		return holdID;
	}
	
	/*
	 * Check If Resource_Master already has The resource :
	 * If yes then Return ID 
	 * Else Insert The Entry and return value
	 * */
	public static int getResourceId(String resName, int uID){
		int holdID = 0;
		String sql = QueriesLibrary.ifResourceExists(resName);
		String insertSQL = QueriesLibrary.insertIntoResourceMasterTable;

		try (Connection connection = SSMSDataMigrationCredentials.getSSMSConnection()) {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(sql);
			boolean flag = false;
			while (result.next()) {
				int id = result.getInt("Res_ID");
				String fullname = result.getString("Res_Name");

				if (fullname.equals(resName)) {
					holdID = id;
					flag = true;
				}
			}
			if (flag == false) {
				int nRowsInserted = 0;
				PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
				preparedStatement.setString(1, resName);
				preparedStatement.setInt(2, uID);
				nRowsInserted += preparedStatement.executeUpdate();
				String sqlNew = QueriesLibrary.getResourceId(resName);
				ResultSet results = statement.executeQuery(sqlNew);
				while (results.next()) {
					int id = results.getInt("Res_ID");
					String fullname = results.getString("Res_Name");
					if (fullname.equals(resName)) {
						holdID = id;
						flag = true;
					}
				}
			}              
		} catch (Exception e) {   
			e.printStackTrace();
		}
		return holdID;
	}
	
	/*
	 * Get navigation ID, with Respect to current TestCase Execution Credentials
	 * */
	public static int getRecentNavigationID(int appId, int pageId, int testCaseId, int UId) {
		String SQL = QueriesLibrary.getIdOfLastNavigation(appId, pageId, testCaseId, UId);
		int holdID = 0;
		try (Connection connection = SSMSDataMigrationCredentials.getSSMSConnection()) {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(SQL);

			while (result.next()) {
				holdID = result.getInt("Nav_ID");
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return holdID;
	}
}
