package fr.sio1.boiteoutil;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class RequetteHttp {
    public static enum Method{
        POST,PUT,DELETE,GET;
    }
    private URL url;
    private HttpURLConnection con;
    private OutputStream os;
    //After instantiation, when opening connection - IOException can occur
    public RequetteHttp(URL url)throws IOException {
        this.url=url;
        con = (HttpURLConnection)this.url.openConnection();
    }
    //Can be instantiated with String representation of url, force caller to check for IOException which can be thrown
    public RequetteHttp(String url)throws IOException{ this(new URL(url)); Log.d("TestJSON", url); }

    /**
     * Sending connection and opening an output stream to server by pre-defined instance variable url
     *
     * @param //isPost boolean - indicates whether this request should be sent in POST method
     * @throws IOException - should be checked by caller
     * */
    private void prepareAll(Method method)throws IOException{
        con.setDoInput(true);
        con.setRequestMethod(method.name());
        if(method==Method.POST||method==Method.PUT){
            con.setDoOutput(true);
            os = con.getOutputStream();
        }
    }
    //prepare request in GET method
    //@return HttpRequest this instance -> for chaining method @see line 22
    public RequetteHttp prepare() throws IOException{
        prepareAll(Method.GET);
        return this;
    }
    /**
     * Prepares HttpRequest method with for given method, possible values: HttpRequest.Method.POST,
     * HttpRequest.Method.PUT, HttpRequest.Method.GET & HttpRequest.Method.DELETE
     *
     * @param method HttpRequest.Method - nested enum HttpRequest.Method constant
     * @return HttpRequest this instance -> for chaining method @see line 22
     * @throws IOException - should be checked by caller
     * */
    public RequetteHttp prepare(RequetteHttp.Method method)throws IOException{
        prepareAll(method);
        return this;
    }
    /**
     * Adding request headers (standard format "Key":"Value")
     *
     * @param headers String variadic params in standard format "Key":"Value"
     * @return HttpRequest this instance -> for chaining method @see line 22
     * */
    public RequetteHttp withHeaders(String... headers){
        for(int i=0,last=headers.length;i<last;i++) {
            String[]h=headers[i].split("[:]");
            con.setRequestProperty(h[0],h[1]);
        }
        return this;
    }

    /**
     * Writes query to open stream to server
     *
     * @param query String params in format of key1=v1&key2=v2 to open stream to server
     * @return HttpRequest this instance -> for chaining method @see line 22
     * @throws IOException - should be checked by caller
     * */
    public RequetteHttp withData(String query) throws IOException{
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(query);
        writer.close();
        return this;
    }
    /**
     * Builds query on format of key1=v1&key2=v2 from given hashMap structure
     * for map: {name=Bubu, age=29} -> builds "name=Bubu&age=29"
     * for map: {Iam=Groot} -> builds "Iam=Groot"
     *
     * @param params HashMap consists of key-> value pairs to build query from
     * @return HttpRequest this instance -> for chaining method @see line 22
     * @throws IOException - should be checked by caller
     * */
    public RequetteHttp withData(HashMap<String,String> params) throws IOException{
        StringBuilder result=new StringBuilder();
        for(Map.Entry<String,String>entry : params.entrySet()){
            result.append((result.length()>0?"&":"")+entry.getKey()+"="+entry.getValue());//appends: key=value (for first param) OR &key=value(second and more)
            Log.d("TestJSON",entry.getKey()+"  ===>  "+ entry.getValue());
        }
        withData(result.toString());
        return this;
    }
    //When caller only need to send, and don't need String response from server
    public int send() throws IOException{
        return con.getResponseCode(); //return HTTP status code to indicate whether it successfully sent
    }
    /**
     * Sending request to the server and pass to caller String as it received in response from server
     *
     * @return String printed from server's response
     * @throws IOException - should be checked by caller
     * */
    public String sendAndReadString() throws IOException {
        BufferedReader br=new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder response=new StringBuilder();
        for(String line;(line=br.readLine())!=null;)response.append(line+"\n");
        Log.d("TestJSON",response.toString());
        return response.toString();
    }
    /**
     * Sending request to the server and pass to caller its raw contents in bytes as it received from server.
     *
     * @return byte[] from server's response
     * @throws IOException - should be checked by caller
     * */
    public byte[] sendAndReadBytes() throws IOException{
        byte[] buffer = new byte[8192];
        InputStream is = con.getInputStream();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (int bytesRead;(bytesRead=is.read(buffer))>=0;)output.write(buffer, 0, bytesRead);
        return output.toByteArray();
    }
    //JSONObject representation of String response from server
    public JSONObject sendAndReadJSON() throws JSONException, IOException{
        return new JSONObject(sendAndReadString());
    }
}
