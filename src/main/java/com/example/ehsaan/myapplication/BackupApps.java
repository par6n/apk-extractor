package com.example.ehsaan.myapplication;

import android.support.v7.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;

public class BackupApps {
    public BackupApps( Context mContext ) {
        if ( ! MainActivity.isSDCardPresent() ) { // Check for SD Card
            new AlertDialog.Builder( mContext, R.style.AppCompatAlertDialogError ) // Build a dialog
                    .setTitle( "SD Card is not available" ) // Here's the title
                    .setMessage( "SD Card isn't available. We can't continue." ) // And the content
                    .setPositiveButton( android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick( DialogInterface dialog, int which ) {
                            // Do nothing...
                        }
                    })
                    .show(); // Show it
            return; // Exit the function.
        }
        BackupOperation bOperation = new BackupOperation( mContext );
        bOperation.execute();

    }

    private class BackupOperation extends AsyncTask<Void, String, Boolean> {
        Context mContext;
        ProgressDialog mDialog;
        Integer iApps = 0;
        Integer iConverted = 0;
        List mApps;

        public BackupOperation( Context mAContext ) { this.mContext = mAContext; }

        protected void onPreExecute() {
            try {
                ProgressDialog pDialog = new ProgressDialog(this.mContext);
                pDialog.setTitle("Extracting all apps...");
                pDialog.setMessage("Initializing... Be patient.");
                pDialog.setIndeterminate(true);
                pDialog.setCancelable(false);
                pDialog.setCanceledOnTouchOutside(false);
                pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

                if ( ! pDialog.isShowing() ) {
                    pDialog.show();
                    this.mDialog = pDialog;
                }

                final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                final List AppsList = this.mContext.getPackageManager().queryIntentActivities(mainIntent, 0);
                Collections.sort(AppsList, new ResolveInfo.DisplayNameComparator(this.mContext.getPackageManager()));
                mApps = AppsList;
                for( Object object : mApps ) {
                    try {
                        ResolveInfo info = (ResolveInfo) object;

                        if (info.activityInfo.applicationInfo.icon != 0 && ( info.activityInfo.applicationInfo.packageName != null ) && ( ( info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM ) == 0 ) ) {
                            this.iApps++;
                        }
                    } catch ( Exception e ) {
                        Log.e("apkextractorbad", e.getMessage());
                        e.printStackTrace();
                    }
                }
            } catch( Exception e ) {
                Log.e("apkextractorerrorhere", e.getMessage());
                e.printStackTrace();
            }
        }

        @Override
        protected Boolean doInBackground( Void... params ) {
            String packageName;
            for( Object object : mApps ) {
                try {
                    ResolveInfo info = (ResolveInfo) object;
                    if ( info.activityInfo.applicationInfo.packageName == null )
                        continue;
                    if (info.activityInfo.applicationInfo.icon != 0 && ( ( info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM ) == 0 ) ) {
                        packageName = info.activityInfo.applicationInfo.packageName;
                        publishProgress( packageName );

                        ExtractResults res = MainActivity.ExtractPackage(this.mContext, packageName);
                        if (res.result) {
                            iConverted++;
                        } else {
                            Toast.makeText(mContext, packageName + " extraction failed", Toast.LENGTH_LONG).show();
                        }
                    }
                } catch ( Exception e ) {
                    Log.e( "apkextractorbaderror", e.getMessage() );
                    e.printStackTrace();
                    publishProgress( "error" );
                }
            }

            return ( iApps == iConverted );
        }

        @Override
        protected void onProgressUpdate( String... values ) {
            if ( values[0].equals( "error" ) ) {
                this.mDialog.dismiss();
                Toast.makeText(this.mContext, "Something bad occurred!", Toast.LENGTH_SHORT).show();
            }
            this.mDialog.setMessage( "Extracting app : " + values[0] );
            super.onProgressUpdate( values );
        }

        @Override
        protected void onPostExecute( Boolean converted ) {
            this.mDialog.dismiss();
            if ( converted ) {
                new AlertDialog.Builder( this.mContext, R.style.AppCompatAlertDialogStyle )
                        .setTitle( "Successful" )
                        .setMessage( "" + iConverted.toString() + "/" + iApps.toString() + " applications extracted to /YourApps/ at SD Card." )
                        .setPositiveButton("Got it", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Perfect!
                            }
                        }).show();
            } else {
                new AlertDialog.Builder( this.mContext, R.style.AppCompatAlertDialogError )
                        .setTitle( "Unsuccessful" )
                        .setMessage( "" + iConverted.toString() + "/" + iApps.toString() + " applications extracted to /YourApps/ at SD Card." )
                        .setPositiveButton("Got it", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Perfect!
                            }
                        }).show();
            }
        }
    }
}
