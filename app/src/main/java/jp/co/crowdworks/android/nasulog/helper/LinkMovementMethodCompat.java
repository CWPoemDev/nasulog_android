package jp.co.crowdworks.android.nasulog.helper;

import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;

public class LinkMovementMethodCompat extends LinkMovementMethod {
    @Override
    public boolean canSelectArbitrarily() {
        return true;
    }

    public static MovementMethod getInstance() {
        if (sInstance == null)
            sInstance = new LinkMovementMethodCompat();

        return sInstance;
    }

    private static LinkMovementMethodCompat sInstance;
}