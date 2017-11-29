package uk.ac.wlv.myblogger1228264;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareButton;

import java.io.File;
import java.util.UUID;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by andrew on 18/03/2017.
 */

public class BlogListActivity extends FragmentActivity implements OnClickListener{
    /*twitter stuff/*/
    	/* Shared preference keys */
    private static final String PREF_NAME = "sample_twitter_pref";
    private static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    private static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
    private static final String PREF_KEY_TWITTER_LOGIN = "is_twitter_loggedin";
    private static final String PREF_USER_NAME = "twitter_user_name";

    /* Any number for uniquely distinguish your request */
    public static final int WEBVIEW_REQUEST_CODE = 100;

    private ProgressDialog pDialog;

    private static Twitter twitter;
    private static RequestToken requestToken;

    private static SharedPreferences mSharedPreferences;

    private EditText mShareEditText;
    private TextView userName;
    private View loginLayout;
    private View shareLayout;

    private String consumerKey = null;
    private String consumerSecret = null;
    private String callbackUrl = null;
    private String oAuthVerifier = null;
    /* end twitter stuff*/

    EditText mBlogTextField;
    Fragment blogListFragment;
    Fragment newBlogFragment;
    ShareButton shareButton;
    Button mBlogAddButton;
    ImageView imageView;
    private static final int PHOTO_CAPTURE = 102;
    private static final int TWITTER_LOGON = 103;
    private static final int SELECT_PICTURE = 101;
    public Uri fileUri;
    private String fileUriString;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* next three lines creating database if not exists. possible extract method????*/
        MyDBHandler dbHandler = new MyDBHandler(this, null, null, 1);
        SQLiteDatabase db = dbHandler.getWritableDatabase();
        dbHandler.onCreate(db);

       // setContentView(R.layout.activity_common);
/*twitter stuff*/
        		/* initializing twitter parameters from string.xml */
        initTwitterConfigs();

		/* Enabling strict mode */
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

		/* Setting activity layout file */
        setContentView(R.layout.activity_common);

        loginLayout = (RelativeLayout) findViewById(R.id.login_layout);
        shareLayout = (LinearLayout) findViewById(R.id.share_layout);
//        mShareEditText = (EditText) findViewById(R.id.share_text);
        userName = (TextView) findViewById(R.id.user_name);

		/* register button click listeners */
        findViewById(R.id.btn_login).setOnClickListener(this);
        findViewById(R.id.btn_share).setOnClickListener(this);

		/* Check if required twitter keys are set */
        if (TextUtils.isEmpty(consumerKey) || TextUtils.isEmpty(consumerSecret)) {
            Toast.makeText(this, "Twitter key and secret not configured",
                    Toast.LENGTH_SHORT).show();
            return;
        }

		/* Initialize application preferences */
        mSharedPreferences = getSharedPreferences(PREF_NAME, 0);

