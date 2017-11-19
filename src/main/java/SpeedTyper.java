import com.google.protobuf.ByteString;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.List;
import java.util.Random;

/**
 * Created by miloshzelembaba on 2017-10-16.
 */
public class SpeedTyper {
    // due to the randomness i incorporated in the typing speed below, this doesn't directly correlate to the desired
    // wpm. increasing this tho will increase the speed.
    final static double desiredWordsPerMinute = 170;
    final static double mistakesProbability = 0.05;

    public static void main(String[] args){
        // Initialize browser
        WebDriver driver=new ChromeDriver();

        // Open typeracer
        driver.get("http://play.typeracer.com/");

        // put this sleep in here to allow the user to log in to their account
        try{
            Thread.sleep(20000);
        } catch (Exception e){
            System.out.println("EXCEPTION");
        }

        while(true) {
            System.out.println("Starting a new round");

            // start the game
            String selectAll = Keys.chord(Keys.ALT, Keys.CONTROL, "i");
            driver.findElement(By.tagName("html")).sendKeys(selectAll);

            // get the gameview table and rows
            WebElement table_element = (new WebDriverWait(driver, 20))
//                .until(ExpectedConditions.presenceOfElementLocated(By.className("gameView")));
                    .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table[@class='gameView']")));
            List<WebElement> tr_collection = table_element.findElements(By.xpath("//tbody/tr"));
            System.out.println("NUMBER OF ROWS IN THIS TABLE = " + tr_collection.size());

            // get the text
            String textToType = "";
            for (WebElement e : tr_collection) {
                if (e.getText().length() > 80 && (e.getText().length() - e.getText().replace("\n","").length() == 1)) {
                    System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                    if (is_real_text(e.getText())) {
                        System.out.println("TEXT HAS BEEN FOUND");
                        textToType = e.getText();
                        System.out.println(textToType);
                        textToType = textToType.substring(0,textToType.indexOf("\n"));
                        break;
                    }
                }
            }

            // get the text input area
            WebElement text_input = (new WebDriverWait(driver, 25))
                    .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@class='txtInput']")));

            try {
                Thread.sleep(getDeviatedNumber(300,150));
            } catch (Exception e) {}


            Random rand = new Random();
            int numWords = textToType.length() - textToType.replace(" ", "").length() + 1;
            int textLength = textToType.length();
            double expectedNumMistakes = textLength * mistakesProbability;
            double averageWordLength = textLength / numWords;
            double secondsPerChar = 1 / ((desiredWordsPerMinute * averageWordLength + expectedNumMistakes * 4) / 60.0);
            System.out.println("Seconds per char: " + secondsPerChar);

            for (char c : textToType.toCharArray()) {
                text_input.sendKeys(c + ""); // type the next char


                double randomNumber = rand.nextInt(100) + 1;
                if (randomNumber <= 100 * mistakesProbability) { // make a mistake every once in a while
                    int[] a = {1, 1, 1, 1, 2, 2, 2, 3, 3, 4};
                    int pos = rand.nextInt(10);
                    for (int i = 0; i < a[pos]; i++) {
                        text_input.sendKeys(pickRandomCharacter() + "");
                        try {
                            Thread.sleep((long) (secondsPerChar * getDeviatedNumber(625,125))); // 625, 125
                        } catch (Exception e) {
                        }
                    }
                    for (int i = 0; i < a[pos]; i++) {
                        text_input.sendKeys(Keys.BACK_SPACE);
                    }
                }

                try {
                    Thread.sleep((long) (secondsPerChar * getDeviatedNumber(625,125)));
                } catch (Exception e) {
                }

            }
            try {
                Thread.sleep(getDeviatedNumber(10000,2000));
            } catch (Exception e) {}


            try {
                List<WebElement> elements = driver.findElements(By.xpath("//*[contains(text(), 'Begin Test')]"));
                Vision vision = new Vision(); // the class that will get the text from the img
                // click the begin test button
                elements.get(1).click();

                /*
                    At this point, we have successfully rekted the original typing test and now need to move over the teh
                    test verification part
                */

                while (true) {
                    System.out.println("searching for challenge img");
                    WebElement challengeImg = (new WebDriverWait(driver, 10))
                            .until(ExpectedConditions.presenceOfElementLocated(By.className("challengeImg")));
                    String logoSRC = challengeImg.getAttribute("src");
                    String challengeText = "";

                    System.out.println("got the challenge image");

                    try {
                        URL imageURL = new URL(logoSRC);
                        System.out.println("Downloading image....");
                        BufferedImage saveImage = ImageIO.read(imageURL);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(saveImage, "jpg", baos);
                        baos.flush();
                        byte[] imageInByte = baos.toByteArray();
                        ByteString imgBytes = ByteString.copyFrom(imageInByte);
                        System.out.println("image downloaded");
                        challengeText = vision.getTextFromImage(imgBytes);

                    } catch (Exception e) {
                        System.out.println(e);
                    }

                    WebElement challengeTextArea = (new WebDriverWait(driver, 6))
                            .until(ExpectedConditions.presenceOfElementLocated(By.className("challengeTextArea")));

                    challengeTextArea.clear();
                    challengeText = challengeText.replace("\n", " ");

                    for (char c : challengeText.toCharArray()) {
                        challengeTextArea.sendKeys(c + ""); // type the next char


                        try {
                            Thread.sleep((long) (secondsPerChar * getDeviatedNumber(625,125)));
                        } catch (Exception e) {}

                    }

                    try {
                        Thread.sleep(6000);
                        elements = driver.findElements(By.xpath("//*[contains(text(), 'Begin Re-test')]"));
                        // click the begin test button
                        elements.get(0).click();
                    } catch(Exception e){
                        break;
                    }

                }

            } catch (Exception e){
                System.out.println("couldn't find the button");
            }


            WebElement header = (new WebDriverWait(driver, 150))
                    .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[@href='http://play.typeracer.com/']")));
            header.click();
            try {
                Thread.sleep(6000,1000);
            } catch (Exception e) {}

        }

    }


    public static char pickRandomCharacter(){
        Random rand = new Random();
        int offset = rand.nextInt(27);

        return (char)(97 + offset);
    }

    public static int getDeviatedNumber(int original, int deviation){
        Random rand = new Random();
        deviation = rand.nextInt(deviation) * (-1 * rand.nextInt(2));
        return original + deviation;
    }

    public static boolean is_real_text(String s){
        if (s.contains("typeracer")){
            return false;
        }
        if (s.contains("Waiting for more people")){
            return false;
        }
        if (s.contains("Hall of Fame")){
            return false;
        }
        if (s.contains("name speed time")){
            return false;
        }
        if (s.contains("The race is about to start")){
            return false;
        }

        return true;
    }
}
