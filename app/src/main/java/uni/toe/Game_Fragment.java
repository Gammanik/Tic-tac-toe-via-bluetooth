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

    //TextView c11, c12;

    BluetoothService mConnectedThread = null;
    //TODO: how to figure who's turn is it?
    private String mark;
    public static String MARK_CHOSEN;
    public static int[][] matrix = new int[3][3];



    public static final Game_Fragment newInstance(String mark) {
        //instead of pulling the connectedThread it's better to put it in constructor
        //TODO: put BluetoothService mConnectedThread in constructor
        //depends if it's client or server
        //does it matter?
        Game_Fragment game = new Game_Fragment();
        Bundle bdl = new Bundle();
        bdl.putString(MARK_CHOSEN, mark);

        game.setArguments(bdl);
        return game;
    }

    public Game_Fragment() {
        // Required empty public constructor
        /**
         * TODO: pull connectedThread
         *  depends on fragment created the class
         * TODO: put the mark
         *  depends what were chosen by server
         */
    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                //assume already connected
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    //make msg
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Toast.makeText(activity, readMessage,
                            Toast.LENGTH_SHORT).show();

                    //
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConnectedThread = Server_Fragment.getBluetoothService(); //doesn't matter they are same
        mConnectedThread.putNewHandler(handler); //really bad

        mark = getArguments().getString(MARK_CHOSEN);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.fragment_game, container, false);

        //TODO? all 9 cells clicks implemented here
        final TextView c11 = (TextView) myView.findViewById(R.id.cell11);
        c11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mConnectedThread.write("gogogo".getBytes()); //need to it it in handler
                //checkIfEmpty() method here - if not not put text
                c11.setText(mark);
                //c11.setText("you");
                //c11.setEnabled(false);
                //also need to block all the ohter cells when it's another player's turn
                //but how to implement another player's turn?
                //and we will not be able to change them back after blocking as if they are final
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

    public static void cellClick(View v) {
        /**
         * call final TextView c11 = (TextView) myView.findViewById(R.id.cell11);
         * to change them again?
         * block
         */


    }

    public void handleCellClick(TextView cell) {
        //check for winning combination
        //X goes first
        //cell.
    }

    public boolean checkIfWin() {
        //check matrix array
        return false;
    }
}
