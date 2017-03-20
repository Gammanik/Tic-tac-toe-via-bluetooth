package uni.toe;

import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 */
public class Server_Fragment extends Fragment {

    FragmentManager fragmentManager;
    private static String TAG = "serverFragment";
    private BluetoothService mChatService = null;

    BluetoothAdapter mBluetoothAdapter = null;

    TextView output;
    Button btn_start;

    public Server_Fragment() {
        // Required empty public constructor
    }

    //TODO: put it in the separate class to avoid repetition?
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    mkmsg(writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);

                    output.append("msg from client: " + readMessage + "\n");
                    //in the beginning of game query sent
                    if(readMessage.equals("choosingDialogQuery"))  {
                        AlertDialog dialog = createDialog();
                        dialog.show();
                        output.append("dialog box created - starting the game");
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    //TODO: save the connected device's name
                    //mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to ", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };


    @Override
    public void onStart() {
        super.onStart();
        //creating a BluetoothService here
        if(mChatService == null) {
            mChatService = new BluetoothService(getActivity(), handler);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_server, container, false);
        //text field for output info.
        output = (TextView) myView.findViewById(R.id.sv_output);
        btn_start = (Button) myView.findViewById(R.id.start_server);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                output.append("sending msg\n");
                sendMessage("wassup");
            }
        });

        //setup the bluetooth adapter.
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            output.append("No bluetooth device.\n");
            btn_start.setEnabled(false);
        }

        return myView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop(); //not close it
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth service
                mChatService.start();
            }
        }

    }

    public AlertDialog createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Choose your symbol: ").setTitle("The player 2 is ready");
        builder.setPositiveButton("X", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User choose to play X
                //switch to a new fragment here??
                mkmsg("server decided to be X");

                fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.activity_main, new Game_Fragment()); //??? R.id.fragment_client?
                transaction.addToBackStack(null);
                transaction.commit();
                Toast.makeText(getActivity(), "X symbol is shosen", Toast.LENGTH_SHORT).show();

            }
        });
        builder.setNegativeButton("O", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User choose to play O
                mkmsg("server decided to be O");
                Toast.makeText(getActivity(), "O symbol is shosen", Toast.LENGTH_SHORT).show();
            }
        });
        return builder.create();
    }

    public void mkmsg(String str) {
        //handler junk, because thread can't update screen!
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putString("msg", str);
        msg.setData(b);
        handler.sendMessage(msg);
    }


    public void sendMessage(String msg) {
        if (mChatService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(getActivity(), "not connected", Toast.LENGTH_SHORT).show();
            return;
        }
       mChatService.write(msg.getBytes());
    }

    public BluetoothService getBluetoothService() {
        //invoke it in Game_Fragment to get the connectedThread??
        return mChatService;
    }

}

