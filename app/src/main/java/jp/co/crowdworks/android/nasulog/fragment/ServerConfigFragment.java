package jp.co.crowdworks.android.nasulog.fragment;

import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import jp.co.crowdworks.android.nasulog.Prefs;
import jp.co.crowdworks.android.nasulog.R;

public class ServerConfigFragment extends AbstractFragment {
    public ServerConfigFragment(){}

    @Override
    protected int getLayout() {
        return R.layout.serverconfig_screen;
    }

    @Override
    protected void onSetupRootView() {
        setupEditor();

    }

    private void setupEditor(){
        EditText editor = (EditText) mRootView.findViewById(R.id.editor_server_config);
        editor.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_NEXT){
                    return handleNext();
                }
                return false;
            }
        });

        mRootView.findViewById(R.id.btn_next).setOnClickListener(v -> {
            handleNext();
        });
    }

    private boolean handleNext(){
        EditText editor = (EditText) mRootView.findViewById(R.id.editor_server_config);
        String input = editor.getText().toString();
        if (TextUtils.isEmpty(input)) return false;

        return Prefs.get(getContext()).edit()
                .putString(Prefs.KEY_SERVER, input.trim())
                .commit();
    }
}
