package com.nicocorp.nr1;

import android.app.DownloadManager;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Nico on 05/07/13.
 */
public class FilesActivity extends ListActivity
{

    final long MILLIS_IN_DAY = 24 * 60 * 60 * 1000;
    public DownloadManager dmgr;
    public ArrayList<downloadItemObject> tabDdls;
    CommonFunctions cf;
    boolean bDownloadAtStartup;
    SharedPreferences settings;
    BroadcastReceiver receiver = new BroadcastReceiver()
    {
		@Override
        public void onReceive(Context context, Intent intent)
        {
            cf = (CommonFunctions) getApplication();
            Intent intentToLaunch;
            long resultQuery;
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action))
            {
                resultQuery = intent.getLongExtra(
					DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(resultQuery);
                Cursor c = dmgr.query(query);
                if (c.moveToFirst())
                {
                    int columnIndex = c
						.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    if (DownloadManager.STATUS_SUCCESSFUL == c
						.getInt(columnIndex))
                    {

                        String uriString = c
							.getString(c
									   .getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                        String strToDisplay = uriString;
                        uriString = Uri.withAppendedPath(Uri.parse(cf.getStoragePath()), uriString).getPath();
//                        intentToLaunch = cf.startFileDefaultAppIntent(uriString);
                        intentToLaunch = new Intent(getApplication(), FilesActivity.class);
                        cf.createNotification(getApplication(),
											  intentToLaunch,
											  "NR1 downloaded [" + strToDisplay + "]",
											  strToDisplay,
											  true,
											  getResources().getInteger(R.integer.DdlNotifId)
											  );

                        //****************************************************************

                        cf.makeToast(strToDisplay + " has been downloaded");
                        updateFileList();
                        //****************************************************************
                    }
                    else if (DownloadManager.STATUS_FAILED == c
							 .getInt(columnIndex))
                    {

                        int errReason = c
							.getInt(c
									.getColumnIndex(
										DownloadManager.COLUMN_REASON));
                        String ur = c
							.getString(c
									   .getColumnIndex(
										   DownloadManager.COLUMN_URI));
                        String errMsg;
                        if (errReason == 404)
                        {
                            errMsg = "Today's file does not exists";
                        }
                        else
                        {
                            errMsg = "Err Received xcode: " + errReason + " - " + ur;
                        }

                        cf.makeToast(errMsg);

                        intentToLaunch = new Intent(getApplication(), FilesActivity.class);
                        cf.createNotification(getApplication(),
											  intentToLaunch,
											  "NR1 downloading error",
											  errMsg,
											  true,
											  getResources().getInteger(R.integer.DdlNotifId)
											  );

                    }
                }
            }

			unregisterReceiver(this);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.barmenu, menu);

        MenuItem mi = menu.findItem(R.id.btnShowFiles);
        if (mi != null)
        {
            mi.setEnabled(false);
            mi.setIcon(R.drawable.active_newspapericon);
        }
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.fileitemmenu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {

        cf = (CommonFunctions) getApplication();

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        String strFileName = (((TextView) (info.targetView)).getText()).toString();

        String strPath =
			Uri.withAppendedPath(
			Uri.parse(cf.getStoragePath()),
			strFileName.toString())
			.getPath();

        switch (item.getItemId())
        {
            case R.id.ctxtBtnFileDelete:
                //cf.makeToast("delete "+cf.getFiles(cf.getStoragePath())[info.position]);
                try
                {
                    deleteFile(strPath);
                    cf.makeToast("The file has been deleted");
                    updateFileList();
                }
                catch (Exception e)
                {
                    cf.makeToast(cf.getErrorDetailsInString(e));
                }
                return true;

            case R.id.ctxtBtnFileRead:

                cf.makeToast("Starting the file reader...");
                cf.startFileDefaultAppIntent(strPath);
                return true;

            default:

                return super.onContextItemSelected(item);
        }
    }

    public void ClickActivityFromMenu(MenuItem item)
    {
        Intent intent;
        switch (item.getItemId())
        {
            case R.id.btnShowContacts:
                intent = new Intent(this, AboutActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                break;
            case R.id.btnShowSettings:
                intent = new Intent(this, SettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                break;
            case R.id.btnShowFiles:
                intent = new Intent(this, FilesActivity.class);
                startActivity(intent);
                break;
        }
    }

    public void startDownload()
    {
        String strUrl;

        cf = (CommonFunctions) (getApplication());

        strUrl = getResources().getString(R.string.urlPAR);
        // strUrl = getResources().getString(R.string.urlDev);


        //http://pdf.20mn.fr/$year$/quotidien/$fulldate$_PAR.pdf</string>
        String tmp = cf.getFormattedDate(R.string.str_yearDate);
        strUrl = strUrl.replace("$year$",
								tmp);

        tmp = cf.getFormattedDate(R.string.str_fullDate);
        strUrl = strUrl.replace("$fulldate$",
								tmp);

        tmp = cf.getFormattedDate(R.string.str_fullDate);
        String ddlFilename = "20min_$ddlDate$.pdf";
        ddlFilename = ddlFilename.replace(
			"$ddlDate$",
			tmp);

        //getFormattedDate(R.string.str_fullDateWithHour));
        ((TextView) findViewById(R.id.tvDownloadMessage)).setText(strUrl);

        long resultQuery = downloadFile(
			getApplicationContext(), ddlFilename, strUrl);
        if (resultQuery == 0)
        {
            return;
        }

    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		Log.d("NR1", "xxxxx ON CREATE");

		if (getCallingActivity() != null)
		{Log.d("NR1", "parent : " + getCallingActivity().getClass().getName());}
		else
		{Log.d("NR1", "parent : nothing");}
		
		
	}

	@Override
	protected void onStart()
	{
		Log.d("NR1", "xxxxx ON Start");

		super.onStart();
		cf = (CommonFunctions) getApplication();
		boolean firstTime=cf.isStartup();

        getWindow().setWindowAnimations(android.R.anim.fade_in);
		setContentView(R.layout.files);
	    settings = PreferenceManager.getDefaultSharedPreferences(cf);

        //check if started by alarm

		boolean isStartedByAlarm=false;
        String startUpMode ="";
		//  Intent intent= getIntent();
		// if(intent.hasExtra("StartupMode")){
        //    startUpMode  = intent.getStringExtra("StartupMode");
        //}
		//	isStartedByAlarm = (startUpMode == "Alarm");

		Log.d("NR1", "xxxxxxxxx isAlarm (extra : " + startUpMode + ")=" + isStartedByAlarm);

        boolean bDownloadAtStartup = settings.getBoolean(getResources().getString(R.string.cfgDownloadOnStartup), false);
        try
        {
            updateFileList();
        }
        catch (Exception e)
        {
            cf.makeToast(cf.getErrorDetailsInString(e));
        }

        Button btnGo = (Button) (this.findViewById(R.id.btnDownloadTodaysFile));
        btnGo.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					startDownload();
				}
			});

        cf = (CommonFunctions) getApplication();
        boolean strProp = settings.getBoolean(getResources().getString(R.string.cfgChkDeleteFileAferDays), false);

        if (strProp)
        {
            deleteOldFiles();
        }
        else
        {
            //cf.makeToast("File cleaning is not activated in settings");
            Log.d("NR1", "File cleaning is not activated in settings");
        }

        //download at startup if setting is checked
        if (bDownloadAtStartup)
        {

			if (firstTime | isStartedByAlarm)
			{ 
				startDownload();
			}

        }
        else
        {
            ((TextView) findViewById(R.id.tvDownloadMessage)).setText("Download at startup is not activated in the settings");
        }
		if (firstTime | isStartedByAlarm)
		{
			String returnWakeUps = cf.setAppliWakeUps(false);
			((TextView) findViewById(R.id.tvNextDownloadTime)).setText(returnWakeUps);
		}
        //check if started by alarm
        //if (isStartedByAlarm)
        //{
		//   finish();
		//}
    }

