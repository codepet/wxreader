package com.guochao.reader.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.guochao.reader.R;
import com.guochao.reader.adapter.ListItemAdapter;
import com.guochao.reader.entity.News;
import com.guochao.reader.entity.NewsResult;
import com.guochao.reader.http.HttpCore;
import com.guochao.reader.net.NetService;
import com.guochao.reader.util.NetConnectionUtil;
import com.guochao.reader.util.ResponseException;

import java.util.ArrayList;
import java.util.List;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class SearchActivity extends BaseActivity {

    private List<News> mNewsList;
    private RecyclerView mNewsListView;
    private ListItemAdapter mListAdapter;
    private SwipeRefreshLayout mRefreshLayout;
    private Toolbar mToolbar;
    private NetService mNetService;
    private CompositeSubscription mSubscriptions;
    private int mCurrentPage = 1;
    private String mKeyWord;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_list);
        initToolbar();
        initRecyclerView();
        initSwipeRefreshLayout();
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.id_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationIcon(R.mipmap.ic_back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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
                    try {
                        fetchData(mKeyWord, ++mCurrentPage);
                    } catch (ResponseException e) {
                        Snackbar.make(mNewsListView, getString(R.string.load_error), Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        });
        mListAdapter.setOnItemListener(new ListItemAdapter.OnItemListener() {
            @Override
            public void onClick(View view, int position) {
                Intent intent = new Intent(SearchActivity.this, WebViewActivity.class);
                intent.putExtra("url", mNewsList.get(position).getUrl());
                startActivity(intent);
            }

            @Override
            public boolean onLongClick(View view, int position) {
                showChoiceDialog(position);
                return true;
            }
        });
    }

    private void showChoiceDialog(final int position) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_single_choice, new LinearLayout(this), false);
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setView(view);
        final Dialog dialog = builder.create();
        view.findViewById(R.id.id_bt_collect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Snackbar.make(mNewsListView, getString(R.string.action_collect_success), Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.look_favourite), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        })
                        .show();
            }
        });
        view.findViewById(R.id.id_bt_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        Button button = (Button) view.findViewById(R.id.id_bt_search);
        button.setText("搜索\"" + mNewsList.get(position).getDescription() + "\"");
        dialog.show();
    }

    private void initSwipeRefreshLayout() {
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.id_swipe_refresh_layout);
        mRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(this, R.color.colorPrimary),
                ContextCompat.getColor(this, R.color.colorPrimaryDark));
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    fetchData(mKeyWord, mCurrentPage = 1);
                } catch (ResponseException e) {
                    Snackbar.make(mNewsListView, getString(R.string.load_error), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void fetchData() {
        mKeyWord = getIntent().getStringExtra("src");
        mNetService = HttpCore.getInstance(getApplicationContext()).getNetService();
        mSubscriptions = new CompositeSubscription();
        mRefreshLayout.setRefreshing(true);
        try {
            fetchData(mKeyWord, mCurrentPage = 1);
        } catch (ResponseException e) {
            Snackbar.make(mNewsListView, getString(R.string.load_error), Snackbar.LENGTH_LONG).show();
        }
    }

    private void fetchData(String keyWord, int page) throws ResponseException {
        if (!NetConnectionUtil.isNetConnected(this)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.setRefreshing(false);
                    }
                    Snackbar.make(mNewsListView, getString(R.string.net_error), Snackbar.LENGTH_LONG).show();
                }
            }, 1000);
            return;
        }
        try {
            Subscription mSubscription = mNetService.queryDetail(keyWord, page)
                    .subscribeOn(Schedulers.newThread())
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
                    .subscribe(new Observer<List<News>>() {
                        @Override
                        public void onCompleted() {
                            if (mRefreshLayout.isRefreshing()) {
                                mRefreshLayout.setRefreshing(false);
                                Snackbar.make(mNewsListView, getString(R.string.load_complete), Snackbar.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(List<News> list) {
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
                        }
                    });
//                    .subscribe(new Action1<List<News>>() {
//                        @Override
//                        public void call(List<News> list) {
//                            if (list == null || list.size() <= 0) {
//                                if (mRefreshLayout.isRefreshing()) {
//                                    mRefreshLayout.setRefreshing(false);
//                                }
//                                return;
//                            }
//                            if (mCurrentPage == 1) {
//                                mNewsList.clear();
//                            }
//                            mNewsList.addAll(list);
//                            mListAdapter.notifyDataSetChanged();
//                            if (mRefreshLayout.isRefreshing()) {
//                                mRefreshLayout.setRefreshing(false);
//                                Snackbar.make(mNewsListView, getString(R.string.load_complete), Snackbar.LENGTH_LONG).show();
//                            }
//
//                        }
//                    });
            mSubscriptions.add(mSubscription);
        } catch (Exception e) {
            unSubscribed();
            e.printStackTrace();
            throw new ResponseException(e);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mToolbar.setTitle(mKeyWord);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unSubscribed();
    }

    private void unSubscribed() {
        if (mSubscriptions != null && mSubscriptions.isUnsubscribed()) {
            mSubscriptions.unsubscribe();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.id_filter:
                break;
            case R.id.id_collect:
                Snackbar.make(mNewsListView, getString(R.string.action_collect_success), Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.look_favourite), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity(new Intent(SearchActivity.this, FavouriteActivity.class));
                            }
                        })
                        .show();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
