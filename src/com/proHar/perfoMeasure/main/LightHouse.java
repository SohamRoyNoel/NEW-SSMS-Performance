package com.proHar.perfoMeasure.main;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

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
			int flag = TsAuthentication(getApplicationID, Test_Scenario_Name);
			if (flag != 3) {
				/*
				 * Set TestCase Name and UserName
				 * on the TestCases table, with an unique ID
				 * */

				int SSMStestCaseID = SSMSUtils.setTestCaseName(Test_Scenario_Name, getApplicationID, userID, flag);

				/*
				 * Set the Page Name with new ID,
				 * Fetch the Current PAGE NAME :: Collect it from VALUE PARSER class
				 * PASS the appname_id :: insert it as (Page_ID|Page_NAME|Application_ID)
				 * */
				String getCurrentURL = driver.getCurrentUrl();
				int SSMSpager_id = SSMSUtils.getPagerId(getCurrentURL);

				// Performance Methods
				try {
					// Recently added navigation Id
					int Navigation_Master_Nav_id = ValueParser.NavigationAnalyser(driver, getApplicationID, SSMSpager_id, SSMStestCaseID, userID);
					System.out.println("NavId : "+Navigation_Master_Nav_id);
					// Insert Value in Resource_Master and Return the value
					
					/*
					 * Resources
					 * */
					ValueParser.ResourceAnalyser(driver, userID, Navigation_Master_Nav_id);
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
	 * Check if the Application Name Exists!
	 * If yes : it validates if the user with an API key has an access to the Application : throws NoApplicationAccessException
	 * */
	private static boolean ApplicationAuthentication(int userId, String ApplicationName) {

		/*
		 * First Check if Application name exists in Application Table
		 * */
		int getApplicationIdIfExists = SSMSUtils.hasApplication(ApplicationName);
		System.out.println("get Application Id :  " + getApplicationIdIfExists);		
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
		int flag = 1; // Insert Token 1
		try (Connection connection = SSMSDataMigrationCredentials.getSSMSConnection()) {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);
			while (result.next()) {
				String testSCname = result.getString("TS_Name");
				int applicationId = result.getInt("TS_Application_ID");

				/*
				 * PopUp that verifies if TS_Reg_UserID will be updated 
				 * */
				if (testSCname.equals(testCaseName) && (applicationId == appID)) {
					if (JOptionPane.showConfirmDialog(null, "Do you really want to override User ID of the existing Test Case '"+testCaseName+"'?", "WARNING",
							JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						flag = 2; // Update Token 2
					} else {
						flag = 3; // Refuse Updation 3
					}
				}                
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return flag;
	}

	// Access Database generation class
	public void SSMSAgent() throws InterruptedException {
		Connection con = SSMSDataMigrationCredentials.getSSMSConnection();
		SSMSDataMigrationUtils sdm = new SSMSDataMigrationUtils();
		try{
			sdm.SSMSDatabaseManagerAgent(con);
		} catch (Exception e) {   
			e.printStackTrace();
		}
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