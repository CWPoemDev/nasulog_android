package jp.co.crowdworks.android.nasulog.helper;

import okhttp3.Response;

public class RedirectNotAllowdError extends HttpError {
    public RedirectNotAllowdError(Response response) {
        super(response);
    }
}
