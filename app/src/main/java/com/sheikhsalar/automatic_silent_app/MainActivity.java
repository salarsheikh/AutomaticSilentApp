
package com.sheikhsalar.automatic_silent_app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity{

    WifiManager wifiManager;
    WifiReciever wifiReciever;
    ListAdapter listAdapter;
    ListView wifiList;
    List myWifiList;
    NotificationManager mnotificationManager;
    DatabaseHelper myDb;
//    IntervalDatabaseHelper intervaldb;
    ImageView settingBtn;

    int intervalTime;

    @SuppressLint("WifiManagerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, MyService.class));
        setContentView(R.layout.activity_main);

        wifiList =(ListView) findViewById(R.id.list);
        settingBtn =findViewById(R.id.settings);
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast toast = Toast.makeText(MainActivity.this, "hello bhai chal ja", Toast.LENGTH_SHORT);
//                toast.setGravity(Gravity.CENTER, 0, 0);
//                toast.show();
                Intent i=new Intent(MainActivity.this,Setting.class);
                startActivity(i);
            }
        });

        myDb=new DatabaseHelper(this);
//        intervaldb =new IntervalDatabaseHelper(this);

        if (myDb.getAllData().getCount() == 0 && myDb.intervalData().getCount() == 0)
        {
            myDb.insertData("PTCL-WIFI-BB");
            myDb.intervalinsertData(10);
            Toast.makeText(this, "inserted", Toast.LENGTH_SHORT).show();

        }
        else
        {
            Toast.makeText(this, "No Values Inserted", Toast.LENGTH_SHORT).show();
        }

        wifiManager =(WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiReciever = new WifiReciever();

        registerReceiver(wifiReciever,new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},0);
        }
        else {
            ScanWifiList();
            Toast.makeText(this, "wifilist", Toast.LENGTH_SHORT).show();
        }
   }

    private void ScanWifiList() {

        Cursor result =myDb.intervalData();
        result.moveToNext();
        intervalTime = result.getInt(1);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                wifiManager.startScan();
                ScanWifiList();
                myWifiList = wifiManager.getScanResults();
                setAdapter();

            }
        }, intervalTime*1000);

        mnotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Cursor res=myDb.getAllData();
        if (res.getCount()==0){

//            Toast.makeText(this, "nothing found", Toast.LENGTH_SHORT).show();
            return;
        }
        while(res.moveToNext())
        {
            if (String.valueOf(myWifiList).contains(res.getString(1).toString()))
            {
                changeInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
            }
            else
            {
                changeInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
            }

        }
    }

    protected void changeInterruptionFilter(int interruptionFilter){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){ // If api level minimum 23

            // If notification policy access granted for this package
            if(mnotificationManager.isNotificationPolicyAccessGranted()){

                // Set the interruption filter
                mnotificationManager.setInterruptionFilter(interruptionFilter);
            }else {

                // If notification policy access not granted for this package
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                startActivity(intent);
            }
        }
    }
    private void setAdapter(){
        listAdapter = new com.sheikhsalar.automatic_silent_app.ListAdapter(getApplicationContext(),myWifiList);
        wifiList.setAdapter(listAdapter);
    }
//
//    public void setting(View view) {
//
////        Intent intent = new Intent(this,Setting.class);
////        startActivity(intent);
//        Toast.makeText(this, "setting ok", Toast.LENGTH_SHORT).show();
//    }

    class WifiReciever extends BroadcastReceiver
   {

       @Override
       public void onReceive(Context context, Intent intent) {

       }
   }
}