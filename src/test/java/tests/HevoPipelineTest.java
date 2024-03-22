package tests;

import static org.testng.Assert.assertEquals;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;

import org.json.simple.parser.ParseException;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.api.client.json.Json;

import DBActions.DatabaseActions;
import DBActions.JsonReader;
import base.DriverFactory;
import lombok.experimental.UtilityClass;
import utilities.FilePathConstants;
import utilities.Utilities;
import utilities.WaitConditions;

public class HevoPipelineTest {

	WebDriver driver;
	DriverFactory driverFactory;
	WaitConditions wait_type;
	DateTimeFormatter dtf;
	LocalDateTime now;

	String pipelineName = null;
	String destName = null;

	@BeforeTest
	public void init() throws IOException, ClassNotFoundException, SQLException, ParseException {

		driverFactory = new DriverFactory();
		driver = driverFactory.getDriver();
		Utilities.driver = driver;
		dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		DatabaseActions dbAction = new DatabaseActions();
		String host = JsonReader.getDataFromJson(FilePathConstants.dbconfig, "host");
		String username = JsonReader.getDataFromJson(FilePathConstants.dbconfig, "username");
		String password = JsonReader.getDataFromJson(FilePathConstants.dbconfig, "password");
		System.out.println(host + username + password);
		dbAction.connectToDatabase(host, username, password);
		dbAction.createTable();
	}

	@Test
	public void createPipeline()
			throws InterruptedException, FileNotFoundException, IOException, ParseException, AWTException {

		driver.get(JsonReader.getDataFromJson(FilePathConstants.config, "url"));

		WebElement emailField = driver.findElement(By.xpath("//input[@placeholder='name@company.com']"));
		emailField.sendKeys(JsonReader.getDataFromJson(FilePathConstants.config, "username"));

		WebElement continueBtn = driver.findElement(By.xpath("//button//span[normalize-space()='Continue']"));
		continueBtn.click();

		WebElement pswdField = driver.findElement(By.xpath("//input[@id='password']"));
		pswdField.sendKeys(JsonReader.getDataFromJson(FilePathConstants.config, "password"));
		WebElement logInBtn = driver.findElement(By.xpath("//button[text()='Log In']"));
		logInBtn.click();
		boolean visible = true;

		while (visible) {
			try {
				visible = Utilities.wait_for("//div[text()='Invalid Token.']", wait_type.VISIBLE, 7).isDisplayed();
				logInBtn.click();
			} catch (Exception e) {
				visible = false;
			}
		}

		// To close the tour or tutorial box if its visible after signing in
		try {
			Utilities.wait_for("//div[contains(@class, 'tourBox')]//button[@data-id='product-tour-close-icon-button']",
					wait_type.CLICKABLE, 20).click();
		} catch (TimeoutException e) {

		}

		try {
			// Clicking on create pipeline button on main page
			Utilities.wait_for("//button[text()=' Create Pipeline ']", wait_type.CLICKABLE, 5).click();
			System.err.println("Main add pipeline button not visible");
		} catch (TimeoutException e) {
			System.err.println("Clicking on sidebar menu");
			// Clicking on sidebar pipeline button
			WebElement pipelineBtn = driver.findElement(By.xpath("//div[@id='product-tour-pipeline']"));
			pipelineBtn.click();
			// Wait for Pipeline drawer screen to shown
			Utilities.wait_for("//div[@id='product-tour-pipeline']", wait_type.VISIBLE, 5);
			Utilities.wait_for("//div[@class='drawer-header']//div[text()='Pipelines']", wait_type.VISIBLE, 2);

			// Click on Create pipeline button
			driver.findElement(By.xpath("//button[normalize-space()='Create']")).click();
		}

		// Waiting for Select Source Type screen to show
		Utilities.wait_for_url(JsonReader.getDataFromJson(FilePathConstants.pageUrl, "sourcetype"), 5);

		// Clicking on MYSQL source button
		driver.findElement(By.xpath("//button[@data-id='mysql-tile-wrapper-button']")).click();

		// Waiting for Configure source screen to show
		Utilities.wait_for_url(JsonReader.getDataFromJson(FilePathConstants.pageUrl, "configsource"), 10);

		configureSource();
		addDestination();
		verifyData();
	}