    void deleteOldFiles()
    {

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String strMaxDays = settings.getString(getResources().getString(R.string.cfgMaxFileDateDays), null);
        if ((strMaxDays != null) && (!TextUtils.equals(strMaxDays, "")))
        {
            Integer intMaxDateDays = Integer.parseInt(strMaxDays);

            CommonFunctions cf = (CommonFunctions) (this.getApplication());
            String strFolder = cf.getStoragePath();
            ArrayList<File> tabFiles = cf.getFileObjects(strFolder);
            // cf.makeToast("CLEANING FOLDER : Test " + tabFiles.size() + " files");

            for (File f : tabFiles)
            {
                if (fileIsOlderThanSettings(f, intMaxDateDays))
                {
                    //  cf.makeToast(f.getName() + " will be deleted");
                    try
                    {
                        String fpath = f.getPath();
                        deleteFile(fpath);
						//   cf.makeToast("AutoDel (>" + intMaxDateDays + " days): " + fpath + " has been deleted");
                        updateFileList();
                    }
                    catch (Exception e)
                    {
                        cf.makeToast(cf.getErrorDetailsInString(e));
                    }

                }

            }
        }
        else
        {
            cf.makeToast("intMaxDateDays is not defined in the properties");
        }
    }

    boolean fileIsOlderThanSettings(File f, int maxDays)
    {
        long filetimeInMillis = f.lastModified();
        cf = (CommonFunctions) getApplication();
        long nowMillis = System.currentTimeMillis();

        double maxAging = maxDays;// * MILLIS_IN_DAY;
        double nbDays = (nowMillis - filetimeInMillis) / MILLIS_IN_DAY;
        nbDays = nbDays + 1;
        //	cf.makeToast(f.getPath() + " >> " + nbDays + " days");

        return (nbDays > maxDays);
    }

