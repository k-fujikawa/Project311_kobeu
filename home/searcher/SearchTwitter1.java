package cs24.kitaguchi.Microblog.twitter.searcher;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * Servlet implementation class SearchTwitter1
 */
@WebServlet("/SearchTwitter1")
public class SearchTwitter1 extends HttpServlet {
	private static final long serialVersionUID = 1L;
	/** インデックスのディレクトリのパス */
	public static final String PATH_OF_INDEX = "../../../../../Users/KitaguchiSayaka/Desktop/IndexTest2";
	private int hitNumber; /** 検索結果の最大数 */

	private Directory         index;
	private Analyzer          analyzer;
	private IndexReader       reader;
	private IndexSearcher     searcher;
	private TwitterQueryMaker TQM;
	private TwitterSearcher   TS;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchTwitter1() {
      super();
      try { // lucene関連
        this.index = FSDirectory.open(new File(PATH_OF_INDEX));
        AnalyzerFactory af = new AnalyzerFactory(Version.LUCENE_36);
        this.analyzer = af.getJapaneseEnglishAnalyzer();
        this.reader = IndexReader.open(index);
        this.searcher = new IndexSearcher(reader);
        this.TQM = new TwitterQueryMaker(analyzer);
        this.TS = new TwitterSearcher(searcher, analyzer);
      } catch (IOException e) {
        System.err.println("error: SearchTwitter1コンストラクタlucene関連");
        e.printStackTrace();
        System.exit(1);
      }
      hitNumber = 100;
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();

    out.println("<!DOCTYPE html>");
    out.println("<html lang=\"ja\">");
    // head
    out.println("<head>");
    out.println("<meta http-equiv=\"content-type\" content=\"text/html\" charset=\"UTF-8\">");
    out.println("<link href=\"" + request.getContextPath() + "/css/MyStyle.css\" rel=\"stylesheet\">");
    out.println("<title>Twitter検索</title>");
    out.println("</head>");
    // body
    out.println("<body>");
      // ヘッダ
    out.println("<div id=\"header\">");
    out.println("Twitter検索");
    out.println("</div>");
    // タイトル
    out.println("<div id=\"top-title\">");
    out.println("<h1>Twitter検索</h1>");
    out.println("<p>Twitterを検索できます．</p>");
    out.println("</div>");
    // サーチゾーン
    out.println("<div id=\"search-zone\">");
    out.println("<form action=\"./SearchTwitter1\" method=\"POST\">");
    out.println("<p>");
    out.println("<input type=\"text\" name=\"QueryStr\" size=\"40\">");
    out.println("時間範囲指定");
    out.println("<input type=\"checkbox\" name=\"time-on-off\" value=\"1\">");
    displayTimeSelect("start-time", out);
    out.println("〜");
    displayTimeSelect("end-time", out);
    out.println("結果表示数");
    displayHitNumSelect(out);
    out.println("<input type=\"submit\" value=\"検索\">");
    out.println("</p>");
    out.println("</form>");
    out.println("</div>");
    // フッタ
    out.println("<br><br><hr>");
    out.println("<a style=\"text-align:center;\" href=\"http://localhost:8080/MicroblogSearcher/IndexPageSV\">Topに戻る</a>");
    out.println("</div>");
    out.println("</body>");
    out.println("</html>");
    out.close();
	 }


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("text/html; charset=UTF-8");
    request.setCharacterEncoding("UTF-8");
    PrintWriter out = response.getWriter();
    String queryStr = request.getParameter("QueryStr");
    String timeOnOff = request.getParameter("time-on-off");
    hitNumber = Integer.parseInt(request.getParameter("hitNumber"));
    System.out.println("timeOnOff = " + timeOnOff);
    //QueryTester queryTester = new QueryTester(topicsFile, qrelsFile);

