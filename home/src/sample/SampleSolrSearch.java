package sample;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Servlet implementation class SampleSolrSearch
 */
@WebServlet("/SampleSolrSearch")
public class SampleSolrSearch extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static final String POST_ENCODING = "UTF-8";
	//public static final String VERSION_OF_THIS_TOOL = "1.2";
  private static final String solrRequestURL = "http://localhost:8983/solr/collection1/select?wt=csv&q=text%3A";
	protected static URL solrUrl = null;


    /**
     * @see HttpServlet#HttpServlet()
     */
    public SampleSolrSearch() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("text/html; charset=UTF-8");
    // POSTにリダイレクト
    this.doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("text/html; charset=UTF-8");
    String query = "地震 福島";
    System.out.println("query : " + query);
    String data[] = null;
    data = query.split(" ");
    String solrQuery = "";
    for (int i=0; i<data.length; i++) {
      System.out.println(data[i]);
      if (i != 0) {
        solrQuery = solrQuery + "\"" + data[i] + "\"";
      } else {
        solrQuery = solrQuery + "\"" + data[i] + "\"";
      }
      System.out.println(solrQuery);
    }
    try {
      select(solrQuery);
    } catch (ParserConfigurationException e) {
      // TODO 自動生成された catch ブロック
      e.printStackTrace();
    } catch (TransformerException e) {
      // TODO 自動生成された catch ブロック
      e.printStackTrace();
    }
	}

	static void select(String body) throws ParserConfigurationException, TransformerException {
	  try {
	    StringBuffer request = new StringBuffer();
	    //request.append("http://localhost:8983/solr/select/?wt=csv&fl=*%2Cscore&rows=1000&q=");
	    request.append(solrRequestURL);
	    request.append(URLEncoder.encode(body , "UTF-8"));
	    solrUrl = new URL(request.toString());
	  } catch (Exception e) {
	    e.printStackTrace();
	    return;
	  }
	  getData();
	}


	static void getData() throws ParserConfigurationException, TransformerException {
	  HttpURLConnection urlc = null;
	  try {
	    urlc = (HttpURLConnection) solrUrl.openConnection();
	    try {
	      urlc.setRequestMethod("GET");
	    } catch (ProtocolException e) {
	      e.printStackTrace();
	    }
	    urlc.setRequestProperty("Content-type", "text/xml; charset="
	        + POST_ENCODING);

	    // BufferedReaderで結果を受け取る
	    urlc.connect();
	    BufferedReader reader =
	        new BufferedReader(new InputStreamReader(urlc.getInputStream(), POST_ENCODING));

      /*XMLのための処理*/
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      DOMImplementation domImpl=builder.getDOMImplementation();
      Document document = domImpl.createDocument("","tweets",null);
      Element root = document.getDocumentElement();

	    int row=0;
	    while (true){
	      /** line は返却されるtweet(1行ずつ)**/
	      String line = reader.readLine();
	      if ( line == null ){
	        break;
	      }
	      System.out.println(line);
	      String data[]=null;
	      /** csv形式のデータを各属性に分ける **/
	     data = line.split(",");
	      if(row>0){
	        /** XMLの要素を生成 **/
	        Element docNode = document.createElement("doc");
	        Element idNode = document.createElement("id");
	        Element timeNode = document.createElement("time");
	        Element nameNode = document.createElement("name");
	        Element scoreNode = document.createElement("score");
	        Element tweetNode = document.createElement("tweet");
	        Element lonNode = document.createElement("longitude");
	        Element latNode = document.createElement("latitude");

	        idNode.appendChild(document.createTextNode(data[0]));
	        timeNode.appendChild(document.createTextNode(data[1]));
	        nameNode.appendChild(document.createTextNode(data[2]));
	        scoreNode.appendChild(document.createTextNode(data[3]));
	        tweetNode.appendChild(document.createTextNode(data[4]));
	        lonNode.appendChild(document.createTextNode(data[5]));
	        latNode.appendChild(document.createTextNode(data[6]));

	        docNode.appendChild(idNode);
	        docNode.appendChild(timeNode);
	        docNode.appendChild(nameNode);
	        docNode.appendChild(scoreNode);
	        docNode.appendChild(tweetNode);
	        docNode.appendChild(lonNode);
	        docNode.appendChild(latNode);

	        root.appendChild(docNode);

	        /*
              log.write("<doc>\n");
              log.write("<id>"+data[0]+"</id>\n");
              log.write("<time>"+data[1]+"</time>\n");
              log.write("<tweet>"+data[2]+"</tweet>\n");
              log.write("<longitude>"+data[3]+"</longitude>\n");
              log.write("<latitude>"+data[4]+"</latitude>\n");
              log.write("</doc>\n");
	         */
	      }
	      row++;
	      //com.write(line+"\n");
	    }//while true
	    //com.close();
	    // log.write("</tweets>\n");
	    //出力
	    TransformerFactory transFactory = TransformerFactory.newInstance();
	    Transformer transformer = transFactory.newTransformer();

	    DOMSource source = new DOMSource(document);
	    File newXML = new File("/home/okamu-/html5/newXML.xml");
	    FileOutputStream os = new FileOutputStream(newXML);
	    StreamResult result = new StreamResult(os);
	    transformer.transform(source, result);

	    //  log.close();
	    System.out.println("検索結果 : "+(row-1)+" 件");

	  } catch (IOException e) {
	    e.printStackTrace();
	  } finally {
	    if (urlc != null)
	      urlc.disconnect();
	  }
	}

}
