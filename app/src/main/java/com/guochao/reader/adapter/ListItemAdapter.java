package com.guochao.reader.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.guochao.reader.R;
import com.guochao.reader.entity.News;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * 首页列表的适配器
 */
public class ListItemAdapter extends RecyclerView.Adapter<ListItemAdapter.ItemHolder> {

    private List<News> list;
    private LayoutInflater inflater;
    private Context context;
    private OnItemListener listener;

    public ListItemAdapter(Context context, List<News> list) {
        this.context = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.layout_wx_item, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(final ItemHolder holder, final int position) {
        holder.title.setText(list.get(position).getTitle());
        holder.description.setText(list.get(position).getDescription());
        holder.ctime.setText(list.get(position).getCtime());
        if (list.get(position).getPicUrl() != null && !list.get(position).getPicUrl().isEmpty()) {
            Picasso.with(context)
                    .load(list.get(position).getPicUrl())
                    .into(holder.image);
        }
        if (listener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = holder.getLayoutPosition();
                    listener.onClick(view, position);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    int position = holder.getLayoutPosition();
                    return listener.onLongClick(view, position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    protected class ItemHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public ImageView image;
        public TextView description;
        public TextView ctime;

        public ItemHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.id_wx_title);
            image = (ImageView) itemView.findViewById(R.id.id_wx_image);
            description = (TextView) itemView.findViewById(R.id.id_wx_description);
            ctime = (TextView) itemView.findViewById(R.id.id_wx_ctime);
        }
    }

    public void setOnItemListener(OnItemListener listener) {
        this.listener = listener;
    }

    public interface OnItemListener {
        void onClick(View view, int position);

        boolean onLongClick(View view, int position);
    }
}
