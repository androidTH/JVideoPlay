package com.example.min.jvideoplay.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.example.min.jvideoplay.R;
import com.example.min.jvideoplay.bean.VideoFilterBean;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * 滤镜界面适配器
 */
public class VideoFilterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<VideoFilterBean> mList;
    private OnFilterConfigClickListener mOnFilterConfigClickListener;

    public VideoFilterAdapter(Context context, List<VideoFilterBean> videoFilters) {
        this.mContext = context;
        this.mList = videoFilters;
    }

    public void setOnFilterConfigClickListener(OnFilterConfigClickListener l) {
        this.mOnFilterConfigClickListener = l;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FilterHolder(View.inflate(mContext, R.layout.video_filter_item, null));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final FilterHolder filterHolder = (FilterHolder) holder;
        filterHolder.tvFilter.setText(mList.get(position).getName());

        filterHolder.igvFilterBg.setBackgroundResource(mList.get(position).getDrawable());
        filterHolder.rlFilterRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnFilterConfigClickListener != null) {
                    mOnFilterConfigClickListener.onFilterConfigClick(position);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        if (mList == null || mList.size() == 0) {
            return 1;
        }
        return mList.size();
    }

    public class FilterHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.rl_filter_root)
        public RelativeLayout rlFilterRoot;
        @BindView(R.id.igv_filter_bg)
        public ImageView igvFilterBg;
        @BindView(R.id.tv_filter_item_name)
        public TextView tvFilter;

        public FilterHolder(View view) {
            super(view);
            ButterKnife.bind(this,view);
        }
    }

    public interface OnFilterConfigClickListener {
        void onFilterConfigClick(int position);
    }

}
