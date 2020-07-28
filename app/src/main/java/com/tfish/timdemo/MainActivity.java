package com.tfish.timdemo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.tencent.imsdk.TIMCallBack;
import com.tencent.imsdk.TIMConversationType;
import com.tencent.imsdk.TIMFriendAllowType;
import com.tencent.imsdk.TIMFriendshipManager;
import com.tencent.imsdk.TIMUserProfile;
import com.tencent.qcloud.tim.uikit.TUIKit;
import com.tencent.qcloud.tim.uikit.base.IUIKitCallBack;
import com.tencent.qcloud.tim.uikit.component.TitleBarLayout;
import com.tencent.qcloud.tim.uikit.component.picture.imageEngine.impl.GlideEngine;
import com.tencent.qcloud.tim.uikit.modules.chat.GroupChatManagerKit;
import com.tencent.qcloud.tim.uikit.modules.chat.base.ChatInfo;
import com.tencent.qcloud.tim.uikit.modules.conversation.ConversationLayout;
import com.tencent.qcloud.tim.uikit.modules.conversation.ConversationListLayout;
import com.tencent.qcloud.tim.uikit.modules.conversation.ConversationManagerKit;
import com.tencent.qcloud.tim.uikit.modules.conversation.base.ConversationInfo;
import com.tencent.qcloud.tim.uikit.utils.FileUtil;
import com.tencent.qcloud.tim.uikit.utils.ToastUtil;

import java.util.HashMap;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FileUtil.initPath();
        GroupChatManagerKit.getInstance();


        final EditText editText = findViewById(R.id.et_userId);
        final EditText otherEditText = findViewById(R.id.et_other_userId);

        findViewById(R.id.button_jump)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MsgActivity.startActivity(MainActivity.this, editText.getText().toString());
                    }
                });

        findViewById(R.id.button_change_icon)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateProfile(editText.getText().toString());
                    }
                });

        findViewById(R.id.button_go_chart)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ChatActivity.startActivity(MainActivity.this, otherEditText.getText().toString());
                    }
                });

        findViewById(R.id.button_login)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 获取userSig函数
                        String userId = editText.getText().toString();
                        String userSig = GenerateTestUserSig.genTestUserSig(userId);
                        TUIKit.login(userId, userSig, new IUIKitCallBack() {
                            @Override
                            public void onError(String module, final int code, final String desc) {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        ToastUtil.toastLongMessage("登录失败, errCode = " + code + ", errInfo = " + desc);
                                    }
                                });
                            }

                            @Override
                            public void onSuccess(Object data) {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        ToastUtil.toastLongMessage("登录成功");
                                    }
                                });
                            }
                        });
                    }
                });

    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences shareInfo = getSharedPreferences(Constants.USERINFO, Context.MODE_PRIVATE);
    }

    private void updateProfile(String mIconUrl) {
        HashMap<String, Object> hashMap = new HashMap<>();

        mIconUrl = String.format("https://picsum.photos/id/%d/200/200", new Random().nextInt(1000));
        GlideEngine.loadImage((ImageView) findViewById(R.id.iv), Uri.parse(mIconUrl));

        // 头像
        if (!TextUtils.isEmpty(mIconUrl)) {
            hashMap.put(TIMUserProfile.TIM_PROFILE_TYPE_KEY_FACEURL, mIconUrl);
        }

        // 昵称
        String nickName = "";
        hashMap.put(TIMUserProfile.TIM_PROFILE_TYPE_KEY_NICK, nickName);

        // 个性签名
        String signature = "";
        hashMap.put(TIMUserProfile.TIM_PROFILE_TYPE_KEY_SELFSIGNATURE, signature);

        // 地区
        hashMap.put(TIMUserProfile.TIM_PROFILE_TYPE_KEY_LOCATION, "sz");

        // 加我验证方式
        String allowType = TIMFriendAllowType.TIM_FRIEND_ALLOW_ANY;
        hashMap.put(TIMUserProfile.TIM_PROFILE_TYPE_KEY_ALLOWTYPE, allowType);

        TIMFriendshipManager.getInstance().modifySelfProfile(hashMap, new TIMCallBack() {
            @Override
            public void onError(int i, String s) {
                ToastUtil.toastShortMessage("Error code = " + i + ", desc = " + s);
            }

            @Override
            public void onSuccess() {
                ToastUtil.toastShortMessage("modifySelfProfile success");
            }
        });
    }

    @Override
    protected void onStop() {
        ConversationManagerKit.getInstance().destroyConversation();
        super.onStop();
    }
}