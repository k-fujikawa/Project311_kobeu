package search;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;



import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.mongodb.DBObject;

public class RunQuery {
	private String field = null;
	private String index = null;
	private String server = null;

	// for Lucene
	private Directory indexDir = null;
  private IndexReader reader = null;
  private IndexSearcher searcher = null;
  private Similarity similarity = null;
  private Analyzer analyzer = null;

	public RunQuery(String index_) {
		this.index = index_;
	}

	public RunQuery(String hostname, int port) {
		this.server = hostname + ":" + Integer.toString(port);
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getIndexPath() {
		return this.index;
	}

	public String getServerName() {
		return this.server;
	}

	/**
	 * クエリ文字列を合成する
	 * @param qs クエリ文字列のリスト
	 * @param filed 検索するフィールド
	 * @return 合成されたクエリ文字列
	 */
	public String qs2query(List<String> qs, String filed) {
    if (filed != null) {
      this.field = filed;
    } else {
      this.field = "text";
    }
	  StringBuffer sb = new StringBuffer();
    for (String q : qs ) {
      sb.append(" " + q);
      /*if (q.split(" ").length >= 2) {
        sb.append(" +" + q); // AND
      } else {
        sb.append(" " + q);  // OR
      }*/
    }
    return sb.toString();
	}

	/*
	public List<Map> search2(List<String> qs, int retnum) {
		String[] names;
		List<Map> list = new ArrayList<Map>();
		try {
			String query = qs2query(qs, field);
			System.out.println("QUERY : " + query + "(" + retnum + ")");
			System.out.println("Index : " + this.index);
			System.out.println("Server : " + this.server);

			String[] cmd = { "/usr/local/bin/IndriRunQuery", "-query=" + query,
					"-count=" + Integer.toString(retnum), "-server=" + server,
					"-trecFormat=true" };
			System.out.println("COMMAND : " + StringUtils.join(cmd, " "));
			Process process = Runtime.getRuntime().exec(cmd);
			InputStream is = process.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = br.readLine()) != null) {
				String tweetid = line.split(" ")[2];
				String score = line.split(" ")[4];
				DBObject tweet_obj = db.getTweet(tweetid);
				String tweet_str = (String) tweet_obj.get("tweet");
				Date date = (Date) tweet_obj.get("date");
				Map map = new HashMap<String, String>();
				map.put("score", new Double(score));
				map.put("tweetid", tweetid);
				map.put("date", date);
				map.put("tweet", tweet_str);
				list.add(map);
			}
			// InputStream eis =process.getErrorStream();
			// BufferedReader ebr = new BufferedReader(new
			// InputStreamReader(eis));
			// while ((line = ebr.readLine()) != null) {
			// System.err.println(line);
			// }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public List<Map> search1(List<String> qs, int retnum) {
		String[] names;
		List<Map> list = new ArrayList<Map>();

		try {
			env = new QueryEnvironment();
			if (index != null) {
				System.out.println("Add index : " + index);
				env.addIndex(index);
			} else if (server != null) {
				System.out.println("Add server : " + server);
				env.addServer(server);
			} else {
				System.err.println("You should specify index or server.");
			}
			String query = qs2query(qs, field);
			System.out.println("QUERY : " + query + "(" + retnum + ")");
			System.out.println("Index : " + this.index);
			System.out.println("Server : " + this.server);

			ScoredExtentResult[] results = env.runQuery(query, retnum);

			System.out.println("RetNum : " + results.length);

			names = env.documentMetadata(results, "docno");
			for (int i = 0; i < results.length; i++) {
				DBObject tweet_obj = db.getTweet(names[i]);
				String tweet_str = (String) tweet_obj.get("tweet");
				Date date = (Date) tweet_obj.get("date");

				Map map = new HashMap<String, String>();
				map.put("score", results[i].score);
				map.put("tweetid", names[i]);
				map.put("date", date);
				map.put("tweet", tweet_str);
				list.add(map);
			}
			env.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
*/

	/**
	 * Luceneを使った検索を行なって結果のList<Map>を返す
	 * @param query 合成済みクエリ
	 * @param retnum
	 * @return
	 */
	public List<Map> search(String query, int retnum) {
    String[] names;
    List<Map> list = new ArrayList<Map>();              // 検索結果のList<Map>

    try {
      if (index != null) {                              // indexがセットされている時は、そのindexで検索
        System.out.println("Add index : " + index);
        indexDir = FSDirectory.open(new File(index));
        reader = IndexReader.open(indexDir);
        searcher = new IndexSearcher(reader);
        similarity = new BM25Similarity();
        //similarity = new DefaultSimilarity();
        searcher.setSimilarity(similarity);
        analyzer = new AnalyzerFactory(Version.LUCENE_40).getJapaneseEnglishAnalyzer();
      } else if (server != null) {
        System.out.println("Add server : " + server);   // サーバがセットされている時は、そのサーバで検索
        System.err.println("Sorry, I don't know how to use Lucene on server...");
      } else {                                          // indexかサーバどちらかがセットされていないとエラー
        System.err.println("You should specify index or server.");
      }
      System.out.println("QUERY : " + query + "(" + retnum + ")");
      System.out.println("Index : " + this.index);
      System.out.println("Server : " + this.server);

      // 検索
      QueryParser qp = new QueryParser(Version.LUCENE_40, field, analyzer);
      Query luceneQuery = qp.parse(query);
      TopDocs topDocs = searcher.search(luceneQuery, retnum);
      ScoreDoc[] hits = topDocs.scoreDocs;

      System.out.println("RetNum : " + hits.length);

      for (int i=0; i<hits.length; i++) {
        int docId = hits[i].doc;
        Document doc = searcher.doc(docId);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date datetime = sdf.parse(doc.get("datetime"));

        Map map = new HashMap<String, String>();
        map.put("score", new Double(hits[i].score));
        map.put("tweetid", (String) doc.get("tweet_id"));
        map.put("userid", (String) doc.get("user_id"));
        map.put("date", datetime);
        map.put("tweet", (String) doc.get("text"));
        list.add(map);
      }
      indexDir.close();
      reader.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return list;
	}

	public Map<String, Double> tagcloud(Map<String, Double> map_) {
		Map<String, Double> map = new HashMap<String, Double>();
		double Z = 0;
		double[] vs = ArrayUtils.toPrimitive(map_.values().toArray(
				new Double[0]));
		double v_max = StatUtils.max(vs);
		double v_min = StatUtils.min(vs);

		for (String term : map_.keySet()) {
			double score = ((map_.get(term) - v_min) / (v_max - v_min) + 0.1) * 10;
			map.put(term, score);
			System.out.println(score + "\t" + term);
		}
		return map;
	}

	private Map sortByValue(Map map) {
		List list = new LinkedList(map.entrySet());
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return -((Comparable) ((Map.Entry) (o1)).getValue())
						.compareTo(((Map.Entry) (o2)).getValue());
			}
		});
		Map result = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	public static void printMap(Map mp) {
		Iterator it = mp.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			if ((Integer) pairs.getValue() == 1) {
				continue;
			}
			System.out.println("'" + pairs.getKey() + " = " + pairs.getValue());
			it.remove(); // avoids a ConcurrentModificationException
		}
	}

	/**
	 * Make temporal profile from retrieved tweets
	 *
	 * @param tweets
	 * @return
	 */
	public Map<Date, Float> tweets2tp(List<Map> tweets) {
		Map<Date, Float> dateMap = new HashMap<Date, Float>();
		for (Map tweet : tweets) {
			try {
				String date_str = (String) tweet.get("date");
				DateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
				Date date = df.parse(date_str);
				// System.out.println(date);
				// System.exit(-1);
				Float count = dateMap.get(date);
				dateMap.put(date, count == null ? 1 : count + 1);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return dateMap;
	}

	/**
	 * Make chart for the temporal profile of a word
	 *
	 * @param tp_map
	 * @return
	 */
	public List chart(Map<String, Map> tp_map) {
		List tps = new ArrayList();
		Map<String, Map> tpsMap = new HashMap<String, Map>();
		Set tps_ = new HashSet();

		for (String q : tp_map.keySet()) {
			Map<Date, Double> tp = tp_map.get(q);
			tpsMap.put(q, tp);
			tps_.addAll(tp.keySet());
		}

		Date dayMax = Collections.max(tps_);
		Date dayMin = Collections.min(tps_);
		dayMax = DateUtils.addDays(dayMax, 1);
		dayMin = DateUtils.addDays(dayMin, -1);

		List days = new ArrayList();
		for (Date day = dayMin; day.getTime() <= dayMax.getTime(); day = DateUtils
				.addDays(day, 1)) {
			days.add(day);
			Map<String, Object> tp_norm = new HashMap<String, Object>();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
			tp_norm.put("time", sdf.format(day));

			for (String q : tpsMap.keySet()) {
				Map<Date, Double> tp = tpsMap.get(q);
				Double count = tp.get(day);
				count = (count == null ? 0 : count / sum(tp.values()));
				tp_norm.put(q, count);
			}
			tps.add(tp_norm);
		}
		return tps;
	}

	private double sum(Collection<Double> cols) {
		double sum = StatUtils.sum(ArrayUtils.toPrimitive(cols
				.toArray(new Double[0])));
		return sum;
	}

	/**
	 * 引数のクエリリストに少なくとも1つクエリが含まれているかを調べる.<br>
	 * @param qs
	 * @return クエリリストに少なくとも1つクエリが含まれるときtrue / すべて空またはnullのときfalse
	 */
	public boolean queryCheck(List<String> qs) {
		for (String q : qs) {
			if (q != "" && q != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param args[0] 索引のパス
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
    List<String> query = new ArrayList<String>();
    query.add("福島");
    query.add("地震情報");
    //query.add("fifa");
    //query.add("soccer");
    //query.add("2022");
    int retnum = 10;
    //RunQuery rq = new RunQuery("rubicon.cs.scitec.kobe-u.ac.jp", 5600);
    RunQuery rq = new RunQuery("/Users/kitaguchisayaka/Project/Project311/indexAllField");
    //List<Map> tweets = rq.search(rq.qs2query(query, null), retnum);
    List<Map> tweets = rq.search(rq.qs2query(query, null), retnum);
    for (Map tweet : tweets) {
      Object[] t = { tweet.get("score"), tweet.get("tweetid"),
          tweet.get("date"), tweet.get("tweet") };
      System.out.println(StringUtils.join(t, "\t"));
    }
	}
}
