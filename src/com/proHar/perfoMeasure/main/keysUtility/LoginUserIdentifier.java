package com.proHar.perfoMeasure.main.keysUtility;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.proHar.perfoMeasure.main.queries.QueriesLibrary;
import com.proHar.perfoMeasure.main.ssmsModules.SSMSDataMigrationCredentials;

public class LoginUserIdentifier {

	private static int FindUserId;

	public static int getFindUserId(String api) {
				
		String getLoginId = QueriesLibrary.findUserLogin(api);
		try (Connection connection = SSMSDataMigrationCredentials.getSSMSConnection()) {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(getLoginId);

			while (result.next()) {
				FindUserId = result.getInt("Reg_UserID");               
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}

		return FindUserId;

	}

}
