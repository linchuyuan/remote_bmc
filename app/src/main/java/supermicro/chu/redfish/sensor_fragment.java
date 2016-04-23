package supermicro.chu.redfish;

import android.content.Context;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class sensor_fragment extends Fragment {
    private restful api;

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Button sensor_mode = (Button) getActivity().findViewById(R.id.sensors);
        final LinearLayout login_page = (LinearLayout) getActivity().findViewById(R.id.login_page);;
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
                sensor_reading.setVisibility(View.VISIBLE);
                fan_layout.setVisibility(View.INVISIBLE);
                event_layout.setVisibility(View.INVISIBLE);
                overall_layout.setVisibility(View.INVISIBLE);
                api.set_context();
                api.get_temperature_sensors();
            }
        };
        sensor_mode.setOnClickListener(nav);
    }

    public sensor_fragment() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sensor_fragment, container, false);
        Context context = getActivity();
        init(context, view);
        return view;
    }

    public void init(final Context context,final View view){
        api = new restful(context, view);
        Button show_temperature = (Button) view.findViewById(R.id.show_Temperature);
        Button show_voltage = (Button) view.findViewById(R.id.show_voltage);
        Button show_fan = (Button) view.findViewById(R.id.show_fan);
        View.OnClickListener click_temperature = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                api.set_context();
                api.get_temperature_sensors();
            }
        };
        View.OnClickListener click_voltage = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                api.set_context();
                api.get_voltage_sensors();
            }
        };
        View.OnClickListener click_fan = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                api.set_context();
                api.get_fan_sensors();
            }
        };
        show_temperature.setOnClickListener(click_temperature);
        show_voltage.setOnClickListener(click_voltage);
        show_fan.setOnClickListener(click_fan);
    }

    private class restful {
        private RequestQueue queue;
        private String header;
        private String ip ="";
        private JsonObjectRequest get_request = null;
        private View fragment_view;
        private TableLayout sensor_table;
        private TableRow sensor_row;
        private restful(Context context, View view) {
            queue = Volley.newRequestQueue(context);
            fragment_view = view;
            sensor_table = (TableLayout) fragment_view.findViewById(R.id.sensor_table);
        }
        private void set_context(){
            TextView token = (TextView) getActivity().findViewById(R.id.token);
            TextView context_ip = (TextView) getActivity().findViewById(R.id.ip_address);
            ip = context_ip.getText().toString();
            header = token.getText().toString();
        }
        private void get_temperature_sensors(){
            final ImageView loading = (ImageView) getActivity().findViewById(R.id.loading);
            loading.setVisibility(View.VISIBLE);
            String url = "https://"+ip+"/redfish/v1/Chassis/1/Thermal";
            JsonObjectRequest get_request = new JsonObjectRequest(Request.Method.GET, url,null , new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    loading.setVisibility(View.INVISIBLE);
                    try {
                        loading.setVisibility(View.INVISIBLE);
                        final TableLayout.LayoutParams tableRowParams= new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT);
                        tableRowParams.setMargins(1, 10, 5, 10);

                        sensor_table.removeAllViews();
                        sensor_row = new TableRow(getActivity());
                        TextView name_name = new TextView (getActivity());
                        TextView reading_name = new TextView (getActivity());
                        TextView status_name = new TextView (getActivity());
                        TextView UpperThresholdNonCritical_name = new TextView (getActivity());
                        TextView UpperThresholdCritical_name = new TextView (getActivity());
                        TextView LowerThresholdNonCritical_name = new TextView (getActivity());
                        TextView LowerThresholdCritical_name = new TextView (getActivity());
                        name_name.setText("   Sensor Name   ");
                        sensor_row.addView(name_name);
                        reading_name.setText("   Reading   ");
                        sensor_row.addView(reading_name);
                        status_name.setText("   Status   ");
                        sensor_row.addView(status_name);
                        UpperThresholdNonCritical_name.setText("   UpperThresholdNonCritical   ");
                        sensor_row.addView(UpperThresholdNonCritical_name);
                        UpperThresholdCritical_name.setText("   UpperThresholdCritical    ");
                        sensor_row.addView(UpperThresholdCritical_name);
                        LowerThresholdNonCritical_name.setText("   LowerThresholdNonCritical   ");
                        sensor_row.addView(LowerThresholdNonCritical_name);
                        LowerThresholdCritical_name.setText("   LowerThresholdCritical   ");
                        sensor_row.addView(LowerThresholdCritical_name);

                        sensor_row.setBackground(getResources().getDrawable(R.drawable.border_all, null));
                        sensor_table.addView(sensor_row);


                        JSONArray temperature_reponse_array = response.getJSONArray("Temperatures");
                        int sensor_data_length = temperature_reponse_array.length();
                        for (int i = 0; i<sensor_data_length; i++) {
                            sensor_row = new TableRow(getActivity());

                            JSONObject one_object = temperature_reponse_array.getJSONObject(i);
                            String name = one_object.getString("Name");
                            TextView name_text = new TextView (getActivity());
                            name_text.setText("   "+name+"   ");
                            sensor_row.addView(name_text);

                            String ReadingCelsius = one_object.getString("ReadingCelsius");
                            TextView reading_text = new TextView (getActivity());
                            reading_text.setText("   " + ReadingCelsius + " C  ");
                            sensor_row.addView(reading_text);

                            JSONObject status_temp = one_object.getJSONObject("Status");
                            String status;
                            try {
                                status = status_temp.getString("Health");
                            }catch (JSONException e){ status = status_temp.getString("State");}
                            TextView status_text = new TextView (getActivity());
                            status_text.setText("   "+status+"   ");
                            sensor_row.addView(status_text);

                            String UpperThresholdNonCritical = one_object.getString("UpperThresholdNonCritical");
                            TextView UpperThresholdNonCritical_text = new TextView (getActivity());
                            UpperThresholdNonCritical_text.setText("   "+UpperThresholdNonCritical+" C  ");
                            sensor_row.addView(UpperThresholdNonCritical_text);

                            String UpperThresholdCritical = one_object.getString("UpperThresholdCritical");
                            TextView UpperThresholdCritical_text = new TextView (getActivity());
                            UpperThresholdCritical_text.setText("   " + UpperThresholdCritical + " C  ");
                            sensor_row.addView(UpperThresholdCritical_text);

                            String LowerThresholdNonCritical = one_object.getString("LowerThresholdNonCritical");
                            TextView LowerThresholdNonCritical_text = new TextView (getActivity());
                            LowerThresholdNonCritical_text.setText("   "+LowerThresholdNonCritical+" C  ");
                            sensor_row.addView(LowerThresholdNonCritical_text);

                            String LowerThresholdCritical = one_object.getString("LowerThresholdCritical");
                            TextView LowerThresholdCritical_text = new TextView (getActivity());
                            LowerThresholdCritical_text.setText("   " + LowerThresholdCritical + " C  ");
                            sensor_row.addView(LowerThresholdCritical_text);

                            sensor_row.setBackground(getResources().getDrawable(R.drawable.border_all, null));
                            sensor_table.addView(sensor_row);
                        }
                    }catch(JSONException e){
                    }
                }
            }, new Response.ErrorListener() {
                @Override public void onErrorResponse(VolleyError e) {get_temperature_sensors();}
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
        private void get_voltage_sensors(){
            final ImageView loading = (ImageView) getActivity().findViewById(R.id.loading);
            loading.setVisibility(View.VISIBLE);
            String url = "https://"+ip+"/redfish/v1/Chassis/1/Power";
            JsonObjectRequest get_request = new JsonObjectRequest(Request.Method.GET, url,null , new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    loading.setVisibility(View.INVISIBLE);
                    try {
                        loading.setVisibility(View.INVISIBLE);
                        final TableLayout.LayoutParams tableRowParams= new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT);
                        tableRowParams.setMargins(1, 10, 5, 10);

                        sensor_table.removeAllViews();
                        sensor_row = new TableRow(getActivity());
                        TextView name_name = new TextView (getActivity());
                        TextView reading_name = new TextView (getActivity());
                        TextView status_name = new TextView (getActivity());
                        TextView UpperThresholdNonCritical_name = new TextView (getActivity());
                        TextView UpperThresholdCritical_name = new TextView (getActivity());
                        TextView LowerThresholdNonCritical_name = new TextView (getActivity());
                        TextView LowerThresholdCritical_name = new TextView (getActivity());
                        name_name.setText("   Sensor Name   ");
                        sensor_row.addView(name_name);
                        reading_name.setText("   Reading   ");
                        sensor_row.addView(reading_name);
                        status_name.setText("   Status   ");
                        sensor_row.addView(status_name);
                        UpperThresholdNonCritical_name.setText("   UpperThresholdNonCritical   ");
                        sensor_row.addView(UpperThresholdNonCritical_name);
                        UpperThresholdCritical_name.setText("   UpperThresholdCritical   ");
                        sensor_row.addView(UpperThresholdCritical_name);
                        LowerThresholdNonCritical_name.setText("   LowerThresholdNonCritical   ");
                        sensor_row.addView(LowerThresholdNonCritical_name);
                        LowerThresholdCritical_name.setText("   LowerThresholdCritical   ");
                        sensor_row.addView(LowerThresholdCritical_name);
                        sensor_row.setBackground(getResources().getDrawable(R.drawable.border_all, null));
                        sensor_table.addView(sensor_row);


                        JSONArray temperature_reponse_array = response.getJSONArray("Voltages");
                        int sensor_data_length = temperature_reponse_array.length();
                        for (int i = 0; i<sensor_data_length; i++) {
                            sensor_row = new TableRow(getActivity());

                            JSONObject one_object = temperature_reponse_array.getJSONObject(i);
                            String name = one_object.getString("Name");
                            TextView name_text = new TextView (getActivity());
                            name_text.setText("   "+name+"   ");
                            sensor_row.addView(name_text);

                            String ReadingVolts = one_object.getString("ReadingVolts");
                            TextView reading_text = new TextView (getActivity());
                            reading_text.setText("   " + ReadingVolts + " V  ");
                            sensor_row.addView(reading_text);

                            JSONObject status_temp = one_object.getJSONObject("Status");
                            String status;
                            try {
                                status = status_temp.getString("Health");
                            }catch (JSONException e){ status = status_temp.getString("State");}
                            TextView status_text = new TextView (getActivity());
                            status_text.setText("   "+status+"   ");
                            sensor_row.addView(status_text);

                            String UpperThresholdNonCritical = one_object.getString("UpperThresholdNonCritical");
                            TextView UpperThresholdNonCritical_text = new TextView (getActivity());
                            UpperThresholdNonCritical_text.setText("   "+UpperThresholdNonCritical+" Volt  ");
                            sensor_row.addView(UpperThresholdNonCritical_text);

                            String UpperThresholdCritical = one_object.getString("UpperThresholdCritical");
                            TextView UpperThresholdCritical_text = new TextView (getActivity());
                            UpperThresholdCritical_text.setText("   " + UpperThresholdCritical + " Volt  ");
                            sensor_row.addView(UpperThresholdCritical_text);

                            String LowerThresholdNonCritical = one_object.getString("LowerThresholdNonCritical");
                            TextView LowerThresholdNonCritical_text = new TextView (getActivity());
                            LowerThresholdNonCritical_text.setText("   "+LowerThresholdNonCritical+" Volt  ");
                            sensor_row.addView(LowerThresholdNonCritical_text);

                            String LowerThresholdCritical = one_object.getString("LowerThresholdCritical");
                            TextView LowerThresholdCritical_text = new TextView (getActivity());
                            LowerThresholdCritical_text.setText("   " + LowerThresholdCritical + " Volt  ");
                            sensor_row.addView(LowerThresholdCritical_text);

                            sensor_row.setBackground(getResources().getDrawable(R.drawable.border_all, null));
                            sensor_table.addView(sensor_row);
                        }
                    }catch(JSONException e){
                    }
                }
            }, new Response.ErrorListener() {
                @Override public void onErrorResponse(VolleyError e) {get_voltage_sensors();}
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
        private void get_fan_sensors(){
            final ImageView loading = (ImageView) getActivity().findViewById(R.id.loading);
            loading.setVisibility(View.VISIBLE);
            String url = "https://"+ip+"/redfish/v1/Chassis/1/Thermal";
            JsonObjectRequest get_request = new JsonObjectRequest(Request.Method.GET, url,null , new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    loading.setVisibility(View.INVISIBLE);
                    try {
                        loading.setVisibility(View.INVISIBLE);
                        final TableLayout.LayoutParams tableRowParams= new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT);
                        tableRowParams.setMargins(1, 10, 5, 10);

                        sensor_table.removeAllViews();
                        sensor_row = new TableRow(getActivity());
                        TextView name_name = new TextView (getActivity());
                        TextView reading_name = new TextView (getActivity());
                        TextView status_name = new TextView (getActivity());
                        TextView UpperThresholdNonCritical_name = new TextView (getActivity());
                        TextView UpperThresholdCritical_name = new TextView (getActivity());
                        TextView LowerThresholdNonCritical_name = new TextView (getActivity());
                        TextView LowerThresholdCritical_name = new TextView (getActivity());
                        name_name.setText("   Fan   ");
                        sensor_row.addView(name_name);
                        reading_name.setText("   Reading   ");
                        sensor_row.addView(reading_name);
                        status_name.setText("   Status   ");
                        sensor_row.addView(status_name);
                        UpperThresholdNonCritical_name.setText("|UpperThresholdNonCritical  ");
                        sensor_row.addView(UpperThresholdNonCritical_name);
                        UpperThresholdCritical_name.setText("   UpperThresholdCritical   ");
                        sensor_row.addView(UpperThresholdCritical_name);
                        LowerThresholdNonCritical_name.setText("   LowerThresholdNonCritical   ");
                        sensor_row.addView(LowerThresholdNonCritical_name);
                        LowerThresholdCritical_name.setText("   LowerThresholdCritical   ");
                        sensor_row.addView(LowerThresholdCritical_name);
                        sensor_row.setBackground(getResources().getDrawable(R.drawable.border_all, null));
                        sensor_table.addView(sensor_row);


                        JSONArray fan_reponse_array = response.getJSONArray("Fans");
                        int sensor_data_length = fan_reponse_array.length();
                        for (int i = 0; i<sensor_data_length; i++) {
                            sensor_row = new TableRow(getActivity());

                            JSONObject one_object = fan_reponse_array.getJSONObject(i);
                            String name = one_object.getString("FanName");
                            TextView name_text = new TextView (getActivity());
                            name_text.setText("   "+name+"  ");
                            sensor_row.addView(name_text);

                            String ReadingVolts = one_object.getString("Reading");
                            TextView reading_text = new TextView (getActivity());
                            reading_text.setText("   " + ReadingVolts + " RPM  ");
                            sensor_row.addView(reading_text);

                            JSONObject status_temp = one_object.getJSONObject("Status");
                            String status;
                            try {
                                status = status_temp.getString("Health");
                            }catch (JSONException e){ status = status_temp.getString("State");}
                            TextView status_text = new TextView (getActivity());
                            status_text.setText("   "+status+"   ");
                            sensor_row.addView(status_text);

                            String UpperThresholdNonCritical = one_object.getString("UpperThresholdNonCritical");
                            TextView UpperThresholdNonCritical_text = new TextView (getActivity());
                            UpperThresholdNonCritical_text.setText("   "+UpperThresholdNonCritical+" RPM  ");
                            sensor_row.addView(UpperThresholdNonCritical_text);

                            String UpperThresholdCritical = one_object.getString("UpperThresholdCritical");
                            TextView UpperThresholdCritical_text = new TextView (getActivity());
                            UpperThresholdCritical_text.setText("   " + UpperThresholdCritical + " RPM  ");
                            sensor_row.addView(UpperThresholdCritical_text);

                            String LowerThresholdNonCritical = one_object.getString("LowerThresholdNonCritical");
                            TextView LowerThresholdNonCritical_text = new TextView (getActivity());
                            LowerThresholdNonCritical_text.setText("   "+LowerThresholdNonCritical+" RPM  ");
                            sensor_row.addView(LowerThresholdNonCritical_text);

                            String LowerThresholdCritical = one_object.getString("LowerThresholdCritical");
                            TextView LowerThresholdCritical_text = new TextView (getActivity());
                            LowerThresholdCritical_text.setText("   " + LowerThresholdCritical + " RPM  ");
                            sensor_row.addView(LowerThresholdCritical_text);

                            sensor_row.setBackground(getResources().getDrawable(R.drawable.border_all, null));
                            sensor_table.addView(sensor_row);
                        }
                    }catch(JSONException e){
                        VolleyLog.d(e.toString());
                    } 
                }
            }, new Response.ErrorListener() {
                @Override public void onErrorResponse(VolleyError e) {get_fan_sensors();}
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
        private void get_cpu_info(){
            String url = "https://"+ip+"/redfish/v1/Chassis/1/Power";
            JsonObjectRequest get_request = new JsonObjectRequest(Request.Method.GET, url,null , new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                }
            }, new Response.ErrorListener() {
                @Override public void onErrorResponse(VolleyError e) {}
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

}

