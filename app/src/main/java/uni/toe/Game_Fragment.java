package uni.toe;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class Game_Fragment extends Fragment {

    public Game_Fragment() {
        // Required empty public constructor
        /**
         * TODO: pull connectedThread
         *  depends on fragment created the class
         */
    }

    //TODO: put handler here

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.fragment_game, container, false);

        return myView;
    }

    public void cellClick(View v) { //make it work

    }
}
