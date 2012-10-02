package search;

import java.util.Date;

public class DateScore {
	Date date;
	Double score;
	
	public DateScore(Date date_, Double score_){
		date = date_;
		score = score_;
	}
	
	public String toString(){
		return date.toString()+" "+score.toString();
	}
}
