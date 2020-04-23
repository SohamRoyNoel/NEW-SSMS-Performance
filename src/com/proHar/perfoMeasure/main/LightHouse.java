package com.proHar.perfoMeasure.main;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.sql.PreparedStatement;


import javax.swing.JOptionPane;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import com.proHar.perfoMeasure.main.exceptions.NoApplicationAccessException;
import com.proHar.perfoMeasure.main.exceptions.NoExistingTestCaseOrApplicationNameFoundLightHouseException;
import com.proHar.perfoMeasure.main.keysUtility.ApiKeyContainer;
import com.proHar.perfoMeasure.main.keysUtility.LoginUserIdentifier;
import com.proHar.perfoMeasure.main.queries.QueriesLibrary;
import com.proHar.perfoMeasure.main.ssmsModules.SSMSDataMigrationCredentials;
import com.proHar.perfoMeasure.main.ssmsModules.SSMSDataMigrationUtils;
import com.proHar.perfoMeasure.main.ssmsModules.SSMSUtils;

/*
 * Performance Entry Point
 * */
public class LightHouse {

	/*
	 * Set the API key before execution 
	 * */
	ApiKeyContainer ac = new ApiKeyContainer();
	public void SetApiKey(String apiKey) {
		ac.setApiKey(apiKey);

	}
	//public void TestPerformer() throws NoExistingTestCaseOrApplicationNameFoundLightHouseException, NoApplicationAccessException {
	public void Performer(WebDriver driver, String Test_Scenario_Name, String ProjectName) throws NoExistingTestCaseOrApplicationNameFoundLightHouseException, NoApplicationAccessException {

		// Application name
		JavascriptExecutor js = (JavascriptExecutor)driver;  
		String ApplicationName = js.executeScript("return document.domain;").toString();
		//		String TestApplicationName = "www.jhancockpensions.com";
		//		String Test_Scenario_Name = "JohnHancocKK";
		//		String getCurrentURL = "https://www.jhancockpensions.com/do/registration/userAuthenticationInfo";
		/*
		 * Find The Login ID
		 * */
		String api = ac.getApiKey();
		int userID = LoginUserIdentifier.getFindUserId(api);
		/*
		 * Application ID
		 * */
		int getApplicationID = SSMSUtils.getApplicationID(ApplicationName, userID);

		//		Boolean appLicationAccessAuthentication = ApplicationAuthentication(userID, ApplicationName);
		//		System.out.println("Application Authentication : " + appLicationAccessAuthentication);

		// Test
		Boolean TestappLicationAccessAuthentication = ApplicationAuthentication(userID, ApplicationName);
		if (TestappLicationAccessAuthentication) {
			/*
			 * Check if Testcase exists then OVERRIDE it else THROW AN EXCEPTION 
			 * Go to UI; login/signup
			 * Create TEST CASE NAME and Application name - CHECK Availability 
			 * Use the same name for the RUN 
			 * */
			int TS_ID = TsAuthentication(getApplicationID, Test_Scenario_Name);
			if (TS_ID > 0) {
				/*
				 * Set TestCase Name and UserName
				 * on the TestCases table, with an unique ID
				 * */

				// int SSMStestCaseID = SSMSUtils.setTestCaseName(Test_Scenario_Name, getApplicationID, userID, flag);

				/*
				 * Set the Page Name with new ID,
				 * Fetch the Current PAGE NAME :: Collect it from VALUE PARSER class
				 * PASS the appname_id :: insert it as (Page_ID|Page_NAME|Application_ID)
				 * */
				String getCurrentURL = driver.getCurrentUrl();
				int SSMSpager_id = SSMSUtils.getPagerId(getCurrentURL);

//				System.out.println("App " + getApplicationID + " Page " + SSMSpager_id+ "TC " + SSMStestCaseID + "uerId " + userID);
				// Performance Methods
				try {
					// Recently added navigation Id
					int Navigation_Master_Nav_id = ValueParser.NavigationAnalyser(driver, getApplicationID, SSMSpager_id, TS_ID, userID);
					System.out.println("NavId : "+Navigation_Master_Nav_id);
					// Insert Value in Resource_Master and Return the value

					/*
					 * Resources
					 * */
					ValueParser.ResourceAnalyser(driver, userID, Navigation_Master_Nav_id);
					
					RemovalAndDeletion();
					System.out.println("done");
				} catch (InterruptedException e) {   
					e.printStackTrace();
				}
			} else {
				throw new NoExistingTestCaseOrApplicationNameFoundLightHouseException("You might not have previous registered test case named \"" + Test_Scenario_Name + "\" or appplication named \"" + ApplicationName + "\". You can simply visit performancelighthouse.com and register your TestCase Name");
			}
		} else {
			throw new NoApplicationAccessException("Yo do not have Permission for the application : " + ApplicationName + ", You should raise a access request from the performancelighthouse.com portal");
		}

	}