	public void configureSource() throws FileNotFoundException, IOException, ParseException {

		WebElement pipelineNameTF = Utilities.wait_for("//input[@id='sourceName']", wait_type.CLICKABLE, 5);
		Utilities.selectAllClearAction(pipelineNameTF);
		now = LocalDateTime.now();
		pipelineName = "Automated test pipeline " + dtf.format(now);
		pipelineNameTF.sendKeys(pipelineName);

		WebElement hostField = driver.findElement(By.xpath("//input[@id='host']"));
		Utilities.selectAllClearAction(hostField);
		hostField.sendKeys(JsonReader.getDataFromJson(FilePathConstants.pipelineConfig, "host"));

		WebElement portField = driver.findElement(By.xpath("//input[@id='port']"));
		Utilities.selectAllClearAction(portField);
		String port = JsonReader.getDataFromJson(FilePathConstants.pipelineConfig, "port");
		System.err.println(port);
		portField.sendKeys(JsonReader.getDataFromJson(FilePathConstants.pipelineConfig, "port"));

		WebElement dbUserField = driver.findElement(By.xpath("//input[@id='user']"));
		Utilities.selectAllClearAction(dbUserField);
		dbUserField.sendKeys(JsonReader.getDataFromJson(FilePathConstants.pipelineConfig, "dbuser"));

		WebElement dbPswd = driver.findElement(By.xpath("//input[@id='password']"));
		Utilities.selectAllClearAction(dbPswd);
		dbPswd.sendKeys(JsonReader.getDataFromJson(FilePathConstants.pipelineConfig, "dbpass"));

		// See more button on select an Ingestion mode section
		WebElement seeMoreBtn = driver
				.findElement(By.xpath("//div[text()= 'Select an Ingestion Mode']//following-sibling::button"));
		Utilities.scrollToElement(seeMoreBtn);
		seeMoreBtn.click();

		WebElement ingestionModeRb = driver.findElement(By.xpath("//label[@data-id='job-mode-table-radio']"));
		Utilities.scrollToElement(ingestionModeRb);
		ingestionModeRb.click();

		driver.findElement(By.xpath("//input[@id='databaseName']"))
				.sendKeys(JsonReader.getDataFromJson(FilePathConstants.pipelineConfig, "dbname"));

		// Click on test connection button
		WebElement testConnectionBtn = driver.findElement(By.xpath("//button[text()= 'Test Connection']"));
		Utilities.scrollToElement(testConnectionBtn);
		testConnectionBtn.click();

		// Wait for connection successsful message to appear
		Utilities.wait_for("//div[text()= 'Connection successful']", wait_type.VISIBLE, 10);

		// Click on test and continue
		driver.findElement(By.xpath("//button[text()= 'Test & Continue']")).click();

		Utilities.wait_for_url(JsonReader.getDataFromJson(FilePathConstants.pageUrl, "selectobject"), 10);

		// Click on continue button
		driver.findElement(By.xpath("//button[text()='Continue']")).click();

		boolean result = Utilities
				.wait_for_url(JsonReader.getDataFromJson(FilePathConstants.pageUrl, "configureobject"), 10);
		System.err.println(result);

		// Query mode dd button
		WebElement dd = driver.findElement(By.xpath("//input[contains(@data-id,'mode-dropdown-input')]"));
		Utilities.selectAllClearAction(dd);
		dd.sendKeys("Full Load");
		dd.sendKeys(Keys.DOWN);
		dd.sendKeys(Keys.ENTER);

		// Click on continue button
		driver.findElement(By.xpath("//button[text()='Continue']")).click();
	}

