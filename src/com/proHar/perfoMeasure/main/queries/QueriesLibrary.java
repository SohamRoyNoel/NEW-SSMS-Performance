package com.proHar.perfoMeasure.main.queries;

public class QueriesLibrary {

	// AccessDatabaseManagerAgent

	// Navigations
	public static String insertIntoNavigationTable = "insert into Navigation_Master (Nav_TS_ID,Nav_Application_ID,Nav_Page_ID,Nav_Reg_ID,Nav_UnloadEvent,Nav_RedirectEvent,Nav_AppCache,Nav_TTFB,Nav_Processing,Nav_DomInteractive,Nav_DomComplete,Nav_ContentLoad,Nav_PageLoad,Nav_EntrySyetemTimes) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
	public static String getIdOfLastNavigation(int appId, int pageId, int testCaseId, int UId) {
		String getNavId = "select top 1 Nav_ID from Navigation_Master where Nav_TS_ID="+testCaseId+" and Nav_Application_ID="+ appId + " and Nav_Page_ID="+pageId+" and Nav_Reg_ID="+UId+" order by Nav_ID desc";
		return getNavId;
	}
	public static String insertIntoNavigationHistory = "INSERT INTO Navigation_History(Nav_TS_ID,Nav_Application_ID,Nav_Page_ID,Nav_Reg_ID,Nav_UnloadEvent,Nav_RedirectEvent,Nav_AppCache,Nav_TTFB,Nav_Processing,Nav_DomInteractive,Nav_DomComplete,Nav_ContentLoad,Nav_PageLoad,Nav_EntrySyetemTimes) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?); ";

	
	public static String insertToHistory = "insert into Resource_History (TestScenarioID,TestScenarioID,Page_ID,Res_ElementName,Res_Duration,Res_DateTimes,Res_StartTimes,Res_EndTimes) values (?,?,?,?,?,?,?,?)";
	public static String moveResToHistory = "SELECT * INTO Resource_History FROM Resources;";
	public static String deleteFromEntries = "DELETE FROM Resources";
	public static String deleteReferencialConstrains = "ALTER TABLE Resource_History DROP CONSTRAINT ResourcesResource_History";
	public static String AskApplicationMaster = "select * from Application_Master";
	
	// TestScenario_Master
	public static String insertIntoTestScenario_Master = "insert into TestScenario_Master (TS_Name, TS_Application_ID, TS_Reg_UserID, TS_CreationTime) values (?,?,?,?);";
	public static String getTestScenario_Master(String testScenarioName) {
		String r = "select * from TestScenario_Master where TS_Name = '" + testScenarioName + "';";
		return r;
	}
	public static String updateTestScenario_Master(String testScenarioName, int userId) {
		String r = "update TestScenario_Master set TS_Reg_UserID="+userId+", TS_CreationTime=GETDATE() where TS_Name='"+testScenarioName+"'";
		return r;
	}
	// Checking Existing Test Cases
	public static String authQuery() {
		String authenticationTest = "select * from TestScenario_Master";
		return authenticationTest;
	}
	public static String insertIntoTestScenario_Master_History = "insert into TestScenario_Master_History(TS_U_TS_ID,TS_U_TS_Name,TS_U_TS_Application_ID,TS_U_TS_Reg_UserID,TS_U_Status,TS_U_CreationTime) values(?,?,?,?,?,?) ";
	
	// page Table
	public static String getPages = "select * from Page_Master";
	public static String setPages = "insert into Page_Master (Page_Name) values (?);";
	public static String getPageId(String pageName) {
		String getPageId = "select * from Page_Master where Page_Name = '" + pageName + "';";
		return getPageId;
	}
	
	// Resources 
	public static String ifResourceExists(String resName) {
		String rn = "select * from Resource_Master where Res_Name='"+resName+"'";
		return rn;
	}
	public static String insertIntoResourceMasterTable = "insert into Resource_Master (Res_Name,Res_Reg_ID) values (?,?)";
	public static String getResourceId(String resName) {
		String getPageId = "select * from Resource_Master where Res_Name = '" + resName + "';";
		return getPageId;
	}
	public static String insertIntoResource_Mapper = "insert into Resource_Mapper (RS_Res_ID,RS_Nav_ID,RS_Res_Duration,RS_Res_StartTimes,RS_Res_EndTimes,RS_Res_EntrySyetemTimes) values (?,?,?,?,?,?)";
	public static String moveResourceMapperToResourceMapperHistory = "INSERT INTO Resource_Mapper_History(RS_Map_ID,RS_Res_ID,RS_Nav_ID,RS_Res_Duration,RS_Res_StartTimes,RS_Res_EndTimes,RS_Res_EntrySyetemTimes) SELECT * FROM Resource_Mapper; ";
	public static String deleteResourceMapper = "delete from Resource_Mapper";
	
	
	// Checking if user have access to the application
	public static String hasAccessToApplication() {
		String r = "select * from Application_User_Mapper";
		return r;
	}
	
	public static String findUserLogin(String apiKey) {
		String getUserIDquery = "select Reg_UserID from User_Registration where Reg_API_KEY='"+apiKey+"'";
		return getUserIDquery;
	}
	
	
	/*
	 * Query To find and Drop the Referencial Integrity Constrains(Access):
	 *
	 * SELECT szRelationship FROM Msysrelationships WHERE szObject = 'Resource_History' and szReferencedObject = 'Resources' --> ResourcesResource_History
	 * ALTER TABLE Resource_History DROP CONSTRAINT ResourcesResource_History
	 *
	 * */

}
