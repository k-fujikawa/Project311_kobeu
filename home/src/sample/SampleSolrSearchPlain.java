package sample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class SampleSolrSearchPlain {

  private static final String solrRequestURL = "http://localhost:8983/solr/collection1/select?wt=json&indent=true&fl=*%2Cscore&rows=1000&q=text%3A";
  protected static URL solrUrl = null;
  public static final String POST_ENCODING = "UTF-8";


  /**
   * @param args
   */
  public static void main(String[] args) {
    try {
      test1();
    } catch (IOException e) {
      // TODO 自動生成された catch ブロック
      e.printStackTrace();
    }
  }

  private static void test1() throws IOException {
    // クエリ
    String query = "地震 福島";

    // リクエストURLの作成
    StringBuffer sb = new StringBuffer();
    sb.append(solrRequestURL);
    sb.append(URLEncoder.encode(query, "UTF-8"));
    solrUrl = new URL(sb.toString());

    // リクエスト
    HttpURLConnection urlc = null;
    urlc = (HttpURLConnection) solrUrl.openConnection();
    urlc.setRequestMethod("GET");
    urlc.setRequestProperty("Content-type", "text/xml; charset=" + POST_ENCODING);
    urlc.connect();

    // リクエスト結果受信
    BufferedReader reader =
        new BufferedReader(new InputStreamReader(urlc.getInputStream(), POST_ENCODING));

    // 結果の処理
    int lineNum = 0;
    int lineNum2 = 0;
    while (true) {
      String line = reader.readLine(); // 結果を１行ずつ読み込む
      lineNum++;
      if ( line == null ) {
        break;
      }
      if (lineNum < 12) {
        continue;
      }
      lineNum2++;
      if (lineNum2 == 2) {
        //System.out.println(line);
        String tweetid = line.substring(20, line.length()-2);
        System.out.println(tweetid);
      } else if (lineNum2 == 3) {
        //System.out.println(line);
        String userid = line.substring(19, line.length()-2);
        System.out.println(userid);
      } else if (lineNum2 == 4) {
        //System.out.println(line);
        String datetime = line.substring(20, line.length()-2);
        System.out.println(datetime);
      } else if (lineNum2 == 5) {
        //System.out.println(line);
        String text = line.substring(16, line.length()-2);
        System.out.println(text);
      } else if (lineNum2 == 6) {
        //System.out.println(line);
        String score = line.substring(16, line.length()-2);
        System.out.println(score);
      } else if (lineNum2 == 7) {
        //System.out.println(line);
        lineNum2 = 1;
      }
    }
    // 終了処理
    if (urlc != null) {
      urlc.disconnect();
    }
  }

}
