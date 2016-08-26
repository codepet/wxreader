package com.guochao.reader.entity;

/**
 * 后台返回数据的实体类
 */
public class News {

    private final String title;
    private final String description;
    private final String picUrl;
    private final String url;
    private final String ctime;

    public News(Builder builder) {
        this.title = builder.title;
        this.description = builder.description;
        this.picUrl = builder.picUrl;
        this.url = builder.url;
        this.ctime = builder.ctime;
    }

    public static class Builder {
        private String title;
        private String description;
        private String picUrl;
        private String url;
        private String ctime;

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder picUrl(String picUrl) {
            this.picUrl = picUrl;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder time(String time) {
            this.ctime = time;
            return this;
        }

        public News build() {
            return new News(this);
        }
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public String getUrl() {
        return url;
    }

    public String getCtime() {
        return ctime;
    }

    @Override
    public String toString() {
        return "News{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", picUrl='" + picUrl + '\'' +
                ", url='" + url + '\'' +
                ", ctime='" + ctime + '\'' +
                '}';
    }
}
