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

    public View getView(int position, View convertView, ViewGroup parent){
        // here you let SimpleAdapter built the view normally.
        View v = super.getView(position, convertView, parent);

        // Then we get reference for Picasso
        ImageView img = (ImageView) v.getTag();
        if(img == null){
            img = (ImageView) v.findViewById(R.id.imageView);
            v.setTag(img); // <<< THIS LINE !!!!
        }
        // get the url from the data you passed to the `Map`
        Uri url = (Uri) ((Map)getItem(position)).get("img");
        // do Picasso
        Picasso.with(v.getContext()).load(url).into(img);

        // return the view
        return v;
    }
}