package com.csye6225.model;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Response {
    private String response;

    public Response(String response) {
        this.response=response;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }


    public static String jsonString(String resp){
        try {
            Response response = new Response(resp);
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(response);
            return jsonString;
        }
        catch(Exception ex) {
            return null;
        }
    }
}
