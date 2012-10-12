package kita.search;

import org.apache.lucene.search.DefaultSimilarity;

/**
 * tf(float freq)をオーバーライドして，tfの値として常に1を返すようにした．<br>
 * {@link DefaultSimilarity}
 * @author KitaguchiSayaka
 */
public class NonTfSimilarity extends DefaultSimilarity {

 private static final long serialVersionUID = -8563042586739289034L;

 /**
  * Tweetの検索なのでtfは常に1を返すようにしてみよう．
  */
 @Override
 public float tf(float freq) {
   return 1;
 }
}
