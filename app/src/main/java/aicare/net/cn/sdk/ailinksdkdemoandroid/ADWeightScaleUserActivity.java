package aicare.net.cn.sdk.ailinksdkdemoandroid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import cn.net.aicare.modulelibrary.module.ADWeight.ADWeightScaleBleConfig;
import cn.net.aicare.modulelibrary.module.ADWeight.ADWeightScaleUserData;


/**
 * xing<br>
 * 2019/11/14<br>
 * java类作用描述
 */
public class ADWeightScaleUserActivity extends AppCompatActivity implements View.OnClickListener {


    private ListView listView;
    private Context mContext;
    private UserAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(Activity.RESULT_OK);
        setContentView(R.layout.activity_ad_weight_scale_user);
        findViewById(R.id.back_btn).setOnClickListener(this);
        findViewById(R.id.add_user_btn).setOnClickListener(this);
        listView = findViewById(R.id.listView);
        mContext = this;
        mAdapter = new UserAdapter();
        listView.setAdapter(mAdapter);
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.back_btn:
                finish();
                break;
            case R.id.add_user_btn:
                if (ADWeightScaleCmdActivity.sADWeightScaleUserDataList.size() < 8) {
                    ADWeightScaleCmdActivity.mUserId++;
                    ADWeightScaleUserData adWeightScaleUserData = new ADWeightScaleUserData();
                    adWeightScaleUserData.setUserId(ADWeightScaleCmdActivity.mUserId);
                    adWeightScaleUserData.setSex(ADWeightScaleBleConfig.SEX.MALE);
                    adWeightScaleUserData.setAge(20);
                    adWeightScaleUserData.setHeight(170);
                    adWeightScaleUserData.setWeight(50);
                    adWeightScaleUserData.setAdc(500);
                    ADWeightScaleCmdActivity.sADWeightScaleUserDataList.add(adWeightScaleUserData);
                    mAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(mContext, "最多只允许添加8个用户", Toast.LENGTH_SHORT).show();
                }
                break;

        }

    }


    private class UserAdapter extends BaseAdapter implements View.OnClickListener,
            RadioGroup.OnCheckedChangeListener {


        @Override
        public int getCount() {
            return ADWeightScaleCmdActivity.sADWeightScaleUserDataList.size();
        }

        @Override
        public Object getItem(int position) {
            return ADWeightScaleCmdActivity.sADWeightScaleUserDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null || convertView.getTag() == null) {
                convertView = LayoutInflater.from(mContext)
                        .inflate(R.layout.item_ad_weight_scale, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            ADWeightScaleUserData adWeightScaleUserData =
                    ADWeightScaleCmdActivity.sADWeightScaleUserDataList
                    .get(position);
            viewHolder.user_id_ctv.setText(adWeightScaleUserData.getUserId() + "");
            viewHolder.user_id_ctv.setChecked(adWeightScaleUserData.getUserId() == ADWeightScaleCmdActivity.sCheckedUserId);
            viewHolder.user_id_ctv.setOnClickListener(this);
            viewHolder.user_id_ctv.setTag(position);
            int sex = adWeightScaleUserData.getSex();
            viewHolder.sex_radioGroup.check(sex == ADWeightScaleBleConfig.SEX.MALE ?
                    R.id.man_radioButton : R.id.female_radioButton);
            viewHolder.sex_radioGroup.setTag(position);
            viewHolder.sex_radioGroup.setOnCheckedChangeListener(this);
            viewHolder.tv_item_age.setText(adWeightScaleUserData.getAge() + "");
            viewHolder.tv_item_height.setText(adWeightScaleUserData.getHeight() + "");
            viewHolder.tv_item_weight.setText(adWeightScaleUserData.getWeight() + "");
            viewHolder.tv_item_adc.setText(adWeightScaleUserData.getAdc() + "");

            viewHolder.sb_item_age.setProgress(adWeightScaleUserData.getAge());
            viewHolder.sb_item_age.setTag(position);
            viewHolder.sb_item_age.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    refreshSeekBar(seekBar,progress,viewHolder.tv_item_age);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    notifyDataSetChanged();
                }
            });
            viewHolder.sb_item_height.setProgress(adWeightScaleUserData.getHeight());
            viewHolder.sb_item_height.setTag(position);
            viewHolder.sb_item_height.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    refreshSeekBar(seekBar,progress,viewHolder.tv_item_height);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    notifyDataSetChanged();
                }
            });
            viewHolder.sb_item_weight.setProgress(adWeightScaleUserData.getWeight());
            viewHolder.sb_item_weight.setTag(position);
            viewHolder.sb_item_weight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    refreshSeekBar(seekBar,progress,viewHolder.tv_item_weight);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    notifyDataSetChanged();
                }
            });
            viewHolder.sb_item_adc.setProgress(adWeightScaleUserData.getAdc());
            viewHolder.sb_item_adc.setTag(position);
            viewHolder.sb_item_adc.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    refreshSeekBar(seekBar,progress,viewHolder.tv_item_adc);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    notifyDataSetChanged();
                }
            });
            convertView.setOnLongClickListener(v -> {
                {
                    new AlertDialog.Builder(mContext).setMessage("确定删除?")
                            .setPositiveButton("确定", (dialog, which) -> {
                                ADWeightScaleUserData adWeightScaleUserData1 =
                                        ADWeightScaleCmdActivity.sADWeightScaleUserDataList
                                        .get(position);
                                if (ADWeightScaleCmdActivity.sCheckedUserId != adWeightScaleUserData1
                                        .getUserId()) {
                                    ADWeightScaleCmdActivity.sADWeightScaleUserDataList.remove(position);
                                    notifyDataSetChanged();
                                } else {
                                    Toast.makeText(mContext, "选中不可删除", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                            .create()
                            .show();


                }
                return true;
            });
            return convertView;
        }


        @Override
        public void onClick(View v) {
            if (v.getTag() == null) {
                return;
            }
            if (v.getId() == R.id.user_id_ctv) {
                int po = (int) v.getTag();
                ADWeightScaleUserData adWeightScaleUserData =
                        ADWeightScaleCmdActivity.sADWeightScaleUserDataList
                        .get(po);
                if (adWeightScaleUserData.getUserId() == ADWeightScaleCmdActivity.sCheckedUserId)
                    return;
                ADWeightScaleCmdActivity.sCheckedUserId = adWeightScaleUserData.getUserId();
                notifyDataSetChanged();
            }
        }

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (group.getTag() == null) {
                return;
            }
            int po = (int) group.getTag();
            if (po == -1)
                return;
            ADWeightScaleCmdActivity.sADWeightScaleUserDataList.get(po)
                    .setSex((group.getCheckedRadioButtonId() == R.id.man_radioButton) ?
                            ADWeightScaleBleConfig.SEX.MALE : ADWeightScaleBleConfig.SEX.FEMALE);
        }


        private void refreshSeekBar(SeekBar seekBar, int progress, TextView textView) {
            if (seekBar.getTag() == null) {
                return;
            }
            int po = (int) seekBar.getTag();
            if (po == -1)
                return;
            ADWeightScaleUserData adWeightScaleUserData =
                    ADWeightScaleCmdActivity.sADWeightScaleUserDataList
                    .get(po);
            switch (seekBar.getId()) {
                case R.id.sb_item_age:
                    adWeightScaleUserData.setAge(progress);
                    break;
                case R.id.sb_item_height:
                    adWeightScaleUserData.setHeight(progress);
                    break;
                case R.id.sb_item_weight:
                    adWeightScaleUserData.setWeight(progress);
                    break;
                case R.id.sb_item_adc:
                    adWeightScaleUserData.setAdc(progress);
                    break;
            }
            textView.setText(progress + "");
            ADWeightScaleCmdActivity.sADWeightScaleUserDataList.set(po, adWeightScaleUserData);
        }


        class ViewHolder {

            CheckedTextView user_id_ctv;
            RadioGroup sex_radioGroup;
            RadioButton man_radioButton;
            RadioButton female_radioButton;
            TextView tv_item_age;
            TextView tv_item_height;
            TextView tv_item_weight;
            TextView tv_item_adc;
            SeekBar sb_item_age;
            SeekBar sb_item_height;
            SeekBar sb_item_weight;
            SeekBar sb_item_adc;

            public ViewHolder(View view) {
                user_id_ctv = view.findViewById(R.id.user_id_ctv);
                sex_radioGroup = view.findViewById(R.id.sex_radioGroup);
                man_radioButton = view.findViewById(R.id.man_radioButton);
                female_radioButton = view.findViewById(R.id.female_radioButton);
                tv_item_age = view.findViewById(R.id.tv_item_age);
                tv_item_height = view.findViewById(R.id.tv_item_height);
                tv_item_weight = view.findViewById(R.id.tv_item_weight);
                tv_item_adc = view.findViewById(R.id.tv_item_adc);

                sb_item_age = view.findViewById(R.id.sb_item_age);
                sb_item_height = view.findViewById(R.id.sb_item_height);
                sb_item_weight = view.findViewById(R.id.sb_item_weight);
                sb_item_adc = view.findViewById(R.id.sb_item_adc);
            }
        }

    }


}
