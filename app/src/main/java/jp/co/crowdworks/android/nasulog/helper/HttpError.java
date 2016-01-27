package jp.co.crowdworks.android.nasulog.helper;

import okhttp3.Response;

public class HttpError extends Exception {
    private Response mResponse;
    private HttpError(){}
    public HttpError(Response response) {
        mResponse = response;
    }
    public int code() {
        return mResponse.code();
    }
    public String message() {
        return mResponse.message();
    }

}
