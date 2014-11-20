package com.example.neur0genesis.nfc_tag_reader;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

/*
* Activity for reading data from an NDEF Tag.
* */

public class MainActivity extends Activity {

    public static final String TAG = "Robust NFC Reader";

    private TextView mTextView; //Object for displaying text to the user. (Can be edited but optional).
    private NfcAdapter mNfcAdapter; //Object for fetching the default NFC adapter by using a helper.

    @Override //Annotation to indicate that we are overriding the superclass.
    protected void onCreate(Bundle savedInstanceState) { //'Protected' limits access to the package.
        super.onCreate(savedInstanceState); //Parse in the 'savedInstanceState' into the onCreate method in the
        setContentView(R.layout.activity_main); //Show main activity after initializing the application.

        mTextView = (TextView) findViewById(R.id.textView_explanation); //Display the view with a specific ID (in the .xml).

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this); //Set mNfcAdapter to the default NFC adapter.

        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show(); //LENGTH_LONG refers to the display time
            finish(); //Finish main activity
            return;

        }

        if (!mNfcAdapter.isEnabled()) {
            mTextView.setText("NFC is disabled.");
        } else {
            mTextView.setText(R.string.explanation);
        }

    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