	public void addDestination() throws FileNotFoundException, IOException, ParseException {

		Utilities.wait_for_url(JsonReader.getDataFromJson(FilePathConstants.pageUrl, "selectdest"), 10);
		driver.findElement(By.xpath("//button[@data-id='destination-list-add-new-button']")).click();

		Utilities.wait_for_url(JsonReader.getDataFromJson(FilePathConstants.pageUrl, "desttype"), 10);
		// Clicking on postgres button as destination
		try {
			WebElement postgresTab = Utilities.wait_for("//h5[normalize-space()='PostgreSQL']", wait_type.CLICKABLE,
					10);
			postgresTab.click();
		} catch (ElementClickInterceptedException e) {
			WebElement postgresTab = Utilities.wait_for("//h5[normalize-space()='PostgreSQL']", wait_type.CLICKABLE,
					10);
			postgresTab.click();
		}

		// Entering destination name
		WebElement destinamationNameTextField = driver.findElement(By.id("destinationName"));
		Utilities.selectAllClearAction(destinamationNameTextField);

		now = LocalDateTime.now();
		destinamationNameTextField.sendKeys(
				JsonReader.getDataFromJson(FilePathConstants.destinationConfig, "destname") + dtf.format(now));

		// Entering host
		WebElement hostTextField = driver.findElement(By.id("host"));
		Utilities.selectAllClearAction(hostTextField);
		hostTextField.sendKeys(JsonReader.getDataFromJson(FilePathConstants.destinationConfig, "host"));

		// Entering port number
		WebElement portTextField = driver.findElement(By.id("port"));
		Utilities.selectAllClearAction(portTextField);
		portTextField.sendKeys(JsonReader.getDataFromJson(FilePathConstants.destinationConfig, "port"));

		// Entering database user
		WebElement databaseuserTextField = driver.findElement(By.id("user"));
		Utilities.selectAllClearAction(databaseuserTextField);
		databaseuserTextField.sendKeys(JsonReader.getDataFromJson(FilePathConstants.destinationConfig, "dbuser"));

		// Entering database password
		WebElement passwordTextField = driver.findElement(By.id("password"));
		Utilities.selectAllClearAction(passwordTextField);
		passwordTextField.sendKeys(JsonReader.getDataFromJson(FilePathConstants.destinationConfig, "dbpass"));

		// Entering database name
		WebElement databasenameTextField = driver.findElement(By.id("databaseName"));
		Utilities.selectAllClearAction(databasenameTextField);
		databasenameTextField.sendKeys(JsonReader.getDataFromJson(FilePathConstants.destinationConfig, "dbname"));

		// Entering schema name
		WebElement schemaNameTextField = driver.findElement(By.id("schemaName"));
		Utilities.selectAllClearAction(schemaNameTextField);
		schemaNameTextField.sendKeys(JsonReader.getDataFromJson(FilePathConstants.destinationConfig, "schema"));

		// Searching for test connection button
		WebElement testConnectionBtn = driver.findElement(By.xpath("//button[normalize-space()='Test Connection']"));
		testConnectionBtn.click();

		Utilities.wait_for("//div[text()= 'Connection successful']", wait_type.VISIBLE, 10);

		// Clicking on save and continue button
		WebElement saveBtn = driver.findElement(By.xpath("//button[@data-id='destination-config-save-button']"));
		saveBtn.click();

		// Waiting for final settings page to show
		Utilities.wait_for_url(JsonReader.getDataFromJson(FilePathConstants.pageUrl, "finalsettingpipeline"), 10);

		driver.findElement(By.xpath("//button[normalize-space()='30 Mins']")).click();

		driver.findElement(By.xpath("//button[normalize-space()='Continue']")).click();
	}

