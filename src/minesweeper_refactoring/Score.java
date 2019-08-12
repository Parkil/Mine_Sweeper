package minesweeper_refactoring;

import static java.lang.Math.ceil;
import java.util.ArrayList;
import java.sql.Date;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author alkain77
 * score related variables
 */
public class Score {
	ArrayList<Time> bestTimes;

	private int gamesPlayed;
	private int gamesWon;

	private int longestWinningStreak;
	private int longestLosingStreak;

	private int currentStreak;

	private int currentWinningStreak;
	private int currentLosingStreak;

	public Score() {
		gamesPlayed = gamesWon = currentStreak = longestLosingStreak = longestWinningStreak = currentWinningStreak = currentLosingStreak = 0;
		bestTimes = new ArrayList<Time>();
	}

	public int getGamesPlayed() {
		return gamesPlayed;
	}

	public int getGamesWon() {
		return gamesWon;
	}

	public int getWinPercentage() {
		double gP = gamesPlayed;
		double gW = gamesWon;

		double percentage = ceil((gW / gP) * 100);

		return (int) percentage;
	}

	public int getLongestWinningStreak() {
		return longestWinningStreak;
	}

	public int getLongestLosingStreak() {
		return longestLosingStreak;
	}

	public int getCurrentStreak() {
		return currentStreak;
	}

	public int getCurrentLosingStreak() {
		return currentLosingStreak;
	}

	public int getCurrentWinningStreak() {
		return currentWinningStreak;
	}

	public void incGamesWon() {
		gamesWon++;
	}

	public void incGamesPlayed() {
		gamesPlayed++;
	}

	public void incCurrentStreak() {
		currentStreak++;
	}

	public void incCurrentLosingStreak() {
		currentLosingStreak++;

		if (longestLosingStreak < currentLosingStreak) {
			longestLosingStreak = currentLosingStreak;
		}
	}

	public void incCurrentWinningStreak() {
		currentWinningStreak++;

		if (longestWinningStreak < currentWinningStreak) {
			longestWinningStreak = currentWinningStreak;
		}
	}

	public void decCurrentStreak() {
		currentStreak--;
	}

	public void resetScore() {
		gamesPlayed = gamesWon = currentStreak = longestLosingStreak = longestWinningStreak = currentWinningStreak = currentLosingStreak = 0;
	}

	public ArrayList<Time> getBestTimes() {
		return bestTimes;
	}

	public void addTime(int time, Date date) {
		bestTimes.add(new Time(time, date));
		Collections.sort(bestTimes, new TimeComparator());

		if (bestTimes.size() > 5)
			bestTimes.remove(bestTimes.size() - 1);
	}
	
	public void setGamesPlayed(int gamesPlayed) {
		this.gamesPlayed = gamesPlayed;
	}

	public void setGamesWon(int gamesWon) {
		this.gamesWon = gamesWon;
	}

	public void setLongestWinningStreak(int longestWinningStreak) {
		this.longestWinningStreak = longestWinningStreak;
	}

	public void setLongestLosingStreak(int longestLosingStreak) {
		this.longestLosingStreak = longestLosingStreak;
	}

	public void setCurrentStreak(int currentStreak) {
		this.currentStreak = currentStreak;
	}

	public void setCurrentWinningStreak(int currentWinningStreak) {
		this.currentWinningStreak = currentWinningStreak;
	}

	public void setCurrentLosingStreak(int currentLosingStreak) {
		this.currentLosingStreak = currentLosingStreak;
	}

	public class TimeComparator implements Comparator<Time> {
		@Override
		public int compare(Time a, Time b) {
			if (a.getTimeValue() > b.getTimeValue())
				return 1;
			else if (a.getTimeValue() < b.getTimeValue())
				return -1;
			else
				return 0;
		}
	}
	
	public class Time {
		Date date;
		int time;

		public Time(int t, Date d) {
			time = t;
			date = d;
		}

		public Date getDateValue() {
			return date;
		}

		public int getTimeValue() {
			return time;
		}
	}
}
