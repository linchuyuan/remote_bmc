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
import android.widget.TextView;

import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class fan_mode_fragment extends Fragment {
    public restful api;
    private Button full_speed_button;
    private Button optimal_speed_button;
    private Button heavy_io_button;
    private Button standard_button;
    private TextView fan_display;


    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Button fan_mode = (Button) getActivity().findViewById(R.id.fan_button);
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
                sensor_reading.setVisibility(View.INVISIBLE);
                fan_layout.setVisibility(View.VISIBLE);
                overall_layout.setVisibility(View.INVISIBLE);
                event_layout.setVisibility(View.INVISIBLE);
                api.set_context();
                api.get_fan_mode();
                }
        };
        fan_mode.setOnClickListener(nav);
    }

    public fan_mode_fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fan_mode_fragment, container, false);
        Context context = getActivity();
        init(context, view);
        return view;
    }

    public void init(final Context context,final View view){
        api = new restful(context, view);
        fan_display = (TextView) view.findViewById(R.id.fan_display);
        full_speed_button = (Button) view.findViewById(R.id.mode_full_speed);
        optimal_speed_button = (Button) view.findViewById(R.id.mode_optimal_speed);
        heavy_io_button = (Button) view.findViewById(R.id.mode_heavy_speed);
        standard_button = (Button) view.findViewById(R.id.mode_standard);

        View.OnClickListener fan_operation = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = 0;
                api.set_context();
                Button click = (Button) v;
                String operation = click.getText().toString();
                switch (operation) {
                    case "Full Speed":
                        api.power("Full Speed");
                        api.get_fan_mode();
                        break;
                    case "Optimal Speed":
                        api.power("Optimal Speed");
                        api.get_fan_mode();
                        break;
                    case "Heavy IO":
                        api.power("Heavy IO");
                        api.get_fan_mode();
                        break;
                    case "Standard":
                        api.power("Standard");
                        api.get_fan_mode();
                        break;
                }
            }

        };
        full_speed_button.setOnClickListener(fan_operation);
        optimal_speed_button.setOnClickListener(fan_operation);
        heavy_io_button.setOnClickListener(fan_operation);
        standard_button.setOnClickListener(fan_operation);
    }

    private class restful {
        private RequestQueue queue;
        private String header;
        private String ip ="";
        private JsonObjectRequest get_request = null;
        private View fan_mode_view;
        private TextView display;
        private restful(Context context, View view) {
            queue = Volley.newRequestQueue(context);
            fan_mode_view = view;
            display = (TextView) fan_mode_view.findViewById(R.id.fan_display);
        }
        private void set_context(){
            TextView token = (TextView) getActivity().findViewById(R.id.token);
            TextView context_ip = (TextView) getActivity().findViewById(R.id.ip_address);
            ip = context_ip.getText().toString();
            header = token.getText().toString();
        }
        private void get_fan_mode(){
            final ImageView loading = (ImageView) getActivity().findViewById(R.id.loading);
            loading.setVisibility(View.VISIBLE);
            String url = "https://"+ip+"/redfish/v1/Chassis/1";
            JsonObjectRequest get_request = new JsonObjectRequest(Request.Method.GET, url,null , new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    loading.setVisibility(View.INVISIBLE);
                    JSONObject tempObject = null;
                    String available_fan_mode = "Available Fan Mode: \n\t\t\t";
                    String current_fan_mode = "Current Fan Mode: \n";
                    JSONArray array = null;
                    try{
                        tempObject = response.getJSONObject("Oem");
                        tempObject = tempObject.getJSONObject("OemFan");
                        current_fan_mode = current_fan_mode + "\t\t\t" + tempObject.getString("FanMode");
                        array = tempObject.getJSONArray("FanMode@Redfish.AllowableValues");
                        for (int i = 0; i < array.length();i++){
                            available_fan_mode = available_fan_mode + "\"" + array.getString(i)+ "\"" +" ";
                        }
                    }catch (JSONException e){display.setText(e.toString());}
                    display.setText(current_fan_mode + "\n\n\n" + available_fan_mode);
                }
            }, new Response.ErrorListener() {
                @Override public void onErrorResponse(VolleyError e) {get_fan_mode();}
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
        private void power(String mode){
            final ImageView loading = (ImageView) getActivity().findViewById(R.id.loading);
            loading.setVisibility(View.VISIBLE);
            String url = "https://"+ip+"/redfish/v1/Chassis/1/";
            JSONObject jsonBody = null;
            try {
                switch (mode)
                {
                    case "Standard":
                        JSONObject temp_1111 = new JSONObject();
                        JSONObject temp_2222 = new JSONObject();
                        temp_2222.put("OemFan",temp_1111);
                        temp_1111.put("FanMode","Standard");
                        jsonBody = new JSONObject();
                        jsonBody.put("Oem",temp_2222);
                        break;
                    case "Full Speed":
                        JSONObject temp_1 = new JSONObject();
                        JSONObject temp_2 = new JSONObject();
                        temp_2.put("OemFan",temp_1);
                        temp_1.put("FanMode","FullSpeed");
                        jsonBody = new JSONObject();
                        jsonBody.put("Oem",temp_2);
                        break;
                    case "Optimal Speed":
                        JSONObject temp_11 = new JSONObject();
                        JSONObject temp_22 = new JSONObject();
                        temp_22.put("OemFan",temp_11);
                        temp_11.put("FanMode","Optimal");
                        jsonBody = new JSONObject();
                        jsonBody.put("Oem",temp_22);
                        break;
                    case "Heavy IO":
                        JSONObject temp_111 = new JSONObject();
                        JSONObject temp_222 = new JSONObject();
                        temp_222.put("OemFan",temp_111);
                        temp_111.put("FanMode","HeavyIO");
                        jsonBody = new JSONObject();
                        jsonBody.put("Oem",temp_222);
                        break;
                }
            }catch (JSONException e){display.setText(e.toString());}
            display.setText(jsonBody.toString());
            JsonObjectRequest get_request = new JsonObjectRequest(Request.Method.PATCH, url,jsonBody , new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    get_fan_mode();
                    loading.setVisibility(View.INVISIBLE);
                }
            }, new Response.ErrorListener() {@Override public void onErrorResponse(VolleyError e) {get_fan_mode();loading.setVisibility(View.INVISIBLE);}})
            {
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