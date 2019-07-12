package com.htetznaing.paohmyanmardictionary;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.SubMenu;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.htetznaing.paohmyanmardictionary.Adapter.Adapter;
import com.htetznaing.paohmyanmardictionary.DB.DictionaryDBHelper;
import com.htetznaing.paohmyanmardictionary.DB.WordDBHelper;
import com.htetznaing.paohmyanmardictionary.Model.Model;
import com.htetznaing.paohmyanmardictionary.Utils.AIOmmTool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    EditText edit_query;
    RecyclerView recyclerView;
    Adapter adapter;
    DictionaryDBHelper dictionaryDbHelper;
    WordDBHelper wordDBHelper;
    ArrayList<Model> data = new ArrayList<>();
    NavigationView navigationView;
    ArrayList<Model> tableOfContents = new ArrayList<>();
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Switch swichMyanmar;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("ပအိုဝ်ႏ - မြန်မာ");

        progressDialog = new ProgressDialog(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        //Start
        dictionaryDbHelper = DictionaryDBHelper.getInstance(this);
        wordDBHelper = WordDBHelper.getInstance(this);
        edit_query = findViewById(R.id.edit_query);

        adapter = new Adapter(data);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        edit_query.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ArrayList<Model> temp = new ArrayList<>();
                String unicode = AIOmmTool.getUnicode(s.toString());
                if (isMmOrPaOh(s.toString())) {
                    temp = dictionaryDbHelper.searchWord(unicode);
                }else{
                    String check = s.toString();
                    if (!check.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Myanmar Or PaOh Only", Toast.LENGTH_SHORT).show();
                    }else temp = dictionaryDbHelper.getAll();
                }
                data.clear();
                data.addAll(temp);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        navigationView.getMenu().findItem(R.id.swift2myanmar).setActionView(new Switch(this));

        if (sharedPreferences.getBoolean("myanmar",false)){
            addMyanmarTableOfContents();
            ((Switch) navigationView.getMenu().findItem(R.id.swift2myanmar).getActionView()).setChecked(true);
        }else {
            addPaOhTableOfContents();
            ((Switch) navigationView.getMenu().findItem(R.id.swift2myanmar).getActionView()).setChecked(false);
        }


        swichMyanmar = (Switch) navigationView.getMenu().findItem(R.id.swift2myanmar).getActionView();
        swichMyanmar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    addMyanmarTableOfContents();
                } else {
                    addPaOhTableOfContents();
                }
            }
        });

        new AsyncTask<Void,Void,ArrayList<Model>>(){

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog.show();
            }

            @Override
            protected ArrayList<Model> doInBackground(Void... voids) {
                return dictionaryDbHelper.getAll();
            }

            @Override
            protected void onPostExecute(ArrayList<Model> models) {
                super.onPostExecute(models);
                progressDialog.dismiss();
                if (models!=null){
                    data.clear();
                    data.addAll(models);
                    adapter.notifyDataSetChanged();
                }
            }
        }.execute();
    }

    private void addPaOhTableOfContents(){
        Menu m = navigationView.getMenu();
        m.removeItem(0);
        navigationView.setItemIconTintList(null);
        SubMenu sub = m.addSubMenu(0, 0, Menu.NONE, "မာတိကာ");
        tableOfContents = wordDBHelper.getTableOfContents();
        Collections.sort(tableOfContents, new Comparator() {
            @Override
            public int compare(Object softDrinkOne, Object softDrinkTwo) {
                //use instanceof to verify the references are indeed of the type in question
                return ((Model)softDrinkOne).paoh
                        .compareTo(((Model)softDrinkTwo).paoh);
            }
        });
        for (int i=0;i<tableOfContents.size();i++){
            MenuItem item = sub.add(0, i, Menu.NONE, tableOfContents.get(i).getPaoh());
            item.setIcon(TextDrawable.builder().buildRound(tableOfContents.get(i).getPaoh().substring(0, 1), Constants.getColor()));
        }
        editor.putBoolean("myanmar",false).apply();
    }

    private void addMyanmarTableOfContents() {
        Menu m = navigationView.getMenu();
        m.removeItem(0);
        navigationView.setItemIconTintList(null);
        SubMenu sub = m.addSubMenu(0, 0, Menu.NONE, "မာတိကာ");
        tableOfContents = wordDBHelper.getTableOfContents();
        Collections.sort(tableOfContents, new Comparator() {
            @Override
            public int compare(Object softDrinkOne, Object softDrinkTwo) {
                //use instanceof to verify the references are indeed of the type in question
                return ((Model) softDrinkOne).mm
                        .compareTo(((Model) softDrinkTwo).mm);
            }
        });
        for (int i = 0; i < tableOfContents.size(); i++) {
            MenuItem item = sub.add(0, i, Menu.NONE, tableOfContents.get(i).getMm());
            item.setIcon(TextDrawable.builder().buildRound(tableOfContents.get(i).getMm().substring(0, 1), Constants.getColor()));
        }
        editor.putBoolean("myanmar", true).apply();
    }

    private boolean isMmOrPaOh(String string){
        final String regex = "^[က-႞\\/uaa60\\-\\/uaa7e]+$";
        final Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        final Matcher matcher = pattern.matcher(string);
        if (matcher.find()) {
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.about:
                try {
                    showAbout();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void showAbout() throws PackageManager.NameNotFoundException {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("အကျောင်ꩻလံꩻ")
                .setMessage("App Name: "+getString(R.string.app_name)+"\n\n"+
                        "Version: "+getPackageManager().getPackageInfo(getPackageName(),0).versionName+"\n\n"+
                        "Developer: Khun Htetz Naing\n\n" +
                        "Translator: Khun Naing Ko")
                .setPositiveButton("မွေးသွူ",null);
        builder.show();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id){
            case R.id.swift2myanmar:
                swichMyanmar.toggle();
                return true;
                default:
                    for (Model m:tableOfContents){
                        if (m.getMm().equalsIgnoreCase(item.getTitle().toString()) || m.getPaoh().equalsIgnoreCase(item.getTitle().toString())){
                            if (m.getCount()!=null && !m.getCount().isEmpty()){
                                Intent intent=new Intent(this,DetailsActivity.class);
                                intent.putExtra("category",m.getMm());
                                intent.putExtra("name",item.getTitle());
                                startActivity(intent);
                            }else {
                                Toast.makeText(this, "Empty!", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        }
                    }
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
