package com.example.neur0genesis.nfc_tag_reader;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Outline;
import android.graphics.drawable.AnimationDrawable;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.nfc.NfcAdapter;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;


import com.shamanland.fab.FloatingActionButton;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;



/*
* Activity for reading data from an NDEF Tag.
* */

public class MainActivity extends Activity {

    DBAdapter myDb;

    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "NfcDemo";
    public int count = 1;

    private TextView mTextView;
    private NfcAdapter mNfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.TAGtextView);
        final ImageView cardView = (ImageView) findViewById(R.id.cardView);

        //Open DB
        openDB();

        //Get default adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            //Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
        }

        if (mNfcAdapter != null) {
            if (!mNfcAdapter.isEnabled()) {
                //mTextView.setText("NFC is disabled.");
            } else {
                handleIntent(getIntent());
            }

            Cursor cursor = myDb.getAllRows();
            displayRecordSet(cursor);

        }

        final View fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                count += 1;

                int id = getResources().getIdentifier("com.example.neur0genesis.nfc_tag_reader:drawable/" + "card_" + count, null, null);

                cardView.setImageResource(
                id);
                //fab.setVisibility(View.GONE);
                System.out.println(count);
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("card count",count);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        count = savedInstanceState.getInt("card count", 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        closeDB();
    }

    //Close DB
    private void closeDB() {
        myDb.close();
    }

    //Instantiate the DB adapter
    private void openDB() {
        myDb = new DBAdapter(this);
        myDb.open();
    }

    private void displayText(String message) {
        TextView textView = (TextView) findViewById(R.id.showId);
        textView.setText(message);
    }

    //Display entire record set to screen.
    private void displayRecordSet(Cursor cursor) {
        String message = "";
        //Populate the message from the cursor

        //Reset cursor to start, checking to see if there is data:
        if (cursor.moveToFirst()) {
            do {
                //Process the data:
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                int studentNumber = cursor.getInt(2);

                //Append data to the message:
                message += "id=" + id
                        + ", name" + name
                        + ", #=" + studentNumber
                        + "\n";
            } while(cursor.moveToNext());
        }
        //Close cursor to avoid resource leak:
        cursor.close();

        displayText(message);
    }


    @Override
    protected void onResume() {
        super.onResume();

		/*
		 * It's important, that the activity is in the foreground (resumed). Otherwise
		 * an IllegalStateException is thrown.
		 */

        if (mNfcAdapter != null) {
            setupForegroundDispatch(this, mNfcAdapter);
        }
    }

    @Override
    protected void onPause() {
		/*
		 * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
		 */

        if (mNfcAdapter != null) {
            stopForegroundDispatch(this, mNfcAdapter);
        }

        super.onPause();
    }

    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            String number = Integer.toString(count);
            mTextView.setText(number);
            count = count + 1;
            byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
            mTextView.setText(getDec(id));
            System.out.println(getDec(id));


            if (getDec(id).equals("59240445625")) {
                mTextView.setText("Player 1 | option 1 | with id: " + getDec(id));
                long newID = myDb.insertRow("Waldo", 987,"red");
                Cursor cursor = myDb.getAllRows();
                displayRecordSet(cursor);

            } else if (getDec(id).equals("2848297313")) {
                mTextView.setText("Player 2 | option 1 | with id: " + getDec(id));
                myDb.deleteAll();
                Cursor cursor = myDb.getAllRows();
                displayRecordSet(cursor);
            }
        }
    }

    /**
     * @param activity The corresponding {@link Activity} requesting the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, null, techList); //Set filter to null so that it will always dispatch
    }

    /**
     * @param activity The corresponding {@link MainActivity} requesting to stop the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    /**
     * Background task for reading the data. Do not block the UI thread while reading.
     *
     * @author Ralf Wondratschek
     *
     */
    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {

        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF is not supported by this Tag.
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Unsupported Encoding", e);
                    }
                }
            }

            return null;
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {
			/*
			 * See NFC forum specification for "Text Record Type Definition" at 3.2.1
			 *
			 * http://www.nfc-forum.org/specs/
			 *
			 * bit_7 defines encoding
			 * bit_6 reserved for future use, must be 0
			 * bit_5..0 length of IANA language code
			 */

            byte[] payload = record.getPayload();

            // Get the Text Encoding
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

            // Get the Language Code
            int languageCodeLength = payload[0] & 0063;

            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"

            // Get the Text
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                mTextView.setText("Read content: " + result);
            }
        }
    }

    private String getDec(byte[] bytes) {
        StringBuilder sb = new StringBuilder();

        long result = 0;
        long factor = 1;
        for (int i = 0; i < bytes.length; ++i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 1;
            String s = String.valueOf(result);
            sb.append(s);
        }
        return sb.toString();
    }
}
