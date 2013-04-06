package edu.byu.isys413.afreh20.mobapp;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
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

import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Editable;
import android.util.Base64;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import edu.byu.isys413.afreh20.mystuff.*;


public class MainActivity extends Activity {
	
	ViewFlipper vf = null;
	String custid = null;
	String email = null;
	boolean loggedIn = false;
	ArrayList<HashMap<String, String>> custPics = new ArrayList<HashMap<String, String>>();
	ArrayList<String> picNames = new ArrayList<String>();
	private static final int TAKE_PHOTO_CODE = 1;
	String tempPic64;
	String newUploadId;
	String tempViewPic;
	
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
//			System.out.println("login");
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
	}
	
	public void savepicclick(View view){
		try{
			EditText picNameText = (EditText) findViewById(R.id.picname);
			EditText captionText = (EditText) findViewById(R.id.caption);
			String picName = picNameText.getText().toString();
			String caption = captionText.getText().toString();
			if(picName.trim().matches("") || caption.trim().matches("")){
				showToast("Needs name and caption");
			} else {
//				showToast("good!");
				boolean isNewPic = true;
				UploadPicture upPic = new UploadPicture(picName, caption, tempPic64, custid, isNewPic);
				upPic.execute();
				if(upPic.get().equals("success")){
//					vf.setDisplayedChild(1);
					showToast("upload worked");
					picNames.add(picName);
					HashMap<String, String> reallyTemp = new HashMap<String, String>();
					reallyTemp.put("id", newUploadId);
					reallyTemp.put("caption", caption);
					reallyTemp.put("picname", picName);
					reallyTemp.put("pic", tempPic64);
					custPics.add(reallyTemp);
					viewalbum(view);
				} else {
					showToast("upload failed");
				}
			}
		}catch (Exception e){
			e.printStackTrace();
			showToast("Failed to save");
		}
	}

	private File getTempFile(Context context) {
		// it will return /sdcard/image.tmp
		final File path = new File(Environment.getExternalStorageDirectory(), context.getPackageName());
		if (!path.exists()) {
			path.mkdir();
		}
		return new File(path, "image.tmp");
	}
	
	public void btntakepicclick(View view){
		final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempFile(this)));
		startActivityForResult(intent, TAKE_PHOTO_CODE);
//			vf.setDisplayedChild(0);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == TAKE_PHOTO_CODE) {
			File file = getTempFile(this);
			try {
//				System.out.println("camera stuff");
//				showToast("camera success?");
				Bitmap captureBmp = Media.getBitmap(getContentResolver(), Uri.fromFile(file));
				// ImageView viewer = (ImageView) findViewById(R.id.pictureViewer);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				// viewer.setImageBitmap(captureBmp);
				InputStream in = new FileInputStream(file);
				int count;
				byte[] buffer = new byte[512];
				while ((count = in.read(buffer)) >= 0) {
					out.write(buffer, 0, count);
				}
				tempPic64 = Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);
				vf.setDisplayedChild(3);
				ImageView imgView = (ImageView) findViewById(R.id.imageView);
				imgView.setImageBitmap(captureBmp);
				
				
				
			} catch (Exception e){
				e.printStackTrace();
				showToast("fail in camera");
			}
		}
	}

	public void viewalbum(View view){
		vf.setDisplayedChild(2);
//		if(picNames.size() == 0){
//			picNames.add("No pics uploaded yet");
//		}
		ListView listView = (ListView) findViewById(R.id.album);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, picNames);
		listView.setAdapter(adapter);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		listView.setItemsCanFocus(false);
	}
	
	public void showpicturepage(View view){
		for(HashMap<String, String> onePic : custPics){
			if(onePic.get("picname").equals(tempViewPic)){
				vf.setDisplayedChild(4);
				EditText picNameText = (EditText) findViewById(R.id.existpicname);
				EditText captionText = (EditText) findViewById(R.id.existcaption);
				picNameText.setText(onePic.get("picname"));
				captionText.setText(onePic.get("caption"));
				byte[] imageAsBytes = Base64.decode(onePic.get("pic").getBytes(), 0); 
			    ImageView image = (ImageView)this.findViewById(R.id.existimageView);
			    image.setImageBitmap(
			            BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length)
			    );
			}
		}
	}
	
	public void viewpicture(View view){
		ListView listView = (ListView) findViewById(R.id.album);
		SparseBooleanArray checked = listView.getCheckedItemPositions();
		boolean atLeastOne = false;
        for (int i = 0; i < checked.size(); i++) {
            if(checked.valueAt(i) == true) {
                String checkedout = (String) listView.getItemAtPosition(checked.keyAt(i));
//                Log.i("xxxx", i + " " + checkedout);
                tempViewPic = checkedout;
                showpicturepage(view);
                atLeastOne = true;
                break;
            }
        }
        if(!atLeastOne){
        	showToast("Select a picture");
        }
        
	}
	
	public void purchasepics(View view){
		ListView listView = (ListView) findViewById(R.id.album);
		SparseBooleanArray checked = listView.getCheckedItemPositions();
        for (int i = 0; i < checked.size(); i++) {
            if(checked.valueAt(i) == true) {
                String checkedout = (String) listView.getItemAtPosition(checked.keyAt(i));
//                Log.i("xxxx", i + " " + checkedout);
            }
        }
	}
	
	public void showToast(final String toast) {
		runOnUiThread(new Runnable() {
//			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	private class UploadPicture extends AsyncTask<String, Void, String> {
		private String inName;
		private String inCaption;
		private String inPic;
		private String inCustId;
		private boolean inIsNewPic;
		
		public UploadPicture(String nameIn, String captionIn, String picIn, String custIdIn, boolean isNewPicIn){
			this.inName = nameIn;
			this.inCaption = captionIn;
			this.inPic = picIn;
			this.inCustId = custIdIn;
			this.inIsNewPic = isNewPicIn;
		}
		
		protected String doInBackground(String... image) {
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost("http://10.0.2.2:8080/WebCode/edu.byu.isys413.afreh20.actions.SavePicture.action");

				// setting up the nameVaule pairs
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("picname", inName));
				nameValuePairs.add(new BasicNameValuePair("caption", inCaption));
				nameValuePairs.add(new BasicNameValuePair("pic", inPic));
				nameValuePairs.add(new BasicNameValuePair("custId", inCustId));
				if(inIsNewPic){
					nameValuePairs.add(new BasicNameValuePair("newpic", "true"));
				}else {
					nameValuePairs.add(new BasicNameValuePair("newpic", "false"));
				}
				
				
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				HttpResponse response = null;

				response = httpclient.execute(httppost);

				HttpEntity e = response.getEntity();

				JSONObject respobj = new JSONObject(EntityUtils.toString(e));

				// pulling the balance from the JSON object and posting it to the class variable string object "balance"
//				balance = respobj.getString("balance");

				showToast(respobj.getString("status"));
				if(respobj.getString("status").equals("Success")){
					newUploadId = respobj.getString("newId");
					return "success";
				} else{
					return "failure";
				}
				
			} catch (Exception e){
				e.printStackTrace();
				showToast("Failed to upload");
			}
			return "failed";
		}
		
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
						picNames.add(temppic.getString("picname"));
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
