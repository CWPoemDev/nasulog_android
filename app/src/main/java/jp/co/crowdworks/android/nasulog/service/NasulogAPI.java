package jp.co.crowdworks.android.nasulog.service;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import jp.co.crowdworks.android.nasulog.helper.HttpError;
import jp.co.crowdworks.android.nasulog.helper.OkHttpHelper;
import jp.co.crowdworks.android.nasulog.helper.RedirectNotAllowdError;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import rx.Observable;

public class NasulogAPI {
    private static final String TAG = NasulogAPI.class.getName();

    private final String mHost;
    private final String mToken;
    public NasulogAPI(String host, String token) {
        mHost = host;
        mToken = token;
    }

    private Observable<Response> rxGET(String url) {
        return rxGET(url, true);
    }
    private Observable<Response> rxGET(final String url, boolean allowRedirect) {
        Request request = new Request.Builder()
                .url(url)
                .header("Cookie", "_nasulog_session="+ mToken)
                .build();

        return Observable.create(subscriber -> {
            try {
                Response response = OkHttpHelper.getClient().newCall(request).execute();
                if (response.isSuccessful()) { //302は勝手にリダイレクトしてしまってisRedirectで判定が効かないっぽい
                    if (allowRedirect || url.equals(response.request().url().toString())) {
                        subscriber.onNext(response);
                        subscriber.onCompleted();
                    }
                    else {
                        subscriber.onError(new RedirectNotAllowdError(response));
                    }
                }
                else subscriber.onError(new HttpError(response));
            } catch (IOException e) {
                subscriber.onError(e);
            }
        });
    }

    public Observable<Response> checkConnection() {
        String url = "http://"+mHost+"/api/poems.json";
        return rxGET(url, false); //302はログイン画面に飛ばされる動作なので、エラー扱い
    }

    public Observable<JSONObject> getAllPoems() {
        String url = "http://"+mHost+"/api/home.json";

        return rxGET(url).flatMap(response ->
                Observable.create(subscriber -> {
                    try {
                        JSONArray array = new JSONObject(response.body().string()).getJSONArray("poems");
                        for (int i = 0; i < array.length(); i++) subscriber.onNext(array.getJSONObject(i));
                        subscriber.onCompleted();
                    }
                    catch (Exception e) {
                        subscriber.onError(e);
                    }
                })
        );
    }

    public Observable<JSONObject> getMyPoems() {
        String url = "http://"+mHost+"/api/poems.json";

        return rxGET(url).flatMap(response ->
            Observable.create(subscriber -> {
                try {
                    JSONArray array = new JSONObject(response.body().string()).getJSONArray("poems");
                    for (int i = 0; i < array.length(); i++) subscriber.onNext(array.getJSONObject(i));
                    subscriber.onCompleted();
                }
                catch (Exception e) {
                    subscriber.onError(e);
                }
            })
        );
    }

    public Observable<JSONObject> getPoem(long id) {
        String url = "http://"+mHost+"/api/poems/"+id+".json";

        return rxGET(url).flatMap(response ->
            Observable.create(subscriber -> {
                try {
                    JSONObject poem = new JSONObject(response.body().string()).getJSONObject("poem");
                    subscriber.onNext(poem);
                    subscriber.onCompleted();
                }
                catch (Exception e) {
                    subscriber.onError(e);
                }
            })
        );
    }

    public Observable<JSONObject> getUser() {
        String url = "http://"+mHost+"/api/user.json";

        return rxGET(url).flatMap(response ->
                Observable.create(subscriber -> {
                    try {
                        JSONObject user = new JSONObject(response.body().string()).getJSONObject("user");
                        subscriber.onNext(user);
                        subscriber.onCompleted();
                    }
                    catch (Exception e) {
                        subscriber.onError(e);
                    }
                })
        );
    }

    public Observable<String> getMarkedDown(@NonNull String text) {

        FormBody body = new FormBody.Builder()
                .add("text", text)
                .build();

        Request request = new Request.Builder()
                .url("http://"+mHost+"/api/markdown_previews")
                .header("Cookie", "_nasulog_session="+ mToken)
                .post(body)
                .build();

        return Observable.create(subscriber -> {
            try {
                Response response = OkHttpHelper.getClient().newCall(request).execute();
                if(response.isSuccessful()) {
                    subscriber.onNext(response.body().string());
                    subscriber.onCompleted();
                }
                else subscriber.onError(new HttpError(response));
            } catch (IOException e) {
                subscriber.onError(e);
            }
        });
    }

    public Observable<JSONObject> submitPoem(String title, String description) {
        String url = "http://"+mHost+"/api/poems.json";

        JSONObject json = new JSONObject();
        try{
            json.put("poem", new JSONObject()
                    .put("title", title)
                    .put("description", description));
        }
        catch (JSONException e) {}

        Request request = new Request.Builder()
                .url(url)
                .header("Cookie", "_nasulog_session="+ mToken)
                .post(RequestBody.create(MediaType.parse("application/json"), json.toString()))
                .build();

        return Observable.create(subscriber -> {
            try {
                Response response = OkHttpHelper.getClient().newCall(request).execute();
                if (response.isSuccessful()) {
                    try {
                        JSONObject poem = new JSONObject(response.body().string()).getJSONObject("poem");
                        subscriber.onNext(poem);
                        subscriber.onCompleted();
                    } catch (Exception e) {
                        subscriber.onError(e);
                    }
                }
                else subscriber.onError(new HttpError(response));
            } catch (IOException e) {
                subscriber.onError(e);
            }
        });

    }

    public Observable<Boolean> setPoemRead(long poemId) {
        String url = "http://"+mHost+"/api/poems/"+poemId+"/read_poems.json";

        // ダミーのボディを付けないとOkHttpはPOSTしてくれないので
        FormBody body = new FormBody.Builder()
                .add("hoge", "hoge")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .header("Cookie", "_nasulog_session="+ mToken)
                .post(body)
                .build();

        return Observable.create(subscriber -> {
            try {
                Response response = OkHttpHelper.getClient().newCall(request).execute();
                if(response.isSuccessful()) {
                    subscriber.onNext(true);
                    subscriber.onCompleted();
                }
                else subscriber.onError(new HttpError(response));
            } catch (IOException e) {
                subscriber.onError(e);
            }
        });
    }
}
