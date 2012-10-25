package search;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ja.tokenattributes.PartOfSpeechAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

public class TemporalProfile {
	static String urlpat = "(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+";
	boolean stem = false;
	String[] sws = { "a", "about", "above", "according", "across", "after",
			"afterwards", "again", "against", "albeit", "all", "almost",
			"alone", "along", "already", "also", "although", "always", "am",
			"among", "amongst", "an", "and", "another", "any", "anybody",
			"anyhow", "anyone", "anything", "anyway", "anywhere", "apart",
			"are", "around", "as", "at", "av", "be", "became", "because",
			"become", "becomes", "becoming", "been", "before", "beforehand",
			"behind", "being", "below", "beside", "besides", "between",
			"beyond", "both", "but", "by", "can", "cannot", "canst", "certain",
			"cf", "choose", "contrariwise", "cos", "could", "cu", "day", "do",
			"does", "doesn't", "doing", "dost", "doth", "double", "down",
			"dual", "during", "each", "either", "else", "elsewhere", "enough",
			"et", "etc", "even", "ever", "every", "everybody", "everyone",
			"everything", "everywhere", "except", "excepted", "excepting",
			"exception", "exclude", "excluding", "exclusive", "far", "farther",
			"farthest", "few", "ff", "first", "for", "formerly", "forth",
			"forward", "from", "front", "further", "furthermore", "furthest",
			"get", "go", "had", "halves", "hardly", "has", "hast", "hath",
			"have", "he", "hence", "henceforth", "her", "here", "hereabouts",
			"hereafter", "hereby", "herein", "hereto", "hereupon", "hers",
			"herself", "him", "himself", "hindmost", "his", "hither",
			"hitherto", "how", "however", "howsoever", "i", "ie", "if", "in",
			"inasmuch", "inc", "include", "included", "including", "indeed",
			"indoors", "inside", "insomuch", "instead", "into", "inward",
			"inwards", "is", "it", "its", "itself", "just", "kind", "kg", "km",
			"last", "latter", "latterly", "less", "lest", "let", "like",
			"little", "ltd", "many", "may", "maybe", "me", "meantime",
			"meanwhile", "might", "moreover", "most", "mostly", "more", "mr",
			"mrs", "ms", "much", "must", "my", "myself", "namely", "need",
			"neither", "never", "nevertheless", "next", "no", "nobody", "none",
			"nonetheless", "noone", "nope", "nor", "not", "nothing",
			"notwithstanding", "now", "nowadays", "nowhere", "of", "off",
			"often", "ok", "on", "once", "one", "only", "onto", "or", "other",
			"others", "otherwise", "ought", "our", "ours", "ourselves", "out",
			"outside", "over", "own", "per", "perhaps", "plenty", "provide",
			"quite", "rather", "really", "round", "said", "sake", "same",
			"sang", "save", "saw", "see", "seeing", "seem", "seemed",
			"seeming", "seems", "seen", "seldom", "selves", "sent", "several",
			"shalt", "she", "should", "shown", "sideways", "since", "slept",
			"slew", "slung", "slunk", "smote", "so", "some", "somebody",
			"somehow", "someone", "something", "sometime", "sometimes",
			"somewhat", "somewhere", "spake", "spat", "spoke", "spoken",
			"sprang", "sprung", "stave", "staves", "still", "such",
			"supposing", "than", "that", "the", "thee", "their", "them",
			"themselves", "then", "thence", "thenceforth", "there",
			"thereabout", "thereabouts", "thereafter", "thereby", "therefore",
			"therein", "thereof", "thereon", "thereto", "thereupon", "these",
			"they", "this", "those", "thou", "though", "thrice", "through",
			"throughout", "thru", "thus", "thy", "thyself", "till", "to",
			"together", "too", "toward", "towards", "ugh", "unable", "under",
			"underneath", "unless", "unlike", "until", "up", "upon", "upward",
			"upwards", "us", "use", "used", "using", "very", "via", "vs",
			"want", "was", "we", "week", "well", "were", "what", "whatever",
			"whatsoever", "when", "whence", "whenever", "whensoever", "where",
			"whereabouts", "whereafter", "whereas", "whereat", "whereby",
			"wherefore", "wherefrom", "wherein", "whereinto", "whereof",
			"whereon", "wheresoever", "whereto", "whereunto", "whereupon",
			"wherever", "wherewith", "whether", "whew", "which", "whichever",
			"whichsoever", "while", "whilst", "whither", "who", "whoa",
			"whoever", "whole", "whom", "whomever", "whomsoever", "whose",
			"whosoever", "why", "will", "wilt", "with", "within", "without",
			"worse", "worst", "would", "wow", "ye", "yet", "year", "yippee",
			"you", "your", "yours", "yourself", "yourselves",
			"com", "gt", "jp", "rt", "あたし", "あなた", "いわく", "お気", "今日", "今回", "こちら", "こっち",
			"さん","そ", "そう", "ちゃん", "どこ", "どれ", "はず", "はん", "ふう", "み", "みたい",
			"皆さん", "みなさん", "よ", "よう", "私", "わたし"
	    };
	Set<String> stopwords = new HashSet<String>();
	RunQuery rq;
	List<Map> tweets;
	// List<DateScore> dsList = new ArrayList<DateScore>();
	//List<String> qs;
	String query;

