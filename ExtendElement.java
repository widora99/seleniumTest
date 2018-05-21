package com.toyota_cs.radget.util;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;

/**
 * JQuery風にWebElementを拡張利用するクラス。
 * @author t-hashimoto
 *
 */
public class ExtendElement {

	WebDriver driver;
	WebElement elm;
	WebDriverWait wait;
	
	public ExtendElement(WebDriver driver, WebElement elm) {
		this.driver = driver;
		this.elm = elm;
		wait = new WebDriverWait(driver, 3);
	}
	
	/**
	 * 要素内のテキストを取得する
	 * @return
	 */
	public String text() {
		return elm.getText();
	}
	
	/**
	 * 要素にテキストを入力する
	 * @param text
	 */
	public void text(String text) {
		elm.clear();
		elm.sendKeys(text);
	}
	
	/**
	 * 要素のvalue値を取得する
	 * @return
	 */
	public String val() {
		return elm.getAttribute("value");
	}
	
	/**
	 * 要素にvalue値をセットする
	 * @param val
	 */
	public void val(String val) {
		switch(elm.getTagName().toLowerCase()) {
		case "input":
			switch(elm.getAttribute("type")) {
			case "text":
			case "password":
				wait.until(ExpectedConditions.elementToBeClickable(elm));
				elm.clear();
				wait.until(ExpectedConditions.elementToBeClickable(elm));
				elm.sendKeys(val);
				break;
			default:
				break;
			}
			break;
		case "select":
			Select select = new Select(elm);
			select.selectByValue(val);
			break;
		default:
			break;
		}
	}
		
	/**
	 * 要素に対し指定したキーを押下する
	 * @param key
	 */
	public void keypress(Keys key) {
		elm.sendKeys(key);
	}
	
	/**
	 * 要素を選択する
	 * @param key
	 */
	public void select() {
		switch(elm.getTagName()) {
		case "input":
			switch(elm.getAttribute("type")) {
			case "radio":
				elm.click();
				break;
			case "checkbox":
				if(!elm.isSelected()) {
					elm.click();
				}
				break;
			}
			break;
		}
	}
	
	/**
	 * 要素をクリックする
	 */
	public void click() {
		Actions action = new Actions(driver);
		wait.until(ExpectedConditions.elementToBeClickable(elm));
		action.click().build().perform();
	}
	
	/**
	 * 要素をダブルクリックする
	 */
	public void dblclick() {
		Actions action = new Actions(driver);
		wait.until(ExpectedConditions.elementToBeClickable(elm));
		action.doubleClick(elm).build().perform();
	}

	/**
	 * 要素を右クリックする
	 */
	public void ctxtclick() {
		Actions action = new Actions(driver);
		wait.until(ExpectedConditions.elementToBeClickable(elm));
		action.contextClick(elm).build().perform();
	}

	/**
	 * 要素上に移動する
	 */
	public void moveon() {
		Actions action = new Actions(driver);
		action.moveToElement(elm).perform();
	}
	
	/**
	 * 要素上で指定時間ホバーする
	 */
	public void hover(long millis) {
		moveon();
		try {
			Thread.sleep(millis);
		} catch(Exception e) {}
	}
	
	/**
	 * 要素を他の要素にドラッグ＆ドロップする
	 * @param target
	 */
	public void dragto(ExtendElement target) {
		Actions action = new Actions(driver);
		action.dragAndDrop(elm, target.origin()).perform();
	}
	
	/**
	 * 要素内の要素を取得する
	 * @param selector
	 * @return
	 */
	public ExtendElement find(String selector) {
		return new ExtendElement(driver, elm.findElement(By.cssSelector(selector)));
	}
	
	/**
	 * 要素内の要素（複数）を取得する
	 * @param selector
	 * @return
	 */
	public List<ExtendElement> finds(String selector) {
		List<ExtendElement> elist = new ArrayList<ExtendElement>();
		List<WebElement> wlist = elm.findElements(By.cssSelector(selector));
		for(WebElement welm : wlist) {
			elist.add(new ExtendElement(driver, welm));
		}
		return elist;
	}
	
