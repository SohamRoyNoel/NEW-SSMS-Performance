package com.proHar.perfoMeasure.test.keyTest;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import com.proHar.perfoMeasure.main.LightHouse;
import com.proHar.perfoMeasure.main.exceptions.NoApplicationAccessException;
import com.proHar.perfoMeasure.main.exceptions.NoExistingTestCaseOrApplicationNameFoundLightHouseException;
import com.proHar.perfoMeasure.main.keysUtility.ApiKeyContainer;
import com.proHar.perfoMeasure.main.keysUtility.LoginUserIdentifier;

public class GetIDbyApiKey {

	public static LightHouse ap = new LightHouse();

	public static void main(String[] args) throws NoExistingTestCaseOrApplicationNameFoundLightHouseException, NoApplicationAccessException {

		ap.SetApiKey("5088YhYEFYFV9iaJhM5gQK9o7131");
		// 2. set CHROMEDRIVER path
		System.setProperty("webdriver.chrome.driver","E:\\Jars\\ChromeDriver-79\\chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		driver.manage().window().maximize();
		String baseUrl = "https://www.jhancockpensions.com/do/home_page/pu_0";
		driver.get(baseUrl);

		ap.Performer(driver,"TCs23", "JOHN");

		driver.get("https://www.johnhancock.com/index.html");
		ap.Performer(driver,"TCs24", "JOHNC");
		driver.quit();




	}

}