	public void verifyData() throws FileNotFoundException, IOException, ParseException, AWTException {

		Utilities.wait_for_contain_url(JsonReader.getDataFromJson(FilePathConstants.pageUrl, "pipelineoverview"), 10);

		destName = driver.findElement(By
				.xpath("//div[@class='integration-node destination-node']//span[@class='node-name-container']//div[1]"))
				.getText();

		Utilities.wait_for("//span[text()='Introducing Roles on Hevo!']", wait_type.VISIBLE, 30);
		driver.findElement(By.xpath("//span[@class='hevo-icon hevo-close icon-size-3 text-light ']")).click();
		
		// Clicking on the destination icon in the side panel
		WebElement destinationIcon = driver.findElement(By.id("product-tour-destination"));
		destinationIcon.click();

		// Searching for the destination
		WebElement searchBoxTextField = driver.findElement(By.xpath("//input[@placeholder='Search Destinations']"));
		searchBoxTextField.sendKeys(destName);
		searchBoxTextField.sendKeys(Keys.ENTER);

		// Clicking on the workbech
		WebElement workbenchBtn = driver.findElement(By.xpath("//a[@data-id='workbench']"));
		workbenchBtn.click();

		Utilities.wait_for("//span[@class='hevo-icon hevo-resize']", wait_type.CLICKABLE, 20);
		// Inserting a query
		WebElement queryTextEditer = driver.findElement(By.xpath("(//div[contains(@class,'CodeMirror-scroll')])[2]"));

		Actions action = new Actions(driver);

		action.moveToElement(queryTextEditer).click().perform();
		
		Robot robot = new Robot();

		String text = "SELECT * FROM automation_employees;";
		StringSelection stringSelection = new StringSelection(text);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, stringSelection);

		robot.keyPress(KeyEvent.VK_CONTROL);
		robot.keyPress(KeyEvent.VK_V);
		robot.keyRelease(KeyEvent.VK_V);
		robot.keyRelease(KeyEvent.VK_CONTROL);
		
