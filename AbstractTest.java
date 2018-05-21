public class AbstractTest {

	protected static WebDriver driver;
	
	@Rule
	public SeleniumTestWatcher watcher = new SeleniumTestWatcher();

	/**
	 * JQuery風セレクタ
	 * @param selector
	 * @return 要素
	 */
	public static ExtendElement $(String selector) {
		return new ExtendElement(driver, driver.findElement(By.cssSelector(selector)));
	}
	
	/**
	 * JQuery風セレクタ（複数要素)
	 * @param selector
	 * @return 要素
	 */
	public static List<ExtendElement> $$(String selector) {
		ExtendElement ele = new ExtendElement(driver, driver.findElement(By.tagName("body")));
		return ele.finds(selector);
	}
	
	
}