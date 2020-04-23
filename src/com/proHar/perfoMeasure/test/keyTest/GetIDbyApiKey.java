package com.proHar.perfoMeasure.test.keyTest;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import com.proHar.perfoMeasure.main.LightHouse;
import com.proHar.perfoMeasure.main.exceptions.NoApplicationAccessException;
import com.proHar.perfoMeasure.main.exceptions.NoExistingTestCaseOrApplicationNameFoundLightHouseException;

public class GetIDbyApiKey {

	public static LightHouse ap = new LightHouse();

	public static void main(String[] args) throws NoExistingTestCaseOrApplicationNameFoundLightHouseException, NoApplicationAccessException {
		
		
		ap.SetApiKey("68638YkNPCpk7YOWU9IoUcbv8472");
		// 2. set CHROMEDRIVER path
		System.setProperty("webdriver.chrome.driver","E:\\Jars\\ChromeDriver-81\\chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		driver.manage().window().maximize();
		String baseUrl = "https://www.johnhancock.com/index.html";
		driver.get(baseUrl);

		ap.Performer(driver,"John30", "JH");

		driver.get("https://www.johnhancock.com/investing.html");
		ap.Performer(driver,"John10", "JH");
		driver.quit();


	}

}
