package com.guochao.reader.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.guochao.reader.R;
import com.guochao.reader.entity.News;

import java.util.List;

public class FavouriteItemAdapter extends RecyclerView.Adapter<FavouriteItemAdapter.ItemHolder> {

    private List<News> list;
    private LayoutInflater inflater;
    private OnItemListener listener;

    public FavouriteItemAdapter(Context context, List<News> list) {
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.layout_favourite_item, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(final ItemHolder holder, int position) {
        holder.title.setText(list.get(position).getTitle());
        holder.description.setText(list.get(position).getDescription());
        holder.time.setText(list.get(position).getCtime());
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

        public View tag;
        public TextView title;
        public TextView description;
        public TextView time;

        public ItemHolder(View itemView) {
            super(itemView);
            tag = itemView.findViewById(R.id.id_tag_view);
            title = (TextView) itemView.findViewById(R.id.id_favourite_title);
            description = (TextView) itemView.findViewById(R.id.id_favourite_description);
            time = (TextView) itemView.findViewById(R.id.id_favourite_time);
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
