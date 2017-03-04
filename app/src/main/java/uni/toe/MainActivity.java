package uni.toe;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;


public class MainActivity extends AppCompatActivity implements Start_Fragment.OnFragmentInteractionListener  {


    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //fragment for start or join the game
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.activity_main, new Start_Fragment()).commit();
    }

    @Override
    public void onButtonSelected(int id) {

    }
}
