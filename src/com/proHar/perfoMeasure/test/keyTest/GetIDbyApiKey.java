package com.proHar.perfoMeasure.test.keyTest;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import com.proHar.perfoMeasure.main.LightHouse;
import com.proHar.perfoMeasure.main.exceptions.NoApplicationAccessException;
import com.proHar.perfoMeasure.main.exceptions.NoExistingTestCaseOrApplicationNameFoundLightHouseException;

public class GetIDbyApiKey {

	public static LightHouse ap = new LightHouse();

	public static void main(String[] args) throws NoExistingTestCaseOrApplicationNameFoundLightHouseException, NoApplicationAccessException, InterruptedException {
		
		
		ap.SetApiKey("53293tSkfLcqi04NAxUYrRbN169");
		// 2. set CHROMEDRIVER path
		System.setProperty("webdriver.chrome.driver","E:\\Jars\\ChromeDriver-81\\chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		driver.manage().window().maximize();
		String baseUrl ="https://www.johnhancock.com/index.html#search-link"; 
			    
;
		driver.get(baseUrl);

		ap.Performer(driver,"John3", "JH");

		driver.get("https://www.johnhancock.com/index.html");
		ap.Performer(driver,"John4", "JH");
		driver.quit();


	}

}
