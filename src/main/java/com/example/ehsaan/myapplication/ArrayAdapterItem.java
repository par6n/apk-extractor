package com.example.ehsaan.myapplication;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by Ehsaan on 9/29/2015.
 */
public class ArrayAdapterItem extends ArrayAdapter<PackageItem> {
    Context mContext;
    int layoutResourceId;
    List data = null;

    public ArrayAdapterItem(Context mContext, int layoutResourceId, List data) {
        super( mContext, layoutResourceId, data );

        this.layoutResourceId = layoutResourceId;
        this.mContext = mContext;
        this.data = data;
    }

    @Override
    public View getView( int position, View convertView, ViewGroup parent ) {
        if ( convertView == null ) {
            LayoutInflater inflater = ( (Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate( layoutResourceId, parent, false );
        }

        PackageItem packageItem = (PackageItem) data.get( position );

        TextView textViewNameItem = (TextView) convertView.findViewById(R.id.textViewNameItem);
        TextView textViewPackageItem = (TextView) convertView.findViewById(R.id.textViewPackageItem);
        TextView textViewSize = (TextView) convertView.findViewById( R.id.textViewSize );
        ImageView appIcon = (ImageView) convertView.findViewById(R.id.appIcon);

        textViewNameItem.setText( packageItem.getName() );
        textViewPackageItem.setText( packageItem.getPackageName() );
        textViewSize.setText( packageItem.getApkSize() );
        appIcon.setImageDrawable( packageItem.getIcon() );

        return convertView;
    }
}
