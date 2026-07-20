# 私聊主叫、被叫 Socket.IO 信令流程

范围：`MainViewModel.privateChat()` 进入的普通私聊，走 SFU 房间模式，复用 `CallerChatActivity` / `CalleeChatActivity`。不包含 `p2p()` 的 `req_p2p_*`、`notify_p2p_*`、offer、answer、ice。

## 1. 入口

```text
MainViewModel.privateChat()
  -> CheckUserActivity(ChatMode.PRIVATE_MODE)
  -> CheckUserViewModel.toSfu(selectedUsers)
  -> CallerChatActivity(CALLEE_INFO_LIST)
```

被叫侧：

```text
notify_request_call
  -> MainViewModel.requestCall(bean)
  -> CalleeChatActivity(REQUEST_CALL)
```

Socket 连接地址：

```text
SIGNAL.SOCKET_IO_WSS_CLIENT_URL + "?userId={userId}"
```

通用 ack 回包：

```json
{
  "code": 0,
  "data": {},
  "msg": "ok"
}
```

客户端以 `code == Constant.RESULT_OK` 判断成功，失败时使用 `msg`。

## 2. 数据结构

### ClientInfoBean

```json
{
  "createdAt": "2022-01-01T00:00:00.000Z",
  "id": 1,
  "userId": "10001",
  "userType": "android",
  "username": "Alice"
}
```

### RequestCallBean

