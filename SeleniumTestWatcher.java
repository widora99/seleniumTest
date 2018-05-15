package com.toyota_cs.radget.util;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.Alert;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SeleniumTestWatcher extends TestWatcher {

	private WebDriver driver;

	private static String imgpath;
	
	static {

		if(imgpath == null) {
			File jenkins = new File("C:\\Program Files (x86)\\Jenkins");
			// for server
			if(jenkins.exists()) {
				String buildnum = System.getProperty("BUILD_NUMBER");	 
				imgpath = "【Jenkinsのworkspace】\\target\\error-img\\BUILD" + buildnum + "\\";
			// for local
			} else {
				Date date = new Date();
				Format fmt = new SimpleDateFormat("yyyyMMdd_HHmmss");
				String time = fmt.format(date);
				imgpath = "C:\\temp\\" + time + "\\";
			}
			File dir = new File(imgpath);
			
			// フォルダがなかったら作成する
			if (!dir.exists()) {
				dir.mkdir();
	        }

		}
	}
	
	@Override
	protected void starting(Description description) {
		ChromeOptions options = new ChromeOptions();
		// windowsサイズを固定。　infobarを非表示に。
		options.addArguments("--window-size=1024,768", "--disable-infobars");

		try {
			// クッキーデータを取得します。
			String jsessionid = RadgetTestHelper.makeStringFromTestDataFile("chrome.cookie");
			Cookie ck = null;
			if (!"".equals(jsessionid)) {
			ck = new Cookie("JSESSIONID", jsessionid);
			}
			
			String path = RadgetTestHelper.makeStringFromTestDataPath("driver/chromedriver.exe");
			// chrome driverの指定
			// Windowsの場合
			System.setProperty("webdriver.chrome.driver", path);
		} catch (Exception e) {
			e.printStackTrace();
		}
		driver = new ChromeDriver(options);
	}

	@Override
	protected void finished(Description description) {
		WebDriverWait wait = new WebDriverWait(driver, 3);
		Alert alert = null;
		try {
			alert = wait.until(ExpectedConditions.alertIsPresent());
		} catch (Exception e) {
			
		}
	    if(alert != null) {
	    	alert.accept();
	    }
		driver.manage().deleteAllCookies();
		driver.quit();
	}

	// 失敗したときはスクリーンショットを撮る
	@Override
	protected void failed(Throwable e, Description description) {
		super.failed(e, description);
		String[] classname = description.getClassName().split("\\.");
		try {
			capAll(String.format("%s_%s",
				      classname[classname.length - 1], description.getMethodName()));
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}

	public WebDriver getWebDriver() {
		return this.driver;
	}
	/**
     * このメソッドを呼び出したメソッドの呼び出し元のメソッド名を取得する。
     *
     * @return 呼び出し元メソッド名
     */
    public String calledFrom() {
        StackTraceElement[] steArray = Thread.currentThread().getStackTrace();
        if (steArray.length <= 3) {
            return "";
        }
        StackTraceElement ste = steArray[3];
        String[] classname = ste.getFileName().split("\\.");
        return  classname[0] + "_" + ste.getMethodName();
    }
    
    public void capAll() throws WebDriverException, IOException {
    	capAll(calledFrom());
    }
    
    /**
     * screenshotを取得する
     * @param title
     * @throws WebDriverException
     * @throws IOException
     */
    public void capture(String title) throws WebDriverException, IOException {
 
		WebDriverWait wait = new WebDriverWait(driver, 3);
		try {
		    Alert alert = wait.until(ExpectedConditions.alertIsPresent());
		    alert.accept();
	    } catch (TimeoutException e) {
	    }
		
        String path = "";
       	path = imgpath + title +".png";
        
		File file = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		FileUtils.copyFile(file, new File(path));
		
    }
    
    /**
     * screenshotを取得する
     * @param title
     * @throws WebDriverException
     * @throws IOException
     */
    public void capAll(String title) throws WebDriverException, IOException {

        driver.switchTo().defaultContent();
        TakesScreenshot ts = (TakesScreenshot) new Augmenter().augment(driver);

        //JS実行用のExecuter
        JavascriptExecutor jexec = (JavascriptExecutor) driver;
        
    	final String SCROLLAREA = "【スクロールエリアのcssセレクタ】";
    	
        // スクロールエリアのある画面
        String isContent = String.valueOf(jexec.executeScript("return $('" + SCROLLAREA + "').length"));
        if("1".equals(isContent)) {

	        //画面サイズで必要なものを取得
	        int innerH = Integer.parseInt(String.valueOf(jexec.executeScript("return $('" + SCROLLAREA + "').height()")));
	        int innerW =Integer.parseInt(String.valueOf(jexec.executeScript("return $('" + SCROLLAREA + "').width()")));
	        int scrollH = Integer.parseInt(String.valueOf(jexec.executeScript("return $('" + SCROLLAREA + "')[0].scrollHeight")));
	        int scrollW = Integer.parseInt(String.valueOf(jexec.executeScript("return $('" + SCROLLAREA + "')[0].scrollWidth")));
	        int windowH = Integer.parseInt(String.valueOf(jexec.executeScript("return window.innerHeight")));
	        int windowW = Integer.parseInt(String.valueOf(jexec.executeScript("return window.innerWidth")));
	        if(scrollW > innerW) {
	        	windowH -= 12; // scrollbarの分
	        	innerH -= 12;
	        }
	        if(scrollH > innerH) {
	        	windowW -= 12; // scrollbarの分
	        	innerW -= 12;
	        }

	        int headerH = windowH - innerH;
	        int headerW = windowW - innerW;
	        //イメージを扱うための準備
	        BufferedImage img = new BufferedImage(headerW + scrollW, headerH + scrollH, BufferedImage.TYPE_INT_ARGB);
	        Graphics g = img.getGraphics();
	

        	int j = 0;
        	int scrollableW = scrollW + innerW;
        	// 横スクロールのループ
            while(scrollableW > innerW){
	            scrollableW -= innerW;
	            int scrollableH = scrollH + innerH;
	            int i = 0;
	            // 縦スクロールのループ
	            while(scrollableH > innerH){
	                scrollableH -= innerH;
	                // スクリーンショットを取得
	                BufferedImage imageParts = ImageIO.read(ts.getScreenshotAs(OutputType.FILE));
	                
	                if(i == 0 && j == 0) {
	                	// 1枚目は画面をそのまま貼る
	                	g.drawImage(imageParts, windowW * j, windowH * i, null);
	                } else if(scrollableH <= innerH && scrollableW <= innerW) {
	                	// 縦、横ともに最後は右下を埋めるように貼り付け
	                	g.drawImage(imageParts, headerW + innerW * j, headerH + innerH * i, headerW + innerW * (j + 1), headerH + innerH * (i + 1),
	    	            		windowW-scrollableW, windowH - scrollableH, windowW, windowH, null);
	                } else if(scrollableH <= innerH) {
	                	// 縦スクロールの最後は下から埋めるように貼り付け
	                	g.drawImage(imageParts, headerW + innerW * j, headerH + innerH * i, headerW + innerW * (j + 1), headerH + innerH * (i + 1),
	    	            		headerW, windowH - scrollableH, windowW, windowH, null);
	                } else if(scrollableW <= innerW) {
	                	// 横スクロール最後は右から埋めるように貼り付け
	                	g.drawImage(imageParts, headerW + innerW * j, headerH + innerH * i, headerW + innerW * (j + 1), headerH + innerH * (i + 1),
	                			windowW-scrollableW, headerH, windowW, windowH, null);
	                } else {
	                	g.drawImage(imageParts, windowW * j, windowH * i, null);
	                	// 途中の場合はscrollした分だけ貼る
	                	//g.drawImage(imageParts, headerW + innerW * j, headerH + innerH * i, headerW + innerW * (j + 1), headerH + innerH * (i + 1),
	                	//		headerW, headerH, windowW, windowH, null);
	                }

	                // 縦に１画面分スクロール
	                i++;
	                jexec.executeScript("$('" + SCROLLAREA + "').animate({scrollTop:" + innerH * i + "});");
	                try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
	                
	            }        
	            
	            // 縦スクロールが終わったら１回右にスクロールしてまた縦スクロール
	            j++;
	            jexec.executeScript("$('" + SCROLLAREA + "').animate({scrollTop: 0, scrollLeft:" + innerW * j + "});");     
                try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
	            
            }
	        
            // 画像をファイルに出力する
            String path = imgpath + title +".png";
            ImageIO.write(img, "png", new File(path));

        } else {
        	capture(title);
        }
		
    }

}
