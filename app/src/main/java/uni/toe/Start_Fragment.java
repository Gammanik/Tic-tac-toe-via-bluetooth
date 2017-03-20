package uni.toe;


import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Start_Fragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    public interface OnFragmentInteractionListener {
        //Callback for when an item has been selected
        public void onButtonSelected(int id);
    }
    //UI stuff
    Button btn_client, btn_server;
    TextView logger; //show connection info

    //bluetooth device and code to turn the device on if needed.
    BluetoothAdapter mBluetoothAdapter = null;
    private static final int REQUEST_ENABLE_BT = 2;


    public Start_Fragment() {
        // Required empty public constructor
    }

    //A simple method to append data to the logger textview.
    //so I don't have to think about it in the code.
    public void mkmsg(String msg) {
        logger.append(msg + "\n");
    }


    //This code will check to see if there is a bluetooth device and
    //turn it on if is it turned off.
    public void startbt() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            mkmsg("This device does not support bluetooth");
            return;
        }
        //make sure bluetooth is enabled.
        if (!mBluetoothAdapter.isEnabled()) {
            mkmsg("There is bluetooth, but turned off");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            mkmsg("The bluetooth is ready to use.");
            //bluetooth is on, so list paired devices from here.
            querypaired();
        }
    }

    /*
     * This method will query the bluetooth device and ask for a list of all
     * paired devices.  It will then display to the screen the name of the device and the address
     *   In client fragment we need this address to so we can connect to the bluetooth device that is acting as the server.
     */

    public void querypaired() {
        mkmsg("Paired Devices:");
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            final BluetoothDevice blueDev[] = new BluetoothDevice[pairedDevices.size()];
            String item;
            int i = 0;
            for (BluetoothDevice devicel : pairedDevices) {
                blueDev[i] = devicel;
                item = blueDev[i].getName() + ": " + blueDev[i].getAddress();
                mkmsg("Device: " + item);
                i++;
            }

        } else {
            mkmsg("There are no paired devices");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_start, container, false);
        logger = (TextView) myView.findViewById(R.id.logger1);


        btn_client = (Button) myView.findViewById(R.id.button2);
        btn_client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) //don't call if null, duh...
                    mListener.onButtonSelected(2);
            }
        });
        btn_server = (Button) myView.findViewById(R.id.button1);
        btn_server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) //don't call if null, duh...
                    mListener.onButtonSelected(1);
            }
        });

        startbt();
        return myView;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            //bluetooth result code.
            if (resultCode == Activity.RESULT_OK) {
                mkmsg("Bluetooth is on.");
                querypaired();
            } else {
                mkmsg("Please turn the bluetooth on.");
            }
        }
    }


    // This is all for the callbacks
    //onAttach(Activity activity) is deprecated so new may not be invoked in API < 23
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity = getActivity();
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
