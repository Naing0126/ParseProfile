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
    private ParseObject mUserProfile;
    private ParseFile mImageFile;

    TextView mTextCloud;
    ParseImageView mParsedImage;
    EditText mEditName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextCloud = (TextView)findViewById(R.id.textCloud);
        mParsedImage = (ParseImageView)findViewById(R.id.parsedImage);
        mEditName = (EditText)findViewById(R.id.editName);

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, getString(R.string.parse_application_id), getString(R.string.parse_client_key));

        // Cloud Code testing

        // hello
        ParseCloud.callFunctionInBackground("hello", new HashMap<String, Object>(), new FunctionCallback<String>() {
            public void done(String result, ParseException e) {
                if (e == null) {
                    Log.d(TAG, "Retrieved : " + result);
                    mTextCloud.setText(result);
                }
            }
        });

        // averageStars
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("movie", "The Matrix");
        ParseCloud.callFunctionInBackground("averageStars", params, new FunctionCallback<String>() {
            public void done(String ratings, ParseException e) {
                if (e == null) {
                    // ratings is 4.5
                    mTextCloud.setText(ratings);
                }else{
                    mTextCloud.setText(e.getMessage());
                }
            }
        });


        // Load User Profile object
        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserProfile");
        query.getInBackground("xcyfI3wRJG", new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    mUserProfile = object;
                    Log.d(TAG, "Retrieved " + mUserProfile.getString("userName"));
                    mEditName.setText(mUserProfile.getString("userName"));

                    ParseFile parsedFile = mUserProfile.getParseFile("photo");
                    if(parsedFile!=null){
                        mParsedImage.setParseFile(parsedFile);
                        mParsedImage.loadInBackground(new GetDataCallback() {
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

        mParsedImage.setOnClickListener(new View.OnClickListener() {
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
                    mImageFile = new ParseFile("photo.jpg", scaledData);
                    mImageFile.saveInBackground(new SaveCallback() {
                            public void done(ParseException e) {
                            if (e != null) {
                                Log.d(TAG, "put image error! " + e.getMessage());
                            } else {
                                // put image file
                                mUserProfile.put("photo", mImageFile);
                                mUserProfile.saveInBackground();
                                // reload image file from parse
                                ParseFile photoFile = mUserProfile.getParseFile("photo");
                                if(photoFile != null){
                                    mParsedImage.setParseFile(mUserProfile.getParseFile("photo"));
                                    mParsedImage.loadInBackground(new GetDataCallback() {
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
