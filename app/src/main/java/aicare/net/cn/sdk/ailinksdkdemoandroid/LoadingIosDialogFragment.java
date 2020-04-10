package aicare.net.cn.sdk.ailinksdkdemoandroid;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


/**
 * elink-android<br>
 * LoadingIosDialogFragment<br>
 * xing<br>
 * 2019/2/26 16:07<br>
 * 加载提示框
 */
public class LoadingIosDialogFragment extends DialogFragment {
    private static String TAG = LoadingIosDialogFragment.class.getName();
    private final static int DISMISS_OUT = 1;
    /**
     * 默认超时时间
     */
    private int timeOut = 30;
    private boolean show = false;

    public boolean isShow() {
        return show;
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DISMISS_OUT:
                    LoadingIosDialogFragment.this.dismiss();
                    break;

            }
        }
    };


    @Override
    public void dismiss() {
        try {
            mHandler.removeMessages(DISMISS_OUT);
            if (getFragmentManager() != null)
                super.dismiss();
            show = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    public void show(@NonNull FragmentManager manager) {
        this.show(manager, "LoadingIosDialogFragment");
    }

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        try {
            super.show(manager, tag);
            show = true;
        } catch (Exception e) {
            show = false;
            e.printStackTrace();
        }
        mHandler.sendEmptyMessageDelayed(DISMISS_OUT, timeOut * 1000);
    }

    @Override
    public int show(@NonNull FragmentTransaction transaction, @Nullable String tag) {
        return super.show(transaction, tag);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getDialog() != null) {
            getDialog().setOnShowListener(null);
            getDialog().setOnCancelListener(null);
            getDialog().setOnDismissListener(null);
        }


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View v = inflater.inflate(R.layout.dialog_loading_ios, null);// 得到加载view
        Dialog loadingDialog = new Dialog(requireContext(), R.style.MyDialog);// 创建自定义样式dialog

        loadingDialog.setCancelable(false);//设置点击空白处是否可以取消
        //按返回键是否可以取消
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.setContentView(v);// 设置布局

        loadingDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                //返回不关闭
                return keyCode == KeyEvent.KEYCODE_BACK;
            }
        });

        return loadingDialog;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }
}
