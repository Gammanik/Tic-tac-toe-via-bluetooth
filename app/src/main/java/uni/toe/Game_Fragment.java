package uni.toe;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class Game_Fragment extends Fragment {

    TextView status;

   public String turn = "X"; //X goes first
   private String myMark;

    BluetoothService mConnectedThread = null;

    public static String MARK_CHOSEN = "MARK_CHOSEN";
    public int[][] matrix = new int[3][3]; //matrix to know who won

    public static TextView arrayOfButtons[][] = new TextView[3][3];
    //buttons
    TextView c00 = null;
    TextView c01 = null;
    TextView c02 = null;
    TextView c10 = null;
    TextView c11 = null;
    TextView c12 = null;
    TextView c20 = null;
    TextView c21 = null;
    TextView c22 = null;

    public static String IS_SERVER;


    public static final Game_Fragment newInstance(String mark, boolean server) {
        Game_Fragment game = new Game_Fragment();
        Bundle bdl = new Bundle(2);
        bdl.putString(MARK_CHOSEN, mark);
        bdl.putBoolean(IS_SERVER, server);

        game.setArguments(bdl);
        return game;
    }

    public Game_Fragment() {
        // Required empty public constructor
    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                //assume already connected
                case Constants.MESSAGE_WRITE:
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);

                    if(!isMatrixFull() && !isWinnerFounded()) {
                        //in message we getting coordinates
                        int i = readMessage.codePointAt(0) - 48;
                        int j = readMessage.codePointAt(1) - 48;
                        //only for messages with coordinates info
                        if (i < 3 && j < 3 && readMessage.length() == 2) {
                            putInMatrix(i, j, turn);
                            updateUI();
                            switchTurn(turn);
                        }
                        Toast.makeText(activity, "i= " + i + "  j= " + j,
                                Toast.LENGTH_SHORT).show();

                    } else {
                        //TODO: dialog for a new game here
                    }


                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isServer;

        myMark = getArguments().getString(MARK_CHOSEN);
        isServer = getArguments().getBoolean(IS_SERVER);
        if(isServer) {
            mConnectedThread = Server_Fragment.getBluetoothService(); }
        else {
            mConnectedThread = Client_Fragment.getBluetoothService(); }

        mConnectedThread.putNewHandler(handler); //really bad
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.fragment_game, container, false);
        status = (TextView) myView.findViewById(R.id.Status);
        status.setText("playing for: " + myMark);

        initButtons(myView);
        buttonsToArray();

        //put listeners for all the buttons
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                final String col = String.valueOf(i);
                final String row = String.valueOf(j);
                final String colRow = col + row;
                arrayOfButtons[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO: check if win or matrix is full
                        if(!isMatrixFull() && !isWinnerFounded()) {
                            handleCellClick(colRow);
                        } else {
                            //create dialog proposing a new game
                            //clearMatrix(); if agreed
                            Toast.makeText(getActivity(), "game is done",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }

        return myView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mConnectedThread != null) {
            mConnectedThread.stop();
        }
    }

    private void initButtons(View myView) {
        c00 = (TextView) myView.findViewById(R.id.cell11);
        c01 = (TextView) myView.findViewById(R.id.cell12);
        c02 = (TextView) myView.findViewById(R.id.cell13);
        c10 = (TextView) myView.findViewById(R.id.cell21);
        c11 = (TextView) myView.findViewById(R.id.cell22);
        c12 = (TextView) myView.findViewById(R.id.cell23);
        c20 = (TextView) myView.findViewById(R.id.cell31);
        c21 = (TextView) myView.findViewById(R.id.cell32);
        c22 = (TextView) myView.findViewById(R.id.cell33);
        //TODO: then I need to put buttonsToArray() here
    }

    //TODO?: if I'll return array[][] then might not need to have static var buttons
    private void buttonsToArray() {
        //to iterate trough buttons later
        arrayOfButtons[0][0] = c00;
        arrayOfButtons[0][1] = c01;
        arrayOfButtons[0][2] = c02;
        arrayOfButtons[1][0] = c10;
        arrayOfButtons[1][1] = c11;
        arrayOfButtons[1][2] = c12;
        arrayOfButtons[2][0] = c20;
        arrayOfButtons[2][1] = c21;
        arrayOfButtons[2][2] = c22;
    }

    private void handleCellClick(String colRow) {
        int col = colRow.codePointAt(0)-48;
        int row = colRow.codePointAt(1)-48;

        if (turn.equals(myMark)) { //if our turn
            mConnectedThread.write(colRow.getBytes());
            putInMatrix(col, row, myMark);
            updateUI();
            switchTurn(myMark);
        } else {
            Toast.makeText(getActivity(), "not your turn",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void switchTurn(String currentTurn) { //change var turn
        if(currentTurn.equals("X"))
            turn = "O";
        if(currentTurn.equals("O"))
            turn = "X";
    }

    public void updateUI() {
        //invoke after every event; merge matrix with UI
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                switch (matrix[i][j]) {
                    case Constants.X: arrayOfButtons[i][j].setText("X");
                        break;
                    case Constants.O: arrayOfButtons[i][j].setText("O");
                }
            }
        }
    }

    public void putInMatrix(int i, int j, String currentMark) { //sending
        //only if not occupied before
        if(matrix[i][j] != Constants.X && matrix[i][j] != Constants.O) {
            switch (currentMark) {
                case "X": matrix[i][j] = Constants.X;
                   break;
                case "O": matrix[i][j] = Constants.O;
            }
        }
    }

    private boolean isWinnerFounded() {
        //check matrix array for winning combination
        //check diagonals
        if(isWinCombination(matrix[0][0], matrix[1][1], matrix[2][2]))
            return true;
        if(isWinCombination(matrix[0][2], matrix[1][1], matrix[2][0]))
            return true;

        for(int i = 0; i < 3; i++) {
            //check rows
            if(isWinCombination(matrix[i][0], matrix[i][1], matrix[i][2]))
                return true;
            //check columns
            if(isWinCombination(matrix[0][i], matrix[1][i], matrix[2][i]))
                return true;
        }
        return false;
    }

    private boolean isWinCombination(int a, int b, int c) {

        if(a == Constants.X && b == Constants.X && c == Constants.X)
            return true;
        if(a == Constants.O && b == Constants.O && c == Constants.O)
            return true;
        //otherwise
        return false;
    }


    private boolean isMatrixFull() {
        //if there is no free cells left
        int filledCellsCounter = 0;
        for(int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if(matrix[i][j] == Constants.O || matrix[i][j] == Constants.X)
                    filledCellsCounter++;
            }
        }

        return filledCellsCounter == 9;
    }

    private void clearMatrix() {

    }

}
