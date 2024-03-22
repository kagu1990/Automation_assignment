package utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Utilities {
	
	public static WebDriver driver;
	public static JavascriptExecutor js;
	
	public static WebElement wait_for(String xpath, WaitConditions conditionType, int wait_time) {
		
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(wait_time));
		
		if(conditionType == WaitConditions.PRESENCE) {
			return wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
		}
		if(conditionType == WaitConditions.VISIBLE) {
			return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
		}
		if(conditionType == WaitConditions.CLICKABLE) {
			return wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
		}
		
		return null;
	}
	
	public static List<WebElement> wait_for_elements(String xpath, WaitConditions conditionType, int wait_time) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(wait_time));
		
		if(conditionType == WaitConditions.VISIBLE_ALL) {
			return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath(xpath)));
		}
		return null;
	}
	
	public static boolean wait_for_url(String url, int wait_time) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(wait_time));
		return wait.until(ExpectedConditions.urlToBe(url));
	}
	
	public static boolean wait_for_contain_url(String url, int wait_time) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(wait_time));
		return wait.until(ExpectedConditions.urlContains(url));
	}
	
	public static void scrollToElement(WebElement e) {
		
		js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].scrollIntoView();", e);
	}
	
	public static void selectAllClearAction(WebElement e) {
		
		e.sendKeys(Keys.chord(Keys.CONTROL, "a"));
		e.sendKeys(Keys.BACK_SPACE);
	}
	
	public static String formatData(String date) throws ParseException {

		// Format the web store timestamp to exclude milliseconds (yyyy-MM-dd HH:mm:ss)
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  // Adjust for specific format (replace .SSS with your pattern if needed)
		Date webStoreDate = format.parse(date);
		String formattedWebStoreTimestamp = format.format(webStoreDate);

		// Now you can use formattedWebStoreTimestamp for comparisons or further processing

		System.out.println("Original web store timestamp: " + date);
		System.out.println("Formatted web store timestamp (without milliseconds): " + formattedWebStoreTimestamp);
		
		return formattedWebStoreTimestamp;
	}
}
