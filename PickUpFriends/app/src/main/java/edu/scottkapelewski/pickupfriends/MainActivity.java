package edu.scottkapelewski.pickupfriends;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import java.util.ArrayList;

public class MainActivity extends FragmentActivity {

    private final String BUNDLE_KEY_ADDRESSES = "grabAddressesFromBundle";
    private final String MY_ADDRESS = "96 Marcella Street, Boston, MA";

    Button b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        b = (Button) findViewById(R.id.go_btn);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getThisActivity(), MapActivity.class);
                i.putStringArrayListExtra(BUNDLE_KEY_ADDRESSES, getAddresses());
                startActivity(i);
            }
        });
    }

    private ArrayList<String> getAddresses(){
        ArrayList<String> tmp = new ArrayList<>();

        tmp.add(MY_ADDRESS);

        EditText v = (EditText) findViewById(R.id.one);
        tmp.add(v.getText().toString());

        v = (EditText) findViewById(R.id.two);
        tmp.add(v.getText().toString());

        v = (EditText) findViewById(R.id.three);
        tmp.add(v.getText().toString());

        v = (EditText) findViewById(R.id.dest);
        tmp.add(v.getText().toString());

        return tmp;
    }

    private MainActivity getThisActivity(){
        return this;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


}
