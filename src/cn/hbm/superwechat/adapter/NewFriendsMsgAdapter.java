/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.hbm.superwechat.adapter;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;

import cn.hbm.superwechat.R;
import cn.hbm.superwechat.SuperWeChatApplication;
import cn.hbm.superwechat.bean.GroupAvatar;
import cn.hbm.superwechat.bean.Result;
import cn.hbm.superwechat.bean.UserAvatar;
import cn.hbm.superwechat.data.OkHttpUtils2;
import cn.hbm.superwechat.db.InviteMessgeDao;
import cn.hbm.superwechat.domain.InviteMessage;
import cn.hbm.superwechat.domain.InviteMessage.InviteMesageStatus;
import cn.hbm.superwechat.task.DowAllMemberListTask;
import cn.hbm.superwechat.utils.I;
import cn.hbm.superwechat.utils.UserUtils;
import cn.hbm.superwechat.utils.Utils;

public class NewFriendsMsgAdapter extends ArrayAdapter<InviteMessage> {

    private Context context;
    private InviteMessgeDao messgeDao;

    public NewFriendsMsgAdapter(Context context, int textViewResourceId, List<InviteMessage> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        messgeDao = new InviteMessgeDao(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.row_invite_msg, null);
            holder.avator = (ImageView) convertView.findViewById(R.id.avatar);
            holder.reason = (TextView) convertView.findViewById(R.id.message);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.status = (Button) convertView.findViewById(R.id.user_state);
            holder.groupContainer = (LinearLayout) convertView.findViewById(R.id.ll_group);
            holder.groupname = (TextView) convertView.findViewById(R.id.tv_groupName);
            // holder.time = (TextView) convertView.findViewById(R.id.time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String str1 = context.getResources().getString(R.string.Has_agreed_to_your_friend_request);
        String str2 = context.getResources().getString(R.string.agree);

        String str3 = context.getResources().getString(R.string.Request_to_add_you_as_a_friend);
        String str4 = context.getResources().getString(R.string.Apply_to_the_group_of);
        String str5 = context.getResources().getString(R.string.Has_agreed_to);
        String str6 = context.getResources().getString(R.string.Has_refused_to);
        final InviteMessage msg = getItem(position);
        if (msg != null) {
            if (msg.getGroupId() != null) { // 显示群聊提示
                holder.groupContainer.setVisibility(View.VISIBLE);
                holder.groupname.setText(msg.getGroupName());
            } else {
                holder.groupContainer.setVisibility(View.GONE);
            }

            holder.reason.setText(msg.getReason());
//			holder.name.setText(msg.getFrom());
            // holder.time.setText(DateUtils.getTimestampString(new
            // Date(msg.getTime())));
            if (msg.getStatus() == InviteMesageStatus.BEAGREED) {
                holder.status.setVisibility(View.INVISIBLE);
                holder.reason.setText(str1);
            } else if (msg.getStatus() == InviteMesageStatus.BEINVITEED || msg.getStatus() == InviteMesageStatus.BEAPPLYED) {
                holder.status.setVisibility(View.VISIBLE);
                holder.status.setEnabled(true);
                holder.status.setBackgroundResource(android.R.drawable.btn_default);
                holder.status.setText(str2);
                if (msg.getStatus() == InviteMesageStatus.BEINVITEED) {
                    if (msg.getReason() == null) {
                        // 如果没写理由
                        holder.reason.setText(str3);
                    }
                } else { //入群申请
                    if (TextUtils.isEmpty(msg.getReason())) {
                        holder.reason.setText(str4 + msg.getGroupName());
                    }
                }
                // 设置点击事件
                holder.status.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // 同意别人发的好友请求
                        acceptInvitation(holder.status, msg);
                    }
                });
            } else if (msg.getStatus() == InviteMesageStatus.AGREED) {
                holder.status.setText(str5);
                holder.status.setBackgroundDrawable(null);
                holder.status.setEnabled(false);
            } else if (msg.getStatus() == InviteMesageStatus.REFUSED) {
                holder.status.setText(str6);
                holder.status.setBackgroundDrawable(null);
                holder.status.setEnabled(false);
            }

            //设置好友请求头像
            UserUtils.setMyAvatar(context, msg.getFrom(), holder.avator);
            //设置昵称http://localhost:8080/SuperWeChatServer/Server?request=find_user&m_user_nam
            String url = I.SERVER_URL + "?request=find_user&m_user_name=" + msg.getFrom();
            OkHttpUtils2<String> utils2 = new OkHttpUtils2<String>();
            utils2.url(url)
                    .targetClass(String.class)
                    .execute(new OkHttpUtils2.OnCompleteListener<String>() {
                        @Override
                        public void onSuccess(String s) {
                            Result result = Utils.getResultFromJson(s, UserAvatar.class);
                            if (result != null && result.isRetMsg()) {
                                UserAvatar user = (UserAvatar) result.getRetData();
                                if (user == null) {
                                    holder.name.setText(msg.getFrom());
                                } else {
                                    UserUtils.setMyNewUserNick(user, holder.name);
                                }
                            } else {
                                holder.name.setText(msg.getFrom());

                            }
                        }

                        @Override
                        public void onError(String error) {

                        }
                    });
        }

        return convertView;
    }

    /**
     * 同意好友请求或者群申请
     *
     * @param button
     * @param msg
     */
    private void acceptInvitation(final Button button, final InviteMessage msg) {
        final ProgressDialog pd = new ProgressDialog(context);
        String str1 = context.getResources().getString(R.string.Are_agree_with);
        final String str2 = context.getResources().getString(R.string.Has_agreed_to);
        final String str3 = context.getResources().getString(R.string.Agree_with_failure);
        pd.setMessage(str1);
        pd.setCanceledOnTouchOutside(false);
        pd.show();

        new Thread(new Runnable() {
            public void run() {
                // 调用sdk的同意方法
                try {
                    if (msg.getGroupId() == null) { //同意好友请求
                        EMChatManager.getInstance().acceptInvitation(msg.getFrom());

                    } else {
                        //同意加群申请
                        EMGroupManager.getInstance().acceptApplication(msg.getFrom(), msg.getGroupId());
                        //群主同意新成员入群申请后,本地服务器数据库添加新群成员到群用户表
                        addSuperGroup(msg.getFrom(), msg.getGroupId());

                    }
                    ((Activity) context).runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            pd.dismiss();
                            button.setText(str2);
                            msg.setStatus(InviteMesageStatus.AGREED);
                            // 更新db
                            ContentValues values = new ContentValues();
                            values.put(InviteMessgeDao.COLUMN_NAME_STATUS, msg.getStatus().ordinal());
                            messgeDao.updateMessage(msg.getId(), values);
                            button.setBackgroundDrawable(null);
                            button.setEnabled(false);

                        }
                    });
                } catch (final Exception e) {
                    ((Activity) context).runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            pd.dismiss();
                            Toast.makeText(context, str3 + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        }).start();
    }

    //群主同意新成员入群申请后,本地服务器数据库添加新群成员到群用户表
    private void addSuperGroup(String newGroupUser, final String groupId) {
        final String addGroupUserUrl = I.SERVER_URL + "?request=add_group_member&m_member_user_name=" + newGroupUser + "&m_member_group_hxid=" + groupId;
        OkHttpUtils2<String> utils2 = new OkHttpUtils2<String>();
        utils2.url(addGroupUserUrl)
                .targetClass(String.class)
                .execute(new OkHttpUtils2.OnCompleteListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        Log.i("main", "addGuperUserUrl=" + addGroupUserUrl + "\ns=" + s);
                        if (s == null) {
                            return;
                        }
                        Result result = Utils.getResultFromJson(s, GroupAvatar.class);
                        if (result.isRetMsg()) {
                            SuperWeChatApplication.mMyUtils.toast(context, "本地服务器添加新群成员成功");
                            new DowAllMemberListTask(context, groupId).dowAllMemberLsit();
                        }


                    }

                    @Override
                    public void onError(String error) {

                    }
                });
    }

    private static class ViewHolder {
        ImageView avator;
        TextView name;
        TextView reason;
        Button status;
        LinearLayout groupContainer;
        TextView groupname;
        // TextView time;
    }

}
