package uk.ac.wlv.myblogger1228264;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by andrew on 18/03/2017.
 */

public class BlogListFragment extends Fragment {
    private RecyclerView mBlogRecyclerView;
    private BlogAdapter mBlogAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_blog_list, container, false);
        mBlogRecyclerView = (RecyclerView) view.findViewById(R.id.blog_recycler_view);
        mBlogRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mBlogRecyclerView.setNestedScrollingEnabled(false);
        updateUI();
        return view;
    }
    /* for keeping track of each blog. Now modified to bind the Blog data to the Boxes and checkbox
    *  created in the list_item_blog.xml*/
    private class BlogHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView mBlogTextTextView;
        private TextView mBlogDateTextView;
        private CheckBox mBlogSolvedCheckBox;
        private Blog mBlog;

        public BlogHolder(View itemView){
            super(itemView);
            // added listeners on the the individual items so that the check box can be used for multi deletes
            mBlogTextTextView = (TextView) itemView.findViewById(R.id.list_item_blog_title_text_view);
            mBlogTextTextView.setOnClickListener(myclickhandler1);
            mBlogDateTextView = (TextView)itemView.findViewById(R.id.list_item_blog_date_text_view);
            mBlogDateTextView.setOnClickListener(myclickhandler1);
            /* this is aimed at getting a larger clickable area. While it works its not that great still*/
            RelativeLayout mLayout = (RelativeLayout) itemView.findViewById(R.id.list_item_relative_layout_view);
            mLayout.setOnClickListener(myclickhandler1);

            mBlogSolvedCheckBox = (CheckBox)itemView.findViewById(R.id.list_item_blog_solved_check_box);
            mBlogSolvedCheckBox.setOnClickListener(myclickhandler2);
        }
        public void bindBlog(Blog blog){
            mBlog = blog;
            mBlogTextTextView.setText(mBlog.get_blogText());
            String longDate = DateFormat.format("EEE, dd MMM yyyy",mBlog.get_blogDate()).toString();
            mBlogDateTextView.setText(longDate);

            /* this doesn't sem to be working.*/
            int delete = mBlog.get_blogSolved();
            if (delete == 0){
                mBlogSolvedCheckBox.setChecked(false);
            }else{
                mBlogSolvedCheckBox.setChecked(true);
            }
        }
        /* onClick will create an intent of opening a new BlogActivity. The id of the
        *  Blog clicked and the context will be passed to newIntent() and the avtivity
        *  (the BlogPagerActivity) will be started*/
        @Override
        public void onClick(View v){
            Intent intent = BlogPagerActivity.newIntent(getActivity(),mBlog.get_blogId());
            startActivity(intent);
        }
        /* Creates new intent of creating BlogPager. BlogPagerActivity then creates and opens the new BlogPagerActivity*/
        View.OnClickListener myclickhandler1 = new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = BlogPagerActivity.newIntent(getActivity(),mBlog.get_blogId());
                startActivity(intent);
            }
        };
        /* sets checkbox checked when the method has completed. Sets value of blog_solved to one to be ready for deletion.
        *  Passes blog to setDelete*/
        View.OnClickListener myclickhandler2 = new View.OnClickListener() {
            public void onClick(View v) {
                mBlogSolvedCheckBox.setChecked(true);
                mBlog.set_blogSolved(1);
                setDelete(mBlog);
            }
        };
    }
    /* adapter object to hold the list of blog objects.
    *  acts as interpreter of objects for the recycler view*/
    private class BlogAdapter extends RecyclerView.Adapter<BlogHolder> {
        private List<Blog> mBlogs;

        public BlogAdapter(List<Blog> blogs){
            mBlogs = blogs;
        }
        /* filling/inflating the view and wrapping it by passing to BlogHolder*/
        @Override
        public BlogHolder onCreateViewHolder(ViewGroup parent, int viewType){
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_blog, parent, false);
            return new BlogHolder(view);
        }
        /* getting the text for the title from each blog in mBlogs for each blogholder.
        * * the blog object is bound to the view by passing its title to the view holder's TextView*/
        @Override
        public void onBindViewHolder(BlogHolder holder, int position){
            Blog blog = mBlogs.get(position);
            holder.bindBlog(blog);
        }
        @Override
        public int getItemCount(){
            return mBlogs.size();
        }
    }
    public void setDelete(Blog blog){
        MyDBHandler dbHandler = new MyDBHandler(getActivity(), null, null, 1);
        /* the database method returns a boolean that could be used for something like a message*/
        dbHandler.setDelete(blog);
    }
    /* Creates new dbhandler using this activity as context. calls dbHandler.getBlogs()
    *   and receives list of Blogs. Passes Blog list to new BlogAdapter & then this
    *   blogadptor to recyclerview*/
    /*  could also need some sort of popup if changes have been made but update() not been called from the button click*/
    private  void updateUI(){
        MyDBHandler dbHandler = new MyDBHandler(getActivity(), null, null, 1);
        List<Blog> blogs = dbHandler.getBlogs();
        mBlogAdapter = new BlogAdapter(blogs);
        mBlogRecyclerView.setAdapter(mBlogAdapter);
    }
    /* to repopulate list when updates have been made*/
    @Override
    public void onResume(){
        super.onResume();
        updateUI();
    }
}
