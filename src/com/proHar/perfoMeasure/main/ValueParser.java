package com.proHar.perfoMeasure.main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import com.proHar.perfoMeasure.main.queries.QueriesLibrary;
import com.proHar.perfoMeasure.main.ssmsModules.SSMSDataMigrationCredentials;
import com.proHar.perfoMeasure.main.ssmsModules.SSMSUtils;

public class ValueParser {
	public static ArrayList<String> navHolder = new ArrayList<String>();
	public static ArrayList<String> resHolder = new ArrayList<String>();

	// Resource Status
	@SuppressWarnings("unused")
	public static void ResourceAnalyser(WebDriver driver, int UId, int navId) throws InterruptedException{

		String encodedBodySize = "";
		String entryType = "";
		String responseEnd = "";
		String workerStart = "";
		String responseStart = "";
		String domainLookupEnd = "";
		String domainLookupStart = "";
		String redirectEnd = "";
		String decodedBodySize = "";
		String duration = "";
		String transferSize = "";
		String redirectStart = "";
		String connectEnd = "";
		String connectStart = "";
		String requestStart = "";
		String secureConnectionStart = "";
		String name = "";

		String startTime = "";
		String fetchStart = "";
		String serverTiming = "";
		String nextHopProtocol = "";
		String initiatorType = "";

		String appName = "";
		String createValueString = "";
		String baseURL="";

		String[] restemp;
		int nRowsInserted = 0;

		Thread.sleep(3000);
		baseURL = driver.getCurrentUrl();
		//         appName = base;
		JavascriptExecutor js =(JavascriptExecutor)driver;
		int Counter = Integer.parseInt(js.executeScript("return window.performance.getEntriesByType('resource').length").toString());

		// All Timings
		try{
			for (int i = 1; i < Integer.parseInt(js.executeScript("return window.performance.getEntriesByType('resource').length").toString()); i++) {
				String allStatusString = js.executeScript("return window.performance.getEntriesByType('resource')["+i+"]").toString().replace('{', ' ').replace('}', ' ');
				String[] keyValPairs = allStatusString.split(",");
				Map<String, String> elementHolder = new HashMap<String, String>();
				String keyForSubMap = "Element";
				int mapperCounter = i;
				for (String str : keyValPairs) {
					String[] key = str.split("=");
					try{
						elementHolder.put(key[0].trim(),  key[1].trim());
					} catch (ArrayIndexOutOfBoundsException e) {                     

					}
				}

				// Check if all values are present

				if (elementHolder.get("connectStart") != null && elementHolder.get("domainLookupEnd") != null && elementHolder.get("startTime") != null && elementHolder.get("domainLookupStart") != null && elementHolder.get("connectEnd") != null && elementHolder.get("responseStart") != null && elementHolder.get("requestStart") != null && elementHolder.get("responseEnd") != null && elementHolder.get("name") != null && elementHolder.get("secureConnectionStart") != null && elementHolder.get("duration") != null) {
					// Resource Value Determiner
					Double blocked = Math.max(0, (Double.parseDouble(elementHolder.get("connectStart"))-Double.parseDouble(elementHolder.get("startTime"))));
					Double dns = (Double.parseDouble(elementHolder.get("domainLookupEnd"))-Double.parseDouble(elementHolder.get("domainLookupStart"))) == 0 ? -1 : (Double.parseDouble(elementHolder.get("domainLookupEnd"))-Double.parseDouble(elementHolder.get("domainLookupStart")));
					Double connect = (Double.parseDouble(elementHolder.get("connectEnd"))-Double.parseDouble(elementHolder.get("connectStart"))) == 0 ? -1 : (Double.parseDouble(elementHolder.get("connectEnd"))-Double.parseDouble(elementHolder.get("connectStart")));
					Double send = Math.max(0, Double.parseDouble(elementHolder.get("responseStart"))-Double.parseDouble(elementHolder.get("requestStart")));
					Double receive = Math.max(0, Double.parseDouble(elementHolder.get("responseEnd"))-Double.parseDouble(elementHolder.get("responseStart")));
					Double ssl = -1d;

					String elementName = elementHolder.get("name").toString();

					try {
						if (elementHolder.get("name") != null && elementHolder.get("name").toLowerCase().startsWith("https:")) {
							ssl = (double)Math.round(Double.parseDouble(elementHolder.get("secureConnectionStart")) == 0 ? -1 : (Double.parseDouble(elementHolder.get("connectEnd")) - Double.parseDouble(elementHolder.get("secureConnectionStart"))));
						}
					} catch (Exception e) {   }
					Double time = Double.parseDouble(elementHolder.get("duration"));
					long wait = Math.round(time) - Math.round(Math.max(0, dns)) - Math.round(Math.max(0, connect)) - Math.round(Math.max(0, ssl)) - Math.round(send) - Math.round(receive) - Math.round(blocked);
					if (wait < 0) {
						time -= wait;
						wait = 0;
					}
					double durations = time;

					// Getting the Total Value
					Date date = new Date();
					DateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
					String stringDate = sdf.format(date);

					// System Date time
					Calendar cal = Calendar.getInstance(); 
					java.sql.Timestamp timestamp = new java.sql.Timestamp(cal.getTimeInMillis());

					// get the resource name
					String resource_Name = elementHolder.get("name").toString();
					/*
					 * insert into To Resource_Master or Get Res_ID if the element is present
					 * */
					int RS_Res_Id = SSMSUtils.getResourceId(resource_Name, UId);
					/*
					 * Create the strings and adding it to The List
					 * */
					createValueString = RS_Res_Id+","+navId+","+durations+","+stringDate+","+timestamp+System.lineSeparator();

					resHolder.add(createValueString);
				}
				/*
				 * Insert into Resource_Mapper
				 * */
				List<String> resourceValues = ValueParser.resHolder;
				try (Connection connection = SSMSDataMigrationCredentials.getSSMSConnection()) {
					for (String nav : resourceValues) {
						PreparedStatement preparedStatement = connection.prepareStatement(QueriesLibrary.insertIntoResource_Mapper);
						restemp = nav.split(",");
						preparedStatement.setInt(1, Integer.parseInt(restemp[0])); // RS_Res_ID
						preparedStatement.setInt(2, Integer.parseInt(restemp[1])); // RS_Nav_ID
						preparedStatement.setString(3,  (restemp[2])); // RS_Res_Duration
						
						Calendar cal = Calendar.getInstance(); 
						java.sql.Timestamp timestamp = new java.sql.Timestamp(cal.getTimeInMillis());
						preparedStatement.setTimestamp(4, timestamp); // Res_StartTimes
						preparedStatement.setNull(5, java.sql.Types.DATE); // Res_EndTimes
						java.sql.Timestamp timestamps = new java.sql.Timestamp(cal.getTimeInMillis());
						preparedStatement.setTimestamp(6, timestamps); // RS_Res_EntrySyetemTimes
						nRowsInserted += preparedStatement.executeUpdate();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				/*
				 * Clears the list for next iteration
				 * */
				resHolder.clear();
			}
		} catch (Exception e) {   }
	}

	// Navigation Status
	@SuppressWarnings("unused")
	public static int NavigationAnalyser(WebDriver driver, int appId, int pageId, int testCaseId, int UId) throws InterruptedException{
		// 32 Common fields in case of NAVIGATION
		String redirectCount="";
		String encodedBodySize="";
		String unloadEventEnd="";
		String responseEnd="";
		String domainLookupEnd="";
		String unloadEventStart="";
		String domContentLoadedEventStart="";
		String type="";
		String decodedBodySize="";
		String duration="";
		String redirectStart="";
		String connectEnd="";
		String requestStart="";
		String startTime="";
		String fetchStart="";
		String serverTiming="";
		String domContentLoadedEventEnd="";
		String entryType="";
		String workerStart="";
		String responseStart="";
		String domInteractive="";
		String domComplete="";
		String domainLookupStart="";
		String redirectEnd="";
		String transferSize="";
		String connectStart="";
		String loadEventStart="";
		String secureConnectionStart="";
		String name="";
		String nextHopProtocol="";
		String initiatorType="";

		String loadEventEnd="";

		String appName = "";
		String createValueString = "";
		String baseURL="";


		// value calculation
		double Unload;
		double Redirect;
		double AppCache;

		double TTFB;
		double Processing;
		double Dom_Interactive;
		double Dom_Complete;
		double Content_load;
		double Page_load;
		Thread.sleep(3000);

		String[] navtemp;
		int nRowsInserted = 0;
		int Nav_IDs = 0;

		baseURL = driver.getCurrentUrl();
		//         appName = base;
		JavascriptExecutor js =(JavascriptExecutor)driver;

		// Navigation Timings
		String navigationStatusString = js.executeScript("return window.performance.getEntries()["+0+"]").toString();

		// get Navigation value string length
		int length = navigationStatusString.length();
		// get String without {}
		String newString = navigationStatusString.substring(1, navigationStatusString.length()-1);

		// put the string in map
		String[] keyValPairs = newString.split(",");
		Map<String,String> navigationValMap = new HashMap<>();
		// Push to map
		for (String str : keyValPairs) {
			String[] key = str.split("=");
			navigationValMap.put(key[0].trim(), key[1].trim());
		}

		// Values from Entries-Navigation
		redirectCount = navigationValMap.get("redirectCount");
		encodedBodySize = navigationValMap.get("encodedBodySize");
		unloadEventEnd = navigationValMap.get("unloadEventEnd");
		responseEnd = navigationValMap.get("responseEnd");

		domainLookupEnd = navigationValMap.get("domainLookupEnd");
		unloadEventStart = navigationValMap.get("unloadEventStart");
		domContentLoadedEventStart = navigationValMap.get("domContentLoadedEventStart");
		type = navigationValMap.get("type");
		decodedBodySize = navigationValMap.get("decodedBodySize");
		duration = navigationValMap.get("duration");
		redirectStart = navigationValMap.get("redirectStart");

		connectEnd = navigationValMap.get("connectEnd");
		requestStart = navigationValMap.get("requestStart");
		startTime = navigationValMap.get("startTime");
		fetchStart = navigationValMap.get("fetchStart");
		serverTiming = navigationValMap.get("serverTiming");
		domContentLoadedEventEnd = navigationValMap.get("domContentLoadedEventEnd");
		entryType = navigationValMap.get("entryType");
		workerStart = navigationValMap.get("workerStart");
		responseStart = navigationValMap.get("responseStart");
		domInteractive = navigationValMap.get("domInteractive");
		domComplete = navigationValMap.get("domComplete");
		domainLookupStart = navigationValMap.get("domainLookupStart");
		redirectEnd = navigationValMap.get("redirectEnd");
		transferSize = navigationValMap.get("transferSize");
		connectStart = navigationValMap.get("connectStart");
		loadEventStart = navigationValMap.get("loadEventStart");
		secureConnectionStart = navigationValMap.get("secureConnectionStart");
		name = navigationValMap.get("name");

		nextHopProtocol = navigationValMap.get("nextHopProtocol");
		initiatorType = navigationValMap.get("initiatorType");
		loadEventEnd = navigationValMap.get("loadEventEnd");

		// Event Counter
		try{
			double Onload = Double.parseDouble(loadEventEnd)-Double.parseDouble(loadEventStart);
			double OnContentload = Double.parseDouble(domContentLoadedEventEnd)-Double.parseDouble(domContentLoadedEventStart);
			double domloading = Double.parseDouble(domInteractive)-Double.parseDouble(responseEnd);

			// Calculation
			Unload = Double.parseDouble(unloadEventEnd)-Double.parseDouble(unloadEventStart);
			Redirect = Double.parseDouble(redirectEnd)-Double.parseDouble(redirectStart);
			AppCache = Double.parseDouble(domainLookupStart)-Double.parseDouble(fetchStart);

			TTFB = Double.parseDouble(responseStart)-Double.parseDouble(connectEnd);
			Processing = Double.parseDouble(loadEventStart)-Double.parseDouble(responseEnd);
			Dom_Interactive = Double.parseDouble(domInteractive)-domloading;
			Dom_Complete = Double.parseDouble(domComplete)-domloading;
			Content_load = OnContentload;

			Page_load = Onload;

			Date date = new Date();
			DateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
			String stringDate = sdf.format(date);

			// System date-time
			Calendar cal = Calendar.getInstance(); 
			java.sql.Timestamp timestamp = new java.sql.Timestamp(cal.getTimeInMillis());
			createValueString = testCaseId+","+appId+","+pageId+","+UId+","+Unload+","+Redirect+","+AppCache+","+TTFB+","+Processing+","+Dom_Interactive+","+Dom_Complete+","+Content_load+","+Page_load+","+stringDate+","+timestamp+System.lineSeparator();

			// Added To the list
			//navHolder.add(createValueString);
			// Push To DB
			try (Connection connection = SSMSDataMigrationCredentials.getSSMSConnection()) {
				PreparedStatement preparedStatement = connection.prepareStatement(QueriesLibrary.insertIntoNavigationTable);
				navtemp = createValueString.split(",");
				preparedStatement.setInt(1, Integer.parseInt(navtemp[0])); // TestScenarioID
				preparedStatement.setInt(2, Integer.parseInt(navtemp[1])); // Application_ID
				preparedStatement.setInt(3, Integer.parseInt(navtemp[2])); // Page_ID
				preparedStatement.setInt(4, Integer.parseInt(navtemp[3])); // User_ID
				preparedStatement.setString(5, navtemp[4]); // Nav_UnloadEvent
				preparedStatement.setString(6, navtemp[5]); // Nav_RedirectEvent
				preparedStatement.setString(7, navtemp[6]); // Nav_AppCache
				preparedStatement.setString(8, navtemp[7]); // Nav_TTFB
				preparedStatement.setString(9, navtemp[8]); // Nav_Processing
				preparedStatement.setString(10, navtemp[9]); // Nav_DomInteractive
				preparedStatement.setString(11, navtemp[10]); // Nav_DomComplete
				preparedStatement.setString(12, navtemp[11]); // Nav_ContentLoad
				preparedStatement.setString(13, navtemp[12]); // Nav_PageLoad
				java.sql.Timestamp timestamps = new java.sql.Timestamp(cal.getTimeInMillis());
				preparedStatement.setTimestamp(14, timestamps); // Nav_DateTimes
				nRowsInserted += preparedStatement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			// get The Nav Id for recently added Row
			Nav_IDs = SSMSUtils.getRecentNavigationID(appId, pageId, testCaseId, UId);

		}catch (NullPointerException e) {
			System.out.println("Some Fields Has No Value for " + driver.getCurrentUrl());

		}

		return Nav_IDs;
	}
}