```json
{
  "inviteInfo": { "...": "ClientInfoBean" },
  "callList": null,
  "roomId": "room_123"
}
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `inviteInfo` | `ClientInfoBean` | 主叫用户信息 |
| `callList` | `ClientInfoBean[]?` | 同时被邀请的其他人；私聊通常为 `null` |
| `roomId` | `String` | 房间号 |

### ResInviteeInfoBean

```json
{
  "inviteeInfo": { "...": "ClientInfoBean" },
  "roomId": "room_123"
}
```

### ResAlreadyInRoomBean

```json
{
  "alreadyInRoomList": [
    {
      "userInfo": { "...": "ClientInfoBean" },
      "publishStreamUrl": "webrtc://host/app/stream"
    }
  ],
  "roomId": "room_123"
}
```

`publishStreamUrl` 可为 `null`，表示成员已进房但尚未推流。

## 3. 主叫流程

### 3.1 发起私聊

| 项 | 内容 |
| --- | --- |
| 客户端方法 | `CallSocketIoClient.reqInviteSomeone(userId)` |
| 事件名 | `req_invite_someone` |
| 方向 | caller -> server |
| 传参 | 单个 JSON 对象 |

传参：

```json
{
  "userId": "callee_user_id"
}
```

成功 ack：

```json
{
  "code": 0,
  "data": {
    "inviteeInfo": { "...": "ClientInfoBean" },
    "roomId": "room_123"
  },
  "msg": "ok"
}
```

主叫行为：

1. 保存 `roomId`。
2. 播放铃声并等待被叫接听。
3. 被叫接听前只预览本地流，不正式推流到 SRS。

### 3.2 收到被叫接听

| 项 | 内容 |
| --- | --- |
| 客户端回调 | `SignalEventCallback.acceptCall(bean)` |
| 事件名 | `notify_accept_call` |
| 方向 | server -> caller |
| 回参 | `AcceptCallBean` |

回参：

```json
{
  "userInfo": { "...": "ClientInfoBean" },
  "roomId": "room_123"
}
```

主叫停止铃声，UI 进入通话中，推本端 SRS 流，推流成功后发送 `req_publish_stream`。

### 3.3 通知本端推流地址

| 项 | 内容 |
| --- | --- |
| 客户端方法 | `CallSocketIoClient.reqPublishStream(roomId, publishStreamUrl)` |
| 事件名 | `req_publish_stream` |
| 方向 | client -> server |
| 传参 | 单个 JSON 对象 |

```json
{
  "roomId": "room_123",
  "publishStreamUrl": "webrtc://host/app/stream"
}
```

成功 ack：

```json
{
  "code": 0,
  "data": null,
  "msg": "ok"
}
```

### 3.4 收到拉流通知

| 项 | 内容 |
| --- | --- |
| 客户端回调 | `SignalEventCallback.playSteam(bean)` |
| 事件名 | `notify_play_stream` |
| 方向 | server -> clients |
| 回参 | `PlayStreamBean` |

```json
{
  "userInfo": { "...": "ClientInfoBean" },
  "publishStreamUrl": "webrtc://host/app/stream",
  "roomId": "room_123"
}
```

客户端使用 `publishStreamUrl` 拉取对端 SRS 流。

### 3.5 主叫挂断

| 项 | 内容 |
| --- | --- |
| 客户端方法 | `CallSocketIoClient.reqHangUp(roomId)` |
| 事件名 | `req_hang_up` |
| 方向 | caller -> server |
| 传参 | `roomId` 字符串 |

```json
"room_123"
```

其他端收到 `notify_hang_up`：

```json
{
  "userInfo": { "...": "ClientInfoBean" },
  "callEnded": true,
  "roomId": "room_123"
}
```

`callEnded == true` 时结束通话页；否则只移除该用户远端流。

## 4. 被叫流程

### 4.1 收到来电

| 项 | 内容 |
| --- | --- |
| 客户端回调 | `SignalEventCallback.requestCall(bean)` |
| 事件名 | `notify_request_call` |
| 方向 | server -> callee |
| 回参 | `RequestCallBean` |

被叫页保存 `roomId`，生成本端 `publishStreamUrl`，本地预览并展示来电 UI。

### 4.2 被叫拒接

| 项 | 内容 |
| --- | --- |
| 客户端方法 | `CallSocketIoClient.reqRejectCall(roomId)` |
| 事件名 | `req_reject_call` |
| 方向 | callee -> server |
| 传参 | `roomId` 字符串 |

其他端收到 `notify_reject_call`：

```json
{
  "userInfo": { "...": "ClientInfoBean" },
  "callEnded": true,
  "roomId": "room_123"
}
```

### 4.3 被叫接听

| 项 | 内容 |
| --- | --- |
| 客户端方法 | `CallSocketIoClient.reqAcceptCall(roomId)` |
| 事件名 | `req_accept_call` |
| 方向 | callee -> server |
| 传参 | `roomId` 字符串 |

成功 ack：

```json
{
  "code": 0,
  "data": {
    "alreadyInRoomList": [
      {
        "userInfo": { "...": "ClientInfoBean" },
        "publishStreamUrl": "webrtc://host/app/stream"
      }
    ],
    "roomId": "room_123"
  },
  "msg": "ok"
}
```

被叫停止铃声，推本端流，推流成功后发送 `req_publish_stream`，再拉取 `alreadyInRoomList` 或暂存的 `notify_play_stream`。

## 5. 异常和状态重置

| 场景 | 事件名 | 方向 | 传参/回参 |
| --- | --- | --- | --- |
| 通话中离线 | `notify_offline_during_call` | server -> clients | `OfflineDuringCallBean` |
| 页面销毁重置 | `req_reset_status` | client -> server | 无传参，无 ack |

`OfflineDuringCallBean`：

```json
{
  "userInfo": { "...": "ClientInfoBean" },
  "callEnded": true,
  "reason": "disconnect",
  "roomId": "room_123"
}
```

## 6. 通话中继续邀请

| 场景 | 事件名 | 传参 | ack data / 通知回参 |
| --- | --- | --- | --- |
| 邀请单人进当前房间 | `req_invite_someone_join_room` | `{ "userId": string, "roomId": string }` | `ResInviteeInfoBean` |
| 通知邀请单人进房间 | `notify_invite_someone_join_room` | server -> clients | `InviteSomeoneBean` |
| 邀请多人进当前房间 | `req_invite_some_people_join_room` | `{ "userList": [{ "userId": string }], "roomId": string }` | `ResInviteSomePeopleBean` |
| 通知邀请多人进房间 | `notify_invite_some_people_join_room` | server -> clients | `InviteSomePeopleBean` |

## 7. 事件速查表

| 场景 | 事件名 | 方向 | 传参/回参 |
| --- | --- | --- | --- |
| 发起私聊 | `req_invite_someone` | caller -> server | `{ "userId": string }`，ack `BaseResponseBean<ResInviteeInfoBean>` |
| 通知被叫来电 | `notify_request_call` | server -> callee | `RequestCallBean` |
| 被叫拒接 | `req_reject_call` | callee -> server | `roomId`，ack `BaseResponseBean<Any>` |
| 通知拒接 | `notify_reject_call` | server -> clients | `RejectCallBean` |
| 被叫接听 | `req_accept_call` | callee -> server | `roomId`，ack `BaseResponseBean<ResAlreadyInRoomBean>` |
| 通知接听 | `notify_accept_call` | server -> clients | `AcceptCallBean` |
| 通知推流地址 | `req_publish_stream` | client -> server | `{ "roomId": string, "publishStreamUrl": string }`，ack `BaseResponseBean<Any>` |
| 通知拉流 | `notify_play_stream` | server -> clients | `PlayStreamBean` |
| 挂断 | `req_hang_up` | client -> server | `roomId`，ack `BaseResponseBean<Any>` |
| 通知挂断 | `notify_hang_up` | server -> clients | `HangUpBean` |
| 通话中离线 | `notify_offline_during_call` | server -> clients | `OfflineDuringCallBean` |
| 状态重置 | `req_reset_status` | client -> server | 无 |

## 8. 代码来源

- `app/src/main/java/com/shencoder/srs_rtc_android_client/ui/main/MainViewModel.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/ui/caller_chat/CallerChatActivity.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/ui/caller_chat/CallerChatViewModel.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/ui/callee_chat/CalleeChatActivity.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/ui/callee_chat/CalleeChatViewModel.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/helper/call/CallSocketIoClient.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/helper/call/SignalProtocols.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/helper/call/bean/*.kt`
