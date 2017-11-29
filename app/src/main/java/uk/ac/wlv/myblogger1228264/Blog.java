package uk.ac.wlv.myblogger1228264;

import java.util.Date;

/**
 * Created by andrew on 17/03/2017.
 */

public class Blog {
    private Integer _blogId;
    private String _blogTitle;
    private String _blogText;
    private Date _blogDate;
    /* at present it is undecided what this bool will be for. ??? It will go with a tick box ??*/
    private int _blogSolved;

    /*constructor no parameters*/
    public Blog(){
        _blogDate =new Date();
    }
    public Blog(String title, String text){
        _blogTitle = title;
        _blogText = text;
        _blogDate =new Date();
        _blogSolved=0;
    }
    /* for getBlogs() of MyDBHandler*/
    public Blog(int id, String title, String text, String date, int solved){
        _blogId = id;
        _blogTitle = title;
        _blogText = text;
        /* not using the passed parameter*/
        _blogSolved = 0;
        // date is passed as string so for the present is not being dealt with
        _blogDate =new Date();
    }
    /* constructor takes id as parameter. NOT BEING USED. this is a hook for future get next id from database*/
    public Blog(Integer id){
        _blogId =id;
        _blogDate =new Date();
        _blogSolved=0;
    }

    /* Getters & Setters*/
    public Integer get_blogId() {
        return _blogId;
    }

    public void set_blogId(Integer _blogId) {
        this._blogId = _blogId;
    }

    public String get_blogTitle() {
        return _blogTitle;
    }

    public void set_blogTitle(String _blogTitle) {
        this._blogTitle = _blogTitle;
    }

    public String get_blogText() {
        return _blogText;
    }

    public void set_blogText(String _blogText) {
        this._blogText = _blogText;
    }

    public Date get_blogDate() {
        return _blogDate;
    }

    public void set_blogDate(Date _blogDate) {
        this._blogDate = _blogDate;
    }

    public int get_blogSolved() {
            return _blogSolved;
    }
    public void set_blogSolved(int blogSolved) {
        this._blogSolved = blogSolved;
    }
}
