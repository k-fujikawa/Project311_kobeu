package main;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import search.RunQuery;
import search.TemporalProfile;

import com.google.gson.Gson;

/**
 * Servlet implementation class TwitterSets
 */
@WebServlet("/TwitterSetsServlet")
public class CopyOfTwitterSetsServlet_back extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String url = "";
	public static final String DOCTYPE = "<!DOCTYPE HTML>";
	public static String CSSD = "/css/";
	public static String JSD = "/script/";
	public static String IMGD = "/img/";
	private static String index = "/Users/taiki/data/Tweets2011/data/index/indri/trectext.en.krov";
	//private static String index = "/home/taiki/mnt/rubicon/data/Microblog2011/data/index/indri/trectext.en.krov";
	//private static String index = "/home/taiki/data/Tweets2011/index/indri/trectext.en.krov/";
	String hostname = "rubicon.cs.scitec.kobe-u.ac.jp";
	int port = 5600;
	
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public CopyOfTwitterSetsServlet_back() {	
		super();
		//-Djava.library.path=/usr/local/lib
		//System.setProperty("java.library.path", "/usr/local/lib");
		//System.setProperty("java.library.path", "/home/taiki/app/lib");
		//System.setProperty("java.library.path", "/home/taiki/data/src/indri-5.3/swig/obj/java");
		//System.out.println("index = " + index);
		//System.out.println("java.library.path = " + System.getProperty("java.library.path"));
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.sendRedirect("/TwitterSets/index.html");
		//response.sendRedirect("http://www.ai.cs.kobe-u.ac.jp/~taiki/twisets/index.html");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		//System.out.println("doPost OK!");
		//System.out.println("doPost OK! "+ index);
		//RunQuery rq = new RunQuery(index);
		RunQuery rq = new RunQuery(hostname, port);
		Gson gs = new Gson();
		PrintWriter out = response.getWriter();
		List<String> qs = new ArrayList<String>();
		qs.add(request.getParameter("q1"));
		qs.add(request.getParameter("q2"));
		qs.add(request.getParameter("q3"));
		qs.add(request.getParameter("q4"));
		qs.add(request.getParameter("q5"));
		//System.out.println(ArrayUtils.toString(qs));
		String query = rq.qs2query(qs, null);
		//String query = StringUtils.join(qs, " ");
		//query = StringUtils.strip(query);
		String cw = request.getParameter("cw");
		
		/* original query processing */
		if (rq.queryCheck(qs) && cw == null) {
			System.out.println("QUERY : "+query);
			Set<String> candTerms = new HashSet<String>();
			int tweetnum = 10;
			int tagnum = 25;
			int retnum = 100;
			int M = 30;
			int L = 30;
			double alpha = 0.9;
			double enTh = 0.1;
			TemporalProfile tp = new TemporalProfile(rq, query, retnum);
			System.out.println("TP size : "+tp.size());
			////////////////////////////////////			
			//System.out.println("JS"+gs.toJson(js).toString());
			////////////////////////////////////
			if(tp.size() == 0){
				Map js = new HashMap();
				out.println(gs.toJson(js).toString());
			}else{
				Map<String, Double> map = tp.TSQE(M, L, alpha, enTh);
				Map<String, Double> tagcloud = rq.tagcloud(tp.topCandTerms(map, tagnum));
				List<Object[]> tweets = tp.topTweets(tweetnum);
				Map js = new HashMap();
				//js.put("tweets", ArrayUtils.subarray(tweets.toArray(), 0, tweetnum));
				js.put("tweets", tweets);
				js.put("tagcloud", tagcloud);
				out.println(gs.toJson(js).toString());
			}
		}
		/* expanded query processing */
		else if (rq.queryCheck(qs)  && cw != null) {
			List<String> eqs = new ArrayList<String>(qs);
			eqs.add(cw);
			String equery = rq.qs2query(eqs, null);
			int retnum = 100;
			int tweetnum = 10;
			int L = 30;
			
			Set<String> cand_terms = new HashSet<String>();
			cand_terms.add(cw);
			TemporalProfile tp_q = new TemporalProfile(rq, query, retnum);
			TemporalProfile tp_eq = new TemporalProfile(rq, equery, retnum);
			
			Map<String, Map> tp_map = new HashMap<String, Map>();
			tp_map.put(query,  tp_q.temporalModel(tp_q.queryProfile(), L));
			// build temporal model using AND+
			tp_map.put(equery, tp_q.temporalModel(tp_q.exQueryProfile(cand_terms).get(cw), L));
			List tps = rq.chart(tp_map);

			Map js = new HashMap();
			System.out.println("QP size : "+tp_q.size());
			System.out.println("EQP size : "+tp_eq.size());
			
			js.put("tweets", tp_eq.topTweets(tweetnum));
			js.put("chart", tps);
			out.println(gs.toJson(js).toString());
			////////////////////////////////////
			//System.out.println(gs.toJson(tps));
			////////////////////////////////////
		} else {
			System.err.println("No query");
		}
		out.close();
	}
}
