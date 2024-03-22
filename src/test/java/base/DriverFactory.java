package base;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

public class DriverFactory {

    private WebDriver driver;

    static {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
        System.setProperty("current.date.time", dateFormat.format(new Date()));
    }

    public static class Log {
        //Initialize Log4j instance
        private static Logger Log = Logger.getLogger(DriverFactory.Log.class.getName());

        //Info Level Logs
        public static void info(String message) {
            Log.info(message);
        }

        //Error Level Logs
        public static void error(String message) {
            Log.error(message);
        }
    }


    public WebDriver getDriver() throws IOException {
            DOMConfigurator.configure("log4j.xml");
            ChromeOptions options = new ChromeOptions();
            options.addArguments("start-maximized");
            options.addArguments("enable-automation");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-infobars");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-browser-side-navigation");
            options.addArguments("--disable-gpu");
            driver = new ChromeDriver();
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        return driver;
    }


    public void tearDown() throws InterruptedException, IOException, ParseException {
        System.out.println("After Process");
        Thread.sleep(2000);
        killChromeDriver();
    }

    /**
     * kill ChromeDriver
     */
    private void killChromeDriver() throws IOException {
        String cmd;
        cmd = "taskkill /f /t /IM chromedriver.exe";
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
