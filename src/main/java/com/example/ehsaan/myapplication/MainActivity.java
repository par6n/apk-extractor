package com.example.ehsaan.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.widget.ListView;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List Apps = (List) getInstalledApplications();
        ArrayAdapterItem AppsAdapter = new ArrayAdapterItem( this, R.layout.list_view_row_item, Apps );
        ListView appsView = (ListView) findViewById(R.id.listView);
        appsView.setAdapter(AppsAdapter);
        appsView.setOnItemClickListener(new OnItemClickListenerListViewItem());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // No menu
        return true;
    }

    public static void copyFile(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static boolean isSDCardPresent() {
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

    public static ExtractResults ExtractPackage( Context context, String packageName ) {
        Log.v( "extractpackage", packageName + " is being extracted" );
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mainIntent.setPackage(packageName);
        mainIntent.setFlags(ApplicationInfo.FLAG_ALLOW_BACKUP);
        final List pkgAppsList = context.getPackageManager().queryIntentActivities(mainIntent, 0);
        for (Object object : pkgAppsList) {
            ResolveInfo info = (ResolveInfo) object;
            if ( info.activityInfo.applicationInfo.packageName == null ) {
                new AlertDialog.Builder( context )
                        .setTitle( "Wrong package" )
                        .setMessage( "Package isn't available for extracting." )
                        .setPositiveButton( android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick( DialogInterface dialog, int which ) {

                            }
                        })
                        .show();
            }
            File file = new File(info.activityInfo.applicationInfo.publicSourceDir);
            File dest = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/YourApps/" + info.activityInfo.applicationInfo.packageName + ".apk");
            File parent = dest.getParentFile();
            if ( parent != null ) parent.mkdirs();

            try {
                copyFile(file, dest);
            } catch (IOException e) {
                Log.e( "extractpackage", "Exception, Message: " + e.getMessage() );
                new AlertDialog.Builder( context )
                        .setTitle( "Exception detected" )
                        .setMessage( "Exception detected: " + e.getMessage() )
                        .setPositiveButton( android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick( DialogInterface dialog, int which ) {

                            }
                        })
                        .show();
            }

            ExtractResults res = new ExtractResults( true );
            res.setFile( dest );
            return res;
        }

        return new ExtractResults( false );
    }

    /* Old function :
    public List<PackageItem> getInstalledApplications(){

        PackageManager appInfo = getPackageManager();
        final Intent mainIntent = new Intent( Intent.ACTION_MAIN, null );
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ApplicationInfo> listInfo = appInfo.getInstalledApplications( 0 );
        Collections.sort(listInfo, new ApplicationInfo.DisplayNameComparator(appInfo));

        List<PackageItem> data = new ArrayList<PackageItem>();

        for (int index = 0; index < listInfo.size(); index++) {
            try {
                ApplicationInfo content = listInfo.get(index);
                if ( ( ( content.flags & ApplicationInfo.FLAG_SYSTEM) != 0 ) && content.enabled) {
                    if (content.icon != 0) {
                        PackageItem item = new PackageItem();
                        Log.d( "APP", content.packageName );
                        item.setName(getPackageManager().getApplicationLabel(content).toString());
                        item.setPackageName(content.packageName);
                        item.setIcon(getPackageManager().getDrawable(content.packageName, content.icon, content));
                        data.add(item);
                    }
                }
            } catch (Exception e) {

            }
        }
        return data;
    }*/

    public List<PackageItem> getInstalledApplications() {
        final Intent mainIntent = new Intent( Intent.ACTION_MAIN, null );
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List AppsList = getPackageManager().queryIntentActivities( mainIntent, 0 );
        Collections.sort( AppsList, new ResolveInfo.DisplayNameComparator( getPackageManager() ) );

        List<PackageItem> data = new ArrayList<PackageItem>();
        for( Object object : AppsList ) {
            try {
                ResolveInfo info = (ResolveInfo) object;
                if (info.activityInfo.applicationInfo.icon != 0 && ( ( info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM ) == 0 ) ) {
                    Log.i( "knownapp", info.activityInfo.applicationInfo.packageName + " is known and added to list" );
                    PackageItem item = new PackageItem();
                    item.setName(getPackageManager().getApplicationLabel( info.activityInfo.applicationInfo ).toString() );
                    item.setPackageName(info.activityInfo.applicationInfo.packageName);
                    item.setIcon(info.activityInfo.applicationInfo.loadIcon(getPackageManager()));
                    data.add(item);
                }
            } catch( Exception e ) {
                e.printStackTrace();
            }
        }

        return data;
    }
}
