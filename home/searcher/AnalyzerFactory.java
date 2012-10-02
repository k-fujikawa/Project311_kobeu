package cs24.kitaguchi.Microblog.twitter.searcher;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.ja.JapaneseAnalyzer;
import org.apache.lucene.analysis.ja.JapaneseTokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;

import cs24.kitaguchi.Microblog.searcher2.JapaneseEnglishAnalyzer;

public class AnalyzerFactory {

  private Version version; /** Luceneのバージョン */

  /**
   * コンストラクタ
   * @param version Luceneのバージョン
   */
  public AnalyzerFactory(Version version) {
    setVersion(version);
  }

  /**
   * デフォルト設定のJapaneseEnglishAnalyzerを返す
   * @return デフォルト設定のJapaneseEnglishAnalyzer
   */
  public Analyzer getJapaneseEnglishAnalyzer() {
    CharArraySet stopwords = getStopWordsCharArraySet();
    Analyzer analyzer = new JapaneseEnglishAnalyzer(version, null, JapaneseTokenizer.Mode.SEARCH, stopwords, JapaneseAnalyzer.getDefaultStopTags());
    return analyzer;
  }

  /**
   * StandardAnalyzerを返す
   * @return でフォルト設定のStandardAnalyzer
   */
  public Analyzer getStaAnalyzer() {
    Analyzer analyzer = new StandardAnalyzer(version);
    return analyzer;
  }

  /**
   * 日本語と英語のストップワードのCharArraySetを返す
   * @return 日本語と英語のストップワードのCharArraySet
   */
  public CharArraySet getStopWordsCharArraySet() {
    CharArraySet stopwords = new CharArraySet(version, StandardAnalyzer.STOP_WORDS_SET, true);
    stopwords.addAll(JapaneseAnalyzer.getDefaultStopSet());
    return stopwords;
  }

  public Version getVersion() {
    return version;
  }

  public void setVersion(Version version) {
    this.version = version;
  }

}
