package uk.ac.wlv.myblogger1228264;

/**
 * Created by andrew on 17/03/2017.
 */
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class BlogFragment extends Fragment{
    private static final String ARG_BLOG_ID= "blog_id";
    private Blog mBlog;
    TextView mBlogIdField;
    EditText mBlogTitleField;
    EditText mBlogTextField;
    Button mBlogDateButton;
    CheckBox mBlogToDeleteCheckBox;
    ImageView mPhotoView;

    /*implementation of fragment.onCreate()
    * gets blogId from arguments - ARG_BLOG_ID.
    * calls getBlog(int) of MyDBHandler and receives Blog*/
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        int blogId = (int)getArguments().getSerializable(ARG_BLOG_ID);
        MyDBHandler dbHandler = new MyDBHandler(getActivity(),null,null,1);
        SQLiteDatabase db = dbHandler.getWritableDatabase();
        dbHandler.onCreate(db);
        mBlog = dbHandler.getBlog(blogId);
    }

    /*implementation of onCreateView to inflate fragment_blog.xml*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_blog, container, false);
        /* wiring up widgets*/
        mBlogIdField=(TextView) v.findViewById(R.id.blog_id);
        mBlogIdField.setText(String.valueOf(mBlog.get_blogId()));
        mBlogTitleField=(EditText)v.findViewById(R.id.blog_title);
        mBlogTitleField.setText(mBlog.get_blogTitle());
        mBlogTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mBlog.set_blogTitle(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mBlogTextField=(EditText)v.findViewById(R.id.blog_text);
        mBlogTextField.setText(mBlog.get_blogText());
        mBlogTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mBlog.set_blogText(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mBlogDateButton = (Button)v.findViewById(R.id.blog_date);
        String longDate = DateFormat.format("EEE, dd MMM yyyy",mBlog.get_blogDate()).toString();
        mBlogDateButton.setText(longDate);
        mBlogDateButton.setEnabled(false);
        mBlogToDeleteCheckBox = (CheckBox) v.findViewById(R.id.blog_solved);
        boolean checked = false;
        if (mBlog.get_blogSolved()== 1){
            checked = true;
        }
        mBlogToDeleteCheckBox.setChecked(checked);
        mBlogToDeleteCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                mBlog.set_blogSolved(1);
            }
        });
        mPhotoView =(ImageView) v.findViewById(R.id.photo_view);
        mPhotoView.getLayoutParams().height= 120;
        mPhotoView.getLayoutParams().width = 120;
        mPhotoView.setImageDrawable(null);
        mPhotoView.setImageURI(Uri.parse(mBlog.get_blogTitle()));
        return v;
    }
    /* decouples the fragment from the activity. The id is put into a bundle and passed to
    *  the fragment after the fragment has been created.*/
    public static BlogFragment newInstance(int blogId){
        Bundle args = new Bundle();
        args.putSerializable(ARG_BLOG_ID,blogId);
        BlogFragment fragment = new BlogFragment();
        fragment.setArguments(args);
        return fragment;
    }
    public Blog getBlog(){
        return this.mBlog;
    }
}
