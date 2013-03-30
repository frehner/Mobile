package edu.byu.isys413.afreh20.mobapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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
import android.app.Activity;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ViewFlipper;


public class MainActivity extends Activity {
	
	ViewFlipper vf = null;
	
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
//		System.out.println("login");
		EditText usernameText = (EditText) findViewById(R.id.username);
		EditText passwordText = (EditText) findViewById(R.id.password);
		String username = usernameText.getText().toString();
		String password = passwordText.getText().toString();
		
		LoginPosting lposting = new LoginPosting(username, password);
		lposting.execute();
//		showToast("done");
//		vf.setDisplayedChild(1);
	}
	
	public void btntakepicclick(View view){
//		System.out.println("login");
		vf.setDisplayedChild(1);
	}
	
	public void showToast(final String toast) {
		runOnUiThread(new Runnable() {
//			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
			}
		});
	}
	
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

				showToast(respobj.getString("testing"));

//				return S_response;
			} catch (Exception e) {
				showToast("Error");
				Log.v("mytag", e.toString());
			}
			return "failed";
		} /* do in background */

	}

}