	/**
	 * 要素の子要素をすべて取得する
	 * @param selector
	 * @return
	 */
	public List<ExtendElement> children() {
		List<ExtendElement> elist = new ArrayList<ExtendElement>();
		List<WebElement> wlist = elm.findElements(By.xpath("./*"));
		for(WebElement welm : wlist) {
			elist.add(new ExtendElement(driver, welm));
		}
		return elist;
	}
	
	/**
	 * 要素の子要素を取得する
	 * cssだと子要素のみを取得することができないので無理やりxpathに変換
	 * @param selector
	 * @return
	 */
	public List<ExtendElement> children(String selector) {
		List<ExtendElement> elist = new ArrayList<ExtendElement>();
		String xpath = cssToXpath(selector);
		
		List<WebElement> wlist = elm.findElements(By.xpath("./" + xpath));
		for(WebElement welm : wlist) {
			elist.add(new ExtendElement(driver, welm));
		}
		return elist;
	}
	
	/**
	 * 親要素を取得する
	 * @return
	 */
	public ExtendElement parent() {
		return new ExtendElement(driver, elm.findElement(By.xpath("..")));
	}
	
	/**
	 * 要素の先祖要素を取得する
	 * cssだと子要素のみを取得することができないので無理やりxpathに変換
	 * @param selector
	 * @return
	 */
	public ExtendElement closest(String selector) {
		String xpath = cssToXpath(selector);
		List<WebElement> list = elm.findElements(By.xpath("./ancestor::" + xpath));
		if(list.size() == 0) {
			throw new ElementNotFoundException("./ancestor::" + xpath, "", "");
		}
		ExtendElement eelm = new ExtendElement(driver, list.get(list.size() - 1));
		return eelm;
	}
	
	/**
	 * 次要素を取得する
	 * @return
	 */
	public ExtendElement next() {
		return new ExtendElement(driver, elm.findElement(By.xpath("./following-sibling::*")));
	}
	
	/**
	 * 前要素を取得する
	 * @return
	 */
	public ExtendElement prev() {
		return new ExtendElement(driver, elm.findElement(By.xpath("./preceding-sibling::*")));
	}
	
	/**
	 * cssの値を取得する
	 * @param name cssの名前
	 * @return
	 */
	public String css(String name) {
		return elm.getCssValue(name);
	}
	
	/**
	 * 要素の幅を取得する
	 * @return
	 */
	public int width() {
		return elm.getSize().getWidth();
	}
	
	/**
	 * 要素の高さを取得する
	 * @return
	 */
	public int height() {
		return elm.getSize().getHeight();
	}
	
	/**
	 * 要素の左端の座標を取得する
	 * @return
	 */
	public int left() {
		return elm.getLocation().getX();
	}
	
	/**
	 * 要素の上端の座標を取得する
	 * @return
	 */
	public int top() {
		return elm.getLocation().getY();
	}
	
	/**
	 * 要素のタグ名を取得する
	 * @return
	 */
	public String tagName() {
		return elm.getTagName();
	}
	
	/**
	 * 要素の属性を取得する
	 * @param name 属性名
	 * @return
	 */
	public String attr(String name) {
		return elm.getAttribute(name);
	}
	
	/**
	 * 値をclearする
	 */
	public void clear() {
		elm.clear();
	}
	
	/**
	 * 要素が有効になっているかどうか
	 * @return
	 */
	public boolean isEnabled() {
		return elm.isEnabled();
	}
	
	/**
	 * 要素が選択されているか
	 * @return
	 */
	public boolean isSelected() {
		return elm.isSelected();
	}
	
	/**
	 * 要素が表示されているか
	 * @return
	 */
	public boolean isDisplayed() {
		return elm.isDisplayed();
	}

	/**
	 * フォームを送信する
	 */
	public void submit() {
		elm.submit();
	}
	
	/**
	 * オリジナルのWebElementを返す
	 * @return
	 */
	public WebElement origin() {
		return elm;
	}
	
	/**
	 * cssセレクタをxpathに変換する
	 * 複数クラスには非対応です
	 * 
	 * @param selector
	 * @return
	 */
	public String cssToXpath(String selector) {
		if(selector.startsWith("#")) {
			return "*[@id=\"" + selector.replace("#", "") + "\"]";
		} else if(selector.startsWith(".")) {
			return "*[contains(@class, \"" + selector.replace(".", "") + "\")]";
		} else {
			return selector.replace("type", "@type");
		}
	}
	
}
