import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


public class ReversiBoard {
    private final int OPEN = 0;
    private final int INVALIDMOVE = -1;
    private final int PLAYER1 = 1;
    private final int PLAYER2 = 2;

    private int newPlayerCounter = 0;
    private boolean gameOn = true;
    private boolean exitStatus = false;
    private int currentPlayer = 1;
    private int[] boardArray = null;

    public ReversiBoard(){
        boardArray = new int[64];
        this.initializeBoard();
        this.setInitialPieces();
    }

    private void initializeBoard(){
        for(int i = 0; i < boardArray.length; i++) boardArray[i] = OPEN;
    }

    private void setInitialPieces(){
      boardArray[27] = PLAYER1;
      boardArray[36] = PLAYER1;
      boardArray[28] = PLAYER2;
      boardArray[35] = PLAYER2;
    }

    public void resetGame(){
        this.resetBoard();
        this.setInitialPieces();
        currentPlayer = 1;
        this.gameOn = true;
    }

    public void resetBoard(){
        this.initializeBoard();
        this.setInitialPieces();
    }

    public int[] getBoard(){ return boardArray; }

    public String getCurrentPlayer() {
        if(currentPlayer == 1) return "Player1";
        else {
            return "Player2";
        }
    }

    public int getNewPlayer(){
        return ++newPlayerCounter;
    }

    public String getScoreString(){
        return "Score: Player1: "+getScore(1)+"  Player2: "+getScore(2);
    }

    public boolean myTurn(int player){
        if(player == currentPlayer) return true;
        else return false;
    }

    public void passTurn(){
        currentPlayer = opponent(currentPlayer);
    }

    public String getBoardString(){
        String boardString = "[";
        for(int i = 0; i < boardArray.length; i++){
            boardString += boardArray[i] + ",";
        }
        boardString += "]";
        return boardString;
    }

    public boolean makeMove(int move, int player){
        if(validateChosenMove(move, player) && player == currentPlayer){
            boardArray[move] = player;
            updateBoard(player, move);
            currentPlayer = opponent(player);
            checkGameState();
            return true;
        } else return false;
    }

    private boolean validateChosenMove(int index, int player){
        if(indexIsOutOfBounds(index)) return false;
        ArrayList<Integer> validMoves = this.getValidMoves(player);
        return validMoves.contains(index);
    }

    public boolean indexIsOutOfBounds(int index){
        return index < 0 || index >= boardArray.length;
    }

    public ArrayList<Integer> getValidMoves(int player){
        ArrayList<Integer> playerPieces = findPlayerPieces(player);
        ArrayList<Integer> legalMoves = new ArrayList<Integer>();
        for (Integer playerPiece : playerPieces)
            legalMoves = findLegalMoves(playerPiece, player, legalMoves);
        Collections.sort(legalMoves);
        return legalMoves;
    }

    public ArrayList<Integer> findLegalMoves(int startIndex, int player, ArrayList<Integer> moves){
        for (int offset : getOffsets()) {
            int move = findMove(startIndex, offset, player);
            if (move != INVALIDMOVE && !adjacentMove(startIndex,offset,move) && !moves.contains(move))
                moves.add(move);
        }
        return moves;
    }

    public int findMove(int index, int offset, int player){
        int nextIndex = index+offset;
        if(indexIsOutOfBounds(nextIndex)) return INVALIDMOVE;
        else if(boardArray[nextIndex] == OPEN) return nextIndex;
        else if(boardArray[nextIndex] == opponent(player))
                return findMove(nextIndex, offset, player);
        else return INVALIDMOVE;
    }

    public boolean searchForUpdate(int startIndex, int currentIndex, int offset, int player){
        int nextIndex = currentIndex + offset;
        if(indexIsOutOfBounds(nextIndex) || boardArray[nextIndex] == OPEN) return false;
        else if(boardArray[nextIndex] == opponent(player)){
            boolean update = searchForUpdate(startIndex, nextIndex, offset, player);
            if(update){
                boardArray[nextIndex] = player;
                return true;
            }
        }
        else if(boardArray[nextIndex] == player && !adjacentMove(startIndex,offset,nextIndex))
             return true;
        return false;
    }

    public int[] getOffsets(){
        return new int[]{1, -1, -8, 8, 9, -9, 7, -7};
    }

    public boolean adjacentMove(int origIndex, int offset, int testIndex){
        return testIndex == origIndex+offset;
    }

    public void updateBoard(int player, int move){
        for(int offset : getOffsets()) searchForUpdate(move, move, offset, player);
    }

    public ArrayList<Integer> findPlayerPieces(int player){
        ArrayList<Integer> playerPieces = new ArrayList<Integer>();
        for(int i = 0; i < boardArray.length; i++)
            if(boardArray[i] == player) playerPieces.add(i);
        return playerPieces;
    }

    public int checkGameState(){
        ArrayList<Integer> player1Moves = getValidMoves(PLAYER1);
        ArrayList<Integer> player2Moves = getValidMoves(PLAYER2);
        if(player1Moves.isEmpty() && player2Moves.isEmpty()){
            this.gameOn = false;
            return 0;
        }
        else return 1;
    }

    public int getWinner(){
        int player1Score = getScore(1);
        int player2Score = getScore(2);
        if(player1Score > player2Score) return 1;
        if(player2Score > player1Score) return 2;
        else return 3;
    }

    public String getWinnerString(){
        int winner = getWinner();
        if(winner == 1)
            return "Player 1 Wins!";
        else if(winner == 2)
            return "Player 2 Wins!";
        else return "Tie";
    }

    public String getGameStateString(){
        if(checkGameState() == 1){
            return "gameOn";
        }
        else return "gameOver";
    }

    public int getScore(int player){
        int score = 0;
        for(int piece : boardArray) if(piece == player) score++;
        return score;
    }

    public int opponent(int player){
        return player == PLAYER1 ? PLAYER2 : PLAYER1;
    }

    public String getPlayerString(int count){
        if(count == 1) return "Player1 (@)";
        else if(count == 2) return "Player2 (_)";
        else return "Observer";
    }

}
