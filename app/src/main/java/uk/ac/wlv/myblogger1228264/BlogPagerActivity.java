package uk.ac.wlv.myblogger1228264;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

/**
 * Created by andrew on 21/03/2017.
 */

public class BlogPagerActivity extends FragmentActivity {
    private static final String EXTRA_BLOG_ID = "uk.ac.wlv.uk.ac.wlv.myblogger1228264.blog_id";
    private ViewPager mViewPager;
    private List<Blog> mBlogs;

    /* Creates and returns intent with blogId as extra*/
    public static Intent newIntent(Context packageContext, int blogId){
        Intent intent = new Intent(packageContext, BlogPagerActivity.class);
        intent.putExtra(EXTRA_BLOG_ID, blogId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_pager);
        int blogId = (int) getIntent().getSerializableExtra(EXTRA_BLOG_ID);
        doSetAdapter();

        for (int i=0;i< mBlogs.size();i++){
            if (mBlogs.get(i).get_blogId().equals(blogId)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }
    /* extracted this method from onCreate so it could be reused by delete blog method. Though it is still doing more than one thing*/
    private void doSetAdapter() {
        mViewPager=(ViewPager)findViewById(R.id.activity_blog_pager_view_pager);
        MyDBHandler dbHandler= new MyDBHandler(this, null, null, 1);
        mBlogs= dbHandler.getBlogs();
        FragmentManager fragmentManager =getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            /* fetches the blog from a given position in the list*/
            @Override
            public Fragment getItem(int position) {
                Blog blog = mBlogs.get(position);
                return BlogFragment.newInstance(blog.get_blogId());
            }
            @Override
            public int getCount() {
                return mBlogs.size();
            }
        } );
    }

    public void updateBlog(View view){
        TextView mBlogIdField= (TextView) this.mViewPager.findViewById(R.id.blog_id);
        EditText mBlogTitleField =(EditText) this.mViewPager.findViewById(R.id.blog_title);
        EditText mBlogTextField =(EditText) this.mViewPager.findViewById(R.id.blog_text);
        Button mBlogDateButton = (Button) this.mViewPager.findViewById(R.id.blog_date);
        int id = Integer.parseInt(mBlogIdField.getText().toString());
        String title = mBlogTitleField.getText().toString();
        String text = mBlogTextField.getText().toString();
        String date =mBlogDateButton.getText().toString();
        /* this will need to be changed possibly if this method will be used to set delete*/
        int delete =0;
        MyDBHandler dbHandler = new MyDBHandler(this, null, null, 1);
        /*unnecessary returning this blog as the text is already there. would be better to return result
        * maybe?*/
        Blog blog = new Blog();
        blog =dbHandler.editProduct(id,title,text,date,delete);
        ((EditText) this.mViewPager.findViewById(R.id.result_text)).setText("Record Updated");
    }
    /* Called from button click. Inserts new Blog to database via MyDBHandler addBlog()*/
    public void newBlog(View view){
        EditText mBlogTextField =(EditText) this.mViewPager.findViewById(R.id.blog_text);
        EditText mBlogTitleField =(EditText) this.mViewPager.findViewById(R.id.blog_title);
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
    /* this search is working. Though it may be nicer if it searched by blogtext value, rather than title.
    *  Possibly searching for a Like. Though this would need some way to handle more than one returned row*/
    public void lookupBlog(View view){
        EditText mBlogTitleField =(EditText) this.mViewPager.findViewById(R.id.blog_title);
        MyDBHandler dbHandler = new MyDBHandler(this, null,null,1);
        Blog blog = dbHandler.findBlog(mBlogTitleField.getText().toString());
        if (blog != null){
            ((EditText) this.mViewPager.findViewById(R.id.blog_title)).setText(blog.get_blogTitle());
            ((EditText) this.mViewPager.findViewById(R.id.blog_text)).setText(blog.get_blogText());
            ((TextView) this.mViewPager.findViewById(R.id.blog_id)).setText(blog.get_blogId().toString());
            ((Button) this.mViewPager.findViewById(R.id.blog_date)).setText(blog.get_blogDate().toString());
            // !!!!!!!!!!!!!! the Blog object does not have a value for blogSolved-probably
            /* It does now though how this might help? The blog solved/delete would generally be false.*/
            //((CheckBox) fragmentView.findViewById(R.id.blog_solved)).setChecked(blog.get_blogSolved());
            ((EditText) this.mViewPager.findViewById(R.id.result_text)).setText("Record Found");
        }else {
            ((EditText) this.mViewPager.findViewById(R.id.result_text)).setText("No Match Found.");
        }
    }
    public void removeBlog(View view){
        TextView mBlogIdField= (TextView) this.mViewPager.findViewById(R.id.blog_id);
        MyDBHandler dbHandler = new MyDBHandler(this,null,null,1);
        int id = Integer.parseInt(mBlogIdField.getText().toString());
        boolean result = dbHandler.deleteBlog(id);
        if (result){
            /* The above clear fields are redundant now as the adapter of the view pager is now rebuilding the viewpager list
            *  This is the only way that the view finder would work with any more deletes (that I could find)*/
             doSetAdapter();

        }else{
            ((EditText) this.mViewPager.findViewById(R.id.result_text)).setText("No Match Found.");
        }
    }
}
