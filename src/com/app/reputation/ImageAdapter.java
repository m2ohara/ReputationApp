package com.app.reputation;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
    private Context mContext;

    public ImageAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return mThumbIds.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return mThumbIds[position];
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(mThumbIds[position]);
        return imageView;
    }

    // references to our images
    private Integer[] mThumbIds = {
            R.drawable.emoji1, R.drawable.emoji2,
            R.drawable.emoji3, R.drawable.emoji4,
            R.drawable.emoji5, R.drawable.emoji6,
            R.drawable.emoji1, R.drawable.emoji2,
            R.drawable.emoji3, R.drawable.emoji4,
            R.drawable.emoji5, R.drawable.emoji6,
            R.drawable.emoji1, R.drawable.emoji2,
            R.drawable.emoji3, R.drawable.emoji4,
            R.drawable.emoji5, R.drawable.emoji6,
            R.drawable.emoji1, R.drawable.emoji2,
            R.drawable.emoji3, R.drawable.emoji4,
            R.drawable.emoji5, R.drawable.emoji6,
            R.drawable.emoji1, R.drawable.emoji2,
            R.drawable.emoji3, R.drawable.emoji4,
            R.drawable.emoji5, R.drawable.emoji6,
            R.drawable.emoji1, R.drawable.emoji2,
            
    };
}