	public int size() {
		return tweets.size();
	}

	/**
	 * コンストラクタ.
	 * @param rq RunQuery
	 * @param query 合成済みクエリ
	 * @param retnum 返すtweet数
	 */
	public TemporalProfile(RunQuery rq, String query, int retnum) {
		this.rq = rq;
		this.query = query;
		tweets = rq.search(this.query, retnum);
		System.out.println("tweets size : " + tweets.size());
		stopwords = new HashSet<String>(Arrays.asList(sws));
	}

	// public TemporalProfile(RunQuery rq, List<String> qs, int retnum) {
	// this.rq = rq;
	// this.qs = qs;
	// tweets = rq.search(this.qs, retnum);
	// System.out.println("tweets size : "+tweets.size());
	// stopwords = new HashSet<String>(Arrays.asList(sws));
	// }

	/**
	 * tweet本文に含まれる単語の配列を受け取って、ストップワードと1文字以下の単語を除去する。
	 * @param terms_ tweet本文に含まれる単語の配列
	 * @return ストップワードと1文字以下の単語が除かれた単語の配列
	 */
	public String[] stop(String[] terms_) {
		List<String> terms = new ArrayList<String>();
		for (int i = 0; i < terms_.length; i++) {
			if (stopwords.contains(terms_[i]) || terms_[i].length() <= 1) {
				continue;
			}
			terms.add(terms_[i]);
		}
		return terms.toArray(new String[0]);
	}

	public String[] stem(String[] terms_) {
		String[] terms = null;
		return terms;
	}

	/**
	 * tweet本文からURLを除去、また英数字以外も除去．
	 * 単語ごとに分割し、String[]として返す
	 * @param sent tweet本文
	 * @return 単語(String)の配列
	 */
	public String[] prepro_(String sent) {
		String[] terms = null;
		// System.out.println(sent);
		sent = sent.toLowerCase();                 // textをすべて小文字に
		sent = sent.replaceAll(urlpat, " ");       // textからURL除去
		sent = sent.replaceAll("[^a-z0-9]", " ");  // 英数字以外を除去
		sent = sent.replaceAll(" +", " ");         // 空白の連続を1つにマージ
		terms = sent.split(" ");                   // 単語の集合が出来上がり（ストップワード等そのまま）
		return terms;
	}

