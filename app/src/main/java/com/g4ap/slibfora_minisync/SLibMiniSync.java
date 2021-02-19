package com.g4ap.slibfora_minisync;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.CallLog;

import java.util.ArrayList;

public class SLibMiniSync {

    // warning change it only chang phone
    private static String sys_sync_type = "2";
    private static String sys_sync_device = "0008_melrose_miniphone_V01";
    private static String SLibDBPath = "/sdcard/Download/SLib.ax";

    static public long getSyncStateCallhis( Activity act ) {
        // load callhis sys
        long maxCallhisSys = 0;
        //ArrayList<Long> callhisList = new ArrayList<>();
        Cursor c = act.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
        if ( c.getCount() > 0 )
        {
            int idx1 = c.getColumnIndex("_id");
            c.moveToFirst();
            do
            {
                if ( !c.isNull(idx1) )
                {
                    if ( c.getLong(idx1) > maxCallhisSys )
                    {
                        maxCallhisSys = c.getLong(idx1);
                    }
                    //callhisList.add( c.getLong(idx1) );
                }
            }
            while (c.moveToNext());
        }
        c.close();

        return maxCallhisSys;
    }

    static public long getSyncStateSms( Activity act ) {
        // load sms sys
        long maxSMSSys = 0;
        Uri uriSMS = Uri.parse("content://sms");
        Cursor c = act.getContentResolver().query(uriSMS, null, null, null, null);
        if ( c.getCount() > 0 )
        {
            int idx2 = c.getColumnIndex("_id");
            c.moveToFirst();
            do
            {
                if ( !c.isNull(idx2) )
                {
                    if ( c.getLong(idx2) > maxSMSSys )
                    {
                        maxSMSSys = c.getLong(idx2);
                    }
                }
            }
            while (c.moveToNext());
        }
        c.close();
        return maxSMSSys;

    }


    public static void syncAndroidCallhisDB( Activity act ) {

        // load all callhis from android system db
        Cursor c = act.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, "_id");
        if (c.getCount() <= 0) return;

        int idx1 = c.getColumnIndex("_id");
        int idx2 = c.getColumnIndex("number");
        int idx3 = c.getColumnIndex("date");
        int idx4 = c.getColumnIndex("duration");
        int idx5 = c.getColumnIndex("type");

        ArrayList<String> strCheckList = new ArrayList<>();
        ArrayList<String> strInsertList = new ArrayList<>();

        c.moveToFirst();
        do
        {
            if ( c.isNull(idx1) || c.isNull(idx2) || c.isNull(idx3)|| c.isNull(idx4) || c.isNull(idx5) )
            {
                throw new RuntimeException("syncAndroidCallhisDB failed - Column is NULL");
            }

            String a_id;
            String a_number;
            String a_date;
            String a_duration;
            String a_type;

            if (c.isNull(idx1)) a_id		= "NULL";	else a_id		= String.valueOf(c.getLong(idx1));
            if (c.isNull(idx2)) a_number	= "NULL";	else a_number	= "'" + c.getString(idx2) + "'";
            if (c.isNull(idx3)) a_date		= "NULL";	else a_date		= String.valueOf(c.getLong(idx3));
            if (c.isNull(idx4)) a_duration	= "NULL";	else a_duration	= String.valueOf(c.getLong(idx4));
            if (c.isNull(idx5)) a_type		= "NULL";	else a_type		= String.valueOf(c.getLong(idx5));

            String strCheck = "SELECT rowid FROM t_callhis WHERE a_id = %s AND sync_type = %s AND sync_device LIKE '%s'";
            strCheck = String.format( strCheck, a_id, sys_sync_type, sys_sync_device );
            strCheckList.add(strCheck);

            String strInsert =
                    "INSERT INTO t_callhis (ios_rowid,ios_addres,ios_date,ios_duration,ios_flags," +
                            "a_id,a_number,a_date,a_duration,a_type,sync_type,sync_device,isvalid)" +
                            " VALUES (0, 'nil', 0, 0, 0, %s, %s, %s, %s, %s, %s, '%s', 1)";
            strInsert = String.format( strInsert,
                    a_id,
                    a_number,
                    a_date,
                    a_duration,
                    a_type,
                    sys_sync_type, sys_sync_device
            );
            strInsertList.add(strInsert);

        }
        while (c.moveToNext());
        c.close();


