package com.sample.newsample.newsample;


import android.app.Activity;
import android.os.AsyncTask;
import android.widget.TextView;

import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class RssReader extends AsyncTask<String, Void, ArrayList<String>> {

    private Activity mainActivity;

    static final String BR = System.getProperty("line.separator");

    /**
     * 発売日リストタイトル
     */
    Pattern hatubaibiListTitle = Pattern.compile("【.*月.*日付】本日発売の単行本リスト");


    public RssReader(Activity activity) {
        this.mainActivity = activity;
    }

    public ArrayList<String> readFeed (String url) throws Exception {
        URL feedUrl = new URL(url);
        SyndFeedInput input = new SyndFeedInput();

        SyndFeed feed = input.build(new XmlReader(feedUrl));

        boolean isSuccess = false;
        for (Object obj : feed.getEntries()) {
            SyndEntry entry = (SyndEntry) obj;
            Matcher m = hatubaibiListTitle.matcher(entry.getTitle());

            if (m.matches()) {
                return readNewComicList(entry.getLink());
            }
        }
        ArrayList<String> newlist = new ArrayList<String>();
        newlist.add("新着リスト読み込み失敗しました");
        return newlist;
    }

    private ArrayList<String> readNewComicList(String url) throws Exception {
        Document document = Jsoup.connect(url).get();
        Elements elements = document.getElementsByClass("NA_articleBody").get(0).getElementsByTag("a");
        ArrayList<String> titleList = new ArrayList<String>();
        for(int i = 1; i < elements.size(); i++) {
            Element element = elements.get(i);
            int startIndex = element.toString().indexOf("「");
            int lastIndex = element.toString().indexOf("」");
            String title = element.toString().substring(startIndex + 1, lastIndex);
            titleList.add(title);
        }
        return titleList;
    }

    @Override
    protected ArrayList<String> doInBackground(String... params) {

        try {
            return readFeed(params[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<String>();
    }

    protected void onPostExecute(ArrayList<String> list) {
        // 取得した結果をテキストビューに入れる
        TextView resultStr = (TextView) mainActivity.findViewById(R.id.resultStr);

        StringBuffer sb = new StringBuffer();
        boolean result = false;
        if (list != null) {
            for (String comicTitle : list) {
                sb.append(comicTitle);
                sb.append(BR);
                result = true;
            }
        }
        if(!result) {
            sb.append("新刊情報はありません");
        }
        resultStr.setText(sb.toString());

    }

}
