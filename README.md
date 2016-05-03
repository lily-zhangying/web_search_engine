# web_search_engine

* The objective of our project is to build a search engine in which we can query an electronic hard-good product and be able to filter our search based on certain criteria such as: price range for the product, certain popular manufacturers, and which web source the product is on.

# Compile and Run
**First, you must Compile and Run the backend code**

## Back-end
Build the jar file  and run 

* Build Jar File (for Windows OS, change ':' to ';' )
  ```bash
  cd src   //Must be in src folder

  //first compile all .java files with the jar in the folder
  javac -cp "./lucene-core-5.4.1.jar:./lucene-queryparser-5.4.1.jar:./lucene-analyzers-common-5.4.1.jar:./json-simple-1.1.jar:." *.java

  //create the SearchEngine jar file.
  jar cf SearchEngine.jar *.class

 //A sample command to test the back end and the jar file with a sample query (apple+iphone)
 java -cp "./lucene-analyzers-common-5.4.1.jar:lucene-core-5.4.1.jar:lucene-queryparser-5.4.1.jar:json-simple-1.1.jar:SearchEngine.jar:." SearchEngine -query apple+phone -index ../website/indexDir

  ```

  * web site also use these command lines and display the result in web page
  * The index files are in website/indexDir 

**Now that you have finished running the back end, you can now check the front end**

## Front-end
  * We use php, html, css, javascript, JQuery to build our website.
  * **To make our website run, we need one machine support php.** 
  * Put our website directory in the server machine, then run our index.php. it will listen to port 8888.

  ```bash
  cd website //leave the src folder and go into the website folder
  php index.php //use php to run the index file. Make sure php is installed on your system.

  starting server on port 8888

  ```
  * Finally you can open the webserver by visiting: **
   http://localhost:8888/
**
   This leads to the main page of our website
