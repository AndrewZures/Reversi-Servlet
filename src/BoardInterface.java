import java.util.ArrayList;

public interface BoardInterface {

    public int[] getBoard();
    public String getCurrentPlayer();
    public int getScore(int player);
    public boolean makeMove(int move, int player);
    public ArrayList<Integer> getValidMoves(int player);

}
