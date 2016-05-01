
import java.util.Comparator;

public class Item implements Comparable<Item> {

  private double score;
  private int[] qIndex;
  private String name;
  private double price;
  private String url;
  private String imageURL;
  private String manufactor;
  private int index;

  public Item(int numOfQueries) {
    score = 0;
    qIndex = new int[numOfQueries];
    for (int i = 0; i < numOfQueries; i++) {
      qIndex[i] = 0;
    }
  }

  public String getName() {
    return name;
  }

  public double getPrice() {
    return price;
  }

  public String getUrl() {
    return url;
  }

  public String getIamge() {
    return imageURL;
  }

  public String getManufactor() {
    return manufactor;
  }

  public void setQIndex(int index) {
    qIndex[index] = 1;
  }

  public void setName(String n) {
    if ( n == null) {
      name = "";
    } else {
      name = n;
    }    
  }

  public void setPrice(String p) {
    if ( p == null) {
      price  = 0;
      return;
    } 
    if (p.charAt(0) == '$') {
      p = p.substring(1, p.length());
    }
    try {
      price = Double.parseDouble(p);
    } catch (NumberFormatException e) {
      price = 0;
    }    
  }

  public void setUrl(String u) {
    if ( u == null) {
      url = "";
    } else {
      url = u;
    }    
  }

  public void setIamge(String u) {
    if ( u == null){
      imageURL = "";
    } else {
      imageURL = u;
    }    
  }

  public void setManufactor(String brand) {
    if (brand == null) {
      manufactor = "";
    } else if (brand.charAt(0) == ',') {
      manufactor = brand.substring(2, brand.length());
    } else {
      manufactor = brand;
    }
  }


  public void updateScore(double[] weight) {
    int length = qIndex.length;
    if (weight.length != qIndex.length) {
 //     throw new IllegalArgumentException("Weight array must have same length with queries! ");
      length = Math.min(weight.length, qIndex.length);
    }
    for (int i = 0; i < length; i++) {
      score += qIndex[i] * weight[i];
    }
  }
  
  public void setArrayIndex(int in) {
    index = in;
  }

  @Override
  public int compareTo(Item o) {
    // TODO Auto-generated method stub
    if (this.score == o.score) {
      return this.index > o.index ? 1 : -1;
    }
    return this.score > o.score ? -1 : 1;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof Item)) {
      return false;
    } else {
      Item other = (Item) o;
      return this.name.equals(other.name) && this.url.equals(other.url)
          && this.imageURL.equals(other.imageURL) && this.manufactor.equals(other.manufactor)
          && this.price == other.price;
    }
  }

  @Override
  public int hashCode() {
    int hash = 17;
    int hashMultiplikator = 31;
    hash =
        hash * hashMultiplikator + name.hashCode() + url.hashCode() + imageURL.hashCode()
            + manufactor.hashCode() + Double.toString(price).hashCode();
    return hash;
  }

  public static Comparator<Item> PriceComparator = new Comparator<Item>() {
    public int compare(Item it1, Item it2) {
      if (it1.price == it2.price) {
        return it1.index > it2.index ? 1 : -1;
      }
      return it1.price > it2.price ? 1 : -1;
    }
  };

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    // what format do you want?
    sb.append(name);
    sb.append(",$");
    sb.append(price);
    sb.append(",");
    sb.append(url);
    sb.append(",");
    sb.append(imageURL);
    sb.append(",");
    sb.append(manufactor);
    return sb.toString();
  }
}
