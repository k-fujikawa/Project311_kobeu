package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

public class MakeQueryMicroblog {

  /**
   * キーボードからクエリにしたい言葉の入力を受け付ける．
   * @return クエリにしたい言葉
   * @throws IOException キーボード入力でエラー発生
   */
  public String inputQuery() throws IOException {
    System.out.print("Prease Input searchWord:");
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    String searchWord = br.readLine();
    System.out.println("Your input :" + searchWord); //確認用
    return searchWord;
  }

  /**
   * 検索したい文字列を受け取って，クエリを作成して返す．
   * @param searchWord 検索したい文字列
   * @return 作成されたクエリ
   * @throws ParseException
   */
  public static Query makeQuery(String queryStr, Analyzer analyzer) throws ParseException {
    return new QueryParser(Version.LUCENE_36, "TWEET", analyzer).parse(queryStr);
  }

}
