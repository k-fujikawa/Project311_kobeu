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
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * Servlet implementation class SearchMicroblogSVUyouyoTemp
 */
@WebServlet("/SearchMicroblogSVUyouyoTemp")
public class SearchMicroblogSVUyouyoTemp extends HttpServlet {
	private static final long serialVersionUID = 1L;
  /** インデックスの場所 */
  public static final String PATH_OF_INDEX = "/Users/kitaguchisayaka/ProRin/data/TwitterIndex";
  public static final int CLUSTER_SIZE_PARAM = 3; /** 吹き出しトップに表示するTweet数決定のためのlogの底 */

  private int hitNumber;
  private int clusterNum;
  private Similarity similarity;
  private Analyzer analyzer;
  private Directory index;
  private IndexReader reader;
  private IndexSearcher searcher;
  private TwitterSearcher tSeacher;
  private TwitterQueryMaker tQMaker;
  private Result result;
  private ResultMMR resultMMR;
  private TimeParser timeParser;
  private double lambda;
  private String timeOnOff;
  private String time;
  private Calendar minCal; // 時間指定できる最初の時刻
  private Calendar maxCal; // 時間指定できる最後の時刻


  /**
   * @see HttpServlet#HttpServlet()
   */
  public SearchMicroblogSVUyouyoTemp() {
    super();
    similarity = Similarity.getDefault(); // デフォルト
    //similarity = new NonTfSimilarity(); // tfが常に1
    try { // lucene関連
      index = FSDirectory.open(new File(PATH_OF_INDEX));
      reader = IndexReader.open(index);
      searcher = new IndexSearcher(reader);
      searcher.setSimilarity(similarity); // 類似度の設定
    } catch (IOException e) {
      System.err.println("error: SearchTwitterSVUyouyoTempコンストラクタlucene関連");
      e.printStackTrace();
      System.exit(1);
    }
    AnalyzerFactory af = new AnalyzerFactory(Version.LUCENE_36);
    timeParser = new TimeParser();
    analyzer = af.getJapaneseEnglishAnalyzer();
    tSeacher = new TwitterSearcher(searcher, analyzer);
    tQMaker = new TwitterQueryMaker(analyzer);

    hitNumber = 100; // 検索件数の初期値設定
    setClusterNum(3); // クラスタの数の初期値設定
    setLambda(0.3); // λの初期値設定
    // cal設定コメントアウトしてはいけない
    minCal = Calendar.getInstance();
    minCal.set(2012, Calendar.JUNE, 11, 16, 0, 0);
    maxCal = Calendar.getInstance(); // 現在日付まで
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
    System.out.println("*********************************");
    response.setContentType("text/html; charset=UTF-8");
    request.setCharacterEncoding("UTF-8");
    PrintWriter out = response.getWriter();
    String queryStr = request.getParameter("QueryStr");
    lambda = Double.parseDouble(request.getParameter("lambda_val")); // MMRのパラメタλ
    hitNumber = Integer.parseInt(request.getParameter("hitnumber")); // 見つけるTweetの最大数
    clusterNum = Integer.parseInt(request.getParameter("clusternum")); // クラスタの数
    timeOnOff = request.getParameter("time-on-off"); // 時間指定するか否か
    time = request.getParameter("time"); // 指定時間
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

      // うようよキャンバス
      out.println("<canvas width=\"960\" height=\"450\" style=\"margin-left:15px;\"></canvas>");

      // タイムライン表示
      out.println("<div class=\"timeline\">");
      Calendar topCal = Calendar.getInstance();
      Calendar bottomCal = Calendar.getInstance();
      bottomCal.set(2012, Calendar.JUNE, 11); // 表示する最も古い日付をセット
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
          displayDay(out, topCal.get(Calendar.DAY_OF_MONTH), clusterIndexA, clusterCal);
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
        displaySearchTime(out, queryStr, startCal, endCal);
        /*
        if (queryTester.existQuery(queryStr) == -1) { // テスト用クエリでない
          displaySearchTime(out, queryStr, startCal, endCal);
        }
        else {
          displayKanren(out);
          displaySearchTime(out, queryStr, startCal, endCal, queryTester);
        }*/
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

    // Processing.js
    displayProcessingSource(out);

    out.println("</body>");
    out.println("</html>");

    out.close();
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
    out.println("<title>Twitter検索</title>");
    //out.println("<script src=\"http://code.jquery.com/jquery-1.7.2.min.js\"></script>");
    out.println("<script src=\"" + request.getContextPath() + "/jquery-1.7.2.js\"></script>");
    out.println("<script type='text/javascript' src=\"" + request.getContextPath() + "/jquery.ui.datepicker-ja.js\"></script>");
    out.println("<script type='text/javascript' src=\"" + request.getContextPath() + "/jquery.ui.core.js\"></script>");
    out.println("<script type='text/javascript' src=\"" + request.getContextPath() + "/jquery.ui.widget.js\"></script>");
    out.println("<script type='text/javascript' src=\"" + request.getContextPath() + "/jquery.ui.datepicker.js\"></script>");
    // processing.js
    out.println("<script src=\"" + request.getContextPath() + "/processing-1.4.0.js\"></script>");
    displayProcessingInit(out);   // Processing.jsの初期化に関する記述
    out.println("</head>");
  }

