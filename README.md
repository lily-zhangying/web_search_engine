# web_search_engine
* description for project here

# Compile and Run
## Back-end
Build the jar file  and run 

* Build Jar File
  ```bash
  cd wse

  javac -cp "./lucene-core-5.4.1.jar:./lucene-queryparser-5.4.1.jar:./lucene-analyzers-common-5.4.1.jar:./json-simple-1.1.jar:." *.java

  jar cf SearchEngine.jar *.class

 //test command line
 java -cp "./lucene-analyzers-common-5.4.1.jar:lucene-core-5.4.1.jar:lucene-queryparser-5.4.1.jar:json-simple-1.1.jar:SearchEngine.jar:." SearchEngine -query apple -index ./website/indexDir

  ```

  * web site also use these command lines and display the result in web page


## Front-end
  * We use php, html, css, javascript, JQuery to build our website.
  * To make our website run, we need one server machine support php. 
  * Put our website directory in the server machine, then run our index.php. it will listen to port 8888.
  * Open webserver, visit http://localhost:8888/ or http://(server_ip):8888/ to main page our our website