		WebElement runQueryBtn = driver.findElement(By.xpath("(//button[@data-testid='hd-button'])[4]"));
		runQueryBtn.click();

	}

	public void dropTables() throws SQLException, AWTException, InterruptedException {

		DatabaseActions.dropTable();
		WebElement queryTextEditer = driver.findElement(By.xpath("(//div[contains(@class,'CodeMirror-scroll')])[2]"));

		Actions action = new Actions(driver);

		action.moveToElement(queryTextEditer).click().perform();
		// Drop destination table
		String text = "DROP TABLE automation_employees;";
		StringSelection stringSelection = new StringSelection(text);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, stringSelection);

		Robot robot = new Robot();
		robot.keyPress(KeyEvent.VK_CONTROL);
		robot.keyPress(KeyEvent.VK_A);
		robot.keyRelease(KeyEvent.VK_CONTROL);
		robot.keyRelease(KeyEvent.VK_A);
		robot.keyPress(KeyEvent.VK_BACK_SPACE);
		robot.keyRelease(KeyEvent.VK_BACK_SPACE);
		Thread.sleep(2000);
		robot.keyPress(KeyEvent.VK_CONTROL);
		robot.keyPress(KeyEvent.VK_V);
		robot.keyRelease(KeyEvent.VK_V);
		robot.keyRelease(KeyEvent.VK_CONTROL);

		WebElement runQueryBtn = driver.findElement(By.xpath("(//button[@data-testid='hd-button'])[4]"));
		runQueryBtn.click();

		Utilities.wait_for("//span//b[text()='Operation Successful']", wait_type.VISIBLE, 60);
	}

	public void verifySourceAndDestinationData() throws SQLException, java.text.ParseException {
		boolean notDisplayed = false;
		// Wait for query result to show up
		while (!notDisplayed) {
			try {
				Utilities.wait_for("//div[text()= 'Query Results']", wait_type.VISIBLE, 20);
				notDisplayed = true;
			} catch (TimeoutException e) {
				notDisplayed = false;
				WebElement runQueryBtn = driver.findElement(By.xpath("(//button[@data-testid='hd-button'])[4]"));
				runQueryBtn.click();
			}
		}

		try {
			while (driver.findElement(By.xpath("//td[text()= 'No Rows Found']")).isDisplayed()) {
				WebElement runQueryBtn = driver.findElement(By.xpath("(//button[@data-testid='hd-button'])[4]"));
				runQueryBtn.click();
				Utilities.wait_for("//div[text()= 'Query Results']", wait_type.VISIBLE, 30);
			}
		} catch (NoSuchElementException e) {

		}

		List<WebElement> rowData = Utilities.wait_for_elements("//table[@class='data-table']//tbody//tr[1]//div",
				wait_type.VISIBLE_ALL, 10);
		List<String> sourceData = DatabaseActions.getDBData("SELECT * FROM employees;");

		for (int i = 0; i < (rowData.size() - 2); i++) {

			if (i == 2) {
				String date = Utilities.formatData(rowData.get(i).getText());
				assertEquals(date, sourceData.get(i));
			} else {
				System.err.println(rowData.get(i).getText());
				assertEquals(rowData.get(i).getText(), sourceData.get(i));
			}
		}

	}

	public void deletePipeLine() throws AWTException {

		// Clicking on the Pipeline element.
		WebElement pipelineBtn = driver.findElement(By.xpath("//div[@id='product-tour-pipeline']"));
		pipelineBtn.click();
		
		// Searching for the pipeline
		WebElement searchBoxTextField = driver.findElement(By.xpath("//input[@placeholder='Search Pipelines']"));
		searchBoxTextField.sendKeys(pipelineName);
		searchBoxTextField.sendKeys(Keys.ENTER);

		// Clicking on the pause button for the pipeline
		WebElement pauseBtn = driver
				.findElement(By.xpath("//button[@class='btn-outline btn-thumbnail btn-sm btn-secondary']"));
		pauseBtn.click();

		// clicking on the three verticle dots menu
		// (//div[@class='hd-icon-btn-overlay'])[1]
		WebElement actionMenu = driver.findElement(By.xpath("(//div[@class='hd-icon-btn-overlay'])[1]"));
		actionMenu.click();

		// Clicking on the delete option
		WebElement deleteOption = driver.findElement(By.xpath("//hd-menu-item[@iconname='delete']"));
		deleteOption.click();

		Robot robot = new Robot();
		robot.keyPress(KeyEvent.VK_ENTER);
		robot.keyPress(KeyEvent.VK_ENTER);

	}

	public void deleteDestination() {
		Utilities.wait_for("//button[text()=' Create Pipeline ']", wait_type.VISIBLE, 10);
		WebElement pipelineBtn = Utilities.wait_for("//div[@id='product-tour-destination']", wait_type.CLICKABLE, 10);

		pipelineBtn.click();
		// Wait for Pipeline drawer screen to shown
		Utilities.wait_for("//div[@class='drawer-header']//div[text()='Destinations']", wait_type.VISIBLE, 2);
		WebElement searchBoxTextField = driver.findElement(By.xpath("//input[@placeholder='Search Destinations']"));
		searchBoxTextField.sendKeys(destName);
		searchBoxTextField.sendKeys(Keys.ENTER);

		driver.findElement(By.xpath("//span[@class='hevo-icon hevo-more-vertical icon-size-3']")).click();
		driver.findElement(By.xpath("//li[text()='Delete']")).click();
		Utilities.wait_for("//span[@class='hevo-icon hevo-delete']", wait_type.VISIBLE, 5);
		driver.findElement(By.xpath("//button[text()='Yes, delete this Destination']")).click();

	}

	@AfterTest
	public void closeResources() throws InterruptedException, IOException, ParseException, SQLException, AWTException,
			java.text.ParseException {
		verifySourceAndDestinationData();
		dropTables();
		deletePipeLine();
		deleteDestination();
		driverFactory.tearDown();
	}
}
