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
  private Map<Item, Integer> ItemIndex = new HashMap<Item, Integer>();
  private List<Item> finalList = new ArrayList<Item>();
  private String[] queryWord;
  private boolean byPrice;
  private boolean manuFlag;
  private String manufactories;
  private boolean sourceFlag;
  private String sources;
  private boolean priceFlag;
  private double lowRange;
  private double highRange;
  private String dir;
  private int pageNum;
  public static int numPerPage = 10;


  public SearchEngine(String[] args) {
    String usage =
        "Usage:\tjava SearchEngine [-index dir] [-query string] [-brand string] [-price string] [-seller string] [-page_num string] [-priceRank]\n\n";
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
    dir = null;
    for (int i = 0; i < args.length; i++) {
      if ("-index".equals(args[i])) {
        dir = args[i + 1];
        i++;
      } else if ("-query".equals(args[i])) {
        String queries = args[i + 1];
        i++;
        queryWord = queries.split("\\+");
      } else if ("-brand".equals(args[i])) {
        manuFlag = true;
        manufactories = args[i + 1];
        i++;
      } else if ("-price".equals(args[i])) {
        priceFlag = true;
        String[] prices = args[i + 1].split("\\+");
        List<Double> range = new ArrayList<Double>();
        for (String p : prices) {
          String[] subrange = p.split(",");
          for (String price : subrange) {
            range.add(Double.parseDouble(price));
          }
        }
        if (range.size() >= 2) {
          Collections.sort(range);
          lowRange = range.get(0);
          highRange = range.get(range.size() - 1);
        } else if (range.size() == 1) {
          // throw new IllegalArgumentException("Wrong numbers of price ranges.");
          lowRange = highRange = range.get(0);
        } else if (range.size() == 0) {
          lowRange = 0;
          highRange = 6556000;
        }
      } else if ("-seller".equals(args[i])) {
        sourceFlag = true;
        sources = args[i + 1];
        i++;
      } else if ("-page_num".equals(args[i])) {
        pageNum = Integer.parseInt(args[i + 1]);
        i++;
      } else if ("-priceRank".equals(args[i])) {
        byPrice = true;
      }
    }
    weight = new double[queryWord.length];
    for (int i = 0; i < queryWord.length; i++) {
      weight[i] = queryWord.length - i;
    }
  }

  public void runEngine() throws Exception {
    List<String> query = new ArrayList<String>();
    query.add("-query");
    query.add("temp");
    if (dir != null) {
      query.add("-index");
      query.add(dir);
    }
    if (manuFlag) {
      query.add("-manufacturer");
      query.add(manufactories);
    }
    if (sourceFlag) {
      query.add("-websource");
      query.add(sources);
    }
    if (priceFlag) {
      query.add("-price");
      query.add(Double.toString(lowRange));
      query.add(Double.toString(highRange));
    }
    /*
     * for (String str : query) { System.out.println(str); }
     */
    // System.out.println("length: " + queryWord.length);
    int count = 0;
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
        if ((!ItemIndex.isEmpty()) && ItemIndex.containsKey(it)) {
          Item another = finalList.get(ItemIndex.get(it));
          another.setQIndex(i); // always update the object in finalList, ItemIndex in only used to
                                // test duplicates
        } else {
          it.setQIndex(i);
          it.setArrayIndex(count);
          finalList.add(count, it);
          ItemIndex.put(it, count);
          count++;
        }
      }
    }
  }

  public List<Item> getResult() throws Exception {
    runEngine();
    if (byPrice) {
      Collections.sort(finalList, Item.PriceComparator);
    } else {
      for (Item it : finalList) {
        it.updateScore(weight);
      }
      Collections.sort(finalList);
    }    
    return finalList;  
  }
  
  public String OutString (int page_num) throws Exception {
    int page = page_num > 0 ? page_num : pageNum;
    List<Item> list = getResult() ;
    List<JSONObject> result = new ArrayList<JSONObject>();
    for (int i = 0; i < 10; i++) {
      int index = (page - 1) * numPerPage + i;
      if (index >= list.size()) {
        break;
      }
      Item it = list.get(index);
      JSONObject jo = new JSONObject();
      jo.put("name", it.getName());
      jo.put("price", "$" + it.getPrice());
      jo.put("url", it.getUrl());
      jo.put("image", it.getIamge());
      jo.put("brand", it.getManufactor());
      result.add(jo);
    }
    String items = JSONArray.toJSONString(result);
    JSONObject jsonResult = new JSONObject();
    jsonResult.put("items", items);
    jsonResult.put("page_number", Integer.toString(page));
    jsonResult.put("total_items", Integer.toString(list.size()));
    return jsonResult.toJSONString();
  }

  public static void main(String[] args) throws Exception {
    /*
     * 1. read query and weight from args 2. call retriever for each word in query 3. put all
     * results in a set and then calculate the score for each Item object 4. cluster function and
     * output
     */
    SearchEngine engine = new SearchEngine(args);
    String result = engine.OutString(-1);
    System.out.println(result);
  }

}
