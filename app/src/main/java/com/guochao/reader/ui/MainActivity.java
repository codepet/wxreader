package com.guochao.reader.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.guochao.reader.R;
import com.guochao.reader.adapter.ListItemAdapter;
import com.guochao.reader.entity.News;
import com.guochao.reader.entity.NewsResult;
import com.guochao.reader.net.NetService;
import com.guochao.reader.util.NetConnectionUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends BaseActivity {

    private List<News> mNewsList;
    private RecyclerView mNewsListView;
    private ListItemAdapter mListAdapter;
    private SwipeRefreshLayout mRefreshLayout;
    private NetService netService;
    private static final String BASE_URL = "http://apis.baidu.com";
    private int mCurrentPage = 1;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        initRecyclerView();
        initSwipeRefreshLayout();
    }

    private void initRecyclerView() {
        mNewsListView = (RecyclerView) findViewById(R.id.id_recycler_list_view);
        mNewsListView.setLayoutManager(new LinearLayoutManager(this));
        mNewsList = new ArrayList<>();
        mListAdapter = new ListItemAdapter(this, mNewsList);
        mNewsListView.setAdapter(mListAdapter);
        mNewsListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                int totalItemCount = recyclerView.getLayoutManager().getItemCount();
                if (lastVisibleItem >= totalItemCount - 2 && dy > 0) {
                    fetchData(++mCurrentPage);
                }
            }
        });
        mListAdapter.setOnItemListener(new ListItemAdapter.OnItemListener() {
            @Override
            public void onClick(View view, int position) {
                Snackbar.make(mNewsListView, mNewsList.get(position).getTitle(), Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public boolean onLongClick(View view, int position) {
                Snackbar.make(mNewsListView, mNewsList.get(position).getDescription(), Snackbar.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void initSwipeRefreshLayout() {
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.id_swipe_refresh_layout);
        mRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(this, R.color.colorPrimary),
                ContextCompat.getColor(this, R.color.colorPrimaryDark));
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchData(mCurrentPage = 1);
            }
        });
    }

    @Override
    protected void fetchData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        netService = retrofit.create(NetService.class);
        fetchData(mCurrentPage = 1);
    }


    private void fetchData(int page) {
        if (!NetConnectionUtil.isNetConnected(this)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.setRefreshing(false);
                    }
                    Snackbar.make(mNewsListView, getString(R.string.load_error), Snackbar.LENGTH_LONG).show();
                }
            }, 1000);
            return;
        }
        netService.getWxHot(page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<NewsResult, List<News>>() {
                    @Override
                    public List<News> call(NewsResult newsResult) {
                        if (newsResult == null) {
                            Snackbar.make(mNewsListView, getString(R.string.load_error), Snackbar.LENGTH_LONG).show();
                            return null;
                        }
                        return newsResult.getNewslist();

                    }
                })
                .subscribe(new Action1<List<News>>() {
                    @Override
                    public void call(List<News> list) {
                        if (list == null || list.size() <= 0) {
                            if (mRefreshLayout.isRefreshing()) {
                                mRefreshLayout.setRefreshing(false);
                            }
                            return;
                        }
                        if (mCurrentPage == 1) {
                            mNewsList.clear();
                        }
                        mNewsList.addAll(list);
                        mListAdapter.notifyDataSetChanged();
                        if (mRefreshLayout.isRefreshing()) {
                            mRefreshLayout.setRefreshing(false);
                            Snackbar.make(mNewsListView, getString(R.string.load_complete), Snackbar.LENGTH_LONG).show();
                        }

                    }
                });
    }
}
