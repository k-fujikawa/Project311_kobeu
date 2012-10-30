package search;

import java.util.Calendar;
import java.util.Locale;

public class TimeModule {
  /**
   * 日本時間の現在時刻を表示する。
   */
  public static void printTime() {
    Calendar runTime = Calendar.getInstance(Locale.JAPAN);
    String yearStr = String.valueOf(runTime.get(Calendar.YEAR));
    String monthStr = String.valueOf(runTime.get(Calendar.MONTH));
    String dayStr = String.valueOf(runTime.get(Calendar.DAY_OF_MONTH));
    String hourStr = String.valueOf(runTime.get(Calendar.HOUR_OF_DAY));
    String minuteStr = String.valueOf(runTime.get(Calendar.MINUTE));
    StringBuffer sb = new StringBuffer();
    sb.append(yearStr);
    sb.append("/");
    sb.append(monthStr);
    sb.append("/");
    sb.append(dayStr);
    sb.append("\t");
    sb.append(hourStr);
    sb.append(":");
    if (minuteStr.length() < 2) {
      sb.append("0");
    }
    sb.append(minuteStr);
    System.out.println(sb.toString());
  }
}
