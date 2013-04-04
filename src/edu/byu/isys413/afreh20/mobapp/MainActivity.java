package edu.byu.isys413.afreh20.mobapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ViewFlipper;

import edu.byu.isys413.afreh20.mystuff.*;


public class MainActivity extends Activity {
	
	ViewFlipper vf = null;
	String custid = null;
	String email = null;
	boolean loggedIn = false;
	ArrayList<HashMap<String, String>> custPics = new ArrayList<HashMap<String, String>>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		vf = (ViewFlipper)findViewById(R.id.vf);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void loginbtnclick(View view){
		try{
			System.out.println("login");
			EditText usernameText = (EditText) findViewById(R.id.username);
			EditText passwordText = (EditText) findViewById(R.id.password);
			String username = usernameText.getText().toString();
			String password = passwordText.getText().toString();
			
			LoginPosting lposting = new LoginPosting(username, password);
			lposting.execute();
//			showToast("done");
			if(lposting.get().equals("success")){
				vf.setDisplayedChild(1);
			}

		} catch (Exception e){
			e.printStackTrace();
			showToast("Something failed");
		}
//		
	}
	
	public void btntakepicclick(View view){
//		System.out.println("login");
		vf.setDisplayedChild(0);
	}
	
	public void showToast(final String toast) {
		runOnUiThread(new Runnable() {
//			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	/**
	 * Async posts the login info to the server
	 * @author Anthony
	 *
	 */
	private class LoginPosting extends AsyncTask<String, Void, String> {
		private String username = null;
		private String password = null;
//		String customer_Password = null;
//		String customer_id = null;
//		String customer_accountNum = null;
		
		public LoginPosting(String username, String password) {
			this.username = username;
			this.password = password;
		}

		/**
		 * The portion of the asynchronous task that runs in the background
		 */
		protected String doInBackground(String... image) {
			try {
				// Create a new HttpClient and Post Header
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost("http://10.0.2.2:8080/WebCode/edu.byu.isys413.afreh20.actions.Login.action");

				// setting up the nameVaule pairs
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("ismobile", "true"));
				nameValuePairs.add(new BasicNameValuePair("username", username));
				nameValuePairs.add(new BasicNameValuePair("password", password));

				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				HttpResponse response = null;

				response = httpclient.execute(httppost);

				HttpEntity e = response.getEntity();

				JSONObject respobj = new JSONObject(EntityUtils.toString(e));

				// pulling the balance from the JSON object and posting it to the class variable string object "balance"
//				balance = respobj.getString("balance");

				showToast(respobj.getString("status"));
				if(respobj.getString("status").equals("Success")){
					
					//Time to parse some json
					
					
					loggedIn = true;
					custid = respobj.getString("custid");
					email = respobj.getString("username");
//					Log.v("jsonTag", respobj.getString("pics"));
					JSONArray picsJson = new JSONArray(respobj.getString("pics"));
					for(int i = 0; i < picsJson.length(); i++){
						JSONObject temppic = picsJson.getJSONObject(i);
//						Log.v("jsonTag", temppic.getString("picname"));
						HashMap<String, String> tempPicMap = new HashMap<String, String>();
						tempPicMap.put("id", temppic.getString("id"));
						tempPicMap.put("caption", temppic.getString("caption"));
						tempPicMap.put("picname", temppic.getString("picname"));
						tempPicMap.put("pic", temppic.getString("pic"));
						//this way I can tell which pictures have been saved to our db and which haven't.
						tempPicMap.put("alreadyInDB", "true");
						custPics.add(tempPicMap);
					}
					
					return "success";
				} else{
					return "failure";
				}

//				return S_response;
			} catch (Exception e) {
				showToast("Error on app side");
				Log.v("mytag", e.toString());
			}
			return "failed";
		} /* do in background */

	}

}