	/*
	 * Data Movement Functions
	 * */
	private static void RemovalAndDeletion() {
		try (Connection connection = SSMSDataMigrationCredentials.getSSMSConnection()) {
			// Move Resource_Mapper To Resource_Mapper_History
			int nRowsInserted = 0;
			PreparedStatement preparedStatements = connection.prepareStatement(QueriesLibrary.moveResourceMapperToResourceMapperHistory);
			nRowsInserted += preparedStatements.executeUpdate();
			
			// Delete Entries from Resource_Mapper
			PreparedStatement preparedStatementes1 = connection.prepareStatement(QueriesLibrary.deleteResourceMapper);
			nRowsInserted += preparedStatementes1.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Check if the Application Name Exists!
	 * If yes : it validates if the user with an API key has an access to the Application : throws NoApplicationAccessException
	 * */
	private static boolean ApplicationAuthentication(int userId, String ApplicationName) {

		/*
		 * First Check if Application name exists in Application Table
		 * */
		int getApplicationIdIfExists = SSMSUtils.hasApplication(ApplicationName);
//		System.out.println("get Application Id :  " + getApplicationIdIfExists);		
		String query = QueriesLibrary.hasAccessToApplication();
		boolean flag = false;
		try (Connection connection = SSMSDataMigrationCredentials.getSSMSConnection()) {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);
			while (result.next()) {
				int App_Id = result.getInt("App_Application_ID");
				int AccessID = result.getInt("App_user_Reg_ID");

				if ((App_Id == getApplicationIdIfExists) && (AccessID == userId)) {
					flag = true;
				}                
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return flag;
	}

	/*
	 * Check if the Test Scenario name Exists!
	 * if yes Prompt user with OverRide existing TestScenario : That will UPDATE TS_Reg_UserID and Ts_CreationTime field in DB
	 * */
	private static int TsAuthentication(int appID, String testCaseName) {

		String query = QueriesLibrary.authQuery();
		int tsID = 0;
		try (Connection connection = SSMSDataMigrationCredentials.getSSMSConnection()) {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);
			while (result.next()) {
				String testSCname = result.getString("TS_Name");
				int applicationId = result.getInt("TS_Application_ID");
				

				/*
				 * Check if the TestScenario is added from UI
				 * */
				if (testSCname.equals(testCaseName) && (applicationId == appID)) {
					tsID = result.getInt("TS_ID");
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return tsID;
	}


	// get values at any time
	public List<String> getListedNavigationElementsNow(){
		List<String> storedNavValue = ValueParser.navHolder;
		return storedNavValue;
	}

	public List<String> getListedResourceElementsNow(){
		List<String> storedResValue = ValueParser.navHolder;
		return storedResValue;
	}

	private static String getNAVlocation(String baseFolderPath) {
		String navigationPath = baseFolderPath + "\\navTemp.txt";
		File navFile = new File(navigationPath);
		createFile(navFile);
		return navigationPath;
	}

	private static String getRESlocation(String baseFolderPath) {

		String resourcesPath = baseFolderPath + "\\resTemp.txt";
		File resFile = new File(resourcesPath);
		createFile(resFile);
		return resourcesPath;
	}

	private static void createFile(File navFile) {
		try {
			navFile.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}