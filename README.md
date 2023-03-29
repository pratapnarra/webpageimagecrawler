This application can download all the images from a website domain, Example: If you want to download all the images from example.com/start, it finds all the subpage links under the domain example.com present in this page and downloads all images from all subpages. Used Java Multi-Threading, each thread crawls a seperate subpage so the entire crawl is very efficient. 

### Requirements

- Maven 3.5 or higher
- Java 8

### Setup
Open a terminal in the code directory and do the following

>`mvn package`

>`mvn clean`

>`mvn clean test package jetty:run`
The web application will be running in localhost:8080


