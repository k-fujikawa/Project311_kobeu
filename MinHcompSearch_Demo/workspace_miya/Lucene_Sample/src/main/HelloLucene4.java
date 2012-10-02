package main;

import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

/**
 * ツイートデータhtmlファイルのインデックス付け，クエリ作成，検索を一気に行う． 引数 args[0]=インデックスを作るディレクトリ 　　
 * args[1]=インデックスするファイル 　　 args[2]=検索文字列
 */
public class HelloLucene4 {
	public static void main(String[] args) {
		Query query;
		if (args.length != 3) {
			System.err.println("引数が足りません．");
			System.exit(1);
		}
		// 共通で使うアナライザ
		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
		// MakeIndexMicroblogクラスに，インデックスを作るディレクトリのパスargs[0]と，
		// インデックス付けするファイルagrs[1]を渡して，インデックスを作成してもらう．
		try {
			MakeIndexMicroblog.makeIndexSimpleFile(args[0], args[1], analyzer);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// MakeQueryMicroblogクラスに，検索文字列を渡して，クエリを作ってもらう．
		// SearcherMicroBlogクラスに，クエリを渡して検索．結果表示．
		try {
			query = MakeQueryMicroblog.makeQuery(args[2], analyzer);
			SearcherMicroblog.search(args[0], query);
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
