package main;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ForMock.MockResult;

import search.RunQuery;
import search.TemporalProfile;

/**
 * Servlet implementation class TwitterChainSearchMock
 */
@WebServlet("/TwitterChainSearchMock")
public class TwitterChainSearchMock extends HttpServlet {
	private static final long serialVersionUID = 1L;
	 /** 索引の場所 */
  private static final String PATH_OF_INDEX = "/Users/KitaguchiSayaka/MinHcompSearch_Demo/trectext.en.krov";
  private static final String hostname = "rubicon.cs.scitec.kobe-u.ac.jp";
  private static final int port = 5600;
  private int hitNumber;
  private String queryStr = "";

  private List<Object[]> firstSearchResult = null;


    /**
     * @see HttpServlet#HttpServlet()
     */
    public TwitterChainSearchMock() {
        super();
        hitNumber = 100; // デフォルトの検索結果表示数は100
    }

    /**
     * @see HttpServlet#HttpServlet()
     */
    public void destroy() {
      super.destroy();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    PrintWriter out = response.getWriter();
    response.setContentType("text/html; charset=UTF-8");

    displayHead(out, request);
    out.println("<body>");
    displayHeader(out);
    displayNavbar(out, "");

    out.println("<div style=\"height:800px;\">");
    out.println("</div>");

    out.println("<br><br>");
    displayFooter(out);

    out.println("</body>");
    out.println("</html>");
    out.close();



    // 以下、テスト用（Postに飛ぶ）
//    this.doPost(request, response);
    // テストここまで
    out.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {




    PrintWriter out = response.getWriter();
    response.setContentType("text/html; charset=UTF-8");
    request.setCharacterEncoding("UTF-8");

    displayHead(out, request);


//    List<Object[]> result; // 検索結果
    //hitNumber = Integer.parseInt(request.getParameter("hitnumber")); // 検索最大数

    List<String> listForSearch = new ArrayList<String>();

    //最初検索keyword

//    if (!request.getParameter("QueryStr").isEmpty()) {
      queryStr = request.getParameter("QueryStr");
      if (!queryStr.isEmpty()) {
      listForSearch.add(queryStr); // クエリ1
      }
//    }

    // 二回目検索keyword
    String reQueryStr = "";
    if (request.getParameter("ReQueryStr") != null){
      reQueryStr = request.getParameter("ReQueryStr");

      reQueryStr = filterReSearchContent(reQueryStr);

      if (!reQueryStr.isEmpty()) {
        listForSearch.add(request.getParameter("ReQueryStr")); // クエリ2
      }
    }

//    RunQuery runQuery = new RunQuery(hostname, port);
//    String query = runQuery.qs2query(listForSearch, null); // クエリ作成

    // 検索
    System.out.println("QUERY : " + queryStr);
//    int retnum = hitNumber; // requestNumber?
    // 検索して結果を内部のList<Map> tweetsに格納
//    TemporalProfile tp = new TemporalProfile(runQuery, query, retnum);
//    if(tp.size() >= hitNumber) {
//      result = tp.topTweets(hitNumber);
//    } else {
//      result = tp.topTweets(tp.size());
//    }


    displayBody(out, queryStr, listForSearch, reQueryStr);


//    out.println("<!DOCTYPE html>");
//    out.println("<html lang=\"ja\">");
//    out.println("<head>");
//    out.println("<title>Test of TwitterChainSearchMock</title>");
//    out.println("</head>");
//    out.println("<body>");
//
//    out.println("| score\t| tweetid\t| date\t| tweet\t|");
//    Iterator<Object[]> it = result.iterator();
//    while (it.hasNext()) {
//      Object[] obj = it.next();
//      out.println("|" + obj[0] + "\t"); // score
//      out.println("|" + obj[1] + "\t"); // tweetid
//      out.println("|" + obj[2] + "\t"); // date
//      out.println("|" + obj[3] + "\t|"); // tweet
//    }
//    out.println("</body>");
    out.println("</html>");
    out.close();
	}

	 /**
   * show the <head></head> tag of the html
   * @param out
   * @param request
   */
   public void displayHead(PrintWriter out, HttpServletRequest request) {
     out.println("<!DOCTYPE html>");
     out.println("<html lang=\"ja\">");
     // head
     out.println("<head>");
     out.println("<meta http-equiv=\"content-type\" content=\"text/html\" charset=\"UTF-8\">");
     out.println("<link href=\"" + request.getContextPath() + "/css/mircoblogCSS.css\" rel=\"stylesheet\">");
     out.println("<link type=\"text/css\" href=\"" + request.getContextPath() + "/css/jquery.ui.all.css\" rel=\"stylesheet\">");
     out.println("<title>MICROBLOG SEARCHER Ver2.0</title>");
     out.println("<script src=\"http://code.jquery.com/jquery-1.7.2.js\"></script>");
     out.println("<script type='text/javascript' src=\"" + request.getContextPath() + "/js/jquery.ui.core.js\"></script>");
     out.println("<script type='text/javascript' src=\"" + request.getContextPath() + "/js/ui/jquery.ui.sortable.js\"></script>");
     out.println("<script type='text/javascript' src=\"" + request.getContextPath() + "/js/ui/jquery.ui.mouse.js\"></script>");
     out.println("<script type='text/javascript' src=\"" + request.getContextPath() + "/js/ui/jquery.ui.draggable.js\"></script>");
     out.println("</head>");
   }

   /**
    * show <body></body>中のheader
    * @param out
    */
   public void displayHeader(PrintWriter out) {
     out.println("<div id=\"header\">");
     out.println("<a href=\"http://localhost:8080/MinHcompSearch_Demo/TwitterChainSearchMock\" style=\"color:white;\">ver2.0</a>");
     out.println("(Demo version)");
     out.println("</div>");
   }

   /**
    * show footer
    * @param out
    */
   public void displayFooter(PrintWriter out) {
     out.println("<div id=\"footer\">");
     out.println("<hr>");
     // /MinHcompSearch_Demo/src/main/TwitterChainSearchMock.java
     out.println("<a href=\"http://localhost:8080/MinHcompSearch_Demo/TwitterChainSearchMock\">Topに戻る </a>");
     out.println("</div>");
   }


   /**
    * show search area
    * @param out
    * @param queryStr
    */
   public void displayNavbar(PrintWriter out, String queryStr) {
     out.println("<div class=\"navbar\">");
     out.println("<div class=\"navbar-inner\">");
     out.println("<h1>Microblog Searcher</h1>");
     out.println("<div id=\"serch-zone\">");
     out.println("<form action=\"./TwitterChainSearchMock\" method=\"POST\">");
     out.println("<p>");
     out.println("<input type=\"search\" name=\"QueryStr\" size=\"40\" required=\"required\" value=\"" + queryStr + "\">");
     out.println("<input type=\"submit\" value=\"search\">");
//     out.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
//     out.println("<span style=\"color:white;font-size:14.5px;\">");
//     out.println("見つけるTweetの最大数");
//     out.println("<input type=\"text\" name=\"hitnumber\" pattern=\"^[0-9]+$\" value=\"" + hitNumber + "\" size=\"4\">");
//     out.println("件");
//     out.println("<br>");
//     out.println("</span>");
     out.println("</p>");
     out.println("</form>");
     out.println("</div>");
     out.println("</div>");
     out.println("</div>");


   }


   public void displayBody(PrintWriter out, String queryStr, List<String> searchList, String reQueryStrFilter) {

     out.println("<body>");
     displayHeader(out);
     out.println("<div class=\"contents\">");
     displayNavbar(out,queryStr);
     displayListArea(out, searchList, reQueryStrFilter, queryStr);
     out.println("</div>");
     displayFooter(out);
     displayJQuery(out);

     out.println("</body>");

   }

   /**
    * show the jQuery area
    * @param out
    */
   public void displayJQuery(PrintWriter out) {
     out.println("<script>");
       out.println("$(function() {");

       out.println("$('.arrow_box').sortable();");
       out.println("$('.right_arrow_box').sortable();");

       out.println("$('.arrow_box').draggable();");
       out.println("$('.right_arrow_box').draggable();");

       //    $('.arrow_box').draggable();
       //$('.right_arrow_box').draggable();
//       out.println("$(\'#secondSearch\').submit();");
       out.println("});");
       out.println("</script>");
   }


   /**
    * show the list area
    * @param out
    * @param searchList
    * @param reQueryStrFilter
    * @param runQuery
    */
   public void displayListArea(PrintWriter out, List<String> searchList, String reQueryStrFilter, String queryStr) {
     /*
     RunQuery runQuery = new RunQuery(hostname, port);
     String query = runQuery.qs2query(searchList, null); // クエリ作成
     TemporalProfile tempProfile = new TemporalProfile(runQuery, query, hitNumber);*/
     List<Object[]> searchResult = null; // 検索結果
     //int retnum = hitNumber;

     MockResult mock = new MockResult();

   if (searchList.size() == 1) {

     //if(hitNumber <= tempProfile.size()) {
       firstSearchResult = mock.getFIFA_soccer_2022();
     //} else {
       //firstSearchResult = mock.getFIFA_soccer_2022();
     //}

   } else if (searchList.size() == 2) {

     /*if(hitNumber <= tempProfile.size()) {
       searchResult = tempProfile.topTweets(retnum);
     } else {*/
       searchResult = mock.getFIFA_soccer_2022_2();
     //}

   }




//   // 検索
//   System.out.println("QUERY : " + query);
//   int retnum = hitNumber; // requestNumber?
//   // 検索して結果を内部のList<Map> tweetsに格納
//   TemporalProfile tp = new TemporalProfile(runQuery, query, retnum);
//   if(tp.size() >= hitNumber) {
//     result = tp.topTweets(hitNumber);
//   } else {
//     result = tp.topTweets(tp.size());
//   }

//   List<Map> queryList = getSearchList(searchList);

   out.println("<div class=\"listContents\">");

   // left list area
   out.println("<div class=\"leftList\">");
   out.println("<div class=\"arrow_box \">");

   int index = 0;
   StringBuilder indexStrB = new StringBuilder();
   indexStrB.append("index");
   String indexStr = "";

   if (searchList.size() != 0 && firstSearchResult.size() != 0){

     for(Object[] searchArray : firstSearchResult) {
         index++;
         indexStrB.append(String.valueOf(index));
         indexStr = indexStrB.toString();
         out.println("<p id=\""+ indexStr + "\"");
         out.println("ondblclick=\"var id = $(this).attr('id');$(#ReQueryStr).attr('id',id); $('#ReQueryStr').val('" + searchArray[1] + "'); $('#QueryStr').val('" + queryStr + "'); $('#secondSearch').submit();\"");
         out.println("onclick=\"if($(this).attr('style') == 'background-color:#ddcccc'){$(this).attr('style', 'background-color:#d3e9f5;');}else{$(this).attr('style', 'background-color:#ddcccc'); }\"");

         out.println(">");
           out.println("<span style=\"color:#1e90ff\">");
           out.println(searchArray[1]);
           out.println("&nbsp;:&nbsp;</span><span style=\"background-color:transparent\">");
           out.println(searchArray[2]);
           out.println("</span><br><span style=\"color:#1e90ff\">&nbsp;:&nbsp;");
           out.println(searchArray[3]);
           out.println("</span>");
//           if (index < queryList.size() - 1) {
//             out.println("<hr>");
//           }
           out.println("</p>");
       }
   }

   // /MinHcompSearch_Demo/src/main/TwitterChainSearchMock.java

   out.println("<form id=\"secondSearch\" action=\"./TwitterChainSearchMock\" name=\"secondSearch\" method =\"post\">");
   out.println("<input name=\"ReQueryStr\" id=\"ReQueryStr\" class=\"ReQueryStr\"  required=\"required\" type=\"hidden\">");
   out.println("<input name=\"QueryStr\" id=\"QueryStr\" class=\"QueryStr\" required=\"required\" type=\"hidden\">");
   out.println("</form>");

   out.println("</div>");
   out.println("</div>");

   // right list area
   out.println("<div class=\"rightList\">");
   out.println("<div class=\"right_arrow_box\">");

   if (searchList.size() != 0 && null != searchResult && searchResult.size() == 2){

     for(Object[] searchMap : searchResult) {
         index++;
         indexStrB.append(String.valueOf(index));
         indexStr = indexStrB.toString();
         out.println("<p ");
         out.println("onclick=\"if($(this).attr('style') == 'background-color:#ddcccc'){$(this).attr('style', 'background-color:#d3e9f5;');}else{$(this).attr('style', 'background-color:#ddcccc'); }\"");
         out.println(">");
         out.println("<span style=\"color:#1e90ff\">");
           out.println(searchMap[1]);
           out.println("&nbsp;:&nbsp;</span><span style=\"background-color:transparent\">");
           out.println(searchMap[2]);
           out.println("</span><br><span style=\"color:#1e90ff\">&nbsp;:&nbsp;");
           out.println(searchMap[3]);
           out.println("</span>");
//           if (index < queryList.size() - 1) {
//             out.println("<hr>");
//           }
           out.println("</p>");
       }
   }

   out.println("</div>");
   out.println("</div>");
   out.println("</div>");
 }

/**
 *
 * @param reQueryStr
 * @return
 */
   public String filterReSearchContent(String reQueryStr) {

      String[] strArray = reQueryStr.split(" ");

      StringBuilder stringBuilder = new StringBuilder();

      String resultStr = "";

      int index = 1;
      for (String str : strArray) {

        if (3 < str.length() || str.matches("[a-zA-Z]+")) {

          stringBuilder.append(str);
          if (index < strArray.length) {
            stringBuilder.append(" ");
          }
        }
        index++;
      }

      if (stringBuilder.length() != 0){
        resultStr = stringBuilder.toString();
      }
      return resultStr;
   }

}
