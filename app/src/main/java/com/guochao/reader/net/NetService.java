package com.guochao.reader.net;

import com.guochao.reader.entity.NewsResult;

import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import rx.Observable;

public interface NetService {

    /**
     * 微信热门精选接口
     *
     * @param page
     * @return
     */
    @Headers("apikey:b095a4cd0e86b8b6ac36797ac8bcbf91")
    @GET("/txapi/weixin/wxhot?num=10")
    Observable<NewsResult> getWxHot(@Query("page") int page);

}
