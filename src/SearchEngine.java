package finalProject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class SearchEngine {

  private double[] weight;
  private Map<Item, Item> result = new HashMap<Item, Item>();
  private String[] queryWord;
  private boolean byPrice;
  private boolean manuFlag;
  private String manufactories;
  private boolean sourceFlag;
  private String sources;
  private boolean priceFlag;
  private double lowRange;
  private double highRange;
  private int pageNum;
  public static int numPerPage = 10;


  public SearchEngine(String[] args) {
    String usage =
        "Usage:\tjava SearchEngine [-query string] [-brand string] [-price string] [-seller string] [-page_num string] [-priceRank]\n\n";
    if (args.length == 0 || (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0])))) {
      System.out.println(usage);
      System.exit(0);
    }
    byPrice = false;
    manuFlag = false;
    sourceFlag = false;
    lowRange = -1;
    highRange = -1;
    pageNum = 1;
    for (int i = 0; i < args.length; i++) {
      if ("-query".equals(args[i])) {
        String queries = args[i+1];
        i++;
        queryWord = queries.split("\\+");       
      } else if ("-brand".equals(args[i])) {
        manuFlag = true;
        manufactories = args[i+1];
        i++;
      } else if  ("-price".equals(args[i])) {
        priceFlag = true;
        String[] prices = args[i+1].split("\\+");
        List<Double> range = new ArrayList<Double> ();
        for (String p : prices) {
          String[] subrange = p.split(",");
          for (String price: subrange) {
            range.add(Double.parseDouble(price));
          }
        }
        if (range.size() >=2) {
          Collections.sort(range);
          lowRange = range.get(0);
          highRange = range.get(range.size() -1);
        } else if (range.size() == 1){
//          throw new IllegalArgumentException("Wrong numbers of price ranges.");
          lowRange = highRange = range.get(0);
        } else if (range.size() == 0) {
          lowRange = 0;
          highRange = 6556000;
        }
      } else if ("-seller".equals(args[i])) {
        sourceFlag = true;
        sources = args[i+1];
        i++;
      } else if ("-page_num".equals(args[i])) {
        pageNum = Integer.parseInt(args[i+1]);
        i++;
      } else if ("-priceRank".equals(args[i])) {
        byPrice = true;
      }
    }
    
    weight = new double[queryWord.length];
    for (int i = 0; i<queryWord.length; i++) {
      weight[i] = queryWord.length - i;
    }   
  }

  public void runEngine() throws Exception {
    List<String> query = new ArrayList<String> ();
    query.add("-query");
    query.add("temp");
    if (manuFlag) {
      query.add("-manufacturer");
      query.add(manufactories);
    }
    if(sourceFlag) {
      query.add("-websource");
      query.add(sources);
    }
    if (priceFlag){
      query.add("-price");
      query.add(Double.toString(lowRange));
      query.add(Double.toString(highRange));
    }
/*    
    for (String str : query) {
      System.out.println(str);
    } 
    */
    for (int i = 0; i < queryWord.length; i++) {
      String[] arg = query.toArray(new String[queryWord.length]);
      arg[1] = queryWord[i];
      List<Document> retrievedResult = new JsonIndexRetriver(arg).getDocArray();
      for (Document doc : retrievedResult) {
        Item it = new Item(queryWord.length);
        it.setName(doc.get("name"));
        it.setPrice(doc.get("regularPrice"));
        it.setUrl(doc.get("url"));
        it.setIamge(doc.get("image"));
        it.setManufactor(doc.get("manufacturer"));
        if ((!result.isEmpty()) && result.containsKey(it)) {
          Item another = result.get(it);
          result.remove(it);
          another.setIndex(i); // Here is the most important part for debug
          result.put(another, another);
        } else {
          it.setIndex(i);
          result.put(it, it);
        }
      }
    }
  }

  public String getResult() throws Exception {
    runEngine();
    List<Item> finalList = new ArrayList<Item>(result.values());
    if (byPrice) {
      Collections.sort(finalList, Item.PriceComparator);
    } else {
      for (Item it : finalList) {
        it.updateScore(weight);
      }
      Collections.sort(finalList);
    }
    List<JSONObject> result = new ArrayList<JSONObject>();
    for (int i = 0; i<10; i++) {
      int index = (pageNum -1)*numPerPage + i;
      if (index>=finalList.size()){
        break;
      }
      Item it = finalList.get(index);
      JSONObject jo = new JSONObject();
      jo.put("name", it.getName());
      jo.put("price", "$"+it.getPrice());
      jo.put("url", it.getUrl());
      jo.put("image", it.getIamge());
      jo.put("brand", it.getManufactor());
      result.add(jo);
    }
    String items = JSONArray.toJSONString(result);
    JSONObject jsonResult = new JSONObject();
    jsonResult.put("items", items);
    jsonResult.put("page_number", Integer.toString(pageNum));
    jsonResult.put("total_items", Integer.toString(finalList.size()));
    return jsonResult.toJSONString();
  }

  public static void main(String[] args) throws Exception {
    /*
     * 1. read query and weight from args 2. call retriever for each word in query 3. put all
     * results in a set and then calculate the score for each Item object 4. call clustering
     * function and output --- will not do clustering?
     */
    SearchEngine engine = new SearchEngine(args);
    String result = engine.getResult();
    System.out.println(result);  
  }

}
