package com.nicocorp.nr1;

import android.annotation.*;
import android.app.*;
import android.content.*;
import android.content.res.*;
import android.net.*;
import android.os.*;
import android.preference.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.text.*;
import java.util.*;

/**
 * Created by Nico on 22/05/13.
 */
public class CommonFunctions extends Application
{

    NotificationManager notificationManager;
//    private int notificationId;
//	private int AlarmNotificationId;
    private boolean isFirstCall = true;

    public boolean isStartup()
	{
        if (!isFirstCall)
		{
            return false;
        }
        isFirstCall = false;
        return true;
    }
	public void isStartupReset()
	{
        isFirstCall = true;
        return ;
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig)
	{
        super.onConfigurationChanged(newConfig);
    }


    public void clearWakeUps()
	{
		Log.d("NR1","clearWakeUps start");
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		notificationManager =
			(NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		notificationManager.cancel(
			getResources().getInteger(R.integer.AlarmNotifId));

		//set alarm itself
        Intent intentNotif = new Intent(this, FilesActivity.class);
        PendingIntent pi = PendingIntent.getActivity(
			this,
			getResources().getInteger(R.integer.AlarmId),
			intentNotif,
			PendingIntent.FLAG_UPDATE_CURRENT
        );
        // Cancel alarms
        try
		{
			notificationManager =
				(NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            alarmManager.cancel(pi);
            Log.d("NR1", "alarms was removed ");
        }
		catch (Exception e)
		{
            Log.d("NR1", "alarms was NOT removed " + e.toString());
        }
		Log.d("NR1","clearWakeUps end");
		
    }

    public String setAppliWakeUps()
	{
		Log.d("NR1","setAppliWakeUps sans param start");
        return setAppliWakeUps(false);
    }

    public String setAppliWakeUps(boolean bDisplayMsg)
	{
		Log.d("NR1", "setAppliWakeUps avec param start");
		
	//	Log.d("NR1"	,"setWakeUp call clearwakeups start");
		clearWakeUps();
	//	Log.d("NR1"	,"setWakeUp call clearwakeups end");
		
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean bDownloadOnTimer = sp.getBoolean(getResources().getString(R.string.cfgDownloadOnTimer), false);
        if (!bDownloadOnTimer)
		{
			//	makeToast("autostart disabled");
            return "Application auto start is disabled in the settings";
        }

        // create the object
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);


		Calendar todayCalendar = Calendar.getInstance();
        int todayInt = todayCalendar.get(Calendar.DAY_OF_WEEK);

        String strName = getResources().getString(R.string.cfgAutorunningWeekdays);

        try
		{
        //    Log.d("NR1", "starting alarms configuration ");


            Set<String> days = sp.getStringSet(
				strName, new HashSet<String>());

            String dayNames[] = getResources().getStringArray(R.array.tabWeekdays);
            //String dayNamesInt[] = getResources().getStringArray(R.array.tabWeekdaysInt);


            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            String ddlTime = settings.getString(getResources().getString(R.string.cfgDownloadTime), "");
            String[] tabTime = ddlTime.split(":");
            if (tabTime.length != 2)
			{
                makeToast("Download time is not well configured in settings");
                return "Download time is not well configured in settings";
            }

            int ddlHours = -1;
			try
			{
				ddlHours = Integer.parseInt(tabTime[0]);
				if (ddlHours > 24 || ddlHours < 0) throw new Exception();
			}
			catch (Exception e)
			{
				makeToast("Download time is not well configured in settings");
                return "Download time is not well configured in settings";

			}

            int ddlMinutes=-1;
			try
			{
				ddlMinutes = Integer.parseInt(tabTime[1]);
				if (ddlMinutes > 59 || ddlMinutes < 0) throw new Exception();
			}
			catch (Exception e)
			{
				makeToast("Download time is not well configured in settings");
                return "Download time is not well configured in settings";
			}

            String strResult = "";
            int TargetAddDaysToToday = 0;
            Long addMillisToToday = Long.MAX_VALUE;
            Long diffMillisToToday = Long.MAX_VALUE;
            Calendar targetCalendar = (Calendar) Calendar.getInstance().clone();
            Calendar finalTargetCalendar = (Calendar) targetCalendar.clone();
            int addToToday = 0;
            String targetDayName = "";
            for (String iDay : days)
			{

                int targetDayInt = Integer.parseInt(iDay);

                addToToday = targetDayInt - todayInt;
                targetCalendar = (Calendar) Calendar.getInstance().clone();
                targetCalendar.add(Calendar.DAY_OF_MONTH, addToToday);
                targetCalendar.set(Calendar.HOUR_OF_DAY, ddlHours);
                targetCalendar.set(Calendar.MINUTE, ddlMinutes);
                targetCalendar.set(Calendar.SECOND, 0);

                diffMillisToToday = targetCalendar.getTimeInMillis() - todayCalendar.getTimeInMillis();

                //Log.d("NR1", "milli test (" + addToToday + " days) > " + diffMillisToToday);
                if (todayCalendar.after(targetCalendar))
				{
                    addToToday = (7 + targetDayInt) - todayInt;//week days count'
                    //targetCalendar = (Calendar)Calendar.getInstance().clone();
                    targetCalendar.add(Calendar.DAY_OF_MONTH, addToToday);
                    //targetCalendar.set(Calendar.HOUR_OF_DAY,ddlHours);
                    //targetCalendar.set(Calendar.MINUTE,ddlMinutes);
                    diffMillisToToday = targetCalendar.getTimeInMillis() - todayCalendar.getTimeInMillis();
                }

                if (diffMillisToToday < addMillisToToday)
				{
                    addMillisToToday = diffMillisToToday;
                    TargetAddDaysToToday = addToToday;
                    targetDayName = dayNames[targetDayInt];
                    finalTargetCalendar = (Calendar) targetCalendar.clone();

                }
            }

            SimpleDateFormat sdf = new SimpleDateFormat("cccc dd-MM-yyyy kk:mm:ss ");

			
			//utiliser un broadcast receiver
			/*
			BroadcastReceiver AlarmReceiver = new BroadcastReceiver()
			{
				@Override
				public void onReceive(Context context, Intent intent)
				{
					return;		
				}
			}
			*/
			
			/*
			ou encore
			
			
			 Intent startIntent = new Intent("WhatEverYouWant");
			 PendingIntent startPIntent = PendingIntent.getBroadcast(context, 0, startIntent, 0);
			 AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			 alarm.set(AlarmManager.RTC_WAKEUP, triggerTime, startPIntent);

			 // In Manifest.xml file
			 <receiver android:name="com.package.YourOnReceiver">
			 <intent-filter>
			 <action android:name="WhatEverYouWant" />
			 </intent-filter>
			 </receiver>
			*/
            Intent intentAlarm = new Intent(this, FilesActivity.class);
            //intentAlarm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intentAlarm.putExtra("StartupMode", "Alarm");

            PendingIntent pi = PendingIntent.getActivity(
				this,
				Integer.parseInt(getResources().getString(R.integer.AlarmId)),
				intentAlarm,
				PendingIntent.FLAG_UPDATE_CURRENT
            );
			
			
		/////	pi..putExtra("StartupMode", "Alarm");
		
	
			//Log.d("NR1"	,"setWakeUp setAlarm");
            alarmManager.set(AlarmManager.RTC_WAKEUP,
							 finalTargetCalendar.getTimeInMillis(),// addMillisToToday,
							 pi);
			//Log.d("NR1"	,"setWakeUp setAlarm done");
			
            if (bDisplayMsg)
			{
                this.makeToast("Next alarm defined on " + sdf.format(finalTargetCalendar.getTime()));
            }
            //Log.d("NR1", "end of alarms configuration");
			String	retMsg="" + sdf.format(finalTargetCalendar.getTime());

			//ALARM notificatiob
			boolean autoclosenotif=false;
			Intent intentAlarmNotif = new Intent(this, SettingsActivity.class);
			createNotification(
				getApplicationContext(),
				intentAlarmNotif,
                "Next " + getResources().getString(R.string.app_name) + " start",
				retMsg,
				autoclosenotif,
				getResources().getInteger(R.integer.AlarmNotifId),
                R.drawable.autostart
            );
			Log.d("NR1","setAppliWakeUps avec param end");
			
            return "Next application startup defined on " + retMsg;
        }
		catch (Exception e)
		{
            Log.d("NR1", "configuration alarms - " + getErrorDetailsInString(e));
            return "configuration alarms - " + getErrorDetailsInString(e);
        }

    }

    @Override
    public void onCreate()
	{
        super.onCreate();
		//setAppliWakeUps();
    }

    @Override
    public void onLowMemory()
	{
        super.onLowMemory();
    }


    @Override
    public void onTerminate()
	{
        super.onTerminate();
    }

    public String getFormattedDate(int DateFormatId)
	{
        SimpleDateFormat fFullDate = new SimpleDateFormat(
			getString(DateFormatId));
        String strResult = fFullDate.format(new java.util.Date());
        return strResult;
    }

	Toast UniqueToast;
    public void makeToast(String strMessage)
	{
        int dur = Toast.LENGTH_SHORT;

		if(UniqueToast==null)
		{
			UniqueToast = Toast.makeText(getApplicationContext(), strMessage, dur);
			UniqueToast.show();
		}
		else
		{
			UniqueToast.setText(strMessage);
			
		}

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void createNotification(Context ctx,
                                   Intent intentToLaunch,
                                   String notificationTitle,
                                   String notificationMessage,
                                   boolean AutoCloseOnClick,
                                   int NotificationId)
    {
		Log.d("NR1"	, "start create notif 1 call notif 2");
        createNotification(
            ctx,
            intentToLaunch,
            notificationTitle,
            notificationMessage,
            AutoCloseOnClick,
			NotificationId,
            R.drawable.ic_launcher);
		Log.d("NR1", "end create notif 1");
    }
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void createNotification(Context ctx,
                                   Intent intentToLaunch,
                                   String notificationTitle,
                                   String notificationMessage,
                                   boolean AutoCloseOnClick,
                                   int NotificationId,
                                   int IconID)
	{
		Log.d("NR1"	, "start create notif 2");
		PendingIntent pIntent =
			PendingIntent.getActivity(
                    ctx, 0,
                    intentToLaunch, 0);

        Notification noti =
			new Notification.Builder(this)
			.setContentTitle(notificationTitle)
			.setContentText(notificationMessage)
			.setContentIntent(pIntent)
			.setAutoCancel(AutoCloseOnClick)
			.setSmallIcon(IconID)
			.build();
		if (!AutoCloseOnClick)
		{
			noti.flags = Notification.FLAG_NO_CLEAR;

		}
        NotificationManager notificationManager =
			(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NotificationId, noti);
		Log.d("NR1"	, "end create notif 2");
    }


    public String getStoragePath()
	{
        String baseUrl = Environment.getExternalStorageDirectory().toString();
        String folderName = getString(R.string.app_name);
        Uri uriFile = Uri.withAppendedPath(Uri.parse(baseUrl), folderName);
        String strPath = uriFile.getPath();
        File storageDirectory = new File(strPath);
        if (!storageDirectory.exists())
		{
            boolean res = storageDirectory.mkdirs();
            makeToast("Created storage path: " + strPath);
        }
		else
		{
            //makeToast("Storage path is "+strPath,false);
        }
        return strPath;
    }

    public boolean startFileDefaultApp(String PathToStart)
	{
        File file = new File(PathToStart);
        if (file.exists())
		{
            Intent intent = startFileDefaultAppIntent(PathToStart);
            try
			{
                startActivity(intent);
                return true;
            }
			catch (ActivityNotFoundException e)
			{
                makeToast("Aucune application disponible pour lire les PDF");
            }
        }
		else
		{
            makeToast("Requested file does not exist");
        }
        return false;
    }

    public Intent startFileDefaultAppIntent(String PathToStart)
	{
        File file = new File(PathToStart);
        //if ( file.exists() )
        {
            Uri path = Uri.fromFile(file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            //intent.setDataAndType(path, "application/pdf");
            intent.setData(path);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            return intent;
        }
        //return null;
    }


    public ArrayList<File> getFileObjects(String SDfolderPath)
	{

        ArrayList<File> flist = new ArrayList<File>();

        Map<String, File> tmap = new TreeMap<String, File>();
        // Put some values in it

        File f = new File(SDfolderPath);
        if (f.exists())
		{
            String files[] = f.list();
            for (int i = 0; i < files.length; i++)
			{
                Uri fUri = Uri.withAppendedPath(
					(Uri.parse(this.getStoragePath())),
					files[i]
                );

                tmap.put(fUri.getPath(), new File(fUri.getPath()));
            }
        }

        // Iterate through it and it'll be in order!
        for (Map.Entry<String, File> entry : tmap.entrySet())
		{
            flist.add(entry.getValue());
        } //

        return flist;
    }

    public String[] getFiles(String SDfolderPath)
	{

        ArrayList<String> tmap = new ArrayList<String>();
        File f = new File(SDfolderPath);
        if (f.exists())
		{
            tmap.addAll(Arrays.asList(f.list()));
            Collections.sort(tmap, String.CASE_INSENSITIVE_ORDER);
            Collections.reverse(tmap);
            String[] strfinal = new String[tmap.size()];
            return tmap.toArray(strfinal);

            //return files;
        }
		else
		{
            String files[] = {SDfolderPath + " is not a folder..."};
            return files;
        }
    }

    public String getErrorDetailsInString(Exception e)
	{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(baos);
        e.printStackTrace(stream);
        stream.flush();
        String strErr = new String(baos.toByteArray());
        Log.e("xxx", strErr);
        return strErr;
    }

    public View getErrorDetailsInView(Exception e)
	{
        TextView txtView = new TextView(this);
        txtView.setText(getErrorDetailsInString(e));
        ScrollView sv = new ScrollView(this);
        sv.addView(txtView);
        Log.e("xxx", txtView.getText().toString());

        return sv;
    }

}
