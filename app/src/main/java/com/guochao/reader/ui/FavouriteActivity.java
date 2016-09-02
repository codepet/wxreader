package com.guochao.reader.ui;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;

import com.guochao.reader.R;
import com.guochao.reader.adapter.FavouriteItemAdapter;
import com.guochao.reader.entity.News;

import java.util.List;

public class FavouriteActivity extends BaseActivity {

    private List<News> mNewsList;
    private RecyclerView mNewsListView;
    private FavouriteItemAdapter mListAdapter;
    private SwipeRefreshLayout mRefreshLayout;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_list);
    }

    @Override
    protected void fetchData() {

    }
}
