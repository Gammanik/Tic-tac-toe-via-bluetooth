package uni.toe;

import java.util.Set;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class Client_Fragment extends Fragment {
    String TAG = "client";
    TextView output;
    Button btn_start, btn_device, btn_send, btn_ready;
    BluetoothAdapter mBluetoothAdapter =null;
    BluetoothDevice device;

    private BluetoothService mChatService = null;

    public Client_Fragment() {
        // Required empty public constructor
    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            //TODO later: status bar during the game
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
                    output.append("got a msg from server: " + readMessage +"\n");
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
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

    public void mkmsg(String str) {
        //handler junk, because thread can't update screen!
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putString("msg", str);
        msg.setData(b);
        handler.sendMessage(msg);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_client, container, false);

        //output textview
        output = (TextView) myView.findViewById(R.id.ct_output);
        //buttons
        btn_device = (Button) myView.findViewById(R.id.which_device);
        btn_device.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                querypaired();
            }
        });
        btn_start = (Button) myView.findViewById(R.id.start_client);
        btn_start.setEnabled(false);
        btn_start.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                output.append("Starting client\n");
                startClient();
            }
        });
        btn_send = (Button) myView.findViewById(R.id.send_msg_client);
        btn_send.setEnabled(false);
        btn_send.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                output.append("msg sent\n");
                sendMessage("wassup from client\n");
            }
        });
        btn_ready = (Button) myView.findViewById(R.id.ready_game_client);
        btn_ready.setEnabled(false);
        btn_ready.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                sendMessage("choosingDialogQuery");
            }
        });


        //setup the bluetooth adapter.
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            output.append("No bluetooth device.\n");
            btn_start.setEnabled(false);
            btn_device.setEnabled(false);
        }
        Log.v(TAG, "bluetooth");

        return myView;
    }


    @Override
    public void onStart() {
        super.onStart();
        //creating a BluetoothService here
        if(mChatService == null) {
            mChatService = new BluetoothService(getActivity(), handler);
        }
        querypaired();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
    }

    @Override
    public void onResume() {
        Log.v(TAG, "onResume");
        super.onResume();
    }



    //setting the device
    public void querypaired() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            output.append("at least 1 paired device\n");
            final BluetoothDevice blueDev[] = new BluetoothDevice[pairedDevices.size()];
            String[] items = new String[blueDev.length];
            int i =0;
            for (BluetoothDevice devicel : pairedDevices) {
                blueDev[i] = devicel;
                items[i] = blueDev[i].getName() + ": " + blueDev[i].getAddress();
                output.append("Device: "+items[i]+"\n");
                //mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                i++;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Choose Bluetooth:");
            builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    dialog.dismiss();
                    if (item >= 0 && item <blueDev.length) {
                        device = blueDev[item];
                        btn_device.setText("device: "+blueDev[item].getName());
                        btn_start.setEnabled(true);
                    }

                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }


    public void startClient() {
        if (device != null) {
            Log.v(TAG, "connecting with: " + device);

            mChatService.connect(device);
            btn_send.setEnabled(true); //sending only if connected
            btn_ready.setEnabled(true);
        } else
            Log.v(TAG, "device is null");
    }

    public void sendMessage(String msg) {
        if (mChatService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(getActivity(), "not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        mChatService.write(msg.getBytes());
    }



}