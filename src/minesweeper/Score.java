package minesweeper;

import static java.lang.Math.ceil;
import java.sql.Connection;
import java.util.ArrayList;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Comparator;


public class Score
{
    ArrayList<Time> bestTimes;
    
    int gamesPlayed;
    int gamesWon;
       
    int longestWinningStreak;
    int longestLosingStreak;
    
    int currentStreak;

    int currentWinningStreak;
    int currentLosingStreak;
    
    public Score()
    {
        gamesPlayed = gamesWon = currentStreak = longestLosingStreak = longestWinningStreak = currentWinningStreak = currentLosingStreak = 0;
        bestTimes = new ArrayList();
    }
    
    
    public int getGamesPlayed()
    {
        return gamesPlayed;        
    }
    
    public int getGamesWon()
    {        
        return gamesWon;
    }
    
    public int getWinPercentage()
    {
        double gP = gamesPlayed;
        double gW = gamesWon;
        
        double percentage = ceil((gW/gP) * 100);
        
        return (int)percentage;
    }
    
    public int getLongestWinningStreak()
    {
        return longestWinningStreak;
    }
    
    public int getLongestLosingStreak()
    {
        return longestLosingStreak;
    }
    
    public int getCurrentStreak()
    {
        return currentStreak;
    }
    
    public int getCurrentLosingStreak()
    {
        return currentLosingStreak;
    }

    public int getCurrentWinningStreak(){
        return currentWinningStreak;
    }
    
    public void incGamesWon()
    {
        gamesWon++;
    }
    
    public void incGamesPlayed()
    {
        gamesPlayed++;
    }
    
    public void incCurrentStreak()
    {
        currentStreak++;
    }
    
    public void incCurrentLosingStreak()
    {
        currentLosingStreak++;
        
        if (longestLosingStreak < currentLosingStreak)
        {
            longestLosingStreak = currentLosingStreak;
        }                
    }

    public void incCurrentWinningStreak()
    {
        currentWinningStreak++;
        
        if (longestWinningStreak < currentWinningStreak)
        {
            longestWinningStreak = currentWinningStreak;
        }                
    }
    
    
    public void decCurrentStreak()
    {        
        currentStreak--;
    }    
    
    
    public void resetScore()
    {
        gamesPlayed = gamesWon = currentStreak = longestLosingStreak = longestWinningStreak = currentWinningStreak = currentLosingStreak = 0;
    }
    
    
    
    public ArrayList<Time> getBestTimes()
    {
        return bestTimes;
    }
        
    
    public void addTime(int time, Date date)
    {
        bestTimes.add(new Time(time,date));
        Collections.sort(bestTimes,new TimeComparator()); 
        
        if(bestTimes.size() > 5)
            bestTimes.remove(bestTimes.size()-1);
    }
     
    //--------------------------------------------------------//

    
    //------------DATABASE--------------------------//
    
    //------------POPULATE FROM DATABASE------------//
    /*
     * DB에 접근하여 crud를 수행하는자체를 유틸리티 클래스화 시킬 필요는 있어보임
     */
    //지뢰찾기 진행회수 + 최단 클리어시간을 반환
    public boolean populate()
    {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        
        //지뢰찾기 게임 진행 회수를 access db에서 반환
        try {
            String dbURL = Game.dbPath; 

            connection = DriverManager.getConnection(dbURL); 
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM SCORE");

            while(resultSet.next()) 
            {
                gamesPlayed = resultSet.getInt("GAMES_PLAYED");
                gamesWon = resultSet.getInt("GAMES_WON");

                longestWinningStreak = resultSet.getInt("LWSTREAK");
                longestLosingStreak = resultSet.getInt("LLSTREAK");

                currentStreak = resultSet.getInt("CSTREAK");

                currentWinningStreak = resultSet.getInt("CWSTREAK");
                currentLosingStreak = resultSet.getInt("CLSTREAK");
            }
            
            // cleanup resources, once after processing
            resultSet.close();
            statement.close();

            
            //------------------------LOAD TIMES------------------//
            //지뢰찾기 클리어 시간 반환
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM TIME");
            
            
            while(resultSet.next())
            {
                int time = resultSet.getInt("TIME_VALUE");
                Date date = resultSet.getDate("DATE_VALUE");
                
                bestTimes.add(new Time(time,date));
            }
            
            
            // cleanup resources, once after processing
            resultSet.close();
            statement.close();
            
            
            // and then finally close connection
            connection.close();            
            
            return true;
        }
        catch(SQLException sqlex)
        {
            sqlex.printStackTrace();
            return false;
        }
    }

    
    public void save()
    {
        Connection connection = null;
        PreparedStatement statement = null;
        

        try {
            String dbURL = Game.dbPath; 
            
            connection = DriverManager.getConnection(dbURL); 

            
            //----------EMPTY SCORE TABLE------//
            String template = "DELETE FROM SCORE"; 
            statement = connection.prepareStatement(template);
            statement.executeUpdate();
            
            //----------EMPTY TIME TABLE------//
            template = "DELETE FROM TIME"; 
            statement = connection.prepareStatement(template);
            statement.executeUpdate();
            
            //--------------INSERT DATA INTO SCORE TABLE-----------//            
            template = "INSERT INTO SCORE (GAMES_PLAYED,GAMES_WON, LWSTREAK, LLSTREAK, CSTREAK, CWSTREAK, CLSTREAK) values (?,?,?,?,?,?,?)";
            statement = connection.prepareStatement(template);
            
            statement.setInt(1, gamesPlayed);
            statement.setInt(2, gamesWon);
            statement.setInt(3, longestWinningStreak);
            statement.setInt(4, longestLosingStreak);
            statement.setInt(5, currentStreak);
            statement.setInt(6, currentWinningStreak);
            statement.setInt(7, currentLosingStreak);
            
            statement.executeUpdate();
            
            /*
             * access가 batch나 다중 insert를 지원하지 않나?
             * 확인결과 다중 insert를 sql레벨에서 지원하지는 않음
             */
            //-------------------INSERT DATA INTO TIME TABLE-----------//
            template = "INSERT INTO TIME (TIME_VALUE, DATE_VALUE) values (?,?)";
            statement = connection.prepareStatement(template);
            

            for (int i = 0; i < bestTimes.size(); i++)
            {
                statement.setInt(1, bestTimes.get(i).getTimeValue());
                statement.setDate(2, bestTimes.get(i).getDateValue());
                
                statement.executeUpdate();            
            }

            //---------------------------------------------------------//
            
            statement.close();
            
            // and then finally close connection
            connection.close();            
        }
        catch(SQLException sqlex)
        {
            sqlex.printStackTrace();
        }
        
    }

    //--------------------------------------------------//
    
    
    //---------------------------------------------------//
    public class TimeComparator implements Comparator<Time>
    {
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

    //----------------------------------------------------------//
    public class Time{
        Date date;
        int time;
        
        public Time(int t, Date d)
        {
            time = t;
            date = d;
        }
        
        public Date getDateValue()
        {
            return date;
        }
        
        public int getTimeValue()
        {
            return time;
        }        
    }    
}
