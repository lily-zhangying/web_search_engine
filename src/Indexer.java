import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.List;
import java.util.Set;


import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Indexer {

	/** Index all text files under a directory. */
	public Indexer(String[] args) {
		String usage = "java org.apache.lucene.demo.IndexFiles" + " [-index INDEX_PATH] [-docs DOCS_PATH] [-update]\n\n"
				+ "This indexes the documents in DOCS_PATH, creating a Lucene index"
				+ "in INDEX_PATH that can be searched with SearchFiles";
		String indexPath = "indexDir";
		String docsPath = null;
		boolean create = true;
		for (int i = 0; i < args.length; i++) {
			if ("-index".equals(args[i])) {
				indexPath = args[i + 1];
				i++;
			} else if ("-docs".equals(args[i])) {
				docsPath = args[i + 1];
				i++;
			} else if ("-update".equals(args[i])) {
				create = false;
			}
		}

		if (docsPath == null) {
			System.err.println("Usage: " + usage);
			System.exit(1);
		}

		final Path docDir = Paths.get(docsPath);
		if (!Files.isReadable(docDir)) {
			System.out.println("Document directory '" + docDir.toAbsolutePath()
					+ "' does not exist or is not readable, please check the path");
			System.exit(1);
		}

		Date start = new Date();
		
		//JSONArray jsonObjects = parseJSONFile(docsPath);
		
		try {
			System.out.println("Indexing to directory '" + indexPath + "'...");

			Directory dir = FSDirectory.open(Paths.get(indexPath));
			Analyzer analyzer = new StandardAnalyzer();
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

			if (create) {
				// Create a new index in the directory, removing any
				// previously indexed documents:
				iwc.setOpenMode(OpenMode.CREATE);
			} else {
				// Add new documents to an existing index:
				iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			}

			// Optional: for better indexing performance, if you
			// are indexing many documents, increase the RAM
			// buffer. But if you do this, increase the max heap
			// size to the JVM (eg add -Xmx512m or -Xmx1g):
			//
			// iwc.setRAMBufferSizeMB(256.0);
			
			IndexWriter writer = new IndexWriter(dir, iwc);
			
			
			
			
			
			indexDocs(writer, docDir);

			// NOTE: if you want to maximize search performance,
			// you can optionally call forceMerge here. This can be
			// a terribly costly operation, so generally it's only
			// worth it when your index is relatively static (ie
			// you're done adding documents to it):
			//
			// writer.forceMerge(1);

	        try {
	            writer.commit();
	            writer.close();
	        } catch (IOException ex) {
	            System.err.println("We had a problem closing the index: " + ex.getMessage());
	        }

			Date end = new Date();
			System.out.println(end.getTime() - start.getTime() + " total milliseconds");

		} catch (IOException e) {
			System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
		}
	}

	/**
	 * Indexes the given file using the given writer, or if a directory is
	 * given, recurses over files and directories found under the given
	 * directory.
	 * 
	 * NOTE: This method indexes one document per input file. This is slow. For
	 * good throughput, put multiple documents into your input file(s). An
	 * example of this is in the benchmark module, which can create "line doc"
	 * files, one document per line, using the <a href=
	 * "../../../../../contrib-benchmark/org/apache/lucene/benchmark/byTask/tasks/WriteLineDocTask.html"
	 * >WriteLineDocTask</a>.
	 * 
	 * @param writer
	 *            Writer to the index where the given file/dir info will be
	 *            stored
	 * @param path
	 *            The file to index, or the directory to recurse into to find
	 *            files to index
	 * @throws IOException
	 *             If there is a low-level I/O error
	 */
	public void indexDocs(final IndexWriter writer, Path path) throws IOException {

		
		if (Files.isDirectory(path)) {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					try {
						indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
					} catch (IOException ignore) {
						// don't index files that can't be read.
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} else {
			indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
		}
	}

	/** Indexes a single document */
	@SuppressWarnings("unchecked")
	public void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException {

		try (InputStream stream = Files.newInputStream(file)) {

			System.out.println(file.toString());
			JSONArray jsonObjects = parseJSONFile(file.toString());
			for(JSONObject object : (List<JSONObject>) jsonObjects){
	            Document doc = new Document();
	            for(String field : (Set<String>) object.keySet()){
	            	//System.out.println(field);
	            	try {
		            	//System.out.println(object.get(field));
		                Class type = object.get(field).getClass();
		                //System.out.println(type);
		                if(type.equals(String.class)){
		                	if(field.equals("price")){
		                		String noSignPrice = (String)object.get(field);
								if(!noSignPrice.equals("0.0") && !noSignPrice.isEmpty() && noSignPrice != null){
									noSignPrice = noSignPrice.replace(",", "");
									doc.add(new TextField("regularPrice", noSignPrice, Field.Store.YES));
								}
		                	} else if(field.equals("name")){
		                		String checkNull = (String)object.get(field);
		                		if(!checkNull.isEmpty() && checkNull != null && checkNull.length() != 0 && !checkNull.equals("")) {
		                			doc.add(new TextField("name", (String)object.get(field), Field.Store.YES));
		                		}else{
		                			doc.add(new TextField("name", null, Field.Store.YES));
		                		}
		  
		                	} else if(field.equals("Title")){
		                		doc.add(new TextField("name", (String)object.get(field), Field.Store.YES));
		                	} else if(field.equals("DetailPageURL")){
		                		doc.add(new TextField("url", (String)object.get(field), Field.Store.YES));
		                	}else if(field.equals("Manufacturer") || field.equals("manufacturer")){
		                		String manu = (String)object.get(field);
		                		if(manu.isEmpty() || manu == null){
		                			//System.out.println("manu");
		                			doc.add(new TextField("manufacturer", "null", Field.Store.YES));
		                		}
		                		else{
		                			doc.add(new TextField("manufacturer", manu.toLowerCase(), Field.Store.YES));
		                		}
		                	} else if(field.equals("brand")){
		                		String manu = (String)object.get(field);
		                		if(manu.isEmpty() || manu == null){
		                			//System.out.println("manu");
		                			doc.add(new TextField("manufacturer", "null", Field.Store.YES));
		                		}
		                		else{
		                			manu = manu.substring(2);
		                			doc.add(new TextField("manufacturer", manu.toLowerCase(), Field.Store.YES));
		                		}
		                	}else if(field.equals("feature")){
		                		String feat = (String)object.get(field);
		                		if(feat.isEmpty() || feat == null){
		                			doc.add(new TextField("longDescription", " ", Field.Store.YES));
		                		}
		                		else{
		                			doc.add(new TextField("longDescription", feat, Field.Store.YES));
		                		}
		                	}else if(field.equals("ProductGroup") || field.equals("class")){
		                		doc.add(new TextField("category", (String)object.get(field), Field.Store.YES));
		                	}else if(field.equals("productTemplate")){
		                		String pt = (String)object.get(field);
		                		pt = pt.replace('_', ' ');
		                		doc.add(new TextField(field, pt, Field.Store.YES));
		                	}
		                	
		                	else{
		                		doc.add(new TextField(field, (String)object.get(field), Field.Store.YES));
		                	}
		                    
		                    //System.out.println("String " + field);
		                }else if(type.equals(Long.class)){
		                    doc.add(new TextField(field, object.get(field).toString(), Field.Store.YES));
		                    //System.out.println("Long " + field);
		                }else if(type.equals(Double.class)){
		                	if(field.equals("price")){
		                		doc.add(new TextField("regularPrice", object.get(field).toString(), Field.Store.YES));
		                	}
		                	else{
		                		doc.add(new TextField(field, object.get(field).toString(), Field.Store.YES));
		                	}
		                    
		                    //System.out.println("Double " + field);
		                }else if(type.equals(Boolean.class)){
		                    doc.add(new TextField(field, object.get(field).toString(), Field.Store.YES));
		                    //System.out.println("Bool " + field);
		                }else if(type.equals(JSONArray.class)){
		                	if(field.equals("Feature")){
		                		doc.add(new TextField("longDescription", object.get(field).toString(), Field.Store.YES));
		                	}else{
		                		doc.add(new TextField(field, object.get(field).toString(), Field.Store.YES));
		                	}
		                	
		                	//System.out.println("JSONArray " + field);
		                }else if(type.equals(JSONObject.class)){
		                	
		                	if(field.equals("LargeImage")){
		                		JSONObject smallOb = (JSONObject) object.get("LargeImage");
								for(String smallField : (Set<String>) smallOb.keySet()){
									if(smallField.equals("URL")){
										doc.add(new TextField("image", smallOb.get(smallField).toString(), Field.Store.YES));
										//System.out.println("large " + doc.get("image"));
									}
								}
		                	} else if(field.equals("SmallImage")){
		                		if(object.get("LargeImage") == null && object.get("MediumImage") == null){
		                			JSONObject smallOb = (JSONObject) object.get("SmallImage");
									for(String smallField : (Set<String>) smallOb.keySet()){
										if(smallField.equals("URL")){
											doc.add(new TextField("image", smallOb.get(smallField).toString(), Field.Store.YES));
											//System.out.println("small " + doc.get("image"));
										}
									}
		                		}
		                	} else if(field.equals("MediumImage")){
		                		if(object.get("LargeImage") == null){
		                			JSONObject smallOb = (JSONObject) object.get("MediumImage");
									for(String smallField : (Set<String>) smallOb.keySet()){
										if(smallField.equals("URL")){
											doc.add(new TextField("image", smallOb.get(smallField).toString(), Field.Store.YES));
											//System.out.println("med " + doc.get("image"));
										}
									}
		                		}
		                	} else if(field.equals("LowestNewPrice")){
								JSONObject smallOb = (JSONObject) object.get("LowestNewPrice");
								for (String smallField : (Set<String>) smallOb.keySet()) {
									if (smallField.equals("FormattedPrice")) {
										String noSignPrice = smallOb.get(smallField).toString();
										if (!noSignPrice.equals("Too low to display")) {
											noSignPrice = noSignPrice.substring(1);
											// System.out.println(noSignPrice);
											noSignPrice = noSignPrice.replace(",", "");
											doc.add(new TextField("regularPrice", noSignPrice, Field.Store.YES));
											//System.out.println("LNP " + doc.get("regularPrice"));
										}
									}
								}
		                	} else if(field.equals("ListPrice")){
		                		Boolean tooLow = false;
		                		if(object.get("LowestNewPrice") != null){
		                			JSONObject checkOb = (JSONObject) object.get("LowestNewPrice");
		                			if(checkOb.get("FormattedPrice").equals("Too low to display")){
		                				tooLow = true;
		                			}
		                		}
		                		if(object.get("LowestNewPrice") == null || tooLow){
		                			JSONObject smallOb = (JSONObject) object.get("ListPrice");
		                			for(String smallField : (Set<String>) smallOb.keySet()){
										if(smallField.equals("FormattedPrice")){
											String noSignPrice = smallOb.get(smallField).toString();
											if(!noSignPrice.equals("$0.00")){
												noSignPrice = noSignPrice.substring(1);
												//System.out.println(noSignPrice);
												noSignPrice = noSignPrice.replace(",", "");
						                		doc.add(new TextField("regularPrice", noSignPrice, Field.Store.YES));
						                		//System.out.println("ListPrice " + doc.get("regularPrice") + tooLow);
											}
										}
									}
		                		}
								
		                	} else if(field.equals("LowestUsedPrice")){
		                		Boolean tooLow = false;
		                		if(object.get("LowestNewPrice") != null){
		                			JSONObject checkOb = (JSONObject) object.get("LowestNewPrice");
		                			if(checkOb.get("FormattedPrice").equals("Too low to display")){
		                				tooLow = true;
		                			}
		                		}
		                		Boolean zeroList = false;
		                		if(object.get("ListPrice") != null){
		                			JSONObject checkOb = (JSONObject) object.get("ListPrice");
		                			if(checkOb.get("FormattedPrice").equals("$0.00")){
		                				zeroList = true;
		                			}
		                		}
		                		if((object.get("LowestNewPrice") == null && object.get("ListPrice") == null) || (object.get("LowestNewPrice") == null && zeroList) || (object.get("ListPrice") == null&& zeroList)){
		                			JSONObject smallOb = (JSONObject) object.get("LowestUsedPrice");
		                			for(String smallField : (Set<String>) smallOb.keySet()){
										if(smallField.equals("FormattedPrice")){
											String noSignPrice = smallOb.get(smallField).toString();
											noSignPrice = noSignPrice.substring(1);
											// System.out.println(noSignPrice);
											noSignPrice = noSignPrice.replace(",", "");
											doc.add(new TextField("regularPrice", noSignPrice, Field.Store.YES));
											//System.out.println("Used " + doc.get("regularPrice") + " tooLow:"+ tooLow+ " zeroList:" + zeroList);
										}
									}
		                		}
		                	}
							
		                	//doc.add(new TextField(field, object.get(field).toString(), Field.Store.YES));
		                	//System.out.println("Object " + field);
		                }
		                
	            	} catch (Exception ex) {
	            		continue;
	            	}
	            	
	            }
	            try {
	                writer.addDocument(doc);
	            } catch (IOException ex) {
	                System.err.println("Error adding documents to the index. " +  ex.getMessage());
	            }
	        }
		}
	}

	
    public JSONArray parseJSONFile(String jsonFilePath){
        //Get the JSON file, in this case is in ~/resources/test.json
        InputStream jsonFile;
        Reader readerJson = null;
        //System.out.println("got here");
		try {
			jsonFile = new FileInputStream(jsonFilePath);
			//System.out.println(jsonFile);
	        readerJson = new InputStreamReader(jsonFile);
	        //System.out.println(readerJson);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        //Parse the json file using simple-json library
        Object fileObjects = JSONValue.parse(readerJson);
        //System.out.println(fileObjects);
        JSONArray arrayObjects = (JSONArray)fileObjects;
        return arrayObjects;
    	
    }

    
    public static void main(String[] args){
    	Indexer run = new Indexer(args);
    }
}
