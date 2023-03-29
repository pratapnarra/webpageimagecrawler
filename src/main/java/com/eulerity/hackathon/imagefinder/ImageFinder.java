package com.eulerity.hackathon.imagefinder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jsoup.Jsoup;

import org.jsoup.nodes.Element;

import org.jsoup.nodes.Document;

import org.jsoup.select.Elements;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@WebServlet(
    name = "ImageFinder",
    urlPatterns = {"/main"}
)
public class ImageFinder extends HttpServlet{
	private static final long serialVersionUID = 1L;

	protected static final Gson GSON = new GsonBuilder().create();

	//This is just a test array
	public static final String[] testImages = {
			"https://images.pexels.com/photos/545063/pexels-photo-545063.jpeg?auto=compress&format=tiny",
			"https://images.pexels.com/photos/464664/pexels-photo-464664.jpeg?auto=compress&format=tiny",
			"https://images.pexels.com/photos/406014/pexels-photo-406014.jpeg?auto=compress&format=tiny",
			"https://images.pexels.com/photos/1108099/pexels-photo-1108099.jpeg?auto=compress&format=tiny"
  };

     //Using Hashsets to avoid duplicates
     //This is a HashSet which contains all the images from all the sub-pages combined
	 public static HashSet<String> webpageImages = new HashSet<>();
		
	 //This is a HashSet which contains all the sub-pages URLs
	 public static HashSet<String> subpageUrls = new HashSet<>();
	 
	 
  //Recursive function which takes url, mainurl(site domain) and collect all sub-page URLs 
  public void getSubPages(String url, String mainurl) {
	  
	  //Make sure we are not visiting a already visited sub-page
	  if (!subpageUrls.contains(url)) {
		//Make sure sub-page contains main domain url, so that we are not visiting other websites
		  if(!url.contains(mainurl)) {
			  return;
		  }
		  
		  try {
			  //add this url to subpageUrls hashset
			  subpageUrls.add(url);
			  
			  //Jsoup connection, also using user agent so that crawler will not be banned 
			  Document doc = Jsoup.connect(url)
					  .userAgent("Mozilla/5.0 (MacBook Air; M1 Mac OS X 11_4) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.1 Safari/604.1")
					  .get();

			  //get all links in the webpage
			  Elements links = doc.select("a[href]");
			  
			  for (Element subpage : links) {
				  //for each link visit to find more sub-pages
				  getSubPages(subpage.attr("abs:href"),mainurl);
              }
			  
		  }catch(IOException e) {
			  System.err.println(e.getMessage());
			  
		  }
	  }
  }
  
  
  
  

	@Override
	protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/json");
		String path = req.getServletPath();
		String url = req.getParameter("url");
		System.out.println("Got request of:" + path + " with query param:" + url);
		
		//crawl logic
		
		if (url != null){
			//everytime a url is received empty both hashsets to remove old results
			webpageImages = new HashSet<>();
			subpageUrls = new HashSet<>();
			
			getSubPages(url,url);
		}
		 if(subpageUrls.size() > 0) {
			 //executor service to handle the threads
			 //making a new thread for every subpage
			 ExecutorService executor = Executors.newFixedThreadPool(subpageUrls.size());
			 
			 List<Future<?>> futures = new ArrayList<Future<?>>();
			 
			 //array of crawler objects, each for a subpage
			 Crawler[] crawls = new Crawler[subpageUrls.size()];

			 int i=0;
			 for(String subpage: subpageUrls) {
					
					crawls[i] = new Crawler(subpage);
					
                    //submit the crawl task and using futures to check status 
					Future<?> f = executor.submit(crawls[i]);
					futures.add(f);

				i+=1;	
				}
			 
			 //waits until all the thread have finished execution
				for(Future<?> future : futures) {
				try {
					future.get();
				} catch (InterruptedException | ExecutionException e) {
					
					e.printStackTrace();
				}
			}
				
			//check status of threads
			boolean allDone = true;
			for(Future<?> future : futures){
			    allDone &= future.isDone(); // check if future is done
			}
			
			//if all threads have executed then we have the results ready
			if (allDone) {
			   for(Crawler c: crawls) {
				  //add the images of each subpage to the main webpageImages hashset
				  //also at this stage duplicate images are eliminated 
				  webpageImages.addAll(c.getImages());
				
			}
			}
			 
			 
		 }

		
		resp.getWriter().print(GSON.toJson(webpageImages));
	}
}