        // insert or update SLib db
        SQLiteDatabase db = SQLiteDatabase.openDatabase(SLibDBPath, null, SQLiteDatabase.OPEN_READWRITE);
        for ( int i=0; i<strCheckList.size(); i++ )
        {
            // check if exitst
            c = db.rawQuery( strCheckList.get(i), null);
            int nCount = c.getCount();
            c.close();

            // insert or update
            if ( nCount == 0 )
            {
                db.execSQL( strInsertList.get(i) );
            }
            else if ( nCount == 1 )
            {
                // do not update
                // strUpdateList.get(i);
            }
            else
            {
                throw new RuntimeException("syncAndroidCallhisDB failed - maybe t_callhis have duplicate data");
            }
        }
        db.close();

    }

    public static void syncAndroidSMSDB( Activity act )
    {
        // load all SMS from android system db and build the sql string
        Uri uriSMS = Uri.parse("content://sms");
        Cursor c = act.getContentResolver().query(uriSMS, null, null, null, "_id");
        if (c.getCount() <= 0) return;

        int idx01 = c.getColumnIndex("_id");
        int idx02 = c.getColumnIndex("thread_id");
        int idx03 = c.getColumnIndex("address");
        int idx04 = c.getColumnIndex("person");
        int idx05 = c.getColumnIndex("date");
        int idx06 = c.getColumnIndex("date_sent");
        int idx07 = c.getColumnIndex("protocol");
        int idx08 = c.getColumnIndex("read");
        int idx09 = c.getColumnIndex("status");
        int idx10 = c.getColumnIndex("type");
        int idx11 = c.getColumnIndex("reply_path_present");
        int idx12 = c.getColumnIndex("subject");
        int idx13 = c.getColumnIndex("body");
        int idx14 = c.getColumnIndex("service_center");
        int idx15 = c.getColumnIndex("locked");
        int idx16 = c.getColumnIndex("error_code");
        int idx17 = c.getColumnIndex("seen");

        ArrayList<String> strCheckList = new ArrayList<String>();
        ArrayList<String> strInsertList = new ArrayList<String>();
        ArrayList<String> strInsertBodyList = new ArrayList<String>();

        c.moveToFirst();
        do
        {
            String a_id;
            String a_thread_id;
            String a_address;
            String a_person;
            String a_date;
            String a_date_sent;
            String a_protocol;
            String a_read;
            String a_status;
            String a_type;
            String a_reply_path_present;
            String a_subject;
            String a_body;
            String a_service_center;
            String a_locked;
            String a_error_code;
            String a_seen;

            if (c.isNull(idx01)) a_id					= "NULL";	else a_id					= String.valueOf(c.getLong(idx01));
            if (c.isNull(idx02)) a_thread_id			= "NULL";	else a_thread_id			= String.valueOf(c.getLong(idx02));
            if (c.isNull(idx03)) a_address				= "NULL";	else a_address				= "'" + c.getString(idx03) + "'";
            if (c.isNull(idx04)) a_person				= "NULL";	else a_person				= String.valueOf(c.getLong(idx04));
            if (c.isNull(idx05)) a_date					= "NULL";	else a_date					= String.valueOf(c.getLong(idx05));
            if (c.isNull(idx06)) a_date_sent			= "NULL";	else a_date_sent			= String.valueOf(c.getLong(idx06));
            if (c.isNull(idx07)) a_protocol				= "NULL";	else a_protocol				= String.valueOf(c.getLong(idx07));
            if (c.isNull(idx08)) a_read					= "NULL";	else a_read					= String.valueOf(c.getLong(idx08));
            if (c.isNull(idx09)) a_status				= "NULL";	else a_status				= String.valueOf(c.getLong(idx09));
            if (c.isNull(idx10)) a_type					= "NULL";	else a_type					= String.valueOf(c.getLong(idx10));
            if (c.isNull(idx11)) a_reply_path_present	= "NULL";	else a_reply_path_present	= String.valueOf(c.getLong(idx11));
            if (c.isNull(idx12)) a_subject				= "NULL";	else a_subject				= "'" + c.getString(idx12) + "'";
            if (c.isNull(idx13)) a_body					= "NULL";	else a_body					= c.getString(idx13);
            if (c.isNull(idx14)) a_service_center		= "NULL";	else a_service_center		= "'" + c.getString(idx14) + "'";
            if (c.isNull(idx15)) a_locked				= "NULL";	else a_locked				= String.valueOf(c.getLong(idx15));
            if (c.isNull(idx16)) a_error_code			= "NULL";	else a_error_code			= String.valueOf(c.getLong(idx16));
            if (c.isNull(idx17)) a_seen					= "NULL";	else a_seen					= String.valueOf(c.getLong(idx17));

            if ( c.isNull(idx01) || c.isNull(idx05)|| c.isNull(idx06) || c.isNull(idx10)|| c.isNull(idx13) )
            {
                throw new RuntimeException("syncAndroidSMSDB failed - Column is NULL");
            }

            if ( c.isNull(idx03) ) {
                if ( a_type.equals("3") ) {
                    a_address = "'nil'"; //a_type:3 draft a_address can be null
                }
                else {
                    throw new RuntimeException("syncAndroidSMSDB failed - a_address is NULL");
                }
            }


            String strCheck = "SELECT rowid FROM t_sms WHERE a_id = %s AND sync_type = %s AND sync_device LIKE '%s'";
            strCheck = String.format( strCheck, a_id, sys_sync_type, sys_sync_device );
            strCheckList.add(strCheck);

            String strInsert =
                    "INSERT INTO t_sms (ios_rowid,ios_handle_id,ios_date,ios_date_read,ios_is_from_me,ios_text,tel," +
                            "a_id,a_thread_id,a_address,a_person,a_date,a_date_sent,a_protocol,a_read,a_status,a_type," +
                            "a_reply_path_present,a_subject,a_body,a_service_center,a_locked,a_error_code,a_seen," +
                            "sync_type,sync_device,isvalid)" +
                            " VALUES (0, 0, 0, 0, 0, 'nil', 'nil', " +
                            "%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, " +
                            "%s, %s, ?, %s, %s, %s, %s, " +
                            "%s, '%s', 1)";
            strInsert = String.format( strInsert,
                    a_id,
                    a_thread_id,
                    a_address,
                    a_person,
                    a_date,
                    a_date_sent,
                    a_protocol,
                    a_read,
                    a_status,
                    a_type,
                    a_reply_path_present,
                    a_subject,
                    //a_body,
                    a_service_center,
                    a_locked,
                    a_error_code,
                    a_seen,
                    sys_sync_type, sys_sync_device
            );
            strInsertList.add(strInsert);
            strInsertBodyList.add(a_body);

        }
        while (c.moveToNext());
        c.close();


        // insert or update SLib db
        SQLiteDatabase db = SQLiteDatabase.openDatabase(SLibDBPath, null, SQLiteDatabase.OPEN_READWRITE);
        for ( int i=0; i<strCheckList.size(); i++ )
        {
            // check if exitst
            c = db.rawQuery( strCheckList.get(i), null);
            int nCount = c.getCount();
            c.close();

            // insert or update
            if ( nCount == 0 )
            {
                db.execSQL( strInsertList.get(i), new String[]{strInsertBodyList.get(i)} );
            }
            else if ( nCount == 1 )
            {
                // do not update
                // strUpdateList.get(i);
            }
            else
            {
                throw new RuntimeException("syncAndroidSMSDB failed - maybe t_sms have duplicate data");
            }
        }
        db.close();

    }

}
