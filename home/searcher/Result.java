package cs24.kitaguchi.Microblog.twitter.searcher;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;

public class Result {
  IndexSearcher searcher;

  private ScoreDoc[] hits; // 検索結果

  /**
   * コンストラクタ
   * @param hits
   */
  public Result(ScoreDoc[] hits, IndexSearcher searcher) {
    this.searcher = searcher;
    setHits(hits);
  }

  /**
   * index番目のドキュメントを外部に取得させる．
   * @param index
   * @return index番目のドキュメント
   * @throws IOException
   * @throws CorruptIndexException
   *//*
  public Document getHitDocumentAt(int index) {
    if (index >= hitDocuments.size()) {
      System.out.println("そのindexの要素は存在しません．");
      return null;
    }
    return this.hitDocuments.get(index);
  }*/

  /**
   * docIndex番目のドキュメントを外部に取得させる．
   * @param docIndex 取り出すドキュメントのindex
   * @return docIndex番目のドキュメント
   * @throws CorruptIndexException
   * @throws IOException
   */
  public Document getHitDocumentAt(int docIndex) throws CorruptIndexException, IOException {
    if (docIndex >= hits.length) {
      System.out.println("そのdocIndexの要素は存在しません．");
      return null;
    }
    int docId = this.hits[docIndex].doc;
    return searcher.doc(docId);
  }

  /**
   * 検索結果のサイズを返す．
   * @return
   */
  public int size() {
    return this.hits.length;
  }

  /**
   * hitsを設定
   * @param hits 検索結果
   */
  public void setHits(ScoreDoc[] hits) {
    this.hits = hits;
  }

  /**
   * hitsを外部に取得させる．
   * @return 検索結果の配列
   */
  public ScoreDoc[] getHits() {
    return this.hits;
  }

}
