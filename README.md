# web_search_engine
* description for project here

# Compile and Run
## Back-end
Build the jar file  and run 

* Build Jar File (for Windows OS, change ':' to ';' )
  ```bash
  cd src

  javac -cp "./lucene-core-5.4.1.jar:./lucene-queryparser-5.4.1.jar:./lucene-analyzers-common-5.4.1.jar:./json-simple-1.1.jar:." *.java

  jar cf SearchEngine.jar *.class

 //test command line
 java -cp "./lucene-analyzers-common-5.4.1.jar:lucene-core-5.4.1.jar:lucene-queryparser-5.4.1.jar:json-simple-1.1.jar:SearchEngine.jar:." SearchEngine -query apple+phone -index ../website/indexDir

  ```

  * web site also use these command lines and display the result in web page
  * The index files are in website/indexDir 


## Front-end
  * We use php, html, css, javascript, JQuery to build our website.
  * To make our website run, we need one  machine support php. 
  * Put our website directory in the server machine, then run our index.php. it will listen to port 8888.

  ```bash
  cd website
  php index.php 

  starting server on port 8888

  ```
  * Open webserver, visit http://localhost:8888/ to main page our our website
