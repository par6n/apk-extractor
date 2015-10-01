package com.example.ehsaan.myapplication;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

/**
 * OnItemClickListenerListViewItem
 * This class is used for handling click event on the ListView items.
 * This is responsible to starting copying operation and acting.
 *
 * @author Ehsaan
 */
public class OnItemClickListenerListViewItem implements AdapterView.OnItemClickListener {
    @Override
    public void onItemClick( AdapterView<?> parent, View view, int position, long id ) {
        Context context = view.getContext(); // Get a context for further usages.
        TextView textViewPackage = (TextView) view.findViewById(R.id.textViewPackageItem); // Get package name from TextView

        if ( ! MainActivity.isSDCardPresent() ) { // Check for SD Card
            new AlertDialog.Builder( context ) // Build a dialog
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

        String packageName = textViewPackage.getText().toString(); // Convert package name to String
        ExtractOperation operation = new ExtractOperation( context ); // Initialize the operation
        operation.execute( packageName ); // Execute it!
    }

    private class ExtractOperation extends AsyncTask<String, Integer, Boolean> {
        File mApp;
        Context mContext;
        ProgressDialog mDialog;

        public ExtractOperation( Context context ) {
            this.mContext = context;
        }

        protected void onPreExecute() {
            // Display a progress dialog before start the task.
            ProgressDialog dialog = new ProgressDialog( this.mContext );
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setMessage( "Extracting APK in /YourApps/ at SD Card..." );

            if ( ! dialog.isShowing() ) {
                dialog.show();
                this.mDialog = dialog; // Set a global variable to handle this later.
            }
        }

        @Override
        protected Boolean doInBackground( String... params ) {
            String packageName = params[0];
            ExtractResults res = MainActivity.ExtractPackage( this.mContext, packageName );

            if ( res.result ) {
                this.mApp = res.file; // This will be used for sharing intent.
                return true;
            } else {
                return false;
            }
        }

        protected void onPostExecute( Boolean result ) {
            this.mDialog.dismiss(); // Completed, so hide the progress dialog.
            if ( result ) {
                Toast.makeText(this.mContext, "Extracted to /YourApps/ directory on SD Card", Toast.LENGTH_LONG).show(); // Make a toast
                Intent share = new Intent( Intent.ACTION_SEND ); // Make a share intent
                share.setType( "application/vnd.android.package-archive" ); // Set the type for APK

                share.putExtra( Intent.EXTRA_STREAM, Uri.fromFile( this.mApp ) ); // Send the file to sharing intent.
                this.mContext.startActivity(Intent.createChooser(share, "Share the application" ) ); // Start the sharing intent.
            } else {
                Toast.makeText(this.mContext, "A problem occurred.", Toast.LENGTH_SHORT).show(); // Show a toast that says it's failed.
            }
        }
    }
}
