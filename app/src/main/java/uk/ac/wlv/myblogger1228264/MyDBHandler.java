package uk.ac.wlv.myblogger1228264;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.DateFormat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by andrew on 18/03/2017.
 */

public class MyDBHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "blogDB.db";
    public static final String TABLE_BLOGS = "blogs";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_TEXT = "text";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_DELETE = "delblog";

    public MyDBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db){
        //just in case the database changes
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BLOGS);


        String CREATE_BLOGS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_BLOGS +
                "(" + COLUMN_ID + " INTEGER PRIMARY KEY,"
                    + COLUMN_TITLE + " TEXT,"
                    + COLUMN_TEXT  + " TEXT,"
                    + COLUMN_DATE  + " DATE,"
                    + COLUMN_DELETE + " INTEGER" + ")";
        db.execSQL(CREATE_BLOGS_TABLE);
//        db.close();
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BLOGS);
        onCreate(db);

    }
    public void addBlog (Blog blog){
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, blog.get_blogTitle());
        values.put(COLUMN_TEXT, blog.get_blogText());
        /* cant put date in SQLite*/
        String longDate = DateFormat.format("EEE, dd MMM yyyy",blog.get_blogDate()).toString();
        values.put(COLUMN_DATE, longDate);
        values.put(COLUMN_DELETE,0);
        SQLiteDatabase db = this.getWritableDatabase();
        // !!!!!!!!!!!!!!! this was for testing
/*        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BLOGS);*/
        //onCreate(db);
        try {
            db.insert(TABLE_BLOGS, null, values);
        }catch (Exception e){
            Log.d(TAG, e.toString());
        }

        db.close();
    }
    /*Gets all rows with all data from database. Loops cursor - putting each attribute
    *  into a Blog object. Returns a List of Blog objects when query has finished running.*/
    public List<Blog> getBlogs(){
        ArrayList<Blog> mBlogs= new ArrayList<>();

        SQLiteDatabase db = this.getWritableDatabase();
        /* !!!!!!!!!!!!!!1 there possibly needs to be a catch here if the database does not exist. !!!!!!!!!!*/
        String query = "Select * FROM "+TABLE_BLOGS + " ORDER BY "+ COLUMN_ID +" DESC";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do{
                Blog blog = new Blog(
                        Integer.parseInt(cursor.getString(0)),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),// ####!! at present just returning string. Needs to be Date!!!!!
                        cursor.getInt(4)
                );
                mBlogs.add(blog);
           } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return mBlogs;
    }
    public boolean deleteBlogsPlural() {
        boolean result = false;
        String query = "Select * FROM " + TABLE_BLOGS + " WHERE " + COLUMN_DELETE + " = 1 ";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Blog blog = new Blog();
                blog.set_blogId(Integer.parseInt(cursor.getString(0)));
                db.delete(TABLE_BLOGS, COLUMN_ID + " = ?",
                        new String[]{String.valueOf(blog.get_blogId())});

                result = true;
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return result;
    }
    public Blog getBlog(int blogId){
        Blog blog = new Blog();
        String query ="SELECT * FROM "+TABLE_BLOGS+" WHERE "+ COLUMN_ID + "= "+ blogId;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            blog = new Blog(
                    Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getInt(4)
            );
        }
        cursor.close();
        db.close();
        return blog;
    }
    public Blog editProduct(int id, String title, String text, String date, int delete){
        SQLiteDatabase db = this.getWritableDatabase();

        Blog blog = new Blog();
        ContentValues values = new ContentValues();
        int newId = id;
        values.put(COLUMN_TITLE,title);
        values.put(COLUMN_TEXT, text);
        values.put(COLUMN_DATE,date);
        //!!!!!!!!!!!!!!!!!! cant seem to put integer into values and insert to db ??????????
        values.put(COLUMN_DELETE,Integer.toString(delete));

        db.update(TABLE_BLOGS, values," _id = " + newId , null);

        // this line below would need find method
       // blog = findBlog(id);
        db.close();
        return blog;
    }
    public Blog findBlog(int blogId) {
        String query = "Select * FROM " + TABLE_BLOGS + " WHERE "
                + COLUMN_ID + " = " + blogId ;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        Blog blog = new Blog();
        if (cursor.moveToFirst()) {
            cursor.moveToFirst();
            blog.set_blogId(Integer.parseInt(cursor.getString(0)));
            blog.set_blogTitle(cursor.getString(1));
            blog.set_blogText(cursor.getString(2));

            // this is all so wrong trying to parse date
            Date date = new Date();
            SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy");
            try {
                //exception caused by unparsable date
                date = df.parse(cursor.getString(3));
            }catch (Exception e){
                 Log.d(TAG, "Date parse gone wrong");
            }
            blog.set_blogDate(date);
            /// the above seems v. wrong?????
            blog.set_blogDate(date);
            int solved =cursor.getInt(4);
            if (solved ==0){
                blog.set_blogSolved(0);
            }else{
                blog.set_blogSolved(1);
            }

            cursor.close();
        } else {
            blog = null;
        }
        db.close();
        return blog;
    }
    public void setDelete(Blog blog){
        // can possibly put this all back to remove the hard query
/*       ContentValues values = new ContentValues();
       values.put(COLUMN_DELETE, blog.get_blogSolved());*/

        SQLiteDatabase db = this.getWritableDatabase();
        // !!!!!!!!!!!!!!!!!!! remove just for testing
/*        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BLOGS);
        onCreate(db);*/
      // falls over on this query(Hopefully not now
        //db.update(TABLE_BLOGS,values, "  _id = "+ blog.get_blogId(), null);
        // hard coded this query because it kept felling over. There was a mistake in the Create db statement. Hopefully this is sorted.
        String query = "update blogs set "+ COLUMN_DELETE +" = " + blog.get_blogSolved() + " where _id =" + blog.get_blogId();
        try {
            db.execSQL(query);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        db.close();
    }
    /* Method deletes blog by id. Used at present on the BlogPagerActivity*/
    public boolean deleteBlog(int id) {
        boolean result = false;
        String query = "Select * FROM " + TABLE_BLOGS + " WHERE "
                + COLUMN_ID + " = " + id ;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        // surely this is overkill to delete
        Blog blog = new Blog();
        if (cursor.moveToFirst()) {
            blog.set_blogId(Integer.parseInt(cursor.getString(0)));
            db.delete(TABLE_BLOGS, COLUMN_ID + " = ?",
                    new String[] { String.valueOf(blog.get_blogId()) });
            cursor.close();
            result = true;
        }
        db.close();
        return result;
    }
    public Blog findBlog(String title){
        String query = "Select * FROM " + TABLE_BLOGS + " WHERE "
                + COLUMN_TITLE + " = \"" + title + "\"";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        Blog blog = new Blog();
        if (cursor.moveToFirst()) {
            cursor.moveToFirst();
            blog.set_blogId(Integer.parseInt(cursor.getString(0)));
            blog.set_blogTitle(cursor.getString(1));
            blog.set_blogText(cursor.getString(2));
            Date date = new Date();

            try {
                date = java.sql.Date.valueOf(cursor.getString(3));
                //exception caused by unparsable date
                //date = df.parse(cursor.getString(3));
            }catch (Exception e){
                Log.d(TAG, "Date parse gone wrong");
            }
            blog.set_blogDate(date);
            cursor.close();
        } else {
            blog = null;
        }
        db.close();
        return blog;
    }
}
