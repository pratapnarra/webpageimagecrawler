package com.eulerity.hackathon.imagefinder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;

import org.jsoup.nodes.Element;

import org.jsoup.nodes.Document;

import org.jsoup.select.Elements;

//A class that has the crawl logic
public class Crawler implements Runnable {
	
	String url = null;
	
	public Crawler(String url) {
		this.url = url;
	}
	
	//result of individual sub-page will be stored here
	public List<String> subpageImages = new ArrayList<>();
	
	
	
	public void run() {
		
	if (this.url != null){
		
   	Document doc;
	try {
		//Jsoup connection, also using user agent so that crawler will not be banned 
		doc = Jsoup.connect(url)
				.userAgent("Mozilla/5.0 (MacBook Air; M1 Mac OS X 11_4) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.1 Safari/604.1")
				  .get();
		

		
		//get all img elements
		Elements imgs = doc.getElementsByTag("img"); 
		
		//for each element save the image
		//I HAVE OBSERVED SOME PAGES HAVE IMAGE URL IN THE 'src' attribute and some have in 'data-src' attribute 
		//This make sures we crawl all images
		for (Element el : imgs){
			
			if(!(el.absUrl("src")=="")) {
				
				//if img url has .ico or .svg then it's an icon
				if(el.absUrl("src").contains(".ico") || el.absUrl("src").contains(".svg") ) {
					System.out.println("This is an icon" + el.absUrl("src"));
					
				}
				
				//save valid urls that has .jpg, .jpeg, .png, .gif
				if(el.absUrl("src").contains(".jpg") || el.absUrl("src").contains(".jpeg") || el.absUrl("src").contains(".png")
						|| el.absUrl("src").contains(".gif") ) {
					//add img url to the list
					subpageImages.add(el.absUrl("src"));
					continue;
				} 
			}
			if(!(el.absUrl("data-src")=="")) {
				
				//if img url ends with .ico or .svg then it's an icon
				if(el.absUrl("data-src").contains(".ico") || el.absUrl("data-src").contains(".svg") ) {
					System.out.println("This is an icon" + el.absUrl("data-src"));
					
				}
				
				//save valid urls that has .jpg, .jpeg, .png, .gif
				if(el.absUrl("data-src").contains(".jpg") || el.absUrl("data-src").contains(".jpeg") || el.absUrl("data-src").contains(".png")
						|| el.absUrl("data-src").contains(".gif")) {
					//add img url to the list
					subpageImages.add(el.absUrl("data-src"));
					continue;
				} 
				
			}
          
	  }
		
		


		
	} catch (IOException e) {
		
		e.printStackTrace();
	}



		
	}
	
		
	}
	
	//get to return the result
	public List<String> getImages()    {
		return (this.subpageImages) ;
	 }

}
