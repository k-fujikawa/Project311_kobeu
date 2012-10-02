package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


/**
 * Microblog検索の時間に関する処理を行うクラス．<br>
 * 　
 * @author kitaguchisayaka
 *
 */
public class TimeParser {

  private SimpleDateFormat sdf;

  public TimeParser() {
    setSdf(new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.UK));
  }

  /**
   * TwitterAPIが返す"created_at"フィールドの文字列(String)を受け取り，
   * 日本時間に変換した日付/時刻をCalendarに格納して返す．
   * @param createdAt
   * @return
   */
  public Calendar parseCreatedAt(String createdAt) {
    Calendar cal = null;
    try {
      Date date = sdf.parse(createdAt);
      cal = Calendar.getInstance();
      cal.setTime(date);
    } catch (ParseException e) {
      System.err.println("error: TimeParserクラスのcreated_atをDateにする箇所で例外発生");
      e.printStackTrace();
    }
    return cal;
  }

  /**
   * Twitterの日付情報のString(time)を受け取り，
   * 年・月・日・時刻を抜き出して，Calender(calender)にセットする．
   * @param time tweetデータの日付情報
   * @param calender 日付情報を格納するカレンダー
   * @return うまくいったらtrue，失敗したらfalse
   */
  public static boolean parseTime(String time, Calendar calendar) {
    int year, month, date, hour, second, minute;
    String strMonth;
    // 年の解析
    year  = Integer.parseInt(time.substring(26, 30));
    // 月の解析
    strMonth = time.substring(4, 7);
    if(strMonth.equals("Jan"))  month = Calendar.JANUARY;
    else if(strMonth.equals("Feb"))  month = Calendar.FEBRUARY;
    else if(strMonth.equals("Mar"))  month = Calendar.MARCH;
    else if(strMonth.equals("Apr"))  month = Calendar.APRIL;
    else if(strMonth.equals("May"))  month = Calendar.MAY;
    else if(strMonth.equals("Jun"))  month = Calendar.JUNE;
    else if(strMonth.equals("Jul"))  month = Calendar.JULY;
    else if(strMonth.equals("Aug"))  month = Calendar.AUGUST;
    else if(strMonth.equals("Sep"))  month = Calendar.SEPTEMBER;
    else if(strMonth.equals("Oct"))  month = Calendar.OCTOBER;
    else if(strMonth.equals("Nov"))  month = Calendar.NOVEMBER;
    else if(strMonth.equals("Dec"))  month = Calendar.DECEMBER;
    else return false;
    // 日の解析
    date = Integer.parseInt(time.substring(8, 10));
    // 時刻の解析
    hour = Integer.parseInt(time.substring(11, 13));
    minute = Integer.parseInt(time.substring(14, 16));
    second = Integer.parseInt(time.substring(17, 19));
    calendar.set(year, month, date, hour, minute, second);
    return true;
  }

  /**
   * 年/月/日の形の日付をCalendarクラスにする
   * @param time
   * @param calendar
   * @return
   */
  public static boolean parseTimeSelect(String time, Calendar calendar) {
    int year, month, date;
    String[] timeStrArray = time.split("/"); // フィールド毎に分割
    int numTimeField = timeStrArray.length;
    if (numTimeField != 3) {
      System.out.println("範囲指定された値が異常です．");
      return false;
    }
    year = Integer.parseInt(timeStrArray[0]);
    month = Integer.parseInt(timeStrArray[1]);
    month = convertMonthIntToConst(month);
    date = Integer.parseInt(timeStrArray[2]);
    calendar.set(year, month, date, 0, 0, 0);
    return true;
  }

  public static int convertMonthIntToConst(int Intmonth) {
    if (Intmonth == 1) return Calendar.JANUARY;
    else if (Intmonth == 2) return Calendar.FEBRUARY;
    else if (Intmonth == 3) return Calendar.MARCH;
    else if (Intmonth == 4) return Calendar.APRIL;
    else if (Intmonth == 5) return Calendar.MAY;
    else if (Intmonth == 6) return Calendar.JUNE;
    else if (Intmonth == 7) return Calendar.JULY;
    else if (Intmonth == 8) return Calendar.AUGUST;
    else if (Intmonth == 9) return Calendar.SEPTEMBER;
    else if (Intmonth == 10) return Calendar.OCTOBER;
    else if (Intmonth == 11) return Calendar.NOVEMBER;
    else if (Intmonth == 12) return Calendar.DECEMBER;
    else return -1;
  }

  public static boolean inputTimeRange(Calendar calendar) {
    int year, month, date;
    try {
      BufferedReader br =
          new BufferedReader(new InputStreamReader(System.in));
      String timeStr;
      timeStr = br.readLine();  //時間情報文字列
      String[] timeStrArray = timeStr.split("/"); //フィールド毎に分割
      int numTimeField = timeStrArray.length;
      if (numTimeField < 3 || 6 < numTimeField) {
        System.out.println("時間のフィールドに過不足があります．");
        return false;
      }
      year = Integer.parseInt(timeStrArray[0]);
      month = Integer.parseInt(timeStrArray[1]);
      month = convertMonthIntToConst(month);
      date = Integer.parseInt(timeStrArray[2]);
      if (numTimeField == 4) {
        calendar.set(year, month, date, Integer.parseInt(timeStrArray[3]), 0, 0);
        return true;
      }
      else if (numTimeField == 5) {
        calendar.set(year, month, date, Integer.parseInt(timeStrArray[3]), Integer.parseInt(timeStrArray[4]), 0);
        return true;
      }
      else if (numTimeField == 6) {
        calendar.set(year, month, date, Integer.parseInt(timeStrArray[3]), Integer.parseInt(timeStrArray[4]), Integer.parseInt(timeStrArray[5]));
        return true;
      }
      else {
        calendar.set(year, month, date, 0, 0, 0);
        return true;
      }
    } catch (IOException e) {
      System.err.println("error:parseTime");
      e.printStackTrace();
    }
    return false;
  }

  public static void show(Calendar calendar) {
    System.out.print(calendar.get(Calendar.YEAR) + "/" + (calendar.get(Calendar.MONTH)+1)
        + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "  "
        + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE)
        + ":" + calendar.get(Calendar.SECOND));
  }

  /**
   * Calendarクラスで表現された2つの時刻の差を配列として返す．<br>
   * c1 < c2なら正，c1 > c2なら負がかえります．
   * long[0]=日, [1]=時間, [2]=分, [3]＝秒
   * @param c1
   * @param c2
   * @return 時刻の差 long[0]=日, [1]=時間, [2]=分, [3]＝秒
   */
  public static long[] diffTime(Calendar c1, Calendar c2) {
    long[] diffAry = new long[4];
    if (c1.compareTo(c2) == 0) {
      for(int i=0; i<4; i++) {
        diffAry[i] = 0;
      }
      return diffAry;
    }
    long m1 = c1.getTimeInMillis();
    long m2 = c2.getTimeInMillis();
    long diff = m2 - m1;
    diff = diff/1000;
    diffAry[3] = diff % 60;
    diff = diff / 60;
    diffAry[2] = diff % 60;
    diff = diff / 60;
    diffAry[1] = diff % 24;
    diffAry[0] = diff / 24;
    return diffAry;
  }

  /**
   * Calendarクラスで表現された2つの時刻の差の半分を配列として返す．<br>
   * c1 < c2なら正，c1 > c2なら負がかえります．<br>
   * 時刻の平均値を求めるのに使えます．
   * long[0]=日, [1]=時間, [2]=分, [3]＝秒
   * @param c1
   * @param c2
   * @return 時刻の差 long[0]=日, [1]=時間, [2]=分, [3]＝秒
   */
  public static long[] diffTimeAve(Calendar c1, Calendar c2) {
    long[] diffAry = new long[4];
    if (c1.compareTo(c2) == 0) {
      for(int i=0; i<4; i++) {
        diffAry[i] = 0;
      }
      return diffAry;
    }
    long m1 = c1.getTimeInMillis();
    long m2 = c2.getTimeInMillis();
    long diff = m2 - m1;
    diff = diff/2;
    diff = diff/1000;
    diffAry[3] = diff % 60;
    diff = diff / 60;
    diffAry[2] = diff % 60;
    diff = diff / 60;
    diffAry[1] = diff % 24;
    diffAry[0] = diff / 24;
    return diffAry;
  }

  public SimpleDateFormat getSdf() {
    return sdf;
  }

  public void setSdf(SimpleDateFormat sdf) {
    this.sdf = sdf;
  }


}
