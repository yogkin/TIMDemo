package com.tfish.timdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.tencent.imsdk.TIMConversationType;
import com.tencent.qcloud.tim.uikit.modules.chat.ChatLayout;
import com.tencent.qcloud.tim.uikit.modules.chat.base.ChatInfo;
import com.tencent.qcloud.tim.uikit.modules.conversation.base.ConversationInfo;

public class ChatActivity extends AppCompatActivity {

    private static final String CHAT_INFO = "CHAT_INFO";

    static void startActivity(Context context, String userId) {
        Intent intent = new Intent(context, ChatActivity.class);
        ChatInfo chatInfo = new ChatInfo();
        chatInfo.setType(TIMConversationType.C2C);
        chatInfo.setId(userId);
        chatInfo.setChatName(userId);
        intent.putExtra(CHAT_INFO, chatInfo);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        //从布局文件中获取聊天面板组件
        ChatLayout mChatLayout = findViewById(R.id.chat_layout);

        //单聊组件的默认UI和交互初始化
        mChatLayout.initDefault();

        /*
         * 需要聊天的基本信息
         */
        ChatInfo mChatInfo = (ChatInfo) getIntent().getExtras().getSerializable(CHAT_INFO);

        mChatLayout.setChatInfo(mChatInfo);

    }

}
