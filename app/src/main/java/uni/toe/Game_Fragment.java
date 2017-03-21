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

   public static String turn = "X"; //if turn.equals(mark) then go

    BluetoothService mConnectedThread = null;
    //TODO: how to figure who's turn is it?
    public String mark;
    public static String MARK_CHOSEN = "MARK_CHOSEN";
    public static int[][] matrix = new int[3][3]; //matrix to know who won

    public static TextView arrayOfButtons[][] = new TextView[3][3];
    //buttons
    //TODO: is it make sence to make them static here?
    static TextView c00 = null;
    static TextView c01 = null;

    public static String IS_SERVER;
    private boolean isServer;


    public static final Game_Fragment newInstance(String mark, boolean server) {
        //instead of pulling the connectedThread it's better to put it in constructor
        //TODO: put BluetoothService mConnectedThread in constructor
        //depends if it's client or server
        //does it matter?
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
                    Toast.makeText(activity, readMessage,
                            Toast.LENGTH_SHORT).show();

                    //in message we getting coordinates
                    //putInMatrix(0, 2);
                    //updateUI();


                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //depends on who created the class: server or client

        mark = getArguments().getString(MARK_CHOSEN);
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
        status.setText("playing for: " + mark);

        //TODO? all 9 cells clicks implemented here
        //TODO: I could just iterate through all the buttons
        c00 = (TextView) myView.findViewById(R.id.cell11);
        c01 = (TextView) myView.findViewById(R.id.cell12);

        buttonsToArray();


        /**
        //put listeners for all the buttons
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                arrayOfButtons[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //can't make i or j final
                    }
                });
            }
        }
        **/

        c00.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                mConnectedThread.write("00".getBytes()); //need to it it in handler
                arrayOfButtons[0][0].setText("what else");
                //putInMatrix(0, 0);
                //updateUI();

                //checkIfEmpty() method here - if not not put text
                //c11.setEnabled(false);
                //also need to block all the ohter cells when it's another player's turn
                //but how to implement another player's turn?
            }
        });

        return myView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mConnectedThread != null) {
            mConnectedThread.stop();
        }
    }

    private void buttonsToArray() {
        //to iterate trough buttons later
        arrayOfButtons[0][0] = c00;
        arrayOfButtons[0][1] = c01;
    }

    public void blockButtons() {
        //iterate through buttons
        //and setEnable(false) when it's not our turn
        //later
    }

    public void unblockFreeButtons() {
        //invoke when our turn
    }

    public void updateUI() {
        /** invoke after every event
            merge matrix with UI
         **/
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

    public void putInMatrix(int i, int j) { //sending
        if(matrix[i][j] != Constants.X && matrix[i][j] != Constants.O) { //only if not occupied before
            switch (mark) {
                case "X": matrix[i][j] = Constants.X;
                   break;
                case "O": matrix[i][j] = Constants.O;
            }
        }
        //TODO: handle occupied case? or they al'll be blocked anyway?
    }

    public void readClickedButton(String msg) {
        //if(msg.equals(""))

    }

    public void handleCellClick(String msg) {

    }

    public boolean checkIfWin() {
        //check matrix array
        return false;
    }
}
