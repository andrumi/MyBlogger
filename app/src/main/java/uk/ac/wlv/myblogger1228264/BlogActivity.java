package uk.ac.wlv.myblogger1228264;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Date;

public class BlogActivity extends FragmentActivity {
    public static final String EXTRA_BLOG_ID ="uk.ac.wlv.uk.ac.wlv.myblogger1228264.blog_id";
    TextView mBlogIdField;
    EditText mBlogTitleField;
    EditText mBlogTextField;
    EditText mResultTextField;
    CheckBox mBlogSolved;
    Button mBlogDateButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common);
        FragmentManager fragmentManager = getSupportFragmentManager();
        // fragment_container - activity_blog.xml
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (fragment == null){
            int blogId = 1;//(int) getIntent().getSerializableExtra(EXTRA_BLOG_ID);
            fragment = BlogFragment.newInstance(blogId);
            //add fragment to fragment_container - that is the xml that belongs to this BlogActivity
            fragmentManager.beginTransaction().add(R.id.fragment_container, fragment).commit();
        }
    }
    /* The context that has been passed is packaged in a new intent with the BlogActivity class,
    *   this new intent has an extra added of key value wirth the id of the Blog to open
    *   in the BlogActivity*/
    public static Intent newIntent(Context packageContext, int blogId){
        Intent intent = new Intent(packageContext,BlogActivity.class);
        intent.putExtra(EXTRA_BLOG_ID,blogId);
        return intent;
    }

    /* Called from button click. Inserts new Blog to database via MyDBHandler addBlog()*/
    public void newBlog(View view){
        /* The button id/button on fragment_blog.xml is calling this method on the activity.
        *  The following four lines are getting the data from the fragment.*/
        Fragment fragment=(Fragment)getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        View fragmentView =fragment.getView();
       // mBlogTitleField =(EditText) fragmentView.findViewById(R.id.blog_title);
        mBlogTextField =(EditText) fragmentView.findViewById(R.id.blog_text);
        // probably need to capture date and blogSolved here.
        MyDBHandler dbHandler = new MyDBHandler(this, null, null, 1);
        //String title = mBlogTitleField.getText().toString();
        String text = mBlogTextField.getText().toString();

        // is this necessary?????????????
        view.findViewById(R.id.fragment_container);
        Blog blog = new Blog("",text);
        dbHandler.addBlog(blog);
        mBlogTitleField.setText("");
        mBlogTextField.setText("");
    }
    public void updateBlog(View view){
        Fragment fragment=(Fragment)getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        View fragmentView = fragment.getView();
        mBlogIdField= (TextView) fragmentView.findViewById(R.id.blog_id);
        mBlogTitleField =(EditText) fragmentView.findViewById(R.id.blog_title);
        mBlogTextField =(EditText) fragmentView.findViewById(R.id.blog_text);
        int id = Integer.parseInt(mBlogIdField.getText().toString());
        String title = mBlogTitleField.getText().toString();
        String text = mBlogTextField.getText().toString();
        Date date = new Date();
        /* this will need to be changed possibly if this method will be used to set delete*/
        int delete =0;
        MyDBHandler dbHandler = new MyDBHandler(this, null, null, 1);
        /*unnecessary returning this blog. would be better to return result
        * maybe?*/
        Blog blog = new Blog();
        blog =dbHandler.editProduct(id,title,text,date.toString(),delete);
        ((EditText) fragmentView.findViewById(R.id.result_text)).setText("Record Updated");
    }
    public void removeBlog(View view){
        // need extract method get fragmentView()?
        Fragment fragment=(Fragment)getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        View fragmentView = fragment.getView();
        mBlogIdField= (TextView) fragmentView.findViewById(R.id.blog_id);
        MyDBHandler dbHandler = new MyDBHandler(this,null,null,1);

        int id = Integer.parseInt(mBlogIdField.getText().toString());
        boolean result = dbHandler.deleteBlog(id);
        if (result){

            /* can extract method clearData()?*/
            ((EditText) fragmentView.findViewById(R.id.blog_title)).setText("");
            ((EditText) fragmentView.findViewById(R.id.blog_text)).setText("");
            ((TextView) fragmentView.findViewById(R.id.blog_id)).setText("");
            ((Button) fragmentView.findViewById(R.id.blog_date)).setText("");
            ((CheckBox) fragmentView.findViewById(R.id.blog_solved)).setChecked(false);
            ((EditText) fragmentView.findViewById(R.id.result_text)).setText("Record Deleted!");
        }else{
            ((EditText) fragmentView.findViewById(R.id.result_text)).setText("No Match Found.");
        }
    }
    public void lookupBlog(View view){
        Fragment fragment=(Fragment)getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        View fragmentView = fragment.getView();

        mBlogTitleField =(EditText) fragmentView.findViewById(R.id.blog_title);
        MyDBHandler dbHandler = new MyDBHandler(this, null,null,1);
        Blog blog = dbHandler.findBlog(mBlogTitleField.getText().toString());
        if (blog != null){
            ((EditText) fragmentView.findViewById(R.id.blog_title)).setText(blog.get_blogTitle());
            ((EditText) fragmentView.findViewById(R.id.blog_text)).setText(blog.get_blogText());
            ((TextView) fragmentView.findViewById(R.id.blog_id)).setText(blog.get_blogId().toString());
            ((Button) fragmentView.findViewById(R.id.blog_date)).setText(blog.get_blogDate().toString());
            // !!!!!!!!!!!!!! the Blog object does not have a value for blogSolved-probably
            //((CheckBox) fragmentView.findViewById(R.id.blog_solved)).setChecked(blog.get_blogSolved());
            ((EditText) fragmentView.findViewById(R.id.result_text)).setText("Record Found");
        }else {
            ((EditText) fragmentView.findViewById(R.id.result_text)).setText("No Match Found.");
        }
    }
}
