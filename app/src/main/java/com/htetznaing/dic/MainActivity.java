package com.htetznaing.dic;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.SubMenu;
import android.view.View;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.htetznaing.app_updater.AppUpdater;
import com.htetznaing.dic.Adapter.Adapter;
import com.htetznaing.dic.Checker.CheckInternet;
import com.htetznaing.dic.DB.DictionaryDBHelper;
import com.htetznaing.dic.DB.WordDBHelper;
import com.htetznaing.dic.Model.Model;
import com.htetznaing.dic.Utils.AIOmmTool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

    AppUpdater appUpdater;
    CheckInternet checkInternet;
    FloatingActionButton fab;
    LinearLayout paoh_char_layout;
    Button mai_ngar,mai_pat_ngar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("ပအိုဝ်ႏ - မြန်မာ");

        appUpdater = new AppUpdater(this,getString(R.string.update_json_url));
        checkInternet = new CheckInternet(this);
        progressDialog = new ProgressDialog(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();

        initPaOhKB();
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String shareText = "PaOh - Myanmar Dictionary\n" +
                        "Download Free at Google Play Store => https://play.google.com/store/apps/details?id="+getPackageName()+"\n" +
                        "APKPure => https://apkpure.com/store/apps/details?id="+getPackageName();
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);
                startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.app_name)));
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
                String userInput = s.toString();

                //Unicode Mai Ngar
                if (userInput.contains("ꩻ")){
                    userInput = userInput.replace("ꩻ","zMaiNgarz");
                }

                //Unicode Mai Pat Ngar
                if (userInput.contains("ႏ")){
                    userInput = userInput.replace("ႏ","zMaiPatNgarz");
                }

                //Zawgyi Mai Pat Ngar
                if (userInput.contains("ၒ")){
                    userInput = userInput.replace("ၒ","zMaiPatNgarz");
                }

                String unicode = AIOmmTool.getUnicode(userInput);

                if (unicode.contains("zMaiNgarz")){
                    unicode = unicode.replace("zMaiNgarz","ꩻ");
                }

                if (unicode.contains("zMaiPatNgarz")){
                    unicode = unicode.replace("zMaiPatNgarz","ႏ");
                }

                System.out.println(unicode);
                if (isMmOrPaOh(unicode)) {
                    temp = dictionaryDbHelper.searchWord(unicode);
                }else{
                    String check = s.toString();
                    if (!check.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Please input correct word!", Toast.LENGTH_SHORT).show();
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
        edit_query.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (keyboardShown(edit_query.getRootView())) {
                    fab.hide();
                    paoh_char_layout.setVisibility(View.VISIBLE);
                } else {
                    fab.show();
                    paoh_char_layout.setVisibility(View.GONE);
                }
            }
        });
    }

    private void initPaOhKB() {
        paoh_char_layout = findViewById(R.id.paoh_char_layout);
        mai_ngar = findViewById(R.id.mai_ngar);
        mai_ngar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_query.append("ꩻ");
            }
        });
        mai_pat_ngar = findViewById(R.id.mai_pat_ngar);
        mai_pat_ngar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_query.append("ႏ");
            }
        });
    }

    private boolean keyboardShown(View rootView) {
        final int softKeyboardHeight = 100;
        Rect r = new Rect();
        rootView.getWindowVisibleDisplayFrame(r);
        DisplayMetrics dm = rootView.getResources().getDisplayMetrics();
        int heightDiff = rootView.getBottom() - r.bottom;
        return heightDiff > softKeyboardHeight * dm.density;
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
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_about,null);
        final TextView tv_help=view.findViewById(R.id.tv_help),tv_app_name = view.findViewById(R.id.tv_app_name),tv_version=view.findViewById(R.id.tv_version),tv_devloper=view.findViewById(R.id.tv_developer),tv_helper=view.findViewById(R.id.tv_helper);
        Button checkUpdate = view.findViewById(R.id.checkUpdate);
        checkUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appUpdater.check(true);
            }
        });
        tv_help.setText(getPaOhHelp());
        Switch myanmar = view.findViewById(R.id.myanmar);
        myanmar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    tv_help.setText(getMyanmarHelp());
                }else tv_help.setText(getPaOhHelp());
            }
        });

        String version = getPackageManager().getPackageInfo(getPackageName(),0).versionName;
        tv_app_name.setText("Name: "+getString(R.string.app_name));
        tv_version.setText("Version: "+version);

        tv_devloper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFb("100030031876000");
            }
        });


        tv_helper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFb("100003911637398");
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("OK",null);
        builder.show();
    }

    private String getPaOhHelp(){
        return "App အတွက်ပအိုဝ်ႏတွမ်ႏမန်းလိတ်စောင်းမꩻ \n" +
                "ကထေတဲမ်းနယ်ထန်ႏလွေꩻဖေႏဒျာႏ\n" +
                "ခွိုꩻရက်  မူႏလထေတဲမ်းနယ်သား\n" +
                "ခွန်ထွန်းလှိုင်ဦးတွမ်ႏ \n" +
                "နျꩻတဲမ်းငီꩻဖေႏဒျာႏ ခွန်ကျော်စိန် သွꩻပေႏသီး ဝင်ꩻနီ \n" +
                "ကေꩻဇူꩻတင်ႏငါႏမရေႏမရာႏသွူ ဩ";
    }

    private void openFb(String userId){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        try {
            intent.setData(Uri.parse("fb://profile/"+userId));
            startActivity(intent);
        } catch (Exception e) {
            intent.setData(Uri.parse("https://m.facebook.com/"+userId));
            startActivity(intent);
        }
    }

    private String getMyanmarHelp(){
        return "App အတွက် အချက်အလက်များအား\n" +
                " မူရင်းရေးသားသူ မျိုꩻရက်ခွန်ထွန်းလှိုင်\n" +
                "(ပအိုဝ်ႏတွမ်ႏမန်း ငေါဝ်းနီဘာႏသာႏ-စာအုပ်)နှင့်\n" +
                " စာရိုက်ရာတွင် ကူညီရိုက်ပေးသူ ခွန်ကျော်စိန်တို့အား \n" +
                "အထူးပင်ကျေးဇူးတင်အပ်ပါသည်။";
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


    @Override
    protected void onStart() {
        super.onStart();
        if (checkInternet.isInternetOn()){
            appUpdater.check(false);
        }
    }
}
