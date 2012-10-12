package kita.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

public class TwitterQueryMaker {

  private Analyzer analyzer;

  public TwitterQueryMaker(Analyzer analyzer) {
    setAnalyzer(analyzer);
  }

  /**
   * 検索したい文字列と，フィールド名を受け取って，クエリを作成して返す．
   * @param fieldname 検索したいフィールドの名前
   * @param querySry 検索したい文字列
   * @return 作成したクエリ
   * @throws ParseException
   */
  public Query makeQuery(String fieldname, String querySry) throws ParseException {
    return new QueryParser(Version.LUCENE_36, fieldname, analyzer).parse(querySry);
  }

  /**
   * 検索したい文字列を受け取って，textフィールドを検索するクエリを作成して返す．
   * @param queryStr 検索したい文字列
   * @return 作成したクエリ
   * @throws ParseException
   */
  public Query makeQuery(String queryStr) throws ParseException {
    return makeQuery("text", queryStr);
  }

  public Analyzer getAnalyzer() {
    return analyzer;
  }

  public void setAnalyzer(Analyzer analyzer) {
    this.analyzer = analyzer;
  }

}
