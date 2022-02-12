package com.alexbordei.mqttsmsgateway;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class HTTPHelper {
    String response;
    boolean isResponseReady = false;
    Context context;

    public boolean isResponseReady() {
        return this.isResponseReady;
    }

    public String getResponse() {
        return this.response;
    }
    public HTTPHelper(Context context) {
        this.context = context;
    }
    public void sendSMSStatus(Integer id, String status, String errorMessage) {
        RequestQueue queue = Volley.newRequestQueue(this.context);
        String url ="https://ba69-5-12-26-77.ngrok.io/api/sms/" + id.toString();

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    // response
                    Log.d("Response", response);
                },
                error -> {
                    // error
                    Log.d("Error.Response", String.valueOf(error));
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("status", status);
                params.put("error", errorMessage);

                return params;
            }


            @Override
            public Map<String, String> getHeaders() {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Accept", "application/json");
                params.put("Authorization", "Bearer u8SXbu8qsWuNETpjynaZErDFZIdHErUozVNtxVW2GJnlPJeuTdrGZebKqllg");

                return params;
            }
        };
        queue.add(postRequest);
    }
    public void updateSMSServiceStatus() {
        RequestQueue queue = Volley.newRequestQueue(this.context);
        String url ="https://ba69-5-12-26-77.ngrok.io/api/service/sms";

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    // response
                    Log.d("Response", response);
                },
                error -> {
                    // error
                    Log.d("Error.Response", String.valueOf(error));
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Accept", "application/json");
                params.put("Authorization", "Bearer u8SXbu8qsWuNETpjynaZErDFZIdHErUozVNtxVW2GJnlPJeuTdrGZebKqllg");

                return params;
            }
        };
        queue.add(postRequest);
    }
    public void getSMSQueue() {
        isResponseReady = false;
        String url = "https://ba69-5-12-26-77.ngrok.io/api/sms";
        RequestQueue queue = Volley.newRequestQueue(this.context);

        StringRequest getRequest = new StringRequest(Request.Method.GET, url,
                rsp -> {
                    // response
                    Log.d("Response", rsp);
                    isResponseReady = true;
                    response = rsp;
                },
                error -> {
                    // error
                    Log.d("Error.Response", String.valueOf(error));
                    isResponseReady = true;
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Accept", "application/json");
                params.put("Authorization", "Bearer u8SXbu8qsWuNETpjynaZErDFZIdHErUozVNtxVW2GJnlPJeuTdrGZebKqllg");

                return params;
            }
        };
        queue.add(getRequest);
    }
}
