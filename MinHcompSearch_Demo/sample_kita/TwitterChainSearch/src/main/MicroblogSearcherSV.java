import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

//@WebServlet("/MicroblogSearcherSV")
public class MicroblogSearcherSV extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public static final String PATH_OF_INDEX = "";

	// TODO IS NOT THE RIGHT NUM
	public static final int TWEET_SUM_N = 10000;

	// TODO
//	public static final int CLUSTER_SIZE_PARAM = 3;
//
//	private String topicFile1 = "/data/topics.MB1-50.txt";
//
//	private String topicFile2 = "/data/topics.MB51-110.txt";

	private Directory index;
	private IndexReader reader;
	private IndexSearcher searcher;
	private int hitNumber;

	// TODO
	String hostname = "";
	int port = 5600;
//	private Result result;

	public MicroblogSearcherSV() {
		super();
		try {
			index = FSDirectory.open(new File(PATH_OF_INDEX));
			reader = IndexReader.open(index);
			searcher = new IndexSearcher(reader);
		} catch (IOException e) {
			// TODO
			System.err.println("IO error");
			e.printStackTrace();
			System.exit(1);
		}

		hitNumber = 100;
	}

	public void destroy() {
		try {
			index.close();
			reader.close();
			searcher.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.destroy();
	}

	/**
	 * doGet
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();

		// show the head
		displayHead(out, request);

		// show the body
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
//	      displayJQuery(out, request);

	      out.println("</body>");
	      out.println("</html>");
	      out.close();
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		// begin the dopost
		System.out.println("**************doPost method is beginning.*******************");
		// 時間計測
		long startPostTime = System.currentTimeMillis();
		response.setContentType("text/html; charset=UTF-8");
		request.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		RunQuery runQuery = new RunQuery(hostname, port);

		// 最初の検索の内容
		String queryStr = "";
		if (request.getParameter("QueryStr") != null || request.getParameter("QueryStr") != ""){
			queryStr = request.getParameter("QueryStr");
			}
		// 再検索の内容
		String reQueryStr = "";
		if (request.getParameter("ReQueryStr") != null || request.getParameter("ReQueryStr") != ""){
		reQueryStr = request.getParameter("ReQueryStr");
		}
		String reQueryStrFilter = "";
		// 整合後の再検索内容
		if (reQueryStr != "") {
			reQueryStrFilter = filterReSearchContent(reQueryStr);

		}

		List<String> listForSearch = new ArrayList<String>();

		if (queryStr != "" ) {
			listForSearch.add(queryStr);
		}
		if (reQueryStrFilter != "") {
			listForSearch.add(reQueryStrFilter);
			reQueryStrFilter = runQuery.qs2query(listForSearch, null);
		}



		// html開始タグ+head
	      displayHead(out, request);
	      displayBody(out, queryStr, listForSearch, reQueryStrFilter, runQuery);
	      displayFooter(out);

	}

/**
 * 単純にtweetからスロップワードとURLと
 * 1 or 2文字以下の単語、非英語、変な文字を除去したtweetを元のクエリに足す
 * @param reQueryStr
 * @return 再検索内容
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
//        out.println("<link rel=\"shortcut icon\" href=\"" + request.getContextPath() + "/image/favicon_smile.ico\">");
        out.println("<link href=\"" + request.getContextPath() + "/css/mircoblogCSS.css\" rel=\"stylesheet\">");
        out.println("<link type=\"text/css\" href=\"" + request.getContextPath() + "/css/jquery.ui.all.css\" rel=\"stylesheet\">");
        out.println("<title>MICROBLOG SEARCHER Ver2.0</title>");
//        out.println("<link href=\"" + request.getContextPath() + "/css/jquery.ui.all.css\" rel=\"stylesheet\">");
        out.println("<script src=\"http://code.jquery.com/jquery-1.7.2.min.js\"></script>");
//        out.println("<script type='text/javascript' src=\"" + request.getContextPath() + "/jquery.ui.core.js\"></script>");
        out.println("<script type='text/javascript' src=\"" + request.getContextPath() + "/js/jquery.ui.core.js\"></script>");
        out.println("<script type='text/javascript' src=\"" + request.getContextPath() + "/js/jquery.ui.sortable.js\"></script>");
        out.println("<script type='text/javascript' src=\"" + request.getContextPath() + "/js/jquery.ui.mouse.js\"></script>");
        out.println("<script type='text/javascript' src=\"" + request.getContextPath() + "/js/jquery.ui.draggable.js\"></script>");
//        out.println("<script src=\"" + request.getContextPath() + "/jquery-1.7.2.js\"></script>");
//        out.println("<script type='text/javascript' src=\"" + request.getContextPath() + "/jquery.ui.datepicker-ja.js\"></script>");
        out.println("</head>");
      }

    /**
     * bodyタグ内のヘッダーを描画する
     * @param out
     */
    public void displayHeader(PrintWriter out) {
        out.println("<div id=\"header\">");
        out.println("<a href=\"http://localhost:8080/MicroblogSearcher/MicroblogSearcherSV\" style=\"color:white;\">ver2.0</a>");
        out.println("(Demo version)");
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
     * タイトル＆検索アリアを描画する
     * @param out
     * @param queryStr
     */
    public void displayNavbar(PrintWriter out, String queryStr) {
    	out.println("<div class=\"navbar\">");
    	out.println("<div class=\"navbar-inner\">");
    	out.println("<h1>Microblog Searcher</h1>");
    	out.println("<div id=\"serch-zone\">");
    	out.println("<form action=\"./MicroblogSearcherSV\" method=\"POST\">");
    	out.println("<p>");
    	out.println("<input type=\"search\" name=\"QueryStr\" size=\"40\" required=\"required\" value=\"" + queryStr + "\">");
    	out.println("<input type=\"submit\" value=\"検索\">");
    	out.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
    	out.println("<span style=\"color:white;font-size:14.5px;\">");
    	out.println("見つけるTweetの最大数");
    	out.println("<input type=\"text\" name=\"hitnumber\" pattern=\"^[0-9]+$\" value=\"" + hitNumber + "\" size=\"4\">");
    	out.println("件");
    	out.println("<br>");
    	out.println("</span>");
    	out.println("</p>");
    	out.println("</form>");
    	out.println("</div>");
    	out.println("</div>");
    	out.println("</div>");


    }

    public void displayBody(PrintWriter out, String queryStr, List<String> searchList, String reQueryStrFilter, RunQuery runQuery) {

    	out.println("<body>");
    	displayHeader(out);
    	out.println("<div class=\"contents\">");
    	displayNavbar(out,queryStr);
    	displayListArea(out, searchList, reQueryStrFilter, runQuery);
    	out.println("</div>");
    	displayJQuery(out);
    	out.println("</body>");

    }


    @SuppressWarnings({ "rawtypes" })
	public void displayListArea(PrintWriter out, List<String> searchList, String reQueryStrFilter, RunQuery runQuery) {

//    	TemporalProfile tempProfile = new TemporalProfile(runQuery, reQueryStrFilter, hitNumber);


    	List<Map> queryList = getSearchList(searchList);

    	out.println("<div class=\"listContents\">");

    	// left list area
    	out.println("<div class=\"leftList\">");
    	out.println("<div class=\"arrow_box \">");

    	int index = 0;
    	StringBuilder indexStrB = new StringBuilder();
    	indexStrB.append("index");
    	String indexStr = "";

    	if (searchList.size() != 0 && queryList.size() != 0){

    		for(Map searchMap : queryList) {
        		index++;
        		indexStrB.append(String.valueOf(index));
        		indexStr = indexStrB.toString();
        		out.println("<p id=\""+ indexStr + "\"");
        		out.println("ondblclick=\"var id = $(this).attr('id');$('input:hidden').attr('id',id); $('input:hidden').val('" + searchMap.get("title") + "'); $('#secondSearch').submit();\"");
        		out.println("onclick=\"if($(this).attr('style') == 'background-color:#ddcccc'){$(this).attr('style', 'background-color:#d3e9f5;');}else{$(this).attr('style', 'background-color:#ddcccc'); }\"");

        		out.println(">");
            	out.println("<span style=\"color:#1e90ff\">");
            	out.println(searchMap.get("num"));
            	out.println("&nbsp;:&nbsp;</span><span style=\"background-color:transparent\">");
            	out.println(searchMap.get("title"));
            	out.println("</span><br><span style=\"color:#1e90ff\">&nbsp;:&nbsp;");
            	out.println(searchMap.get("querytime"));
            	out.println("</span>");
//            	if (index < queryList.size() - 1) {
//            		out.println("<hr>");
//            	}
            	out.println("</p>");
        	}
    	}

    	out.println("<form id=\"secondSearch\" action=\"./MicroblogSearcherSV\" name=\"secondSearch\" method =\"post\">");
    	out.println("<input name=\"ReQueryStr\" required=\"required\" type=\"hidden\">");
    	out.println("</form>");

    	out.println("</div>");
    	out.println("</div>");

    	// right list area
    	out.println("<div class=\"rightList\">");
    	out.println("<div class=\"right_arrow_box\">");

    	if (searchList.size() != 0 && queryList.size() == 2){

    		for(Map searchMap : queryList) {
        		index++;
        		indexStrB.append(String.valueOf(index));
        		indexStr = indexStrB.toString();
        		out.println("<p ");
        		out.println("onclick=\"if($(this).attr('style') == 'background-color:#ddcccc'){$(this).attr('style', 'background-color:#d3e9f5;');}else{$(this).attr('style', 'background-color:#ddcccc'); }\"");
        		out.println(">");
        		out.println("<span style=\"color:#1e90ff\">");
            	out.println(searchMap.get("num"));
            	out.println("&nbsp;:&nbsp;</span><span style=\"background-color:transparent\">");
            	out.println(searchMap.get("title"));
            	out.println("</span><br><span style=\"color:#1e90ff\">&nbsp;:&nbsp;");
            	out.println(searchMap.get("querytime"));
            	out.println("</span>");
//            	if (index < queryList.size() - 1) {
//            		out.println("<hr>");
//            	}
            	out.println("</p>");
        	}
    	}

    	out.println("</div>");
    	out.println("</div>");
    	out.println("</div>");
    }

    /**
     * 検索を行う結果を表示する
     * @param out
     * @param queryStr
     * @return 検索結果リスト
     */
	@SuppressWarnings("rawtypes")
	public List<Map> getSearchList(List<String> queryList) {

    	// lucene関連
    	Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_31);
//        MakeQueryMicroblog MQM = new MakeQueryMicroblog();
//        SearcherMicroblog searcherMB = new SearcherMicroblog(searcher);
    	// 一回目で入力する検索内容を検索リストに入る．
//    	List<String> queryList = new ArrayList<String>();
//    	queryList.add(queryStr);
    	List<Map> resultFirstTime = new ArrayList<Map>();
    	// TODO hostname and port number is undecided.

    	RunQuery runQuery = new RunQuery(hostname, port);
    	// TODO
    	int retnum = 0;
		resultFirstTime = runQuery.search1(queryList, retnum);

		return resultFirstTime;
    }


    public void displayJQuery(PrintWriter out) {
    	out.println("<script>");
        out.println("$(function() {");

        out.println("$('.arrow_box').sortable();");
        out.println("$('.right_arrow_box').sortable();");

        out.println("$('.arrow_box').draggable();");
        out.println("$('.right_arrow_box').draggable();");

        //    $('.arrow_box').draggable();
        //$('.right_arrow_box').draggable();
//        out.println("$(\'#secondSearch\').submit();");
        out.println("});");
        out.println("</script>");
    }


}
