package com.example.myaddressbook;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "contacts.db2";
    private static final int DATABASE_VERSION = 3;  // 更新数据库版本号

    private static final String TABLE_CONTACTS = "contacts";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_GROUP_NAME = "group_name";
    private static final String COLUMN_NICKNAME = "nickname";
    private static final String COLUMN_IMAGE_PATH = "image_path";
    private static final String TABLE_GROUPS = "groups";
    private static final String COLUMN_GROUP_ID = "group_id";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_PHONE + " TEXT,"
                + COLUMN_GROUP_NAME + " TEXT,"
                + COLUMN_NICKNAME + " TEXT,"
                + COLUMN_IMAGE_PATH + " TEXT" + ")";

        String CREATE_GROUPS_TABLE = "CREATE TABLE " + TABLE_GROUPS + "("
                + COLUMN_GROUP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_GROUP_NAME + " TEXT UNIQUE" + ")";

        db.execSQL(CREATE_CONTACTS_TABLE);
        db.execSQL(CREATE_GROUPS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            String ADD_GROUP_NAME_COLUMN = "ALTER TABLE " + TABLE_CONTACTS + " ADD COLUMN " + COLUMN_GROUP_NAME + " TEXT DEFAULT ''";
            db.execSQL(ADD_GROUP_NAME_COLUMN);

            String CREATE_GROUPS_TABLE = "CREATE TABLE " + TABLE_GROUPS + "("
                    + COLUMN_GROUP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_GROUP_NAME + " TEXT UNIQUE" + ")";
            db.execSQL(CREATE_GROUPS_TABLE);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        onCreate(db);
    }

    // Add new contact
    public void addContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, contact.getName());
        values.put(COLUMN_PHONE, contact.getPhoneNumber());
        values.put(COLUMN_GROUP_NAME, contact.getGroupName());
        values.put(COLUMN_NICKNAME, contact.getNickname());
        values.put(COLUMN_IMAGE_PATH, contact.getImagePath());

        db.insert(TABLE_CONTACTS, null, values);
        db.close();
    }

    // Get single contact by ID
    @SuppressLint("Range")
    public Contact getContact(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CONTACTS, new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_PHONE, COLUMN_GROUP_NAME, COLUMN_NICKNAME, COLUMN_IMAGE_PATH},
                COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            Contact contact = new Contact();
            if (cursor.getColumnIndex(COLUMN_ID) >= 0) contact.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
            if (cursor.getColumnIndex(COLUMN_NAME) >= 0) contact.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
            if (cursor.getColumnIndex(COLUMN_PHONE) >= 0) contact.setPhoneNumber(cursor.getString(cursor.getColumnIndex(COLUMN_PHONE)));
            if (cursor.getColumnIndex(COLUMN_GROUP_NAME) >= 0) contact.setGroupName(cursor.getString(cursor.getColumnIndex(COLUMN_GROUP_NAME)));
            if (cursor.getColumnIndex(COLUMN_NICKNAME) >= 0) contact.setNickname(cursor.getString(cursor.getColumnIndex(COLUMN_NICKNAME)));
            if (cursor.getColumnIndex(COLUMN_IMAGE_PATH) >= 0) contact.setImagePath(cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE_PATH)));
            cursor.close();
            return contact;
        }
        return null;
    }

    // Get all contacts sorted by name
    @SuppressLint("Range")
    public List<Contact> getAllContacts() {
        List<Contact> contacts = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_CONTACTS + " ORDER BY " + COLUMN_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Contact contact = new Contact();
                if (cursor.getColumnIndex(COLUMN_ID) >= 0) contact.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                if (cursor.getColumnIndex(COLUMN_NAME) >= 0) contact.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                if (cursor.getColumnIndex(COLUMN_PHONE) >= 0) contact.setPhoneNumber(cursor.getString(cursor.getColumnIndex(COLUMN_PHONE)));
                if (cursor.getColumnIndex(COLUMN_GROUP_NAME) >= 0) contact.setGroupName(cursor.getString(cursor.getColumnIndex(COLUMN_GROUP_NAME)));
                if (cursor.getColumnIndex(COLUMN_NICKNAME) >= 0) contact.setNickname(cursor.getString(cursor.getColumnIndex(COLUMN_NICKNAME)));
                if (cursor.getColumnIndex(COLUMN_IMAGE_PATH) >= 0) contact.setImagePath(cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE_PATH)));
                contacts.add(contact);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return contacts;
    }
    @SuppressLint("Range")
    public List<String> getAllGroups() {
        List<String> groups = new ArrayList<>();
        String selectQuery = "SELECT " + COLUMN_GROUP_NAME + " FROM " + TABLE_GROUPS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                groups.add(cursor.getString(cursor.getColumnIndex(COLUMN_GROUP_NAME)));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return groups;
    }
    public void addGroup(String groupName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GROUP_NAME, groupName);
        db.insert(TABLE_GROUPS, null, values);
        db.close();
    }


    public void updateGroupName(String oldGroupName, String newGroupName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GROUP_NAME, newGroupName);
        db.update(TABLE_GROUPS, values, COLUMN_GROUP_NAME + " = ?", new String[]{oldGroupName});

        ContentValues contactValues = new ContentValues();
        contactValues.put(COLUMN_GROUP_NAME, newGroupName);
        db.update(TABLE_CONTACTS, contactValues, COLUMN_GROUP_NAME + " = ?", new String[]{oldGroupName});
        db.close();
    }
    public void deleteGroup(String groupName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_GROUPS, COLUMN_GROUP_NAME + " = ?", new String[]{groupName});

        ContentValues contactValues = new ContentValues();
        contactValues.put(COLUMN_GROUP_NAME, "");
        db.update(TABLE_CONTACTS, contactValues, COLUMN_GROUP_NAME + " = ?", new String[]{groupName});
        db.close();
    }
    // Get sorted contacts by group and name
    @SuppressLint("Range")
    public List<Contact> getSortedContacts() {
        List<Contact> contacts = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_CONTACTS + " ORDER BY " + COLUMN_GROUP_NAME + ", SUBSTR(" + COLUMN_NAME + ", 1, 1), " + COLUMN_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Contact contact = new Contact();
                if (cursor.getColumnIndex(COLUMN_ID) >= 0) contact.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                if (cursor.getColumnIndex(COLUMN_NAME) >= 0) contact.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                if (cursor.getColumnIndex(COLUMN_PHONE) >= 0) contact.setPhoneNumber(cursor.getString(cursor.getColumnIndex(COLUMN_PHONE)));
                if (cursor.getColumnIndex(COLUMN_GROUP_NAME) >= 0) contact.setGroupName(cursor.getString(cursor.getColumnIndex(COLUMN_GROUP_NAME)));
                if (cursor.getColumnIndex(COLUMN_NICKNAME) >= 0) contact.setNickname(cursor.getString(cursor.getColumnIndex(COLUMN_NICKNAME)));
                if (cursor.getColumnIndex(COLUMN_IMAGE_PATH) >= 0) contact.setImagePath(cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE_PATH)));
                contacts.add(contact);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return contacts;
    }

    // Update contact
    public int updateContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, contact.getName());
        values.put(COLUMN_PHONE, contact.getPhoneNumber());
        values.put(COLUMN_GROUP_NAME, contact.getGroupName());
        values.put(COLUMN_NICKNAME, contact.getNickname());
        values.put(COLUMN_IMAGE_PATH, contact.getImagePath());

        return db.update(TABLE_CONTACTS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(contact.getId())});
    }

    // Delete contact
    public void deleteContact(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }
    @SuppressLint("Range")
    public List<Contact> getAllContactsSortedByName() {
        List<Contact> contacts = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_CONTACTS + " ORDER BY " + COLUMN_NAME + " COLLATE NOCASE";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Contact contact = new Contact();
                if (cursor.getColumnIndex(COLUMN_ID) >= 0) contact.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                if (cursor.getColumnIndex(COLUMN_NAME) >= 0) contact.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                if (cursor.getColumnIndex(COLUMN_PHONE) >= 0) contact.setPhoneNumber(cursor.getString(cursor.getColumnIndex(COLUMN_PHONE)));
                if (cursor.getColumnIndex(COLUMN_GROUP_NAME) >= 0) contact.setGroupName(cursor.getString(cursor.getColumnIndex(COLUMN_GROUP_NAME)));
                if (cursor.getColumnIndex(COLUMN_NICKNAME) >= 0) contact.setNickname(cursor.getString(cursor.getColumnIndex(COLUMN_NICKNAME)));
                if (cursor.getColumnIndex(COLUMN_IMAGE_PATH) >= 0) contact.setImagePath(cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE_PATH)));
                contacts.add(contact);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return contacts;
    }

}
