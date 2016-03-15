package com.example.alex.logintutorial;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.*;
import com.twitter.sdk.android.core.identity.*;
import com.twitter.sdk.android.core.models.User;

import java.io.InputStream;
import java.net.URL;

import io.fabric.sdk.android.Fabric;

public class LoginActivity extends AppCompatActivity {

    private static final String TWITTER_KEY = "Ql4J1REB1P5oj5whbgq50JQ6C";
    private static final String TWITTER_SECRET = "AVu59z8F9WrmnrMoIPOl6duvDMcldPBuPxouIITucRspssUt3M";
    private TwitterLoginButton loginButton;
    private String twitter_name,twitter_id,t_full_name, t_profile_image;
    private ImageView twitterProfileImage;

    TextView mainTextView;

    ProgressDialog progress;
    ProgressDialog pDialog;

    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);

        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_login);

        loginButton = (TwitterLoginButton) findViewById(R.id.twitterLoginButton);
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                TwitterSession session = result.data;

                twitter_name = session.getUserName();
                twitter_id = session.getUserId() + "";
                String info = "Nombre de usuario: "+twitter_name +"\nUsuario ID: "+ twitter_id+"\n";

                mainTextView = (TextView) findViewById(R.id.mainTextView);
                mainTextView.setText(info);

                loginButton.setVisibility(View.GONE);

                TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
                twitterApiClient.getAccountService().verifyCredentials(false, false, new Callback<User>() {
                    @Override
                    public void success(Result<User> userResult) {
                        t_full_name = userResult.data.name;
                        t_profile_image = userResult.data.profileImageUrl;

                        mainTextView.append("Nombre completo: "+t_full_name);

                        twitterProfileImage = (ImageView) findViewById(R.id.twitterProfileImage);
                        new LoadImage().execute(t_profile_image);
                        twitterProfileImage.setImageURI(Uri.parse(t_profile_image));

                    }

                    @Override
                    public void failure(TwitterException e) {
                        progress.dismiss();
                    }
                });

            }

            @Override
            public void failure(TwitterException exception) {
                Log.d("TwitterKit", "Login with Twitter failure", exception);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        loginButton.onActivityResult(requestCode, resultCode, data);
    }

    private class LoadImage extends AsyncTask<String, String, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("Loading Image ....");
            pDialog.show();

        }
        protected Bitmap doInBackground(String... args) {
            try {
                bitmap = BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent());

            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap image) {

            if(image != null){
                twitterProfileImage.setImageBitmap(image);
                pDialog.dismiss();

            }else{

                pDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Image Does Not exist or Network Error", Toast.LENGTH_SHORT).show();

            }
        }
    }

}
