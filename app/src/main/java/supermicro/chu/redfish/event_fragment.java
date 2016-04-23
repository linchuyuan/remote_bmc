package supermicro.chu.redfish;

import android.content.Context;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class event_fragment extends Fragment {
    public restful api;
    public Button clear_event_log;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Button event_button = (Button) getActivity().findViewById(R.id.event_button);
        final LinearLayout login_page = (LinearLayout) getActivity().findViewById(R.id.login_page);
        final FrameLayout power_control = (FrameLayout) getActivity().findViewById(R.id.content);
        final FrameLayout sensor_reading = (FrameLayout) getActivity().findViewById(R.id.senosr_layout);
        final FrameLayout fan_layout = (FrameLayout) getActivity().findViewById(R.id.fan_layout);
        final FrameLayout event_layout = (FrameLayout) getActivity().findViewById(R.id.event_layout);
        final FrameLayout overall_layout = (FrameLayout) getActivity().findViewById(R.id.overall_layout);

        View.OnClickListener nav = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login_page.setVisibility(View.INVISIBLE);
                power_control.setVisibility(View.INVISIBLE);
                sensor_reading.setVisibility(View.INVISIBLE);
                fan_layout.setVisibility(View.INVISIBLE);
                overall_layout.setVisibility(View.INVISIBLE);
                event_layout.setVisibility(View.VISIBLE);
                api.set_context();
                api.get_event();
            }
        };
        event_button.setOnClickListener(nav);
    }

    public event_fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.event_fragment, container, false);
        Context context = getActivity();
        init(context, view);
        return view;
    }

    public void init (final Context context,final View view){
        api = new restful(context, view);
        clear_event_log = (Button) view.findViewById(R.id.clear_event);
        clear_event_log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                api.clear_event_log();
            }
        });
    }

    private class restful{
        private RequestQueue queue;
        private String header;
        private String ip = "";
        private JsonObjectRequest get_request = null;
        private TextView display;
        private TableLayout listview;


        private restful(Context context, View view) {
            MainActivity mActivity= new MainActivity();
            queue = Volley.newRequestQueue(context);
            listview = (TableLayout) view.findViewById(R.id.listview);
            display = (TextView) view.findViewById(R.id.holder);
        }
        private void set_context() {
            TextView token = (TextView) getActivity().findViewById(R.id.token);
            TextView context_ip = (TextView) getActivity().findViewById(R.id.ip_address);
            ip = context_ip.getText().toString();
            header = token.getText().toString();
        }
        private void get_event(){
            final ImageView loading = (ImageView) getActivity().findViewById(R.id.loading);
            loading.setVisibility(View.VISIBLE);

            String url = "https://"+ip+"/redfish/v1/Managers/1/LogServices/Log1/Entries";
            JsonObjectRequest get_request = new JsonObjectRequest(Request.Method.GET, url,null , new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    loading.setVisibility(View.INVISIBLE);
                    final TableLayout.LayoutParams tableRowParams= new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT);
                    tableRowParams.setMargins(1, 10, 5, 10);
                    int count = 0;
                    try {
                        count = Integer.parseInt(response.getString("Members@odata.count"));
                    }catch (JSONException e ){}
                    for (int i = 0; i < count ; i++){
                        get_log(i+1, new VolleyCallback() {
                            @Override
                            public void onSuccess(TableRow result) {
                                result.setLayoutParams(tableRowParams);
                                result.setBackground(getResources().getDrawable(R.drawable.border_all, null));
                                listview.addView(result);
                            }

                        });
                        SystemClock.sleep(500);
                    }

                }
            }, new Response.ErrorListener() {
                @Override public void onErrorResponse(VolleyError e) {
                    get_event();
                }
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
        private void get_log (final int index,final VolleyCallback callback){
            final ImageView loading = (ImageView) getActivity().findViewById(R.id.loading);
            loading.setVisibility(View.VISIBLE);
            String url = "https://"+ip+"/redfish/v1/Managers/1/LogServices/Log1/Entries/"+String.valueOf(index);
            JsonObjectRequest get_request = new JsonObjectRequest(Request.Method.GET, url,null , new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    loading.setVisibility(View.INVISIBLE);
                    TableRow log = new TableRow(getActivity());
                    try {
                        String name = response.getString("SensorType");
                        TextView type_text = new TextView(getActivity());
                        type_text.setText("  " + name + "   ");
                        log.addView(type_text);

                        String event_message = response.getString("Message");
                        TextView event_message_text = new TextView(getActivity());
                        event_message_text.setText("  " + event_message + "   ");
                        log.addView(event_message_text);

                        String event_type = response.getString("EntryType");
                        TextView event_type_text = new TextView(getActivity());
                        event_type_text.setText("  " + event_type + "   ");
                        log.addView(event_type_text);

                        String event_code = response.getString("EntryCode");
                        TextView event_code_text = new TextView(getActivity());
                        event_code_text.setText("  " + event_code + "   ");
                        log.addView(event_code_text);

                        String time = response.getString("Created");
                        TextView time_text = new TextView(getActivity());
                        time_text.setText("   " + time + "   ");
                        log.addView(time_text);

                        callback.onSuccess(log);
                    }catch (JSONException e){VolleyLog.d(e.toString());}
                }
            }, new Response.ErrorListener() {
                @Override public void onErrorResponse(VolleyError e) { get_log(index, callback);}
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
        private void clear_event_log(){
            String url = "https://"+ip+"/redfish/v1/Managers/1/LogServices/Log1/Actions/LogService.Reset";
            JsonObjectRequest get_request = new JsonObjectRequest(Request.Method.POST, url,null , new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    get_event();
                }
            }, new Response.ErrorListener() {
                @Override public void onErrorResponse(VolleyError e) {
                    clear_event_log();
                }
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
    }
    public interface VolleyCallback{
        void onSuccess(TableRow result);
    }
    public interface array_callback{
        void onSuccess(ArrayList<String> array_callback);
    }
}