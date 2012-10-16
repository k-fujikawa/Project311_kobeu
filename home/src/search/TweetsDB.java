package search;

import java.net.UnknownHostException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class TweetsDB {
	Mongo m;
	DB db;

	/**
	 * データベースを取得して，フィールドにセットする
	 */
	public TweetsDB() {
		try {
			m = new Mongo();                  // データベースに接続
			db = m.getDB("tweet_database");   // データベース"tweet_database"を取得
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	/**
	 * htmlsコレクションの"tweetid"フィールドから文字列docnoを検索する
	 * @param docno "tweetid"フィールドから検索したい文字列
	 * @return 検索結果のDBObject，検索結果が存在しない時はnull
	 */
	public DBObject getTweet(String docno) {
		DBCollection coll = db.getCollection("htmls"); // コレクション"htmls"を取得
		BasicDBObject query = new BasicDBObject();
		query.put("tweetid", docno);                   // "tweetid"フィールドから文字列docnoを検索するクエリ
		DBCursor cursor = coll.find(query);

		try {
			while (cursor.hasNext()) {                   // 検索結果があるとき，それを返す
				// System.out.println(cursor.next());
				return cursor.next();
			}
		} finally {
			cursor.close();
		}
		return null;
	}


	public Double getLang(String docno) {
		DBCollection coll = db.getCollection("language"); // コレクション"language"を取得
		BasicDBObject query = new BasicDBObject();
		query.put("tweetid", docno);                      // "tweetid"フィールドから文字列docnoを検索するクエリ
		DBCursor cursor = coll.find(query);
		try {
			while (cursor.hasNext()) {
				DBObject obj = cursor.next();
				return (Double) obj.get("en");
			}
		} finally {
			cursor.close();
		}
		return null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TweetsDB db = new TweetsDB();
		String docno = "34488644131762176";
		DBObject tweet = db.getTweet(docno);
		System.out.println(tweet);
		System.out.println(tweet.get("date"));
		System.out.println(db.getLang(docno));
	}
}