	/**
	 * Japanese対応版
   * tweet本文からURLを除去、また英数字以外も除去．
   * 単語ごとに分割し、String[]として返す
   * @param sent tweet本文
   * @return 単語(String)の配列
   */
  public String[] prepro(String sent) {
    List<String> termList = new ArrayList<String>();
    Analyzer analyzer = new AnalyzerFactory(Version.LUCENE_40).getJapaneseEnglishAnalyzerSearch();
    // System.out.println(sent);
    sent = sent.toLowerCase();                 // textをすべて小文字に
    sent = sent.replaceAll(urlpat, " ");       // textからURL除去
    String[] words = sent.split(" ");          // usernameとhashtagを選択
    for (String word : words) {
      int at_index = word.indexOf("@");
      if (at_index != -1) {
        if (at_index+2 < word.length()) { // @単体をつぶやくユーザがいるようだ
          String user_name = word.substring(at_index, word.length()-1);
          user_name = user_name.replaceAll("[^@_a-zA-Z0-9]", "");
          termList.add(user_name);
          sent = sent.replace(user_name, "");
          //System.out.println(":::::::::::::::::::::::::::::::::::" + user_name);
        }
      }
      int hashtag_index = word.indexOf("#");
      if (hashtag_index != -1) {
        String hash_tag = word.substring(hashtag_index);
        hash_tag = hash_tag.replaceAll("[^#_a-zA-Z0-9]", "");
        termList.add(hash_tag);
        sent = sent.replace(hash_tag, "");
        //System.out.println(":::::::::::::::::::::::::::::::::::" + hash_tag);
      }
    }
    StringReader sReader = new StringReader(sent);
    try {
      TokenStream stream = analyzer.tokenStream("", sReader);
      while (stream.incrementToken()) {
        CharTermAttribute termAtt
        = stream.getAttribute(CharTermAttribute.class); //単語そのものを取り出す
        PartOfSpeechAttribute psAtt
        = stream.getAttribute(PartOfSpeechAttribute.class); // 品詞を取り出す
        String psAttStr = psAtt.getPartOfSpeech();
        if (psAttStr.indexOf("名詞") != -1) {
          String noun = termAtt.toString();
          if (!noun.matches("^[0-9]*$")) {          // 数字だけで構成されているものはリストに加えない
            termList.add(termAtt.toString());
          }
        }
      }
      stream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    sReader.close();
    String[] terms = (String[]) termList.toArray(new String[termList.size()]);
    return terms;
  }

	/**
	 * ヒットしたtweetに登場した単語のセットを返す（重複なし）
	 * @param top
	 * @return ヒットしたtweetに登場した単語のセット（重複なし）
	 */
	public Set<String> candTerms_(int top) {
		Set<String> terms = new HashSet<String>();
		for (int i = 0; i < top && i < tweets.size(); i++) {
			Map tweet = tweets.get(i);
			String tweetid = (String) tweet.get("tweetid"); // tweet_id 取得
			String tweet_str = (String) tweet.get("tweet"); // text 取得
			String[] terms_ = prepro(tweet_str);            // textに含まれる単語のリストを生成
			terms_ = stop(terms_);                          // ストップワードと1文字以下の単語を除去
			terms.addAll(Arrays.asList(terms_));            // 配列中の単語(String)をSetに追加。同じ単語はマージされる
		}
		return terms;
	}

	 /**
	  * Japanese対応版
   * ヒットしたtweetに登場した単語のセットを返す（重複なし）
   * @param top
   * @return ヒットしたtweetに登場した単語のセット（重複なし）
   */
  public Set<String> candTerms(int top) {
    Set<String> terms = new HashSet<String>();
    System.out.println("########## TemporalPrifile.candTerms ##########");
    System.out.println("size of tweets = " + tweets.size());
    for (int i = 0; i < top && i < tweets.size(); i++) {
      Map tweet = tweets.get(i);
      String tweetid = (String) tweet.get("tweetid"); // tweet_id 取得
      String tweet_str = (String) tweet.get("tweet"); // text 取得
      System.out.println("(tweetid, tweet) = (" + tweetid + ", " + tweet + ")");
      String[] terms_ = prepro(tweet_str);            // textに含まれる単語のリストを生成
      System.out.println("terms = " + terms);
      terms_ = stop(terms_);                          // ストップワードと1文字以下の単語を除去
      terms.addAll(Arrays.asList(terms_));            // 配列中の単語(String)をSetに追加。同じ単語はマージされる
    }
    System.out.println("##########################################");
    return terms;
  }


	// build query profile
	/**
	 * ヒットした全てのtweetの、exp(ヒットしたtweetのスコア)と、dateを持つクラスDateScore、のリストを返す。
	 * @return
	 */
	public List<DateScore> queryProfile() {
		List<DateScore> tp = new ArrayList<DateScore>();
		for (Map tweet : tweets) {
			String tweetid = (String) tweet.get("tweetid");
			Double score = (Double) tweet.get("score");
			Date date = (Date) tweet.get("date");
			score = Math.exp(score);
			DateScore ds = new DateScore(date, score);
			tp.add(ds);
		}
		return tp;
	}

	// build expanded query profile
	/**
	 * 単語ごとに、登場した日付とその日のexp(スコア)の組をリストで返す。
	 * Map<String, List<DateScore>> = Map< 単語, <DateScore, DateScore, ...> >
	 * ここで、DateScoreは[ 日付, exp(スコア) ]
	 * @param cand_terms ヒットしたtweetに登場した単語のリスト（重複なし）
	 * @return
	 */
	public Map<String, List<DateScore>> exQueryProfile(Set<String> cand_terms) {
		Set<String> cts = new HashSet();
		for (String cand_term : cand_terms) { // 登場単語を1つずつ取り出して
			cts.add(cand_term.toLowerCase());   // 小文字にして複製
		}

		Map<String, List<DateScore>> map = new HashMap<String, List<DateScore>>();
		for (Map tweet : tweets) {
			String tweetid = (String) tweet.get("tweetid");
			String tweet_str = (String) tweet.get("tweet");

			for (String term : prepro(tweet_str)) {  // 不要なものを除去した単語、ごとに
				// register a term
				if (cts.contains(term)) {                     // その単語が既に登場していた場合
					Double score = (Double) tweet.get("score"); // tweetのスコア取得
					Date date = (Date) tweet.get("date");       // tweetの日付取得
					score = Math.exp(score);                    // exp(スコア)
					DateScore ds = new DateScore(date, score);  // その日付とスコアの組
					List<DateScore> dsList = null;
					if (map.containsKey(term)) {
						dsList = map.get(term);
					} else {
						dsList = new ArrayList<DateScore>();
					}
					dsList.add(ds);
					map.put(term, dsList);                      //
				}
			}
		}
		return map;
	}

	/**
	 * 日毎に、その日にヒットしたtweetのスコアを全体スコアで割ったものを計算。
	 * Map<Date, Double> = HashMap<年月日, その日のtweetの総スコアを全体スコアで割ったもの(総スコアにおけるその日のスコアの割合)>
	 * @param dsList
	 * @param top
	 * @return
	 */
	public Map<Date, Double> temporalModel(List<DateScore> dsList, int top) {
		Map<Date, Double> tmDic = new HashMap<Date, Double>();
		List<DateScore> tmp = new ArrayList<DateScore>();
		double scoreAll = 0;

		// dsListをtmpに複製
		for (int i = 0; i < top && i < dsList.size(); i++) {
			DateScore df = dsList.get(i);
			tmp.add(df);
		}
		for (DateScore ds : tmp) {
			scoreAll += ds.score;     // 全スコアの合計
		}
		for (DateScore ds : tmp) { // DateScoreを1つずつ取り出して
			Date d = new Date(ds.date.getYear(), ds.date.getMonth(),
					ds.date.getDate());
			if (tmDic.containsKey(d)) { // 同一年月日が既にtmDicにあるとき
				tmDic.put(d, tmDic.get(d) + ds.score / scoreAll);
			} else {
				tmDic.put(d, ds.score / scoreAll);
			}
		}
		return tmDic;
	}

	// public void enFilter(float th) {
	// List<Map> tweets_en = new ArrayList<Map>();
	// for (Map tweet : tweets) {
	// String docno = (String) tweet.get("docno");
	// if (db.getLang(docno) < th) {
	// continue;
	// }
	// tweets_en.add(tweet);
	// }
	// this.tweets = tweets_en;
	// }

	/**
	 * @param tempModelA ヒットした全てのtweet分だけの、日付とその日の正規化スコアのマップ
	 * @param tempModelB 対象の単語が登場した日付ごとの、日付とその日の正規化スコアのマップ
	 * @param alpha
	 * @return
	 */
	public double KLdiv(Map<Date, Double> tempModelA,
	    Map<Date, Double> tempModelB, double alpha) {
		List<Date> dates = new ArrayList<Date>();
		for (Date d : tempModelB.keySet())
			dates.add(d);
		for (Date d : tempModelA.keySet())
			dates.add(d);
		// dates.addAll(tempModelB.keySet());
		// System.exit(-1);
		// System.out.println(dates);
		double kl = 0;
		for (Date date : new HashSet<Date>(dates)) { // 2つのMapから取り出したDateをマージ(重複を削除)
			double probA = 0;
			double probB = 0;
			if (tempModelA.containsKey(date)) {
				probA = tempModelA.get(date) * alpha + 1.0 / dates.size()
						* (1 - alpha);
			} else {
				probA = 1.0 / dates.size() * (1 - alpha);
			}
			if (tempModelB.containsKey(date)) {
				probB = tempModelB.get(date) * alpha + 1.0 / dates.size()
						* (1 - alpha);
			} else {
				probB = 1.0 / dates.size() * (1 - alpha);
			}
			kl += probB * Math.log(probB / probA) / Math.log(2);
		}
		return -kl;
	}

	/**
	 * 単語とKL擬似距離のマップを返す
	 * @param M
	 * @param L
	 * @param alpha
	 * @param enTh
	 * @return
	 */
	public Map<String, Double> TSQE(int M, int L, double alpha, double enTh) {
	  System.out.println("############### TemporalProfile.TSQE #################");
		Map<String, Double> klDic = new HashMap();
		Set<String> candTerms = new HashSet<String>();
		// enFilter(enTh);
		candTerms = candTerms(M); // ヒットしたtweetに登場した単語のリスト（重複なし）
		System.out.println("size of candTerms = " + candTerms.size());
		Map<Date, Double> tempQueryModel = temporalModel(queryProfile(), L);   // ヒットした全てのtweet分だけ、日付とその日の正規化スコアのマップを生成
		System.out.println("size of tempQueryModel = " + tempQueryModel.size());
		Map<String, List<DateScore>> exQueryProfiles = exQueryProfile(candTerms); // 単語と、その単語が登場したtweetの日付とexp(スコア)のマップを作成
		System.out.println("size of exQueryProfiles = " + exQueryProfiles.size());
		Map<String, Double> klScoreList = new HashMap<String, Double>();
		for (String term : exQueryProfiles.keySet()) {                         // 単語を1つずつ取り出して
			List<DateScore> exQueryProfile = exQueryProfiles.get(term);          // その単語に対応するDateScoreのリストを取得
			Map<Date, Double> tempExQueryModel = temporalModel(exQueryProfile,   // その単語が登場した日付ごとに、日付とその日の正規化スコアのマップを作成
					L);
			Double klScore = KLdiv(tempQueryModel, tempExQueryModel, alpha);     // KL擬似距離
			klDic.put(term, klScore);                                            // 単語とKL擬似距離のマップ
		}
		System.out.println("size of KlDic = " + klDic.size());
		System.out.println("######################################################");
		return klDic;
	}

	public Map<String, Double> topCandTerms(Map<String, Double> map, int top) {

		List<Map.Entry> entries = new ArrayList<Map.Entry>(map.entrySet());
		Collections.sort(entries, new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				Map.Entry<String, Double> e1 = (Map.Entry<String, Double>) o1;
				Map.Entry<String, Double> e2 = (Map.Entry<String, Double>) o2;
				return (e1.getValue() - e2.getValue()) < 0 ? 1 : -1;
			}
		});
		Map<String, Double> map_ = new HashMap<String, Double>();
		for (int i = 0; i < top && i < entries.size(); i++) {
			Map.Entry<String, Double> entry = entries.get(i);
			map_.put(entry.getKey(), entry.getValue());
		}
		return map_;
	}

	public List<Object[]> topTweets(int top) {
		List<Object[]> docs = new ArrayList<Object[]>();
		for (int i = 0; i < top && i < tweets.size(); i++) {
			Map tweet = tweets.get(i);
			Double score = (Double) tweet.get("score");
			String tweetid = (String) tweet.get("tweetid");
			Date date = (Date) tweet.get("date");
			String tweet_str = (String) tweet.get("tweet");
			Object[] os = { score, tweetid, date, tweet_str };
			docs.add(os);
		}
		return docs;
	}

	private void printCandTerms(Map<String, Double> map) {
		List<Map.Entry> entries = new ArrayList<Map.Entry>(map.entrySet());
		Collections.sort(entries, new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				Map.Entry<String, Double> e1 = (Map.Entry<String, Double>) o1;
				Map.Entry<String, Double> e2 = (Map.Entry<String, Double>) o2;
				return (e1.getValue() - e2.getValue()) < 0 ? 1 : -1;
			}
		});
		for (Map.Entry<String, Double> entry : entries) {
			System.out.println(entry.getKey() + " "
					+ entry.getValue().toString());
		}
	}

	public void printTopTweets(int top) {
		List<Object[]> tweets = topTweets(top);
		for (Object[] tweet : tweets) {
			System.out.println(StringUtils.join(tweet, "\t"));
		}
	}

	/**
	 * @param args
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		Set<String> candTerms = new HashSet<String>();
		List<String> qs = new ArrayList<String>();
		// String query = "FIFA soccer 2022";
		qs.add("福島");
		// qs.add("World Cup");
		qs.add("原発");
		qs.add("地震");
		int retnum = 1000;
		int M = 10;
		int L = 20;
		double alpha = 0.9;
		double enTh = 0.1;
    //RunQuery rq = new RunQuery("rubicon.cs.scitec.kobe-u.ac.jp", 5600);
    //RunQuery rq = new RunQuery("localhost", 57000);
		RunQuery rq = new RunQuery("/Users/kitaguchisayaka/Project/Project311/indexAllField3000");
		String query = rq.qs2query(qs, null);
		TemporalProfile tp = new TemporalProfile(rq, query, retnum);
		Map<String, Double> map = tp.TSQE(M, L, alpha, enTh);
		tp.printCandTerms(map);
		tp.printTopTweets(10);
	}
}
