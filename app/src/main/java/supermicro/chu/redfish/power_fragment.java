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

/**
 * A placeholder fragment containing a simple view.
 */
public class power_fragment extends Fragment {
    public restful api;
    private Button power_on;
    private Button power_off;
    private Button reset;

    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Button power_mode = (Button) getActivity().findViewById(R.id.power_button);
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
                power_control.setVisibility(View.VISIBLE);
                sensor_reading.setVisibility(View.INVISIBLE);
                fan_layout.setVisibility(View.INVISIBLE);
                overall_layout.setVisibility(View.INVISIBLE);
                event_layout.setVisibility(View.INVISIBLE);
                api.set_context();
                api.get_power_mode();
            }
        };
        power_mode.setOnClickListener(nav);
    }

    public power_fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.power_fragment, container, false);
        Context context = getActivity();
        init(context, view);
        return view;
    }

    public void init(final Context context,final View view){
        power_on = (Button) view.findViewById(R.id.power_on);
        power_off = (Button) view.findViewById(R.id.power_off);
        reset = (Button) view.findViewById(R.id.reset);

        api = new restful(context, view);
        View.OnClickListener power_operation = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                api.set_context();
                Button click = (Button) v;
                String operation = click.getText().toString();
                switch (operation) {
                    case "Power On":
                        api.power("ForceOn");
                        api.get_power_mode();
                        break;
                    case "Power Off":
                        api.power("ForceOff");
                        api.get_power_mode();
                        break;
                    case "Reset":
                        api.power("reset");
                        api.get_power_mode();
                        break;
                    case "Nmi":
                        api.power("Nmi");
                        api.get_power_mode();
                        break;
                    case "Graceful Restart":
                        api.power("graceful_restart");
                        api.get_power_mode();
                        break;
                }
            }

        };

        power_on.setOnClickListener(power_operation);
        power_off.setOnClickListener(power_operation);
        reset.setOnClickListener(power_operation);

    }

    private class restful {
        private RequestQueue queue;
        private String header;
        private String ip ="";
        private JsonObjectRequest get_request = null;
        private View fragment_view;
        private TextView display;
        private restful(Context context, View view) {
            queue = Volley.newRequestQueue(context);
            fragment_view = view;
            display = (TextView) fragment_view.findViewById(R.id.display);
        }
        private void set_context(){
            TextView token = (TextView) getActivity().findViewById(R.id.token);
            TextView context_ip = (TextView) getActivity().findViewById(R.id.ip_address);
            ip = context_ip.getText().toString();
            header = token.getText().toString();
        }
        private void get(String url) {
            get_request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    display.setText(response.toString());
                }
            }, new Response.ErrorListener() {@Override public void onErrorResponse(VolleyError error) {}})
            {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("content-type", "application/json");
                    params.put("X-Auth-Token",header);
                    return params;
                }
            };
            queue.add(get_request);
        }
        private void get_power_mode(){
            final ImageView loading = (ImageView) getActivity().findViewById(R.id.loading);
            loading.setVisibility(View.VISIBLE);
            String url = "https://"+ip+"/redfish/v1/Systems/1";
            JsonObjectRequest get_request = new JsonObjectRequest(Request.Method.GET, url,null , new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    loading.setVisibility(View.INVISIBLE);
                    String available_power_mode = "Available Power Mode: \n\t\t\t";
                    String current_power_mode = "Current Power Mode: \t";
                    try {
                        current_power_mode = current_power_mode + response.getString("PowerState");
                    }catch (JSONException e){};
                    display.setText(current_power_mode);
                }
            }, new Response.ErrorListener() {
                @Override public void onErrorResponse(VolleyError e) {get_power_mode();
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
        private void power(String mode){
            final ImageView loading = (ImageView) getActivity().findViewById(R.id.loading);
            loading.setVisibility(View.VISIBLE);
            String url = "https://"+ip+"/redfish/v1/Systems/1/Actions/ComputerSystem.Reset";
            JSONObject jsonBody = new JSONObject();
            try {
                switch (mode)
                {
                    case "on":
                        jsonBody.put("ResetType", "On");
                        break;
                    case "ForceOff":
                        jsonBody.put("ResetType", "ForceOff");
                        break;
                    case "ForceOn":
                        jsonBody.put("ResetType", "ForceOn");
                        break;
                    case "Nmi":
                        jsonBody.put("ResetType", "Nmi");
                        break;
                    case "reset":
                        jsonBody.put("ResetType","ForceRestart");
                        break;
                    case "grace_restart":
                        jsonBody.put("ResetType","GracefulRestart");
                        break;
                }
            }catch (JSONException e){display.setText(e.toString());}

            final JsonObjectRequest get_request = new JsonObjectRequest(Request.Method.POST,url,jsonBody , new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    display.setText("200 OK");
                    loading.setVisibility(View.INVISIBLE);
                }
            }, new Response.ErrorListener() {@Override public void onErrorResponse(VolleyError e) {get_power_mode();}})
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

