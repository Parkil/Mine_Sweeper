package minesweeper_refactoring;



public class Cell 
{
    private boolean mine;

    /*
     * The content of a field can be a...
     *  "" - indicating an unknown field
     *  "F" - a flagged field
     *  "M" - a mine
     *  a number ranging from 0 to 8 - indicating the number of surrounding mines
     */

    //Only the content of the Cell is visible to the player.
    private String content; //셀의 상태를 저장

    //Number of adjacent surrounding mines
    private int surroundingMines;
    
    /*
     * Cell안에 JButton 및 JButton의 이벤트를 같이 처리하는 로직이 들어가야 할듯
     */

    
    //----------------------------------------------------------//

    public Cell()
    {
        mine = false;
        content = "";
        surroundingMines = 0;
    }


    
    //-------------GETTERS AND SETTERS----------------------------//
    public boolean getMine()
    {
        return mine;
    }

    public void setMine(boolean mine)
    {
        this.mine = mine;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public int getSurroundingMines()
    {
        return surroundingMines;
    }

    public void setSurroundingMines(int surroundingMines)
    {
        this.surroundingMines = surroundingMines;
    }

    //-------------------------------------------------------------//
}
