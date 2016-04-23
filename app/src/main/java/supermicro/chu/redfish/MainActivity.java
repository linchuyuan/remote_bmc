package supermicro.chu.redfish;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private Button login;
    private restful api;
    private EditText login_input;
    private EditText pass_input;
    private EditText ip;
    private LinearLayout login_page;
    private FrameLayout content;
    private HorizontalScrollView navigation_bar;
    private Button overall;
    private boolean isset = false;
    private Thread login_refresher;
    private Thread notification_refresher;
    public TextView token;
    public TextView ip_address;
    public ImageView loading;
    public NotificationCompat.Builder mBuilder;
    public NotificationManager mNotifyMgr;

    @Override
    protected void onDestroy(){
        super.onDestroy();
        VolleyLog.d("Xfficient Destoried");
        if(notification_refresher != null){
            if(notification_refresher.isAlive()){
                notification_refresher.destroy();
            }
        }
        if(login_refresher != null){
            if(login_refresher.isAlive()){
                login_refresher.destroy();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Context context = this;
        login = (Button) findViewById(R.id.login);
        token = (TextView)findViewById(R.id.token);
        ip_address = (TextView) findViewById(R.id.ip_address);
        login_input = (EditText) findViewById(R.id.username);
        pass_input = (EditText) findViewById(R.id.password);
        login_page = (LinearLayout) findViewById(R.id.login_page);
        content = (FrameLayout) findViewById(R.id.content);
        final FrameLayout overall_layout = (FrameLayout) findViewById(R.id.overall_layout);
        navigation_bar = (HorizontalScrollView) findViewById(R.id.navigation_bar);
        ip = (EditText) findViewById(R.id.ip);
        loading = (ImageView) findViewById(R.id.loading);


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isset = true;
                api = new restful(context);
                api.login(login_input.getText().toString(), pass_input.getText().toString(), ip.getText().toString());
                mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                login_page.setVisibility(v.INVISIBLE);
                api.get_overall();
                overall_layout.setVisibility(View.VISIBLE);
                navigation_bar.setVisibility(v.VISIBLE);
                InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

                login_refresher = new Thread() {
                    @Override
                    public void run() {
                        while (true) {
                            SystemClock.sleep(200000);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    api.update_login(login_input.getText().toString(), pass_input.getText().toString(), ip.getText().toString());
                                }
                            });
                        }
                    }
                };
                notification_refresher = new Thread(){
                    @Override
                    public void run() {
                        while (true) {
                            SystemClock.sleep(30000);
                            api.check_event_log();
                        }
                    }
                };
                login_refresher.start();
                notification_refresher.start();

            }
        });

        init();
    }

    public void init() {
        overall = (Button) findViewById(R.id.overall);

        final LinearLayout login_page = (LinearLayout) findViewById(R.id.login_page);;
        final FrameLayout power_control = (FrameLayout) findViewById(R.id.content);
        final FrameLayout sensor_reading = (FrameLayout) findViewById(R.id.senosr_layout);
        final FrameLayout fan_layout = (FrameLayout) findViewById(R.id.fan_layout);
        final FrameLayout event_log = (FrameLayout) findViewById(R.id.event_fragment);
        final FrameLayout overall_layout = (FrameLayout) findViewById(R.id.overall_layout);

        View.OnClickListener operation = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button click = (Button) v;
                String operation = click.getText().toString();
                switch (operation) {
                    case "overall":
                        login_page.setVisibility(View.INVISIBLE);
                        power_control.setVisibility(View.INVISIBLE);
                        sensor_reading.setVisibility(View.INVISIBLE);
                        fan_layout.setVisibility(View.INVISIBLE);
                        overall_layout.setVisibility(View.VISIBLE);
                        break;
                }
            }
        };
        overall.setOnClickListener(operation);
    }
    public class restful {
        public RequestQueue queue;
        public String header;
        public String ip_log;
        public int notification_count = 1;
        public int notification_default_count = 0;
        public Context api_context;
        public TableLayout overall_table;

        public PowerManager mgr;
        public PowerManager.WakeLock wakeLock;

        public restful(Context context) {
            queue = Volley.newRequestQueue(context);
            api_context = context;
            mgr = (PowerManager)api_context.getSystemService(Context.POWER_SERVICE);
            wakeLock = mgr.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "Lock");
            overall_table = (TableLayout) findViewById(R.id.overall_table);
        }
        public void get (String this_url, final get_this get_this_reponse){
            final String url = this_url;
            JsonObjectRequest get_request = new JsonObjectRequest(Request.Method.GET, url,null , new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    get_this_reponse.get(response);
                }
            }, new Response.ErrorListener() {
                @Override public void onErrorResponse(VolleyError e) {get(url,get_this_reponse);}
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("X-Auth-Token",header);
                    return params;
                }
            };
            get_request.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(get_request);
        }
        public void update_login (final String username,final String password, final String ip) {
            String login_url = "https://"+ip+"/redfish/v1/SessionService/Sessions/";
            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("UserName", username);
                jsonBody.put("Password", password);
            }catch (JSONException e){token.setText(e.toString());}
            mJsonObjectRequest loginRequest = new mJsonObjectRequest(Request.Method.POST, login_url, jsonBody, new Response.Listener<JSONObject>() {
                public void onResponse(JSONObject response) {
                    try {
                        header = response.get("token").toString();
                        token.setText(header);
                        ip_address.setText(ip);
                    }catch (JSONException e){token.setText(e.toString());
                    }
                }
            }, new Response.ErrorListener() {
                public void onErrorResponse(VolleyError e) {SystemClock.sleep(3000);update_login(username,password,ip);}
            });
            queue.add(loginRequest);
        }
        public void login(final String username,final String password, final String ip) {
            loading.setVisibility(View.VISIBLE);
            String login_url = "https://"+ip+"/redfish/v1/SessionService/Sessions/";
            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("UserName", username);
                jsonBody.put("Password", password);
            }catch (JSONException e){token.setText(e.toString());}
            mJsonObjectRequest loginRequest = new mJsonObjectRequest(Request.Method.POST, login_url, jsonBody, new Response.Listener<JSONObject>() {
                public void onResponse(JSONObject response) {
                        loading.setVisibility(View.INVISIBLE);
                        try {
                                header = response.get("token").toString();
                                ip_log = ip;
                                token.setText(header);
                                ip_address.setText(ip);
                                set_event_log_count();
                            }catch (JSONException e){token.setText(e.toString());}
                        }
                    }, new Response.ErrorListener() {
                        public void onErrorResponse(VolleyError e) {SystemClock.sleep(3000);login(username, password, ip);}
                    });

            queue.add(loginRequest);
        }
        public void check_event_log(){
            String url = "https://"+ip_log+"/redfish/v1/Managers/1/LogServices/Log1/Entries";
            JsonObjectRequest get_request = new JsonObjectRequest(Request.Method.GET, url,null , new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        int count = response.getInt("Members@odata.count");
                        if ( count > notification_default_count){
                            get_alerted_event(new get_this() {
                                @Override
                                public void get(JSONObject reponse) {
                                    try {
                                        mBuilder = new NotificationCompat.Builder(api_context)
                                                .setSmallIcon(R.mipmap.alert)
                                                .setContentTitle(reponse.getString("SensorType") + "@" + ip_log)
                                                .setContentText(reponse.getString("Message") + ":" + reponse.getString("EntryCode"))
                                                .setDefaults(Notification.DEFAULT_SOUND)
                                                .setDefaults(Notification.DEFAULT_VIBRATE);
                                        mNotifyMgr.notify(notification_count, mBuilder.build());
                                        VolleyLog.d("Activting screen");
                                        wakeLock.acquire(3000);
                                        VolleyLog.d("notification pushed");
                                    }catch (JSONException e){VolleyLog.d("notification not pushed");}
                                }
                            });
                            set_event_log_count();
                            notification_count++;
                            VolleyLog.d("New event Detected");
                        }
                        else {
                            VolleyLog.d("no event Detected");
                        }
                    }catch (JSONException e){VolleyLog.d(e.toString());}
                }
            }, new Response.ErrorListener() {
                @Override public void onErrorResponse(VolleyError e) {check_event_log();}
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("X-Auth-Token",header);
                    return params;
                }
            };
            get_request.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(get_request);
        }
        public void set_event_log_count(){
            String url = "https://"+ip_log+"/redfish/v1/Managers/1/LogServices/Log1/Entries";
            JsonObjectRequest get_request = new JsonObjectRequest(Request.Method.GET, url,null , new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        int count = response.getInt("Members@odata.count");
                        notification_default_count = count;
                        VolleyLog.d(String.valueOf(notification_default_count)+" in the stack");
                    }catch (JSONException e){set_event_log_count();}
                }
            }, new Response.ErrorListener() {
                @Override public void onErrorResponse(VolleyError e) {set_event_log_count();}
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("X-Auth-Token",header);
                    return params;
                }
            };
            get_request.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(get_request);
        }
        public void get_alerted_event(final get_this get_sensor){
            String url = "https://"+ip_log+"/redfish/v1/Managers/1/LogServices/Log1/Entries/"+String.valueOf(notification_default_count);
            JsonObjectRequest get_request = new JsonObjectRequest(Request.Method.GET, url,null , new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    get_sensor.get(response);
                }
            }, new Response.ErrorListener() {
                @Override public void onErrorResponse(VolleyError e) {get_alerted_event(get_sensor);}
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("X-Auth-Token",header);
                    return params;
                }
            };
            get_request.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(get_request);
        }
        public void get_overall(){
            loading.setVisibility(View.VISIBLE);
            String parent_url = "https://"+ip_log+"/redfish/v1/Systems/1";
            JsonObjectRequest get_request = new JsonObjectRequest(Request.Method.GET, parent_url,null , new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        loading.setVisibility(View.INVISIBLE);

                        get("https://" + ip_log + "/redfish/v1/Chassis/1/Power/", new get_this() {
                            @Override
                            public void get(JSONObject get_this_reponse) {
                                try {
                                    TableRow power_row = new TableRow(api_context);
                                    JSONArray power_array = get_this_reponse.getJSONArray("PowerSupplies");
                                    JSONObject power_object = power_array.getJSONObject(1);
                                    String power_string = power_object.getString("Model");
                                    TextView power_text = new TextView(api_context);
                                    power_text.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                                    power_text.setText(power_string);
                                    TextView power_text_1 = new TextView(api_context);
                                    power_text_1.setText("Power: ");
                                    power_row.addView(power_text_1);
                                    power_row.addView(power_text);
                                    overall_table.addView(power_row);
                                } catch (JSONException e) {
                                }
                            }
                        });

                        get("https://" + ip_log + "/redfish/v1/Systems/1/Processors/1", new get_this() {
                            @Override
                            public void get(JSONObject get_this_reponse) {
                                try {
                                    TableRow cpu_row = new TableRow(api_context);
                                    String cpu_string = get_this_reponse.getString("Model");
                                    TextView cpu_text = new TextView(api_context);
                                    cpu_text.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                                    cpu_text.setText(cpu_string);
                                    TextView cpu_text_1 = new TextView(api_context);
                                    cpu_text_1.setText("CPU:");
                                    cpu_row.addView(cpu_text_1);
                                    cpu_row.addView(cpu_text);
                                    overall_table.addView(cpu_row);
                                } catch (JSONException e) {VolleyLog.d(e.toString());}
                            }
                        });

                        TableRow mb_row = new TableRow(api_context);
                        String mb_string = response.getString("Model");
                        TextView mb_text = new TextView (api_context);
                        mb_text.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                        mb_text.setText(mb_string);
                        TextView mb_text_1 = new TextView (api_context);
                        mb_text_1.setText("Model: ");
                        mb_row.addView(mb_text_1);
                        mb_row.addView(mb_text);
                        overall_table.addView(mb_row);


                        TableRow mem_row = new TableRow(api_context);
                        JSONObject mem_object = response.getJSONObject("MemorySummary");
                        String mem_string = mem_object.getString("TotalSystemMemoryGiB");
                        TextView mem_text = new TextView (api_context);
                        mem_text.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                        mem_text.setText(mem_string+"GB");
                        TextView mem_text_1 = new TextView (api_context);
                        mem_text_1.setText("Memory:");
                        mem_row.addView(mem_text_1);
                        mem_row.addView(mem_text);
                        overall_table.addView(mem_row);
/*
                        TableRow powerstate_row = new TableRow(api_context);
                        String one_string = response.getString("PowerState");
                        TextView name_text = new TextView (api_context);
                        name_text.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                        name_text.setText("   "+one_string+"  ");
                        TextView powerstate_text_1 = new TextView (api_context);
                        powerstate_text_1.setText("Power State: ");
                        powerstate_row.addView(powerstate_text_1);
                        powerstate_row.addView(name_text);
                        overall_table.addView(powerstate_row);
*/
                    }catch(JSONException e){VolleyLog.d(e.toString());}
                }
            }, new Response.ErrorListener() {
                @Override public void onErrorResponse(VolleyError e) {get_overall();}
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("X-Auth-Token",header);
                    return params;
                }
            };
            get_request.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(get_request);
        }
        class mJsonObjectRequest extends JsonObjectRequest
        {
            public mJsonObjectRequest(int method, String url, JSONObject jsonRequest,Response.Listener listener, Response.ErrorListener errorListener)
            {
                super(method, url, jsonRequest, listener, errorListener);
            }
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
                    int length = response.headers.toString().length();
                    jsonString = "{\"token\":\""+response.headers.toString().substring(length-33,length-1)+"\","+jsonString.substring(1);
                    VolleyLog.d(jsonString);
                    return Response.success(new JSONObject(jsonString),
                            HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {return Response.error(new ParseError(e));} catch (JSONException je) {return Response.error(new ParseError(je));}
            }

        }
    }
     public interface get_this{
        void get(JSONObject get_this_reponse);
    }

}


