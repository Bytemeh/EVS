package com.entropy.csc.evs;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

public class CardWriter extends AppCompatActivity {
    NfcAdapter nfcAdapter;
    EditText stdNumberEt;
    Button writeButton;
    String stdNumber;

    Boolean loginStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_writer);

        stdNumberEt= (EditText) findViewById(R.id.stdNumberEditText);
        writeButton= (Button) findViewById(R.id.writeButton);

        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stdNumber=stdNumberEt.getText().toString().trim();
            }
        });

        nfcAdapter=NfcAdapter.getDefaultAdapter(this);

        if(nfcAdapter!=null && nfcAdapter.isEnabled()){
            Toast.makeText(this,"NFC is available",Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this,"NFC is not available!",Toast.LENGTH_LONG).show();
        }




    }

    @Override
    protected void onNewIntent(Intent intent) {
        if(stdNumber.isEmpty()){ Toast.makeText(getApplicationContext(),"No student number available ",Toast.LENGTH_LONG).show();}
        else{
            if(intent.hasExtra(NfcAdapter.EXTRA_TAG)){
                Toast.makeText(this,"NFCIntent",Toast.LENGTH_LONG).show();

                Tag tag=intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

                NdefMessage ndefMessage=createNdefMessage(stdNumber);

                writeNdefMessage(tag,ndefMessage);

            }
            Toast.makeText(this,"NFC intent received",Toast.LENGTH_LONG).show();
        }
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        nfcAdapter=NfcAdapter.getDefaultAdapter(this);
        if(nfcAdapter!=null && nfcAdapter.isEnabled()){
            Toast.makeText(this,"NFC is available",Toast.LENGTH_LONG).show();
            enableForegroundDispatchSystem();
        }else{
            Toast.makeText(this,"NFC is not available!",Toast.LENGTH_LONG).show();
        }



        super.onResume();
    }

    @Override
    protected void onPause() {

        disableForegroundDispatchSystem();
        super.onPause();
    }

    private void enableForegroundDispatchSystem(){
        Intent intent=new Intent(this,CardWriter.class);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);

        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intent,0);
        IntentFilter[] intentFilters=new IntentFilter[]{};

        nfcAdapter.enableForegroundDispatch(this,pendingIntent,intentFilters,null);
    }
    private void disableForegroundDispatchSystem(){
        if(nfcAdapter!=null){
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    private void formatTag(Tag tag, NdefMessage ndefMessage){
        try{
            NdefFormatable ndefFormatable=NdefFormatable.get(tag);

            if(ndefFormatable==null){
                Toast.makeText(this,"Tag is not NDEF formattable",Toast.LENGTH_LONG).show();
                return;
            }
            ndefFormatable.connect();
            ndefFormatable.format(ndefMessage);
            ndefFormatable.close();

            Toast.makeText(this,"Tag written",Toast.LENGTH_LONG).show();

        }catch(Exception e){
            e.printStackTrace();
            Log.e("Format Tag",e.getMessage());
        }
    }
    private void writeNdefMessage(Tag tag,NdefMessage ndefMessage){
        try{
            if(tag==null){
                Toast.makeText(this,"Tag object cannot be null",Toast.LENGTH_LONG).show();
                return;
            }
            Ndef ndef=Ndef.get(tag);
            if(ndef==null){
                //format tag with the ndef format and write message
                formatTag(tag,ndefMessage);
            }else{
                ndef.connect();
                if(!ndef.isWritable()){
                    Toast.makeText(this,"Tag is not writable",Toast.LENGTH_LONG).show();
                    ndef.close();
                    return;
                }
                ndef.writeNdefMessage(ndefMessage);
                ndef.close();

                Toast.makeText(this,"Tag written",Toast.LENGTH_LONG).show();
            }


        }catch (Exception e){
            e.printStackTrace();
            Log.e("WriteNDEFMessage",e.getMessage());
        }
    }
    private NdefRecord createTextRecord(String content){
        try{
            byte[] language;
            language= Locale.getDefault().getLanguage().getBytes();

            final byte[] text=content.getBytes("UTF-8");
            final int languageSize=language.length;
            final int textLength=text.length;
            final ByteArrayOutputStream payload=new ByteArrayOutputStream(1+languageSize+textLength);

            payload.write((byte)(languageSize & 0x1F));
            payload.write(language,0,languageSize);
            payload.write(text,0,textLength);

            return new NdefRecord(NdefRecord.TNF_WELL_KNOWN,NdefRecord.RTD_TEXT,new byte[0],payload.toByteArray());
        }catch (Exception e){
            Log.e("createTExtRecord",e.getMessage());
        }
        return null;
    }

    private NdefMessage createNdefMessage(String content){
        NdefRecord ndefRecord=createTextRecord(content);

        NdefMessage ndefMessage=new NdefMessage(new NdefRecord[]{ndefRecord});

        return ndefMessage;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflator=getMenuInflater();
        inflator.inflate(R.menu.my_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.logout_menu_button:
                loginStatus=Boolean.FALSE;
                Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
                intent.putExtra("loginStatus",loginStatus);

                startActivity(intent);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
