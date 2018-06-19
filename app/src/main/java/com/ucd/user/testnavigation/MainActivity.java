package com.ucd.user.testnavigation;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener
{
    WifiManager wifi;
    ListView lv;
    TextView textStatus;
    Button buttonScan;
    Button buttonSubmit;
    EditText txtLat;
    EditText txtLong;
    EditText txtRoom;
    EditText txtFloor;

    int size = 0;
    List<ScanResult> results;

    String ITEM_KEY = "key";
    ArrayList<HashMap<String, String>> arraylist = new ArrayList<HashMap<String, String>>();
    SimpleAdapter adapter;

    /* Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textStatus = (TextView) findViewById(R.id.textStatus);
        buttonScan = (Button) findViewById(R.id.buttonScan);
        buttonSubmit = (Button) findViewById(R.id.buttonSubmit);
        buttonSubmit.setOnClickListener(this);
        txtLat = (EditText)findViewById(R.id.txtLat);
        txtLong = (EditText)findViewById(R.id.txtLon);
        txtRoom = (EditText)findViewById(R.id.txtRoom);
        txtFloor= (EditText)findViewById(R.id.txtFloor);

        buttonScan.setOnClickListener(this);
        lv = (ListView)findViewById(R.id.list);

        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi.isWifiEnabled() == false)
        {
            Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
            wifi.setWifiEnabled(true);
        }
        this.adapter = new SimpleAdapter(MainActivity.this, arraylist, R.layout.row, new String[] { ITEM_KEY }, new int[] { R.id.list_value });
        lv.setAdapter(this.adapter);

        registerReceiver(new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context c, Intent intent)
            {
                wifi.startScan();
                results = wifi.getScanResults();
                size = results.size();
            }
        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonScan: {
                arraylist.clear();
                wifi.startScan();

                Toast.makeText(this, "Scanning...." + size, Toast.LENGTH_SHORT).show();
                try {
                    size = size - 1;
                    while (size >= 0) {
                        HashMap<String, String> item = new HashMap<String, String>();
                        item.put(ITEM_KEY, results.get(size).SSID + "  " + results.get(size).capabilities);

                        arraylist.add(item);
                        size--;
                        adapter.notifyDataSetChanged();
                    }
                } catch (Exception e) {
                }
                break;
            }
            case R.id.buttonSubmit: {

                String BSSID = "";
                String SSID = "";
                String room = "";
                int frequency = 0;
                int level = 0;
                int floor = 0;
                float lattitude = 0;
                float longitude = 0;
                Iterator<ScanResult> itr = results.iterator();
                while (itr.hasNext()) {
                    ScanResult item = itr.next();
                    BSSID = item.BSSID;
                    SSID = item.SSID;
                    // String dist = item.
                    frequency = item.frequency;
                    level = item.level;
                    lattitude = Float.parseFloat(txtLat.getText().toString());
                    longitude = Float.parseFloat(txtLong.getText().toString());
                    room = txtRoom.getText().toString();
                    floor = Integer.parseInt(txtFloor.getText().toString());

                    //String body = "{\"SSID\" : \"test\",\n" +
                    //      "\"room_num\":\"rest\"}";
                    String body = "{\"BSSID\":\"" + BSSID + "\"," +
                            "\"SSID\":\"" + SSID + "\"," +
                            "\"room_num\":\"" + "test" + "\"," +
                            "\"frequency\":\"" + frequency + "\"," +
                            "\"level\":\"" + level + "\"," +
                            "\"latitude1\":\"" + lattitude + "\"," +
                            "\"room_num\":\"" + room + "\"," +
                            "\"floor\":\"" + floor + "\"," +
                            "\"longitude1\":\"" + longitude + "\"}";


                    AWSConnection conn = new AWSConnection();
                    conn.execute(body);


                }

                    Toast.makeText(this, "Putting...." + size, Toast.LENGTH_SHORT).show();
                    break;

                }


            }

        }

    public class AWSConnection extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... params) {
            URL url = null;
            HttpURLConnection client = null;
            try {
                url = new URL("https://search-indoor-navigation-r2fy6yafkgybvrrhsgpcdev62e.us-east-2.es.amazonaws.com/fingerprint/routers");
                client = (HttpURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setRequestProperty("USER-AGENT","Mozilla/5.0");
                client.setRequestProperty("Content-Type", "application/json");


                client.setUseCaches(false);//set true to enable Cache for the req
                client.setDoOutput(true);
                DataOutputStream dStream = new DataOutputStream(client.getOutputStream());
                dStream.writeBytes(params[0]);
                dStream.flush();
                dStream.close();
                int responseCode = client.getResponseCode();

                client.connect();


            } catch (IOException e) {
                //e.printStackTrace();
            }


              return null;
        }

    }

}