    void updateFileList()
    {
        CommonFunctions c = (CommonFunctions) (this.getApplication());
        String strFolder = c.getStoragePath();
        String[] tabFiles = c.getFiles(strFolder);

        ArrayAdapter adapter = new ArrayAdapter<String>(
			c,
			R.layout.fileitem,
			tabFiles);

        ListView listView = getListView();
        registerForContextMenu(listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
				{
//                CustomDialog dlg=new CustomDialog();

					cf = (CommonFunctions) getApplication();
					final String pth = (String) ((TextView) arg1).getText();
					Uri filepth =
                        Uri.withAppendedPath(
						Uri.parse(cf.getStoragePath()), pth);

					final String filespath = filepth.getPath();
					cf.startFileDefaultApp(filespath);
					arg1.setSelected(true);

					//- See more at: http://www.survivingwithandroid.com/2013/04/android-listview-context-menu.html#sthash.E2MCo0WY.dpuf
				}
			});

        getListView().setTextFilterEnabled(true);
    }

    public boolean deleteFile(String ddlFilename)
    {
        File f = new File(ddlFilename);
        if (f.exists())
        {
            try
            {
                f.delete();
                cf.makeToast("The file has been deleted.");

            }
            catch (Exception e)
            {
                cf.makeToast("The file was not deleted." + cf.getErrorDetailsInString(e));
            }

        }
        else
        {
            cf.makeToast("The file does not exist in the filesystem.");
        }
        return false;
    }

    public int downloadFile(Context a, String ddlFilename, String urlToDownload)
    {
        Log.d("NR1", "********* DOWNLOADING **********");

        cf = (CommonFunctions) (a.getApplicationContext());
        dmgr = (DownloadManager) (cf.getSystemService(DOWNLOAD_SERVICE));

        Uri uri = Uri.parse(urlToDownload);

        DownloadManager.Request req =
			new DownloadManager.Request(uri);
        req.setTitle(ddlFilename);
        req.setAllowedNetworkTypes(
			req.NETWORK_MOBILE | req.NETWORK_WIFI);
        req.setAllowedOverRoaming(true);


        req.setDescription("NR1 download " + ddlFilename);

        String pth = cf.getStoragePath() + "/" + ddlFilename;
        //req.setDestinationInExternalPublicDir(pth,strName);

        try
        {
            cf.makeToast("checking need of download in " + pth);

            String sFiles[] = cf.getFiles(cf.getStoragePath());
            for (int i = 0; i < sFiles.length; i++)
            {
                String ExistingFile = cf.getStoragePath() + "/" + sFiles[i];
//							cf.makeToast(ExistingFile+"<1", true);
//							cf.makeToast(pth+"2", true);
                boolean bEqual = android.text.TextUtils.equals(pth, ExistingFile);
//							cf.makeToast("3 > "+bEqual, true);

                if (bEqual)
                {
                    cf.makeToast("This file will not be downloaded, because it already exists");
                    return 0;
                }
                else
                {
                    //cf.makeToast("This file will be downloaded", false);
                    //return 0;
                }
            }
        }
        catch (Exception e)
        {
            cf.makeToast("Error in checking need of download");
            return 0;
        }

        Uri pUri = Uri.parse("file://" + pth);
        req.setDestinationUri(pUri);
        long retID = dmgr.enqueue(req);
        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        return  getResources().getInteger(R.integer.DdlNotifId);

    }

    public ArrayList<String> getDownloadList()
    {
        ArrayList<String> tabRes = new ArrayList<String>();
        for (downloadItemObject ddlId : tabDdls)
        {
            DownloadManager.Query ddlQuery =
				new DownloadManager.Query();
            ddlQuery.setFilterById(ddlId.getId());

            Cursor c = dmgr.query(ddlQuery);
            int colIdDescription = c.getColumnIndex(
				DownloadManager.COLUMN_DESCRIPTION);

            int colDdlIds = c.getColumnIndex(
				DownloadManager.COLUMN_ID);

            int colIdStatus = c.getColumnIndex(
				DownloadManager.COLUMN_STATUS);


            String s = c.getString(colDdlIds) + c.getString(colIdStatus);

        }
        return tabRes;
    }

}
