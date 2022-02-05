package com.alexbordei.mqttsmsgateway;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class HTTPHelper {
    Context context;
    public HTTPHelper(Context context) {
        this.context = context;
    }
    public void sendSMSStatus(Integer id, String status, String errorMessage) {
        RequestQueue queue = Volley.newRequestQueue(this.context);
        String url ="https://4c98-2a02-2f00-300c-b700-a8bc-6232-fdad-a84e.ngrok.io/api/sms/" + id.toString();

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
}
