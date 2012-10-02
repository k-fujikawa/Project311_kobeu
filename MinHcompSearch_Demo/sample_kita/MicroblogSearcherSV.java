package cs24.kitaguchi.Microblog.searcher;

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
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;


/**
 * Servlet implementation class MicroblogSearcherSV
 */
@WebServlet("/MicroblogSearcherSV")
public class MicroblogSearcherSV extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static final String PATH_OF_INDEX = "../../../../../Users/kitaguchisayaka/Documents/workspace/MicroblogSearcher/WebContent/Index_Microblog";
	public static final int TWEET_SUM_N = 16141812;
	public static final int CLUSTER_SIZE_PARAM = 3; // 吹き出しトップに表示するTweet数決定のためのlogの底

  private String topicsFile ="/Users/kitaguchisayaka/ProRin/topics.MB1-50.txt";
  private String qrelsFile = "/Users/kitaguchisayaka/ProRin/microblog11-qrels_new";
  private int hitNumber;
  private int clusterNum;

  private Directory index;
  private IndexReader reader;
  private IndexSearcher searcher;
  private Result result;
  private ResultMMR resultMMR;
  private double lambda;
  private String timeOnOff;
  private String time;
  private Calendar minCal; // 時間指定できる最初の時刻
  private Calendar maxCal; // 時間指定できる最後の時刻

    /**
     * @see HttpServlet#HttpServlet()
     */
    public MicroblogSearcherSV() {
        super();
        //System.out.println("call MicroblogSearvletSV()");
        try { // lucene関連
          index = FSDirectory.open(new File(PATH_OF_INDEX));
          reader = IndexReader.open(index);
          searcher = new IndexSearcher(reader);
        } catch (IOException e) {
          System.err.println("error:MicroblogSearcherSVコンストラクタlucene関連");
          e.printStackTrace();
          System.exit(1);
        }
        hitNumber = 100; // 検索件数の初期師設定
        setClusterNum(3); // クラスタの数の初期値設定
        setLambda(0.3); // λの初期値設定
        minCal = Calendar.getInstance();
        minCal.set(2011, Calendar.JANUARY, 23, 0, 0, 0);
        maxCal = Calendar.getInstance();
        maxCal.set(2011, Calendar.FEBRUARY, 9, 0, 0, 0);
        }

    /**
     * 終了
     */
    public void destroy() {
      try {
        index.close();
        reader.close();
        searcher.close();
      } catch (IOException e) {
        // TODO 自動生成された catch ブロック
        e.printStackTrace();
      }
      super.destroy();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      //System.out.println("call doGet");
      response.setContentType("text/html; charset=UTF-8");
      PrintWriter out = response.getWriter();

      // html開始タグ+head
      displayHead(out, request);
      // body
      out.println("<body>");

      // ヘッダ
      displayHeader(out);

      // タイトル＆検索スペース
      displayNavbar(out, "");

      // 本体スペース
      out.println("<div style=\"height:800px;\">");
      out.println("</div>");

      // フッタ
      out.println("<br><br>");
      displayFooter(out);

      // jQuery
      displayJQuery(out, request);

      out.println("</body>");
      out.println("</html>");
      out.close();
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      //System.out.println("call doPost");
      System.out.println("*********************************");
      // 時間計測
      long startPostTime = System.currentTimeMillis();
      response.setContentType("text/html; charset=UTF-8");
      request.setCharacterEncoding("UTF-8");
      PrintWriter out = response.getWriter();
      String queryStr = request.getParameter("QueryStr");
      lambda = Double.parseDouble(request.getParameter("lambda_val")); // MMRのパラメタλ
      hitNumber = Integer.parseInt(request.getParameter("hitnumber")); // 見つけるTweetの最大数
      clusterNum = Integer.parseInt(request.getParameter("clusternum")); // クラスタの数
      timeOnOff = request.getParameter("time-on-off"); // 時間指定するか否か
      time = request.getParameter("time"); // 指定時間
      //hitNumber = Integer.parseInt(request.getParameter("hitNumber"));
      QueryTester queryTester = new QueryTester(topicsFile, qrelsFile);
      if(timeOnOff != null) {
        System.out.println("timeOnOff= " + timeOnOff);
        System.out.println("time: " + time);
      }

      // html開始タグ+head
      displayHead(out, request);

      // body
      out.println("<body>");
        // ヘッダ
      displayHeader(out);
      // タイトル＆検索スペース
      displayNavbar(out, queryStr);

      if (timeOnOff == null) {
        // 検索
        searchMMR(queryStr, lambda);
        // Subtopic一覧表示
        displaySubtopics(out);

        // 関連度表示
        if (queryTester.existQuery(queryStr) != -1) {
          displayKanren(out);
        }

        // タイムライン表示
        out.println("<div class=\"timeline\">");
        Calendar topCal = Calendar.getInstance();
        Calendar bottomCal = Calendar.getInstance();
        topCal.set(2011, Calendar.FEBRUARY, 8); // 現在表示中の日に表示する最新の日付をセット
        bottomCal.set(2011, Calendar.JANUARY, 23); // 表示する最も古い日付をセット
        int clusterIndex = 0; // 現在表示するクラスタ
        Calendar clusterCal; // クラスタの日付
        displayMonth(out, topCal.get(Calendar.MONTH), topCal.get(Calendar.YEAR));
        while( // 現在表示中の日付が表示する最も古い日付になるまで繰り返す
            (topCal.get(Calendar.YEAR) != bottomCal.get(Calendar.YEAR)) ||
            (topCal.get(Calendar.MONTH) != bottomCal.get(Calendar.MONTH)) ||
            (topCal.get(Calendar.DAY_OF_MONTH) != bottomCal.get(Calendar.DAY_OF_MONTH))
            )
        {
          if(clusterIndex >= clusterNum) { //表示するクラスタがなくなったときは最後まで日付円を表示
            displayDay(out, topCal.get(Calendar.DAY_OF_MONTH));
            topCal.add(Calendar.DAY_OF_MONTH, -1);
            continue;
          }
          clusterCal = resultMMR.getClusterTime(clusterIndex);
          if ( // 表示するツイートがある日
              (topCal.get(Calendar.YEAR) == clusterCal.get(Calendar.YEAR)) &&
              (topCal.get(Calendar.MONTH) == clusterCal.get(Calendar.MONTH)) &&
              (topCal.get(Calendar.DAY_OF_MONTH) == clusterCal.get(Calendar.DAY_OF_MONTH))
              )
          {
            int[] clusterIndexA = new int[1]; //参照渡しのため
            clusterIndexA[0] = clusterIndex;
            if (queryTester.existQuery(queryStr) == -1) { // テスト用クエリでない
              displayDay(out, topCal.get(Calendar.DAY_OF_MONTH), clusterIndexA, clusterCal);
            } else { // テスト用クエリだ
              displayDay(out, topCal.get(Calendar.DAY_OF_MONTH), clusterIndexA, queryTester, queryStr, clusterCal);
            }
            clusterIndex = clusterIndexA[0];
            clusterIndex++; //クラスタインデックスを進める．
          }
          else { // 表示するツイートがない日
            displayDay(out, topCal.get(Calendar.DAY_OF_MONTH));
          }
          int month = topCal.get(Calendar.MONTH);
          topCal.add(Calendar.DAY_OF_MONTH, -1); //日付を戻す
          int topMonth = topCal.get(Calendar.MONTH);
          if (topMonth != month) { //月が変わったとき
            displayMonth(out, topMonth, topCal.get(Calendar.YEAR));
          }
        } //タイムラインのwhile終了
        out.println("</div>");
      } // 日付指定なしのときの表示の終了
      else { // 日付指定あり検索
        // 開始時間と終了時間の設定
        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();
        TimeParser.parseTimeSelect(time, startCal);
        TimeParser.parseTimeSelect(time, endCal);
        endCal.add(Calendar.DAY_OF_MONTH, 1);
        if (startCal.compareTo(minCal) >= 0
            && endCal.compareTo(maxCal) <= 0) {
          out.print("<h1 style=\"margin-left:30px; color:#504F4F;");
          out.print(" text-shadow: 0px 2px 1px #BBBABA;\">");
          out.print(startCal.get(Calendar.YEAR) + "年" + (1 + startCal.get(Calendar.MONTH)) +"月");
          out.print(startCal.get(Calendar.DAY_OF_MONTH) + "日");
          out.println("</h1>");
          if (queryTester.existQuery(queryStr) == -1) { // テスト用クエリでない
            displaySearchTime(out, queryStr, startCal, endCal);
          }
          else {
            displayKanren(out);
            displaySearchTime(out, queryStr, startCal, endCal, queryTester);
          }
        } else { // 時間指定が不正
          out.print("<div style=\"margin-left:20px;\">");
          out.print("コーパスには" + time + "のTweetは含まれていません．");
          out.println("</div>");
        }

      }
      // フッター
      displayFooter(out);

      // jQuery
      displayJQuery(out, request);

      out.println("</body>");
      out.println("</html>");

      out.close();

      long endPostTime = System.currentTimeMillis();
      System.out.println("doPost呼び出しから表示終了までの時間：" + Long.toString(endPostTime - startPostTime) + "(ms)");
    }

    /**
     * <html>開始タグ，<head>〜</head>を描画する
     * @param out
     */
    public void displayHead(PrintWriter out, HttpServletRequest request) {
      out.println("<!DOCTYPE html>");
      out.println("<html lang=\"ja\">");
      // head
      out.println("<head>");
      out.println("<meta http-equiv=\"content-type\" content=\"text/html\" charset=\"UTF-8\">");
      out.println("<link rel=\"shortcut icon\" href=\"" + request.getContextPath() + "/image/favicon_smile.ico\">");
      out.println("<link href=\"" + request.getContextPath() + "/css/mycss3.css\" rel=\"stylesheet\">");
      out.println("<link type=\"text/css\" href=\"" + request.getContextPath() + "/css/jquery.ui.all.css\" rel=\"stylesheet\">");
      //out.println("<link href=\"" + request.getContextPath() + "/css/jquery.ui.datepicker.css\" rel=\"stylesheet\">");
      //out.println("<link href=\"" + request.getContextPath() + "/css/jquery.ui.base.css\" rel=\"stylesheet\">");
      //out.println("<link href=\"" + request.getContextPath() + "/css/jquery.ui.theme.css\" rel=\"stylesheet\">");
      out.println("<title>Tweets2011 コーパス検索2</title>");
      //out.println("<script src=\"http://code.jquery.com/jquery-1.7.2.min.js\"></script>");
      out.println("<script src=\"" + request.getContextPath() + "/jquery-1.7.2.js\"></script>");
      out.println("<script type='text/javascript' src=\"" + request.getContextPath() + "/jquery.ui.datepicker-ja.js\"></script>");
      out.println("<script type='text/javascript' src=\"" + request.getContextPath() + "/jquery.ui.core.js\"></script>");
      out.println("<script type='text/javascript' src=\"" + request.getContextPath() + "/jquery.ui.widget.js\"></script>");
      out.println("<script type='text/javascript' src=\"" + request.getContextPath() + "/jquery.ui.datepicker.js\"></script>");
      out.println("</head>");
    }


    /**
     * bodyタグ内のヘッダーを描画する
     * @param out
     */
    public void displayHeader(PrintWriter out) {
      out.println("<div id=\"header\">");
      out.println("<a href=\"http://localhost:8080/MicroblogSearcher/IndexPageSV\" class=\"other-ver\">TOP</a>&nbsp &nbsp");
      out.println("<a href=\"http://localhost:8080/MicroblogSearcher/MicroblogSearcherSV1\" class=\"other-ver\">ver1.0</a>&nbsp");
      out.println("<a href=\"http://localhost:8080/MicroblogSearcher/MicroblogSearcherSV\" style=\"color:white;\">ver2.0</a>&nbsp");
      out.println("<a href=\"http://localhost:8080/MicroblogSearcher/MicroblogSearcherSV2_1\" class=\"other-ver\">ver2.1</a>&nbsp");
      out.println("</div>");
    }

    /**
     * bodyタグ内のフッターを描画する
     * @param out
     */
    public void displayFooter(PrintWriter out) {
      out.println("<div id=\"footer\">");
      out.println("<hr>");
      out.println("<a href=\"http://localhost:8080/MicroblogSearcher/IndexPageSV\">Topに戻る</a>");
      out.println("</div>");
    }

    /**
     * タイトル＆検索スペースを描画する
     * @param out
     */
    public void displayNavbar(PrintWriter out, String queryStr) {
      out.println("<div class=\"navbar\">");
      out.println("<div class=\"navbar-inner\">");
      out.println("<h1>Tweets2011 コーパス検索2</h1>");
      out.println("<div id=\"serch-zone\">");
      out.println("<form action=\"./MicroblogSearcherSV\" method=\"POST\">");
      out.println("<p>");
      out.println("<input type=\"search\" name=\"QueryStr\" size=\"40\" required=\"required\" value=\"" + queryStr + "\">");
      out.println("<input type=\"submit\" value=\"検索\">");
      out.println("&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp");
      out.println("<span style=\"color:white;\">λ");
      out.println("<input type=\"number\" name=\"lambda_val\" id=\"lambda_value\" min=\"0.00\" max=\"1.00\" step=\"0.05\" value=\"" + lambda + "\" size=\"3\">");
      out.println("<input type=\"range\" name=\"lambda_rg\" id=\"lambda_range\" min=\"0.00\" max=\"1.00\" step=\"0.05\" value=\"" + lambda + "\">");
      out.println("</span>");
      out.println("&nbsp &nbsp &nbsp &nbsp");
      out.println("<span style=\"color:white;font-size:14.5px;\">");
      out.println("見つけるTweetの最大数");
      out.println("<input type=\"text\" name=\"hitnumber\" pattern=\"^[0-9]+$\" value=\"" + hitNumber + "\" size=\"4\">");
      out.println("件");
      out.println("<br><br>");
      out.println("&nbsp &nbsp &nbsp");
      out.println("<input type=\"checkbox\" name=\"time-on-off\" id=\"timecheck\" value=\"1\">");
      out.println("日付指定");
      out.println("<input type=\"text\" name=\"time\" id=\"datepicker\">");
      out.println("&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp");
      out.println("見つけるサブトピックの数");
      out.println("<input type=\"text\" name=\"clusternum\" pattern=\"^[1-9]$\" value=\"" + clusterNum + "\" size=\"1\">");
      out.println("&nbsp &nbsp &nbsp &nbsp");
      out.println("<input type=\"checkbox\" name=\"dayonoff\" id=\"daycheck\" value=\"1\">");
      out.println("サブトピックのある日のみ表示");
      out.println("</span>");
      out.println("</p>");
      out.println("</form>");
      out.println("</div>");
      out.println("</div>");
      out.println("</div>");
    }

    /**
     * jQuery，JavaScript部分を描画する
     * @param out
     */
    public void displayJQuery(PrintWriter out,  HttpServletRequest request) {
      out.println("<script>");
      out.println("$(function() {");
      // 吹き出しの初期状態は閉じ
      out.println("$(document).ready(function(){");
      out.println("$('.arrow_box > p').siblings('.hukidashi-hide').hide();");
      out.println("});");
      // 吹き出しをクリックすると開閉
      out.println("$('.arrow_box > p').click(function(){");
      out.println("$(this).siblings('.hukidashi-hide').slideToggle('slow');");
      out.println("});");
      // ラムダの値を表示
      out.println("$('#lambda_range').change(function() {");
      out.println("var lam1 = $('#lambda_range').val();");
      out.println("$('#lambda_value').val(lam1);");
      out.println("});");
      out.println("$('#lambda_value').change(function() {");
      out.println("var lam2 = $('#lambda_value').val();");
      out.println("$('#lambda_range').val(lam2);");
      out.println("});");
      // ヘッダのリンクをオンマウスでハイライト
      out.println("$('#header > .other-ver').mouseover(function() {");
      out.println("$(this).css(\"color\",\"white\");");
      out.println("});");
      // ヘッダのリンクをマウスアウトで非ハイライト
      out.println("$('#header > .other-ver').mouseout(function() {");
      out.println("$(this).css(\"color\",\"#999999\");");
      out.println("});");
      // サブトピックのある日のみ表示
      out.println("$('#daycheck').change(function() {");
      out.println("$('.day-non').toggle();");
      out.println("});");
      // 時間指定UI
      out.println("$('#datepicker').datepicker({");
      //out.println("showOn: 'button',");
      //out.println("buttonImage: '" + request.getContextPath() + "/image/calendar.gif',");
      //out.println("buttonImageOnly: true,");
      out.println("dateFormat: 'yy/mm/dd',");
      out.println("changeMonth: true,");
      out.println("changeYear: true,");
      out.println("yearRange: '2011:2012',");
      out.println("showMonthAfterYear: false");
      out.println("});");
      out.println("});");
      out.println("</script>");
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
      //System.out.println("call displayHitNumSelect");
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
     * 関連度の色分けを示す
     * @param out
     */
    public void displayKanren(PrintWriter out) {
      //System.out.println("call displayKanren");
      out.println("<br><table class =\"table-line\">");
      out.println("<tr class=\"table-line\">");
      out.println("<th class=\"table-verygood\">強い関係がある</th>");
      out.println("<th class=\"table-good\">関係がある</th>");
      out.println("<th class=\"table-normal\">あまり関係がない</th>");
      out.println("<th class=\"table-spam\">全く関係がない（スパム等）</th>");
      out.println("</tr>");
      out.println("</table><br>");
    }

    /**
     * 通常の検索を行い結果を表示する
     * @param out
     * @param queryStr
     */
    public void displaySearchNormal(PrintWriter out, String queryStr) {
      // lucene関連
      Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
      MakeQueryMicroblog MQM = new MakeQueryMicroblog();
      SearcherMicroblog searcherMB = new SearcherMicroblog(searcher);
      // 検索
      try {
        Query query  = MQM.makeQuery(queryStr, analyzer);
        Result result = searcherMB.search(query, hitNumber);

        if (result.size() > 0) {
          out.println("<table class=\"table-line\">");
          out.println("<tr class=\"table-head\">");
          out.println("<th class=\"table-line\">ID</th><th class=\"table-line\">TIME</th><th class=\"table-line\">USER</th><th class=\"table-line\">TWEET</th>");
          out.println("</tr>");
          for(int i=0; i<result.size(); i++) {
            Document d = result.getHitDocumentAt(i);
            out.println("<tr class=\"table-line\">");
            out.println("<td class=\"table-line\">" + d.get("ID") + "</td>");
            out.println("<td class=\"table-line\">" + d.get("TIME") + "</td>");
            out.println("<td class=\"table-line\">" + d.get("USER") + "</td>");
            out.println("<td class=\"table-line\">" + d.get("TWEET") + "</td>");
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

    /**
     * 通常の検索を行い結果を表示する．<br>
     * テストデータに基づき，クエリとTweetの関連度を色分けして表示．
     * @param out
     * @param queryStr
     * @param queryTester
     */
    public void displaySearchNormal(PrintWriter out, String queryStr, QueryTester queryTester, double lambda) {
      // lucene関連
      Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
      MakeQueryMicroblog MQM = new MakeQueryMicroblog();
      SearcherMicroblog searcherMB = new SearcherMicroblog(searcher);
      // クエリテスト関連
      int queryID = queryTester.existQuery(queryStr);
      queryTester.setNowTestMap(queryID);
      String cssClass = "table-line";
      // 検索
      try {
        Query query  = MQM.makeQuery(queryStr, analyzer);
        Result result = searcherMB.search(query, hitNumber);
        MicroblogMMR mmr = new MicroblogMMR(index, reader, searcher, lambda, clusterNum);
        ResultMMR resultMMR = mmr.MMRfast(result.getHits());
        ScoreDoc[] ddd = new ScoreDoc[3];
        for(int i=0; i < clusterNum; i++) {
          int docId = resultMMR.getScoreDocAtFrom(i, 0).doc;
          System.out.println("*******************************");
          System.out.println("*******************************");
          System.out.print(resultMMR.getScoreDocAtFrom(i, 0).score + " * ");
          Calendar cal = resultMMR.getClusterTime(i);
          TimeParser.show(cal);
          System.out.println(searcher.doc(docId).get("TWEET"));
          /*for (int j=0; j<MicroblogSearcherSV.CLUSTER_NUM; j++) {
            for (int k=0; k<resultMMR.sizeAt(j); k++) {
              int resId = resultMMR.getScoreDocAtFrom(j, k).doc;
              System.out.println(searcher.doc(resId).get("TWEET"));
            }
          }
          */
          ddd[i] = resultMMR.getScoreDocAtFrom(i, 0);
        }
        //System.out.println("d1-d2 :::::: " + mmr.sim2(ddd[0], ddd[1]));
        //System.out.println("d1-d3 :::::: " + mmr.sim2(ddd[0], ddd[2]));
        //System.out.println("d2-d3 :::::: " + mmr.sim2(ddd[1], ddd[2]));


        if (result.size() > 0) {
          out.println("<table class=\"table-line\">");
          out.println("<tr class=\"table-head\">");
          out.println("<th class=\"table-line\">ID</th><th class=\"table-line\">TIME</th><th class=\"table-line\">USER</th><th class=\"table-line\">TWEET</th>");
          out.println("</tr>");
          for(int i=0; i<result.size(); i++) {
            Document d = result.getHitDocumentAt(i);
            // 関連度による色分け
            int point = queryTester.getPoint(d.get("ID"));
            if (point == 2) { // とてもマッチしている
              cssClass = "table-verygood";
            } else if (point == 1) { // マッチしている
              cssClass = "table-good";
            } else if (point == -2) {
              cssClass = "table-spam";
            } else {
              cssClass = "table-line";
            }
            out.println("<tr class=\"" + cssClass + "\">");
            out.println("<td class=\"table-line\">" + d.get("ID") + "</td>");
            out.println("<td class=\"table-line\">" + d.get("TIME") + "</td>");
            out.println("<td class=\"table-line\">" + d.get("USER") + "</td>");
            out.println("<td class=\"table-line\">" + d.get("TWEET") + "</td>");
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

    /**
     * 時間範囲を指定して検索を行い，結果を表示する
     * @param out PrintWriter
     * @param queryStr クエリ文字列
     * @param startCal 開始時間
     * @param endCal 終了時間
     */
    public void displaySearchTime(PrintWriter out, String queryStr, Calendar startCal, Calendar endCal) {
      // lucene関連
      Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
      MakeQueryMicroblog MQM = new MakeQueryMicroblog();
      SearcherMicroblog searcherMB = new SearcherMicroblog(searcher);
      // 検索
      try {
        Query query = MQM.makeQuery(queryStr, analyzer);
        Result result = searcherMB.searchWithTime(query, hitNumber, startCal, endCal);
        if (result.size() > 0) {
          out.println("<table class=\"table-line\">");
          out.println("<tr class=\"table-head\">");
          out.println("<th class=\"table-line\">ID</th><th class=\"table-line\">TIME</th><th class=\"table-line\">USER</th><th class=\"table-line\">TWEET</th>");
          out.println("</tr>");
          for(int i=0; i<result.size(); i++) {
            Document d = result.getHitDocumentAt(i);
            out.println("<tr class=\"table-line\">");
            out.println("<td class=\"table-line\">" + d.get("ID") + "</td>");
            out.println("<td class=\"table-line\">" + d.get("TIME") + "</td>");
            out.println("<td class=\"table-line\">" + d.get("USER") + "</td>");
            out.println("<td class=\"table-line\">" + d.get("TWEET") + "</td>");
            out.println("</tr>");
          }
          out.println("</table>");
        }
        else {
          out.println("ご指定の時間範囲には" + queryStr + "に一致する情報は見つかりませんでした．");
        }
      } catch (ParseException e) {
        // TODO 自動生成された catch ブロック
        e.printStackTrace();
      } catch (IOException e) {
        // TODO 自動生成された catch ブロック
        e.printStackTrace();
      }
    }

    /**
     * 時間範囲を指定して検索を行い，結果を表示する．<br>
     * テストデータに基づき，クエリとTweetの関連度を色分けして表示．
     * @param out PrintWriter
     * @param queryStr クエリ文字列
     * @param startCal 開始時間
     * @param endCal 終了時間
     * @param queryID クエリID
     * @param queryTester
     */
    public void displaySearchTime(PrintWriter out, String queryStr, Calendar startCal, Calendar endCal, QueryTester queryTester) {
      // lucene関連
      Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
      MakeQueryMicroblog MQM = new MakeQueryMicroblog();
      SearcherMicroblog searcherMB = new SearcherMicroblog(searcher);
      // クエリテスト関連
      int queryID = queryTester.existQuery(queryStr);
      queryTester.setNowTestMap(queryID);
      String cssClass = "table-line";
      // 検索
      try {
        Query query = MQM.makeQuery(queryStr, analyzer);
        Result result = searcherMB.searchWithTime(query, hitNumber, startCal, endCal);
        if (result.size() > 0) {
          out.println("<table class=\"table-line\">");
          out.println("<tr class=\"table-head\">");
          out.println("<th class=\"table-line\">ID</th><th class=\"table-line\">TIME</th><th class=\"table-line\">USER</th><th class=\"table-line\">TWEET</th>");
          out.println("</tr>");
          for(int i=0; i<result.size(); i++) {
            Document d = result.getHitDocumentAt(i);
            // 関連度による色分け
            int point = queryTester.getPoint(d.get("ID"));
            if (point == 2) { // とてもマッチしている
              cssClass = "table-verygood";
            } else if (point == 1) { // マッチしている
              cssClass = "table-good";
            } else if (point == -2) {
              cssClass = "table-spam";
            } else {
              cssClass = "table-normal";
            }
            out.println("<tr class=\"" + cssClass + "\">");
            out.println("<td class=\"table-line\">" + d.get("ID") + "</td>");
            out.println("<td class=\"table-line\">" + d.get("TIME") + "</td>");
            out.println("<td class=\"table-line\">" + d.get("USER") + "</td>");
            out.println("<td class=\"table-line\">" + d.get("TWEET") + "</td>");
            out.println("</tr>");
          }
          out.println("</table>");
        }
        else {
          out.println("ご指定の時間範囲には" + queryStr + "に一致する情報は見つかりませんでした．");
        }
      } catch (ParseException e) {
        // TODO 自動生成された catch ブロック
        e.printStackTrace();
      } catch (IOException e) {
        // TODO 自動生成された catch ブロック
        e.printStackTrace();
      }
    }

    /**
     * Subtopic一覧を表示します．
     * @param out
     */
    public void displaySubtopics(PrintWriter out) {
      //System.out.println("call displaySubtopics");
      out.println("<div id=\"subtopic\">");
      out.println("<h3>Subtopics</h3>");
      for (int i=0; i < clusterNum; i++) {
        int docId = resultMMR.getScoreDocAtFrom(i, 0).doc;
        String color = colorseter(i);
        try {
          out.println("<span style=\"color:" + color + ";\">★</span>&nbsp<a href=\"#tweet" + Integer.toString(i) + "\">" + searcher.doc(docId).get("TWEET") + "</a><br>");
        } catch (CorruptIndexException e) {
          // TODO 自動生成された catch ブロック
          e.printStackTrace();
        } catch (IOException e) {
          // TODO 自動生成された catch ブロック
          e.printStackTrace();
        }
      }
      out.println("</div>");
    }

    /**
     * 月の境を示すラインを描画します
     * @param out
     * @param month
     */
    public void displayMonth(PrintWriter out, int month, int year) {
      //System.out.println("call displayMonth");
      out.println("<div class=\"month-box\">");
      out.println("<div class=\"month-line\">");
      // 何月か判断する
      String mst;
      if (month == Calendar.JANUARY) mst = "Jan";
      else if (month == Calendar.FEBRUARY) mst = "Feb";
      else if (month == Calendar.MARCH) mst = "Mar";
      else if (month == Calendar.APRIL) mst = "Apr";
      else if (month == Calendar.MAY) mst = "May";
      else if (month == Calendar.JUNE) mst = "Jun";
      else if (month == Calendar.JULY) mst = "Jul";
      else if (month == Calendar.AUGUST) mst = "Aug";
      else if (month == Calendar.SEPTEMBER) mst = "Sep";
      else if (month == Calendar.OCTOBER) mst = "Oct";
      else if (month == Calendar.NOVEMBER) mst = "Nov";
      else if (month == Calendar.DECEMBER) mst = "Dec";
      else mst = "?";
      out.println("<h1>" + mst + "</h1><br>");
      out.println("<p>" + Integer.toString(year) + "</p><br>");
      out.println("<hr>");
      out.println("</div>");
      out.println("</div>");
    }

    /**
     * 表示するTweetがない日の円を描画する
     * @param out
     * @param day 日付
     */
    public void displayDay(PrintWriter out, int day) {
      //System.out.println("call displayDay");
      out.println("<div class=\"day-non\">");
      out.println("<div class=\"circle\" style=\"background-color:white;\">");
      out.println("<p>" + Integer.toString(day) + "</p>");
      out.println("</div>");
      out.println("</div>");
    }

    /**
     * 表示するTweetがある日の円を描画する
     * @param out
     * @param day
     * @param clusterIndex
     */
    public void displayDay(PrintWriter out, int day, int[] clusterIndexA, Calendar clusterCal) {
      int clusterIndex = clusterIndexA[0];
      //System.out.println("call displayDay");
      String link = "tweet" + Integer.toString(clusterIndex); //リンクラベル名
      String color = colorseter(clusterIndex);
      out.println("<div class=\"day\">");
      out.println("<div class=\"circle\" style=\"background-color:" + color + ";\">");
      out.println("<p>" + Integer.toString(day) + "</p>");
      out.println("</div>");
      // 吹き出し
      out.println("<a name=\"" + link + "\"><div class=\"arrow_box\"></a>");
      displayTweet(out, clusterIndex); // 吹き出し中のTweet描画
      out.println("</div>");
      // クラスタの日付が重なっているときの処理
      while ((clusterIndex+1) < clusterNum) {
        if (
            (resultMMR.getClusterTime(clusterIndex + 1).get(Calendar.YEAR) == clusterCal.get(Calendar.YEAR)) &&
            (resultMMR.getClusterTime(clusterIndex + 1).get(Calendar.MONTH) == clusterCal.get(Calendar.MONTH)) &&
            (resultMMR.getClusterTime(clusterIndex + 1).get(Calendar.DAY_OF_MONTH) == clusterCal.get(Calendar.DAY_OF_MONTH))
            )
        { // 重なっている
          clusterIndex++; // クラスタインデックスを進める
          link = "tweet" + Integer.toString(clusterIndex); //リンクラベル名
          // 吹き出し
          out.println("<a name=\"" + link + "\"><div class=\"arrow_box\"></a>");
          displayTweet(out, clusterIndex); // 吹き出し中のTweet描画
          out.println("</div>");
        }
        else { //重なっていない
          break;
        }
      }
      clusterIndexA[0] = clusterIndex;
      out.println("</div>");
    }

    /**
     * 表示するTweetがある日の円を描画する<br>
     * クエリテスト版
     * @param out
     * @param day
     * @param clusterIndex
     */
    public void displayDay(PrintWriter out, int day, int[] clusterIndexA, QueryTester queryTester, String queryStr, Calendar clusterCal) {
      int clusterIndex = clusterIndexA[0];
      //System.out.println("call displayDay");
      String link = "tweet" + Integer.toString(clusterIndex); //リンクラベル名
      String color = colorseter(clusterIndex);
      out.println("<div class=\"day\">");
      out.println("<div class=\"circle\" style=\"background-color:" + color + ";\">");
      out.println("<p>" + Integer.toString(day) + "</p>");
      out.println("</div>");
      // 吹き出し
      out.println("<a name=\"" + link + "\"><div class=\"arrow_box\"></a>");
      displayTweet(out, clusterIndex, queryTester, queryStr); // 吹き出し中のTweet描画
      out.println("</div>");
      // クラスタの日付が重なっているときの処理
      while ((clusterIndex+1) < clusterNum) {
        if (
            (resultMMR.getClusterTime(clusterIndex + 1).get(Calendar.YEAR) == clusterCal.get(Calendar.YEAR)) &&
            (resultMMR.getClusterTime(clusterIndex + 1).get(Calendar.MONTH) == clusterCal.get(Calendar.MONTH)) &&
            (resultMMR.getClusterTime(clusterIndex + 1).get(Calendar.DAY_OF_MONTH) == clusterCal.get(Calendar.DAY_OF_MONTH))
            )
        { // 重なっている
          clusterIndex++; // クラスタインデックスを進める
          link = "tweet" + Integer.toString(clusterIndex); //リンクラベル名
          // 吹き出し
          out.println("<a name=\"" + link + "\"><div class=\"arrow_box\"></a>");
          displayTweet(out, clusterIndex); // 吹き出し中のTweet描画
          out.println("</div>");
        }
        else { //重なっていない
          break;
        }
      }
      clusterIndexA[0] = clusterIndex;
      out.println("</div>");
    }

    /**
     * 日付円の色を決定する．
     * @param clusterIndex
     * @return
     */
    public String colorseter(int clusterIndex) {
      //System.out.println("call colorseter");
      String color;
      int size = resultMMR.sizeAt(clusterIndex);
      if (size > 50) color = "#ff0083";
      else if (size > 40) color = "#ff3299";
      else if (size > 30) color = "#ff66b2";
      else if (size > 20) color = "#ff99ca";
      else if (size > 10) color = "#ffc4df";
      else color = "#ffe5f0";
      return color;
    }

    /**
     * 吹き出しの中のTweetを描画する．<br>
     * <p>タグの部分のことです．
     * @param out
     * @param clusterIndex
     */
    public void displayTweet(PrintWriter out, int clusterIndex) {
      //System.out.println("call displayTweet");
      int clusterSize = resultMMR.sizeAt(clusterIndex);
      int topSize = (int) (Math.log(clusterSize) / Math.log(CLUSTER_SIZE_PARAM));
      if (topSize == 0) topSize = 1; // 表示0は禁止
      int docIndex = 0; // 現在表示中のTweet

      while(docIndex < topSize) {
        ScoreDoc sd = resultMMR.getScoreDocAtFrom(clusterIndex, docIndex);
        try {
          Document d = searcher.doc(sd.doc);
          out.print("<p>");
          out.print("<span style=\"color:#1e90ff\">");
          out.print(d.get("USER"));
          out.print("&nbsp;:&nbsp;</span>");
          out.print(d.get("TWEET"));
          out.print("<br><span style=\"color:#1e90ff\">&nbsp;:&nbsp;");
          out.print(d.get("TIME"));
          out.print("</span>");
          out.println("</p>");
        } catch (CorruptIndexException e) {
          // TODO 自動生成された catch ブロック
          e.printStackTrace();
        } catch (IOException e) {
          // TODO 自動生成された catch ブロック
          e.printStackTrace();
        }
        docIndex++;
      } // whileここまで

      while(docIndex < clusterSize) {
        ScoreDoc sd = resultMMR.getScoreDocAtFrom(clusterIndex, docIndex);
        try {
          Document d = searcher.doc(sd.doc);
          out.print("<p class=\"hukidashi-hide\">");
          out.print("<span style=\"color:#1e90ff\">");
          out.print(d.get("USER"));
          out.print("&nbsp;:&nbsp;</span>");
          out.print(d.get("TWEET"));
          out.print("<br><span style=\"color:#1e90ff\">&nbsp;:&nbsp;");
          out.print(d.get("TIME"));
          out.print("</span>");
          out.println("</p>");
        } catch (CorruptIndexException e) {
          // TODO 自動生成された catch ブロック
          e.printStackTrace();
        } catch (IOException e) {
          // TODO 自動生成された catch ブロック
          e.printStackTrace();
        }
        docIndex++;
      } // whileここまで
    }

    /**
     * 吹き出しの中のTweetを描画する．<br>
     * <p>タグの部分のことです．<br>
     * テストデータに基づき，クエリとTweetの関連度を色分けして表示．
     * @param out
     * @param clusterIndex
     */
    public void displayTweet(PrintWriter out, int clusterIndex, QueryTester queryTester, String queryStr) {
      //System.out.println("call displayTweet");
      int clusterSize = resultMMR.sizeAt(clusterIndex);
      int topSize = (int) (Math.log(clusterSize) / Math.log(CLUSTER_SIZE_PARAM));
      if (topSize == 0) topSize = 1; // 表示0は禁止
      int docIndex = 0; // 現在表示中のTweet
      // クエリテスト関連
      int queryID = queryTester.existQuery(queryStr);
      queryTester.setNowTestMap(queryID);
      while(docIndex < topSize) {
        ScoreDoc sd = resultMMR.getScoreDocAtFrom(clusterIndex, docIndex);
        try {
          Document d = searcher.doc(sd.doc);
          // 関連度による色設定
          String color;
          int point = queryTester.getPoint(d.get("ID"));
          if (point == 2) { // とてもマッチしている
            color = "#ffaad5";
          } else if (point == 1) { // マッチしている
            color = "#ffbf80";
          } else if (point == -2) { // spam
            color = "#80bfff";
          } else {
            color = "transparent";
          }
          // 描画開始
          out.print("<p>");
          out.print("<span style=\"color:#1e90ff\">");
          out.print(d.get("USER"));
          out.print("&nbsp;:&nbsp;</span>");
          out.print("<span style=\"background-color:" + color + "\">");
          out.print(d.get("TWEET"));
          out.print("</span>");
          out.print("<br><span style=\"color:#1e90ff\">&nbsp;:&nbsp;");
          out.print(d.get("TIME"));
          out.print("</span>");
          out.println("</p>");
        } catch (CorruptIndexException e) {
          // TODO 自動生成された catch ブロック
          e.printStackTrace();
        } catch (IOException e) {
          // TODO 自動生成された catch ブロック
          e.printStackTrace();
        }
        docIndex++;
      } // whileここまで

      while(docIndex < clusterSize) {
        ScoreDoc sd = resultMMR.getScoreDocAtFrom(clusterIndex, docIndex);
        try {
          Document d = searcher.doc(sd.doc);
          // 関連度による色設定
          String color;
          int point = queryTester.getPoint(d.get("ID"));
          if (point == 2) { // とてもマッチしている
            color = "#ffaad5";
          } else if (point == 1) { // マッチしている
            color = "#ffbf80";
          } else if (point == -2) { // spam
            color = "#80bfff";
          } else {
            color = "transparent";
          }
          out.print("<p class=\"hukidashi-hide\">");
          out.print("<span style=\"color:#1e90ff\">");
          out.print(d.get("USER"));
          out.print("&nbsp;:&nbsp;</span>");
          out.print("<span style=\"background-color:" + color + "\">");
          out.print(d.get("TWEET"));
          out.print("</span>");
          out.print("<br><span style=\"color:#1e90ff\">&nbsp;:&nbsp;");
          out.print(d.get("TIME"));
          out.print("</span>");
          out.println("</p>");
        } catch (CorruptIndexException e) {
          // TODO 自動生成された catch ブロック
          e.printStackTrace();
        } catch (IOException e) {
          // TODO 自動生成された catch ブロック
          e.printStackTrace();
        }
        docIndex++;
      } // whileここまで
    }

    /**
     * 検索を行い，MMRで多様化した結果を格納する．
     * @param queryStr
     */
    public void searchMMR(String queryStr, double lambda) {
      //System.out.println("call searchMMR");
      Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
      MakeQueryMicroblog MQM = new MakeQueryMicroblog();
      SearcherMicroblog searcherMB = new SearcherMicroblog(searcher);

      Query query;
      try {
        query = MQM.makeQuery(queryStr, analyzer);
        result = searcherMB.search(query, hitNumber);
        MicroblogMMR mmr = new MicroblogMMR(index, reader, searcher, lambda, clusterNum);
        resultMMR = mmr.MMRfast(result.getHits());
      } catch (ParseException e) {
        // TODO 自動生成された catch ブロック
        e.printStackTrace();
      } catch (IOException e) {
        // TODO 自動生成された catch ブロック
        e.printStackTrace();
      }

    }

    public int getClusterNum() {
      return clusterNum;
    }

    public void setClusterNum(int clusterNum) {
      this.clusterNum = clusterNum;
    }

    public double getLambda() {
      return lambda;
    }

    public void setLambda(double lambda) {
      this.lambda = lambda;
    }

  }