package aicare.net.cn.sdk.ailinksdkdemoandroid;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

public class WifiDialog extends DialogFragment implements View.OnClickListener {

    /**
     * 是否显示
     */

    private EditText mEtName;
    private TextView mTvCancel, mTvSucceed, mTvTitle, mTvTitleHint;
    private boolean mShow;
    private boolean mCancelBlank;
    private Context mContext;
    private CharSequence mTitle = "";
    private CharSequence mTitleHint;
    private CharSequence mContent = "";
    private CharSequence mContentHint = "";
    private CharSequence mCancel = "";
    private OnDialogListener mOnDialogListener;




    public static WifiDialog newInstance() {
        return new WifiDialog();
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialogView = new Dialog(requireContext(), R.style.MyDialog);// 创建自定义样式dialog
        dialogView.setCancelable(false);//设置是否可以关闭
        dialogView.setCanceledOnTouchOutside(mCancelBlank);//设置点击空白处是否可以取消
        dialogView.setOnKeyListener((dialog, keyCode, event) -> {
            if (mCancelBlank) {
                return false;
            } else {
                //返回不关闭
                return keyCode == KeyEvent.KEYCODE_BACK;
            }
        });
        return dialogView;
//        return super.onCreateDialog(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_move_data, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContext = view.getContext();
        init(view);
        initData(mTitle, mTitleHint);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void init(View view) {
        mEtName = view.findViewById(R.id.et_move_data_context);
        mTvCancel = view.findViewById(R.id.tv_move_data_cancel);
        mTvSucceed = view.findViewById(R.id.tv_move_data_ok);
        mTvTitle = view.findViewById(R.id.tv_move_data_title);
        mTvTitleHint = view.findViewById(R.id.tv_move_data_title_hint);
        mTvCancel.setOnClickListener(this);
        mTvSucceed.setOnClickListener(this);
    }

    private void initData(CharSequence title, CharSequence titleHint) {
        setTitle(title, titleHint);
        setCancel("");
        setOk("");
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.tv_move_data_cancel) {
            if (mOnDialogListener != null)
                mOnDialogListener.tvCancelListener(v);
            dismiss();
        } else if (i == R.id.tv_move_data_ok) {
            if (mOnDialogListener != null) {
                String data = mEtName.getText().toString().trim();
                mOnDialogListener.tvSucceedListener(v, data);
            }
            dismiss();
        }
    }


    public WifiDialog setTitle(CharSequence title, CharSequence titleHint) {
        this.mTitle=title;
        this.mTitleHint=titleHint;
        if (mTvTitle != null && !title.equals(""))
            mTvTitle.setText(title);
        if (mTvTitleHint != null && !titleHint.equals(""))
            mTvTitleHint.setText(titleHint);
        return this;
    }

    public WifiDialog setCancel(CharSequence name) {
        if (mTvCancel != null && !name.equals(""))
            mTvCancel.setText(name);
        return this;
    }
    public WifiDialog setOk(CharSequence name){
        if (mTvSucceed != null && !name.equals(""))
            mTvSucceed.setText(name);
        return this;
    }

    public WifiDialog setOnDialogListener(OnDialogListener onDialogListener) {
        mOnDialogListener = onDialogListener;
        return this;
    }

    /**
     * 当前是否显示
     */
    public boolean isShow() {
        return mShow;
    }


    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        try {
            if (!mShow) {
                super.show(manager, tag);
                mShow = true;
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    public void show(@NonNull FragmentManager manager) {
        this.show(manager, "DialogFragment");
    }


    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        mShow = false;
    }


    @Override
    public void dismiss() {
        try {
            mShow = false;
            super.dismiss();
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public interface OnDialogListener {

        /**
         * 取消的点击事件
         */
        default void tvCancelListener(View v) {
        }

        /**
         * 成功的点击事件
         */
        default void tvSucceedListener(View v, String data) {
        }

        /**
         * 修改名称的控件
         */
        default void etModifyName(EditText v) {
        }


    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mShow = false;
    }


}