    out.println("<!DOCTYPE html>");
    out.println("<html lang=\"ja\">");
    out.println("<head>");
    out.println("<meta http-equiv=\"../content-type\" content=\"text/html\" charset=\"UTF-8\">");
    out.println("<link href=\"" + request.getContextPath() + "/css/MyStyle.css\" rel=\"stylesheet\">");
    out.println("<title>Twitter検索</title>");
    out.println("</head>");
    // body
    out.println("<body>");
      // ヘッダ
    out.println("<div id=\"header\">");
    out.println("Twitter検索");
    out.println("</div>");
      // タイトル
    out.println("<div id=\"top-title\">");
    out.println("<h1>Twitter検索</h1>");
    out.println("<p>Twitterを検索できます．</p>");
    out.println("</div>");
      // サーチゾーン
    out.println("<div id=\"search-zone\">");
    out.println("<form action=\"./SearchTwitter1\" method=\"POST\">");
    out.println("<p>");
    out.println("<input type=\"text\" value=\"" + queryStr + "\" name=\"QueryStr\" size=\"40\">");
    out.println("時間範囲指定");
    out.println("<input type=\"checkbox\" name=\"time-on-off\" value=\"1\">");
    displayTimeSelect("start-time", out);
    out.println("〜");
    displayTimeSelect("end-time", out);
    out.println("結果表示数");
    displayHitNumSelect(out);
    out.println("<input type=\"submit\" value=\"検索\">");
    out.println("</p>");
    out.println("</form>");
    out.println("</div>");
    // 検索
    if (timeOnOff != null) {/*
      String startTime = request.getParameter("start-time");
      String endTime = request.getParameter("end-time");
      if( (startTime.compareTo(endTime)) < 0 ) {
        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();
        if(TimeParser.parseTimeSelect(startTime, startCal)) {
          if(TimeParser.parseTimeSelect(endTime, endCal)) {
            out.println("\"" + queryStr + "\"　（" + startTime + "〜" + endTime +"）の検索結果：");
            if (queryTester.existQuery(queryStr) == -1) { // テスト用クエリでない
            displaySearchTime(out, queryStr, startCal, endCal);
            }
            else { // テスト用クエリだ
              displayKanren(out);
              displaySearchTime(out, queryStr, startCal, endCal, queryTester);
            }
          }
        }
      }
      else {
        out.println("時間範囲の指定が異常です．");
      }*/
    }
    else { // 時間なし検索
      out.println("\"" + queryStr + "\"（時間指定なし）の検索結果：");/*
      if (queryTester.existQuery(queryStr) == -1) { // テスト用クエリでない
        displaySearchNormal(out, queryStr);
      }
      else { // テスト用クエリだ
        displayKanren(out);
        displaySearchNormal(out, queryStr, queryTester);
      }*/displaySearchNormal(out, queryStr);
    }
    // フッタ
    out.println("<br><br><hr>");
    out.println("<div id=\"footer\">");
    out.println("<a href=\"http://localhost:8080/MicroblogSearcher/IndexPageSV\">Topに戻る</a>");
    out.println("</div>");
    out.println("</body>");
    out.println("</html>");
    out.close();
    }

  /**
   * 時間範囲を選択するためのプルダウンメニューを作る
   * @param name
   * @param out
   */
  public void displayTimeSelect(String name, PrintWriter out) {
    out.println("<select name=\"" + name + "\">");
    out.println("<option value=\"2011/1/23\">2011/1/23</option>");
    out.println("<option value=\"2011/1/24\">2011/1/24</option>");
    out.println("<option value=\"2011/1/25\">2011/1/25</option>");
    out.println("<option value=\"2011/1/26\">2011/1/26</option>");
    out.println("<option value=\"2011/1/27\">2011/1/27</option>");
    out.println("<option value=\"2011/1/28\">2011/1/28</option>");
    out.println("<option value=\"2011/1/29\">2011/1/29</option>");
    out.println("<option value=\"2011/1/30\">2011/1/30</option>");
    out.println("<option value=\"2011/1/31\">2011/1/31</option>");
    out.println("<option value=\"2011/2/1\">2011/2/1</option>");
    out.println("<option value=\"2011/2/2\">2011/2/2</option>");
    out.println("<option value=\"2011/2/3\">2011/2/3</option>");
    out.println("<option value=\"2011/2/4\">2011/2/4</option>");
    out.println("<option value=\"2011/2/5\">2011/2/5</option>");
    out.println("<option value=\"2011/2/6\">2011/2/6</option>");
    out.println("<option value=\"2011/2/7\">2011/2/7</option>");
    out.println("<option value=\"2011/2/8\">2011/2/8</option>");
    out.println("</select>");
  }

  /**
   * 結果表示数を選択するためのプルダウンメニューを作る
   * @param out
   */
  public void displayHitNumSelect(PrintWriter out) {
    out.println("<select name=\"hitNumber\">");
    out.println("<option value=\"10\">10件</option>");
    out.println("<option value=\"30\">30件</option>");
    out.println("<option value=\"50\">50件</option>");
    out.println("<option value=\"70\">70件</option>");
    out.println("<option value=\"100\" selected>100件</option>");
    out.println("<option value=\"1000\">1000件</option>");
    out.println("</select>");
  }

  /**
   * 通常の検索を行い結果を表示する
   * @param out
   * @param queryStr 検索したい文字列
   */
  public void displaySearchNormal(PrintWriter out, String queryStr) {
    // 検索
    try {
      Query query = TQM.makeQuery(queryStr); // クエリ生成
      Result result = TS.search(query, hitNumber); // 検索
      if (result.size() > 0) {
        out.println("<table class=\"table-line\">");
        out.println("<tr class=\"table-head\">");
        out.println("<th class=\"table-line\">created_at</th><th class=\"table-line\">screen_name</th><th class=\"table-line\">text</th><th class=\"table-line\">reply_name</th>");
        out.println("</tr>");
        for(int i=0; i<result.size(); i++) {
          Document d = result.getHitDocumentAt(i);
          out.println("<tr class=\"table-line\">");
          out.println("<td class=\"table-line\">" + d.get("created_at") + "</td>");
          out.println("<td class=\"table-line\">" + d.get("screen_name") + "</td>");
          out.println("<td class=\"table-line\">" + d.get("text") + "</td>");
          out.println("<td class=\"table-line\">" + d.get("reply_name") + "</td>");
          out.println("</tr>");
        }
        out.println("</table>");
      }
      else {
        out.println(queryStr + "に一致する情報は見つかりませんでした．");
      }
    } catch (ParseException e) {
      // TODO 自動生成された catch ブロック
      e.printStackTrace();
    } catch (IOException e) {
      // TODO 自動生成された catch ブロック
      e.printStackTrace();
    }
  }

}
