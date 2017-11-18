package com.potato997.gplayer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by linjie on 07/11/2017.
 */

public class MyRecycleAdapter extends RecyclerView.Adapter<MyRecycleAdapter.CustomViewHolder> {

    private List<Music> musicList;
    private Context mContext;
    private MainActivity mainActivity;
    public static MusicService musicService;

    public MyRecycleAdapter(Context context, List<Music> musicList) {
        this.musicList = musicList;
        this.mContext = context;
        mainActivity = (MainActivity)mContext;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_music_list, null);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, final int position) {
        final Music music = musicList.get(position);

            Picasso.with(mContext).load(music.getAlbumUri())
                    .error(R.drawable.no_art)
                    .placeholder(R.drawable.no_art)
                    .into(customViewHolder.imageView);

        customViewHolder.textView.setText(music.getTitle());

        customViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicService.setSong(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return (null != musicList ? musicList.size() : 0);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected ImageView imageView;
        protected TextView textView;

        public CustomViewHolder(View view) {
            super(view);
            this.imageView = (ImageView) view.findViewById(R.id.imageView);
            this.textView = (TextView) view.findViewById(R.id.track_title);
        }
    }
}