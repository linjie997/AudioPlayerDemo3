package com.potato997.gplayer;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

/**
 * Created by linjie on 27/10/2017.
 */

public class MyAdapter extends SimpleAdapter {

    public MyAdapter(Context context, List<? extends Map<String, Object>> data, int resource, String[] from, int[] to){
        super(context, data, resource, from, to);
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        final View v = super.getView(position, convertView, parent);

        ImageView img = (ImageView) v.getTag();

        if (img == null) {
            img = (ImageView) v.findViewById(R.id.imageView);
            v.setTag(img);
        }

        Uri url = (Uri) ((Map) getItem(position)).get("img");

        Picasso.with(v.getContext()).load(url).error(R.drawable.no_art).into(img);

        return v;
    }

}