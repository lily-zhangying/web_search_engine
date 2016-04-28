import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

public class JsonIndexRetriver {
	 /** Simple command-line based search demo. */
	   public JsonIndexRetriver(String[] args) throws Exception {
	     String usage =
	       "Usage:\tjava org.apache.lucene.demo.SearchFiles [-index dir] [-field f] [-query string] [-raw] [-paging hitsPerPage]\n\nSee http://lucene.apache.org/core/4_1_0/demo/ for details.";
	     if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
	       System.out.println(usage);
	       System.exit(0);
	     }
	 
	     String index = "index";
	     String defaultFields = "name,regularPrice,url,image,longDescription,shortDescription,manufacturer";
	     /*
	     By default, if -field not in parameter, use all fields
		 inputFields[0] = "name";
		 inputFields[1] = "regularPrice";     //price
		 inputFields[2] = "url";
		 inputFields[3] = "image";			  //image url
		 inputFields[4] = "longDescription";  //feature for all data sets
		 inputFields[5] = "shortDescription"; //additional features for BestBuy Data
		 inputFields[6] = "manufacturer";
		 */
	     
	     String inputFields = "";
	     
	     String queries = null;
	     boolean raw = false;
	     StringBuilder queryStringB = new StringBuilder();
	     String queryString = null;
	     int hitsPerPage = 10;
	     
	     for(int i = 0;i < args.length;i++) {
	       if ("-index".equals(args[i])) {
	         index = args[i+1];
	         i++;
	       } else if ("-field".equals(args[i])) {
			 inputFields = args[i + 1];//make sure input parameter is in a string separated by ","
	         i++;
	       } else if ("-query".equals(args[i])) {
	    	   queryStringB.append(args[i+1]).append(" ");
	    	   i++;
	    	   
	    	   while(i<args.length - 1 && args[i+1].charAt(0) != '-'){
	        	 queryStringB.append(args[i+1]).append(" ");
	        	 i++;
	    	   }
	    	   
	       } else if ("-raw".equals(args[i])) {
	         raw = true;
	       } else if ("-paging".equals(args[i])) {
	         hitsPerPage = Integer.parseInt(args[i+1]);
	         if (hitsPerPage <= 0) {
	           System.err.println("There must be at least 1 hit per page.");
	           System.exit(1);
	         }
	         i++;
	       }
	     }
	     String[] fields;
	     
	     if(!inputFields.equals("") || inputFields.length() > 0){
			 fields = inputFields.split(","); //make sure input parameter is in a string separated by ,
	     } else{
	    	 fields = defaultFields.split(",");
	     }
	     
	     
	     queryString = queryStringB.substring(0, queryStringB.length()-1);
	     
	     IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
	     IndexSearcher searcher = new IndexSearcher(reader);
	     Analyzer analyzer = new StandardAnalyzer();
	 
	     BufferedReader in = null;
	     if (queries != null) {
	       in = Files.newBufferedReader(Paths.get(queries), StandardCharsets.UTF_8);
	     } else {
	       in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
	     }
	     MultiFieldQueryParser multiparser = new MultiFieldQueryParser(fields, analyzer);
	     while (true) {
	       if (queries == null && queryString == null) {                        // prompt the user
	         System.out.println("Enter query: ");
	       }
	 
	       String line = queryString != null ? queryString : in.readLine();
	 
	       if (line == null || line.length() == -1) {
	         break;
	       }
	 
	       line = line.trim();
	       if (line.length() == 0) {
	         break;
	       }
	       
	       Query query = multiparser.parse(line);
	       //System.out.println("Results for query " + query.toString(field) + " in directory " + index);
	 
	       doPagingSearch(in, searcher, query, hitsPerPage, raw, queries == null && queryString == null);
	 
	       if (queryString != null) {
	         break;
	       }
	     }
	     reader.close();
	   }
	 
	   /**
	    * This demonstrates a typical paging search scenario, where the search engine presents 
	    * pages of size n to the user. The user can then go to the next page if interested in
	    * the next hits.
	    * 
	    * When the query is executed for the first time, then only enough results are collected
	    * to fill 5 result pages. If the user wants to page beyond this limit, then the query
	    * is executed another time and all hits are collected.
	    * 
	    */
	   public static void doPagingSearch(BufferedReader in, IndexSearcher searcher, Query query, 
	                                      int hitsPerPage, boolean raw, boolean interactive) throws IOException {
	     // Collect enough docs to show 5 pages
	     TopDocs results = searcher.search(query, hitsPerPage);
	     ScoreDoc[] hits = results.scoreDocs;
	     
	     int numTotalHits = results.totalHits;
	     System.out.println(numTotalHits + " total matching documents");
	 
	     int start = 0;
	     int end = Math.min(numTotalHits, hitsPerPage);

	     while (true) {
	       if (end > hits.length) {
	         System.out.println("Only results 1 - " + hits.length +" of " + numTotalHits + " total matching documents collected.");
	         System.out.println("Collect more (y/n) ?");
	         String line = in.readLine();
	         if (line.length() == 0 || line.charAt(0) == 'n') {
	           break;
	         }
	 
	         hits = searcher.search(query, numTotalHits).scoreDocs;
	       }
	       
	       end = Math.min(hits.length, start + hitsPerPage);
	       for (int i = start; i < end; i++) {
	         if (raw) {                              // output raw format
	           System.out.println("doc="+hits[i].doc+" score="+hits[i].score);
	           continue;
	         }
	 
	         Document doc = searcher.doc(hits[i].doc);
	         String name = doc.get("name");
	         if (name != null) {
	           System.out.println((i+1) + ". " + name);
	           System.out.println("	Price:" + doc.get("regularPrice"));
	           System.out.println("	Url:" + doc.get("url"));
	           System.out.println("	ImageURL:" + doc.get("image"));
	           System.out.println("	Manufacturer:" + doc.get("manufacturer"));

	         } else {
	           System.out.println((i+1) + ". " + "No path for this document");
	         }
	                   
	       }
	 
	       if (!interactive || end == 0) {
	         break;
	       }
	 
	       if (numTotalHits >= end) {
	         boolean quit = false;
	         while (true) {
	           System.out.print("Press ");
	           if (start - hitsPerPage >= 0) {
	             System.out.print("(p)revious page, ");  
	           }
	           if (start + hitsPerPage < numTotalHits) {
	             System.out.print("(n)ext page, ");
	           }
	           System.out.println("(q)uit or enter number to jump to a page.");
	           
	           String line = in.readLine();
	           if (line.length() == 0 || line.charAt(0)=='q') {
	             quit = true;
	             break;
	           }
	           if (line.charAt(0) == 'p') {
	             start = Math.max(0, start - hitsPerPage);
	             break;
	           } else if (line.charAt(0) == 'n') {
	             if (start + hitsPerPage < numTotalHits) {
	               start+=hitsPerPage;
	             }
	             break;
	           } else {
	             int page = Integer.parseInt(line);
	             if ((page - 1) * hitsPerPage < numTotalHits) {
	               start = (page - 1) * hitsPerPage;
	               break;
	             } else {
	               System.out.println("No such page");
	             }
	           }
	         }
	         if (quit) break;
	         end = Math.min(numTotalHits, start + hitsPerPage);
	       }
	     }
	   }
	   
	    public static void main(String[] args) throws Exception{
	    	JsonIndexRetriver run = new JsonIndexRetriver(args);
	    }
	   
}
