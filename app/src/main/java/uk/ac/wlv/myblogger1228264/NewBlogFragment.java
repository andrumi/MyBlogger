package uk.ac.wlv.myblogger1228264;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.facebook.share.widget.ShareButton;


/**
 * Created by andrew on 25/03/2017.
 */

public class NewBlogFragment extends Fragment {
    EditText mBlogTextField;
    Button mBlogAddButton;
    ImageButton mBlogPhotoButton;
    ImageView mImageView;
    private Blog mBlog = new Blog();
    public Uri mFileUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        BlogListActivity myActivity =(BlogListActivity)getActivity();
        View v = inflater.inflate(R.layout.fragment_new_blog, container, false);
        /* wiring up widgets*/
        mBlogTextField=(EditText) v.findViewById(R.id.new_blog_text);
        mBlogTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mBlog.set_blogText(s.toString());
                /* this below set title keeps going with the ontextchanged round so needs to be moved*/
               // mBlog.set_blogTitle("No title");
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mBlogAddButton = (Button) v.findViewById(R.id.add_new_blog_button);
        mBlogAddButton.setEnabled(true);

        // all camera stuff. Camera should be extracted to separate class
        mBlogPhotoButton = (ImageButton) v.findViewById((R.id.photo_button));
        mBlogPhotoButton.setEnabled(true);

        if (!hasCamera()) {
            mBlogPhotoButton.setEnabled(false);
        }
        if (savedInstanceState != null) {
            mImageView = (ImageView) v.findViewById(R.id.photo_imageview);
            mImageView.getLayoutParams().height = 120;
            mImageView.getLayoutParams().width = 120;
            mImageView.setImageDrawable(null);
            mFileUri = myActivity.getFileUri();

            mImageView.setImageURI(mFileUri);
        }
        Button galleryButton = (Button) v.findViewById(R.id.gallery_button);
        galleryButton.setEnabled(true);
        ShareButton shareButton = (ShareButton)v.findViewById(R.id.fb_share_button);
        shareButton.setEnabled(false);
       /* if(mFileUri !=null){
            try{
                Bitmap image = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), mFileUri);

                SharePhoto photo = new SharePhoto.Builder()
                        .setBitmap(image)
                        .build();
                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();
                shareButton.setEnabled(true);
                shareButton.setShareContent(content);
            }catch(Exception e){
                String exception = e.toString();
            }

        }*/

        return v;
    }
    // This camera business should be extracted to separate class
        /* verify the device has a camera*/
    private boolean hasCamera(){
        return getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

}
