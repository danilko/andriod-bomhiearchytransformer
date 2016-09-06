package com.csvconverter.csvconverter;

import android.Manifest;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.csvconverter.csvconverter.translator.dao.BOMDAO;
import com.csvconverter.csvconverter.translator.dao.impl.BaseBOMDAO;

public class MainActivity extends AppCompatActivity {

    private Context context;
    private String currentStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentStatus = "";
        context = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                0);

        Button btn = (Button) findViewById(R.id.btnStartConverting);
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                currentStatus = "Start Converting";
                BOMTranslateAsyncTask asyncTask = new BOMTranslateAsyncTask();

                EditText etxtInput  = (EditText) findViewById(R.id.etxtLevelRowHeader);
                asyncTask.setLevelRowHeader(etxtInput.getText().toString());
                etxtInput = (EditText) findViewById(R.id.etxtItemNumberRowHeader);
                asyncTask.setItemNumberRowHeader(etxtInput.getText().toString());
                etxtInput = (EditText) findViewById(R.id.etxtFatherNumberRowHeader);
                asyncTask.setItemNumberFatherRowHeader(etxtInput.getText().toString());

                etxtInput = (EditText) findViewById(R.id.etxtBomInput);
                asyncTask.execute(etxtInput.getText().toString());

            }
        });
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
    public class BOMTranslateAsyncTask extends AsyncTask<String, Integer, Long> {

        private String levelRowHeader;
        private String itemNumberRowHeader;
        private String itemNumberFatherRowHeader;

        public void setLevelRowHeader(String inputLevelRowHeader)
        {
            levelRowHeader = inputLevelRowHeader;
        }

        public void setItemNumberRowHeader(String inputItemNumberRowHeader)
        {
            itemNumberRowHeader= inputItemNumberRowHeader;
        }

        public void setItemNumberFatherRowHeader(String inputItemNumberFatherRowHeader)
        {
            itemNumberFatherRowHeader = inputItemNumberFatherRowHeader;
        }


        @Override
        protected Long doInBackground(String ... fileNames){

            for (int i = 0; i < fileNames.length; i++) {

                BOMDAO bomDAO = new BaseBOMDAO();

                EditText etxtBOMInput = (EditText) findViewById(R.id.etxtBomInput);

                bomDAO.setLevelRowHeader(levelRowHeader);
                bomDAO.setItemNumberRowHeader(itemNumberRowHeader);
                bomDAO.setItemNumberFatherRowHeader(itemNumberFatherRowHeader);

                bomDAO.translateBOM(fileNames[i], context);

                currentStatus = bomDAO.getStatus();
            }

            return Long.valueOf(fileNames.length);
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
               TextView text = (TextView) MainActivity.this.findViewById(R.id.txtStatus);
               text.setText(currentStatus);

        }


        @Override
        protected void onPostExecute(Long result) {
            TextView text =  (TextView) MainActivity.this.findViewById(R.id.txtStatus);
            text.setText(currentStatus);

            Context context = getApplicationContext();
            CharSequence toastText = currentStatus;
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, toastText, duration);
            toast.show();
        }
    }
}
