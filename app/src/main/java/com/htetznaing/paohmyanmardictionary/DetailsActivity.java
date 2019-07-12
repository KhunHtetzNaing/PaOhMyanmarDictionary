package com.htetznaing.paohmyanmardictionary;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.htetznaing.paohmyanmardictionary.DB.WordDBHelper;
import com.htetznaing.paohmyanmardictionary.Model.Model;

import java.util.ArrayList;

public class DetailsActivity extends AppCompatActivity {
    WordDBHelper wordDBHelper;
    ArrayList<Model> data = new ArrayList<>();
    String category =null;
    LinearLayout paoh,mm;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        category = getIntent().getStringExtra("category");
        String name = getIntent().getStringExtra("name");
        if (category==null || name==null){
            finish();
        }else setTitle(name);

        wordDBHelper = WordDBHelper.getInstance(this);
        progressDialog = new ProgressDialog(this);
        paoh = findViewById(R.id.paoh);
        mm = findViewById(R.id.myanmar);
        init();
    }

    private void init(){
        new AsyncTask<Void,Void,Void>(){

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog.show();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                data = wordDBHelper.getByCategory(category);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (final Model m:data){
                            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.table_item,null);
                            TextView tvMM = view.findViewById(R.id.text);
                            tvMM.setText(m.getMm());
                            mm.addView(view);
                            view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    showDetail(m);
                                }
                            });
                        }


                        for (final Model m:data){
                            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.table_item,null);
                            TextView textView = view.findViewById(R.id.text);
                            textView.setText(m.getPaoh());
                            paoh.addView(view);
                            view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    showDetail(m);
                                }
                            });
                        }
                    }
                });
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                progressDialog.dismiss();
            }
        }.execute();
    }

    private void showDetail(Model model){
        final String message = "(မြန်မာ)\n"+model.getMm()+" \n\n"+
                "(ပအိုဝ်ႏ)\n"+model.getPaoh();
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_details,null);
        TextView text = view.findViewById(R.id.text);
        text.setText(message);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("Share", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String shareBody = message;
                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.app_name)));
                    }
                })
                .setNegativeButton("Copy", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        clipboardManager.setText(message);
                        if (clipboardManager.hasText()){
                            Toast.makeText(DetailsActivity.this, "Copied", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        builder.show();
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