  /**
   * <head></head>タグ内のprocessing.jsの設定に関する部分を描画する
   * @param out
   */
  protected void displayProcessingInit(PrintWriter out) {
    out.println("<script>");
    out.println("window.onload = function(){");
      // canvas要素
      out.println("var canvas = document.getElementsByTagName('canvas')[0];");
      // Proccessingのコードが書かれたscript要素
      out.println("var codeElm = document.getElementById('processing-code');");
      // 上記要素の内容を取得
      out.println("var code = codeElm.textContent || codeElm.innerText;");
      out.println("new Processing(canvas, code);");
    out.println("};");
  out.println("</script>");
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
    out.println("<h1>Twitter検索</h1>");
    out.println("<div id=\"serch-zone\">");
    out.println("<form action=\"./SearchMicroblogSVUyouyoTemp\" method=\"POST\">");
    out.println("<p>");
    out.println("<input type=\"search\" name=\"QueryStr\" size=\"40\" required=\"required\" value=\"" + queryStr + "\">");
    out.println("&nbsp&nbsp<input type=\"submit\" value=\"検索\">");
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
    //System.out.println("call displayDay1");
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
    //System.out.println("call displayDay2");
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
        out.print("<img src=\"" + d.get("image") + "\" align=\"left\">");
        out.print("<span style=\"color:#1e90ff\">");
        out.print(d.get("screen_name"));
        out.print("&nbsp;:&nbsp;</span>");
        out.print(d.get("text"));
        out.print("<br><span style=\"color:#1e90ff\">&nbsp;:&nbsp;");
        out.print(d.get("created_at"));
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
        out.print("<img src=\"" + d.get("image") + "\" align=\"left\">");
        out.print("<span style=\"color:#1e90ff\">");
        out.print(d.get("screen_name"));
        out.print("&nbsp;:&nbsp;</span>");
        out.print(d.get("text"));
        out.print("<br><span style=\"color:#1e90ff\">&nbsp;:&nbsp;");
        out.print(d.get("created_at"));
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
   * 時間範囲を指定して検索を行い，結果を表示する<br>
   * 今は未完成2012/08/02
   * @param out PrintWriter
   * @param queryStr クエリ文字列
   * @param startCal 開始時間
   * @param endCal 終了時間
   */
  public void displaySearchTime(PrintWriter out, String queryStr, Calendar startCal, Calendar endCal) {
    // 検索
    try {
      Query query = tQMaker.makeQuery(queryStr);
      //Result result = tSeacher.searchWithTime(query, hitNumber, startCal, endCal);
      System.out.println(result.size());
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
   * 検索を行い，MMRで多様化した結果を格納する．
   * @param queryStr
   */
  public void searchMMR(String queryStr, double lambda) {
    //System.out.println("call searchMMR");
    Query query;
    try {
      query = tQMaker.makeQuery(queryStr);
      result = tSeacher.search(query, hitNumber);
      TwitterMMR mmr = new TwitterMMR(searcher, reader, analyzer, similarity, lambda, clusterNum);
      resultMMR = mmr.MMR(result.getHits(), timeParser);
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

  protected void displayProcessingSource(PrintWriter out) {
    out.println("<script id=\"processing-code\" type=\"application/processing\">");
    out.println("String focusTweet = \"\";");
    out.println("int W = 960;");
    out.println("int L = 450;");
    out.println("int yBottom = L-80;");
    out.println("int lineCol = #8AD676;");
    out.println("float dayGrid = W/50;");
    out.println("float julLine = dayGrid*19;");
    //フォント
    out.println("PFont monthFont;");
    out.println("PFont dayFont;");
    out.println("PFont tweetFont;");
    out.println("PFont buttonFont;");
    //円関連
    out.println("float SPEED = 2;"); // 移動量
    out.println("float R = 13;"); //半径
    out.println("int NUM1 = 5;"); //円の個数
    out.println("int NUM2 = 3;");
    out.println("int NUM3 = 2;");
    out.println("Circle[] circles1 = new Circle[NUM1];");
    out.println("Circle[] circles2 = new Circle[NUM2];");
    out.println("Circle[] circles3 = new Circle[NUM3];");
    out.println("String[] tweets1 = {\"tweet1-1\",\"tweet1-2\",\"tweet1-3\",\"tweet1-4\",\"tweet1-5\"};");
    out.println("String[] tweets2 = {\"tweet2-1\",\"tweet2-2\",\"tweet2-3\"};");
    out.println("String[] tweets3 = {\"tweet3-1\",\"tweet3-2\"};");
    out.println("int[]    Xpoints1 = {dayGrid*1,dayGrid*2,dayGrid*3,dayGrid*3,dayGrid*3};");
    out.println("int[]    Xpoints2 = {dayGrid*2,dayGrid*14,dayGrid*40};");
    out.println("int[]    Xpoints3 = {dayGrid*2,dayGrid*30};");
    out.println("int[]    Ypoints1 = {R, R, R, R*3, R*5};");
    out.println("int[]    Ypoints2 = {R*3, R, R};");
    out.println("int[]    Ypoints3 = {R*5, R};");
    out.println("boolean gatherFlag = false;");
    //円のパラメータ
    out.println("float r1 = 1;"); //群れの中心に向かう度合
    out.println("float r2 = 0.8;"); //仲間を避ける度合
    out.println("float r3 = 0.1;"); //群れの平均速度に合わせる度合
    out.println("int CENTER_PULL_FACTOR = 300;");
    out.println("int  DIST_THRESHOLD = 30;"); //円同士の距離
    //setup()
    out.println("void setup() {");
      out.println("size(W, L);");
      out.println("frameRate(20);");
      out.println("noFill();");
      out.println("monthFont = createFont(\"FFScala\", 35);");
      out.println("dayFont = createFont(\"FFScala\", 15);");
      out.println("tweetFont = createFont(\"FFScala\", 20);");
      out.println("buttonFont = createFont(\"FFScala\", 12);");

      out.println("float angle = TWO_PI / 10;");
      out.println("for (int i = 1; i <= NUM1; i++) {");
        out.println("float addx = cos(angle * i);");
        out.println("float addy = sin(angle * i);");
        out.println("circles1[i-1] = new Circle(width / 2 + addx * 50, yBottom / 2 + addy * 50, random(- SPEED, SPEED) * addx, random(- SPEED, SPEED) * addy, i - 1, circles1, #F25A69, NUM1, tweets1[i-1], Xpoints1[i-1], Ypoints1[i-1]);");
      out.println("}"); //end of for circles1
      out.println("for (int i = 1; i <= NUM2; i++) {");
        out.println("float addx = cos(angle * i);");
        out.println("float addy = sin(angle * i);");
        out.println("circles2[i-1] = new Circle(width / 2 + addx * 50, yBottom / 2 + addy * 50, random(- SPEED, SPEED) * addx, random(- SPEED, SPEED) * addy, i - 1, circles2, #00E0D5, NUM2, tweets2[i-1], Xpoints2[i-1], Ypoints2[i-1]);");
      out.println("}"); //end of for circles2
      out.println("for (int i = 1; i <= NUM3; i++) {");
        out.println("float addx = cos(angle * i);");
        out.println("float addy = sin(angle * i);");
        out.println("circles3[i-1] = new Circle(width / 2 + addx * 50, yBottom / 2 + addy * 50, random(- SPEED, SPEED) * addx, random(- SPEED, SPEED) * addy, i - 1, circles3, #FC2ED6, NUM3, tweets3[i-1], Xpoints3[i-1], Ypoints3[i-1]);");
      out.println("}"); //end of for circles3
    out.println("}"); //end of setup()
    // draw()
    out.println("void draw() {");
      out.println("background(#ACE9C7);");
      out.println("drawButton();");
      //日付の描写
      out.println("stroke(lineCol);");
      out.println("strokeWeight(5);");
      out.println("line(0,yBottom,W,yBottom);");
      out.println("fill(#7C70BF);");
      out.println("line(julLine, yBottom, julLine, L);");
      out.println("textFont(monthFont);");
      out.println("text(\"July\", julLine+15, L-15);");
      out.println("text(\"June\", 20, L-15);");
      out.println("textFont(dayFont);");
      out.println("text(\"15\", dayGrid*4, yBottom+20);");
      out.println("text(\"20\", dayGrid*9, yBottom+20);;");
      out.println("text(\"25\", dayGrid*14, yBottom+20);;");
      out.println("text(\"30\", dayGrid*18-3, yBottom+20);;");
      out.println("text(\"1\", dayGrid*20-3, yBottom+20);;");
      out.println("text(\"5\", dayGrid*24, yBottom+20);;");
      out.println("text(\"10\", dayGrid*29, yBottom+20);;");
      out.println("text(\"15\", dayGrid*34, yBottom+20);;");
      out.println("text(\"20\", dayGrid*39, yBottom+20);;");
      out.println("text(\"25\", dayGrid*44, yBottom+20);;");
      out.println("text(\"30\", dayGrid*49, yBottom+20);;");

      out.println("for (int i = 0; i < NUM1; i++) {\ncircles1[i].clearVector();\n}");
      out.println("for (int i = 0; i < NUM2; i++) {\ncircles2[i].clearVector();\n}");
      out.println("for (int i = 0; i < NUM3; i++) {\ncircles3[i].clearVector();\n}");
      out.println("for (int i = 0; i < NUM1; i++) {\nCircle circle = (Circle) circles1[i];");
        out.println("circle.check();\ncircle.move();\ncircle.draw();}");
      out.println("for (int i = 0; i < NUM2; i++) {\nCircle circle = (Circle) circles2[i];");
        out.println("circle.check();\ncircle.move();\ncircle.draw();}");
      out.println("for (int i = 0; i < NUM3; i++) {\nCircle circle = (Circle) circles3[i];");
        out.println("circle.check();\ncircle.move();\ncircle.draw();}");
      // Tweetの描写
      out.println("textFont(tweetFont);");
      out.println("fill(#7C70BF);");
      out.println("text(focusTweet, 20, 30);");
    out.println("}"); //end of draw()
    // mousePressed()
    out.println("void mousePressed() {");
      out.println("if(dist(mouseX,mouseY,900,50) < 30) {");
        out.println("if(gatherFlag == true) { gatherFlag = false; }");
        out.println("else { gatherFlag = true; }");
      out.println("}");
    out.println("}"); //end of mousePressed()
    // drawButton()
    out.println("void drawButton() {");
      out.println("textFont(buttonFont);");
      out.println("if(gatherFlag == false) {");
        out.println("fill(#EF6669);\nellipse(900,50,60,60);");
        out.println("fill(#000000);\ntext(\"話題ごと\\n集める\",878,50);");
      out.println("} else {");
        out.println("fill(#0077B5);\nellipse(900,50,60,60);");
        out.println("fill(#FFFFFF);\ntext(\"解除\",889,55);");
      out.println("}");
    out.println("}"); //end of drawButton()
    // start of defining class Circle
    out.println("class Circle {");
      out.println("float x, y;");   //ボールの現在位置（中心）
      out.println("float vx, vy;"); //ボールの移動量
      out.println("PVector v1 = new PVector();");  //移動量：群れの中心に向かう
      out.println("PVector v2 = new PVector();");  //移動量：仲間を避ける
      out.println("PVector v3 = new PVector();");  //移動量：群れの平均速度に合わせる

      out.println("int id;"); //ボールの識別番号
      out.println("Circle[] others;");
      out.println("int color;");
      out.println("int num;");
      out.println("String tweet;");
      out.println("int posx;");
      out.println("int posy;");
      out.println("boolean isFocus;"); //カーソルがのっているか否か

      //コンストラクタ
      out.println("Circle(float _x, float _y, float _vx, float _vy,");
          out.println("int _id, Circle[] _others, int _color, int _num,");
          out.println("String _tweet, int _posx, int _posy) {");
        out.println("x = _x;\ny = _y;\nvx = _vx;\nvy = _vy;\nid = _id;");
        out.println("others = _others;\ncolor = _color;\nnum = _num;");
        out.println("tweet = _tweet;\nposx = _posx;\nposy = _posy;\nisFocus = false;");
      out.println("}");

      out.println("void move() {");
        out.println("vx += r1 * v1.x + r2 * v2.x + r3 * v3.x;");
        out.println("vy += r1 * v1.y + r2 * v2.y + r3 * v3.y;");

        //追加する：移動量が限度を超えてないか
        out.println("float vVector = sqrt(vx * vx + vy * vy);");
        out.println("if (vVector > SPEED) {");
          out.println("vx = (vx / vVector) * SPEED;");
          out.println("vy = (vy / vVector) * SPEED;");
        out.println("}");

        out.println("x += vx;\ny += vy;");

        out.println("if (x - R <= 0) {\nx = R;\nvx *= -1;\n}");
        out.println("if (x + R >= width) {\nx = width - R;\nvx *= -1;\n}");

        out.println("if (y - R <= 0) {\ny = R;\nvy *= -1;\n}");
        out.println("if (y + R >= yBottom) {\ny = yBottom - R;\nvy *= -1;\n}");
      out.println("}"); //end of move

      out.println("void draw() {");
        out.println("if(dist(mouseX,mouseY,x,y) < R) {");
          out.println("isFocus = true;\nfocusTweet = tweet;");
        out.println("} else {");
          out.println("if(isFocus == true) {");
            out.println("focusTweet=\"\";");
          out.println("}");
          out.println("isFocus = false;");
        out.println("}");
        out.println("fill(color, 180);");
        out.println("noStroke();");
        out.println("ellipse(x, y, R * 2, R * 2);");
      out.println("}"); //end of fraw()

      //移動距離オブジェクトを初期化 clearVector()
      out.println("void clearVector() {");
        out.println("v1.x = 0;\nv1.y = 0;\nv2.x = 0;");
        out.println("v2.y = 0;\nv3.x = 0;\nv3.y = 0;");
      out.println("}");

      //衝突判定 check()
      out.println("void check() {");
        out.println("if(gatherFlag == true) {\nrule1_1();\nrule3();");
        out.println("} else {");
          out.println("rule1_2();");
        out.println("}");
        out.println("rule2();");
      out.println("}");//end

      // クラスタの中心に向かう量
      out.println("void rule1_1() {");
        out.println("for (int i = 0; i < num; i++) {");
          out.println("Circle otherCircle = (Circle) others[i];");
          out.println("if (this != otherCircle) {");
            out.println("v1.x += otherCircle.x;");
            out.println("v1.y += otherCircle.y;");
          out.println("}"); // end if
        out.println("}"); // end for

        out.println("v1.x /= (num - 1);");
        out.println("v1.y /= (num - 1);");

        out.println("v1.x = (v1.x - x) / CENTER_PULL_FACTOR;");
        out.println("v1.y = (v1.y - y) / CENTER_PULL_FACTOR;");
      out.println("}");//end rule1

      // 時系列表示
      out.println("void rule1_2() {");
        out.println("v1.x = posx-x;");
        out.println("v1.y = (yBottom-posy)-y;");
      out.println("}");

      // 計算：仲間を避ける量
      out.println("void rule2() {");
        out.println("for (int i = 0; i < num; i++) {");
          out.println("Circle otherCircle = (Circle) others[i];");
          out.println("if (this != otherCircle) {");
            out.println("if (dist(x, y, otherCircle.x, otherCircle.y) < DIST_THRESHOLD) {");
              out.println("v2.x -= otherCircle.x - x;");
              out.println("v2.y -= otherCircle.y - y;");
            out.println("}"); // end if
          out.println("}"); // end if
        out.println("}");
      out.println("}");// end rule2

      // 計算：群れの平均速度に合わせる量
      out.println("void rule3() {");
        out.println("for (int i = 0; i < num; i++) {");
          out.println("Circle otherCircle = (Circle) others[i];");
          out.println("if (this != otherCircle) {");
            out.println("v3.x += otherCircle.vx;");
            out.println("v3.y += otherCircle.vy;");
          out.println("}"); // end if
        out.println("}"); // end for

        out.println("v3.x /= (num - 1);");
        out.println("v3.y /= (num - 1);");

        out.println("v3.x = (v3.x - vx)/2;");
        out.println("v3.y = (v3.y - vy)/2;");
      out.println("}");// end rule3
    out.println("}");

    out.println("</script>");
  }

}
