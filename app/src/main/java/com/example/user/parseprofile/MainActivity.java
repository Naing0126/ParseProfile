package com.example.user.parseprofile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    final int REQ_CODE_SELECT_IMAGE=100;
    final String TAG="naing";
    private ParseObject userProfile;
    private ParseFile imageFile;

    TextView text_cloud;
    ParseImageView parsed_image;
    EditText edit_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text_cloud = (TextView)findViewById(R.id.textCloud);
        parsed_image = (ParseImageView)findViewById(R.id.parsedImage);
        edit_name = (EditText)findViewById(R.id.editName);

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "53EzGw5jV91ZTkHmSlCkXi0gb862SicN5kBNvKsg", "E3gLYj2pu6gTDa95avzeEsXsajEyx73gprF6RMga");

        // Cloud Code
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("movie", "The Matrix");
        ParseCloud.callFunctionInBackground("averageStars", params, new FunctionCallback<Float>() {
            public void done(Float ratings, ParseException e) {
                if (e == null) {
                    // ratings is 4.5
                    text_cloud.setText("rating is "+ratings);
                }else{
                    text_cloud.setText(e.getMessage());
                }
            }
        });
        /*
        ParseCloud.callFunctionInBackground("hello", new HashMap<String, Object>(), new FunctionCallback<String>() {
            public void done(String result, ParseException e) {
                if (e == null) {
                    // result is "Hello world!"
                    text_cloud.setText(result);
                }
            }
        });
        */
        // Load User Profile object
        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserProfile");
        query.getInBackground("xcyfI3wRJG", new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    userProfile = object;
                    Log.d(TAG, "Retrieved " + userProfile.getString("userName"));
                    edit_name.setText(userProfile.getString("userName"));

                    ParseFile parsedFile = userProfile.getParseFile("photo");
                    if(parsedFile!=null){
                        parsed_image.setParseFile(parsedFile);
                        parsed_image.loadInBackground(new GetDataCallback() {
                            @Override
                            public void done(byte[] bytes, ParseException e) {

                            }
                        });
                    }

                } else {
                    // something went wrong
                    Log.d(TAG, "cannot find the user");
                }
            }
        });

        parsed_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQ_CODE_SELECT_IMAGE);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQ_CODE_SELECT_IMAGE)
        {
            if(resultCode== Activity.RESULT_OK)
            {
                try {

                    final Bitmap image_bitmap 	= MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());

                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    image_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);

                    byte[] scaledData = bos.toByteArray();

                    // Save the scaled image to Parse
                    imageFile = new ParseFile("photo.jpg", scaledData);
                    imageFile.saveInBackground(new SaveCallback() {
                            public void done(ParseException e) {
                            if (e != null) {
                                Log.d(TAG, "put image error! " + e.getMessage());
                            } else {
                                // put image file
                                userProfile.put("photo", imageFile);
                                userProfile.saveInBackground();
                                // reload image file from parse
                                ParseFile photoFile = userProfile.getParseFile("photo");
                                if(photoFile != null){
                                    parsed_image.setParseFile(userProfile.getParseFile("photo"));
                                    parsed_image.loadInBackground(new GetDataCallback() {
                                        @Override
                                        public void done(byte[] bytes, ParseException e) {

                                        }
                                    });
                                }

                            }
                        }
                    });

                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Log.d(TAG, "Error: " + e.getMessage());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Log.d(TAG, "Error: " + e.getMessage());
                } catch (Exception e)
                {
                    e.printStackTrace();
                    Log.d(TAG, "Error: " + e.getMessage());
                }
            }
        }else{
            Log.d(TAG, "get image error!");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