        boolean isLoggedIn = mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);

		/*  if already logged in, then hide login layout and show share layout */
        if (isLoggedIn) {
            loginLayout.setVisibility(View.GONE);
            shareLayout.setVisibility(View.VISIBLE);

            String username = mSharedPreferences.getString(PREF_USER_NAME, "");
            userName.setText(getResources ().getString(R.string.hello)
                    + username);

        } else {
            loginLayout.setVisibility(View.VISIBLE);
            shareLayout.setVisibility(View.GONE);

            Uri uri = getIntent().getData();

            if (uri != null && uri.toString().startsWith(callbackUrl)) {

                String verifier = uri.getQueryParameter(oAuthVerifier);

                try {

					/* Getting oAuth authentication token */
                    AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);

					/* Getting user id form access token */
                    long userID = accessToken.getUserId();
                    final User user = twitter.showUser(userID);
                    final String username = user.getName();

					/* save updated token */
                    saveTwitterInfo(accessToken);

                    loginLayout.setVisibility(View.GONE);
                    shareLayout.setVisibility(View.VISIBLE);
                    userName.setText(getString(R.string.hello) + username);

                } catch (Exception e) {
                    //to do
                }
            }

        }
        /* end twitter business*/


        FragmentManager fragmentManager = getSupportFragmentManager();
        newBlogFragment = fragmentManager.findFragmentById(R.id.fragment_container_new_blog);
        if (newBlogFragment == null) {
            newBlogFragment = new NewBlogFragment();
            fragmentManager.beginTransaction().add(R.id.fragment_container_new_blog, newBlogFragment).commit();

        }
        blogListFragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (blogListFragment == null) {
            blogListFragment = new BlogListFragment();
            fragmentManager.beginTransaction().add(R.id.fragment_container, blogListFragment).commit();
        }
        Button btnDelete = (Button) this.findViewById(R.id.delete_cheked);
        btnDelete.setEnabled(true);

        if (savedInstanceState != null) {
            // this is untidy and caused many problems as the key in the bundle has the same name as the string. Problem caused by not
            // putting the key in quotes and rebuilding the key with the value of the string.
            fileUriString = savedInstanceState.getString("fileUriString");
            if (fileUriString != null) {
                fileUri = Uri.parse(fileUriString);
            }
        }
    }

    public Uri getFileUri() {
        return this.fileUri;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (fileUri != null) {
            savedInstanceState.putString("fileUriString", fileUri.toString());
        }
    }

    public void getFromGallery(View view){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), SELECT_PICTURE);
    }

    /* new blog method called from on click of add button of newblogfragment*/
    public void newBlog(View view) {
        Fragment fragment = (Fragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container_new_blog);
        View fragmentView = fragment.getView();
        mBlogTextField = (EditText) fragmentView.findViewById(R.id.new_blog_text);
        MyDBHandler dbHandler = new MyDBHandler(this, null, null, 1);
        String text = mBlogTextField.getText().toString();
        // there is no text edit for title, could create new constructor possibly. *SHOULD*
        // the title of the blog will be discontinued but the space could be used for photo
        String title = "" + fileUri;
        view.findViewById(R.id.new_blog_fragment_container);
        Blog blog = new Blog(title, text);
        dbHandler.addBlog(blog);
        // refreshing the list
        this.blogListFragment.onResume();
        // could possibly change this to a toast, or not as a hint is ok
        mBlogTextField.setText(null);
        // the string value really shoulf be set in values
        mBlogTextField.setHint("Anything more to say?");
        ImageView imageView = (ImageView) fragmentView.findViewById(R.id.photo_imageview);
        // this leaves a space. the image view still has the same size but with nothing in.
/*        imageView.getLayoutParams().height= 120;
        imageView.getLayoutParams().width = 120;*/
        imageView.setImageDrawable(null);
        imageView.setImageURI(null);

    }

    /* Calls dbHandler method to delete any blogs that have the delblog value of 1*/
    public void deleteBlogsPlural(View view) {
        MyDBHandler dbHandler = new MyDBHandler(this, null, null, 1);
        boolean result = dbHandler.deleteBlogsPlural();
        if (result = true) {
            this.blogListFragment.onResume();
        } else {
            // not deleted messanger needed
        }
        dbHandler.close();
    }

    // this camera business all needs to be extracted
    public void takePhoto(View view) {
        // this is all a bit much possibly for naming photos but they are unique
        UUID photoIdentifier;
        photoIdentifier = UUID.randomUUID();
        String photoName = photoIdentifier.toString();
        File mediaFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + photoName + ".jpg");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = Uri.fromFile(mediaFile);


        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, PHOTO_CAPTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_CAPTURE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Photo has been saved to:\n" + fileUri, Toast.LENGTH_LONG).show();

                Fragment fragment = (Fragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container_new_blog);
                View fragmentView = fragment.getView();

                imageView = (ImageView) fragmentView.findViewById(R.id.photo_imageview);
                imageView.getLayoutParams().height = 120;
                imageView.getLayoutParams().width = 120;
                imageView.setImageDrawable(null);

                imageView.setImageURI(fileUri);
                setShare();

            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Photo capture cancelled.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Failed to take photo", Toast.LENGTH_LONG).show();
            }
        }


        if (requestCode == WEBVIEW_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            String verifier = data.getExtras().getString(oAuthVerifier);
            try {
                AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);

                long userID = accessToken.getUserId();
                final User user = twitter.showUser(userID);
                String username = user.getName();

                saveTwitterInfo(accessToken);

                loginLayout.setVisibility(View.GONE);
                shareLayout.setVisibility(View.VISIBLE);
                userName.setText(BlogListActivity.this.getResources().getString(
                        R.string.hello) + username);

            } catch (Exception e) {
                Log.e("Twitter Login Failed", e.getMessage());
            }
        }
        if (requestCode == SELECT_PICTURE){
            if (resultCode == RESULT_OK) {
                Uri selectedImageUri = data.getData();
                fileUri = selectedImageUri;
                Fragment fragment = (Fragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container_new_blog);
                View fragmentView = fragment.getView();

                imageView = (ImageView) fragmentView.findViewById(R.id.photo_imageview);
                imageView.getLayoutParams().height = 120;
                imageView.getLayoutParams().width = 120;
                imageView.setImageDrawable(null);

                imageView.setImageURI(fileUri);
                setShare();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
    /* Builds content of Facebook share button*/
    public void setShare() {
        Fragment fragment = (Fragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container_new_blog);
        View fragmentView = fragment.getView();
        shareButton = (ShareButton) fragmentView.findViewById(R.id.fb_share_button);
        if (fileUri != null) {
            try {
                Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), fileUri);

                SharePhoto photo = new SharePhoto.Builder()
                        .setBitmap(image)
                        .build();
                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();
                shareButton.setEnabled(true);
                shareButton.setShareContent(content);
            } catch (Exception e) {
                 String exception = e.toString();
            }
        }
    }
    /* twitter business*/
    /**
     * Saving user information, after user is authenticated for the first time.
     * You don't need to show user to login, until user has a valid access toen
     */
    private void saveTwitterInfo(AccessToken accessToken) {

        long userID = accessToken.getUserId();

        User user;
        try {
            user = twitter.showUser(userID);

            String username = user.getName();

			/* Storing oAuth tokens to shared preferences */
            Editor e = mSharedPreferences.edit();
            e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
            e.putString(PREF_KEY_OAUTH_SECRET, accessToken.getTokenSecret());
            e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
            e.putString(PREF_USER_NAME, username);
            e.commit();

        } catch (TwitterException e1) {
            e1.printStackTrace();
        }
    }

    /* Reading twitter essential configuration parameters from strings.xml */
    private void initTwitterConfigs() {
        consumerKey = getString(R.string.twitter_consumer_key);
        consumerSecret = getString(R.string.twitter_consumer_secret);
        callbackUrl = getString(R.string.twitter_callback);
        oAuthVerifier = getString(R.string.twitter_oauth_verifier);
    }


    private void loginToTwitter() {
        boolean isLoggedIn = mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);

        if (!isLoggedIn) {
            final ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(consumerKey);
            builder.setOAuthConsumerSecret(consumerSecret);

            final Configuration configuration = builder.build();
            final TwitterFactory factory = new TwitterFactory(configuration);
            twitter = factory.getInstance();

            try {
                requestToken = twitter.getOAuthRequestToken(callbackUrl);

                /**
                 *  Loading twitter login page on webview for authorization
                 *  Once authorized, results are received at onActivityResult
                 *  */
                final Intent intent = new Intent(this, WebViewActivity.class);
                intent.putExtra(WebViewActivity.EXTRA_URL, requestToken.getAuthenticationURL());
                startActivityForResult(intent, WEBVIEW_REQUEST_CODE);

            } catch (TwitterException e) {
                e.printStackTrace();
            }
        } else {

            loginLayout.setVisibility(View.GONE);
            shareLayout.setVisibility(View.VISIBLE);
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//
//        if (resultCode == Activity.RESULT_OK) {
//            String verifier = data.getExtras().getString(oAuthVerifier);
//            try {
//                AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
//
//                long userID = accessToken.getUserId();
//                final User user = twitter.showUser(userID);
//                String username = user.getName();
//
//                saveTwitterInfo(accessToken);
//
//                loginLayout.setVisibility(View.GONE);
//                shareLayout.setVisibility(View.VISIBLE);
//                userName.setText(BlogListActivity.this.getResources().getString(
//                        R.string.hello) + username);
//
//            } catch (Exception e) {
//                Log.e("Twitter Login Failed", e.getMessage());
//            }
//        }
//
//        super.onActivityResult(requestCode, resultCode, data);
//    }
    /** onClick set for either login to twitter or share contents of blog text*/
    @Override
    public void onClick(View v) {
        Fragment fragment = (Fragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container_new_blog);
        View fragmentView = fragment.getView();
        mBlogTextField = (EditText) fragmentView.findViewById(R.id.new_blog_text);
        switch (v.getId()) {
            case R.id.btn_login:
                loginToTwitter();
                break;
            case R.id.btn_share:
               // final String status = mShareEditText.getText().toString();// to do change this to contents of
                final String status = mBlogTextField.getText().toString();
                if (status.trim().length() > 0) {
                    new updateTwitterStatus().execute(status);
                } else {
                    Toast.makeText(this, "Message is empty!!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    class updateTwitterStatus extends AsyncTask<String, String, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(BlogListActivity.this);
            pDialog.setMessage("Posting to twitter...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected Void doInBackground(String... args) {

            String status = args[0];
            try {
                ConfigurationBuilder builder = new ConfigurationBuilder();
                builder.setOAuthConsumerKey(consumerKey);
                builder.setOAuthConsumerSecret(consumerSecret);

                // Access Token
                String access_token = mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, "");
                // Access Token Secret
                String access_token_secret = mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, "");

                AccessToken accessToken = new AccessToken(access_token, access_token_secret);
                Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);

                // Update status
                StatusUpdate statusUpdate = new StatusUpdate(status);
//                InputStream is = getResources().openRawResource(R.drawable.lakeside_view);
//                statusUpdate.setMedia("test.jpg", is);

                twitter4j.Status response = twitter.updateStatus(statusUpdate);

                Log.d("Status", response.getText());

            } catch (TwitterException e) {
                Log.d("Failed to post!", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

			/* Dismiss the progress dialog after sharing */
            pDialog.dismiss();

            Toast.makeText(BlogListActivity.this, "Posted to Twitter!", Toast.LENGTH_SHORT).show();

            // Clearing EditText field
            mBlogTextField.setText("");
        }

    }
}