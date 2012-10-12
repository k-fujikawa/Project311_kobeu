package kita.index;

import java.io.IOException;

import kita.search.AnalyzerFactory;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.Version;

public class MakeTweetIndex {
  /**
   * @param args[0] インデックスを作るディレクトリ
   * @param args[1] インデックスするファイル
   * @param args[2] "text"フィールドの平均長を記録するファイルのパス
   */
  public static void main(String[] args) {
    AnalyzerFactory analyzerFactory = new AnalyzerFactory(Version.LUCENE_36);
    Analyzer analyzer = analyzerFactory.getJapaneseEnglishAnalyzer();
    TwitterIndexMaker twitterIndexMaker = new TwitterIndexMaker(args[0], args[2], analyzer);
    try {
      twitterIndexMaker.updateAddIndex(args[1]); // インデックスを更新
    } catch (IOException e) {
      System.err.println("インデックスの作成で例外が発生しました．");
      e.printStackTrace();
    }
  }

}