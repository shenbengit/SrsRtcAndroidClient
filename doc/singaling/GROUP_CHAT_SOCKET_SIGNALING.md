# 群聊主叫、被叫 Socket.IO 信令流程

范围：`MainViewModel.groupChat()` 进入的普通群聊，走 SFU 房间模式，复用 `CallerChatActivity` / `CalleeChatActivity`。不包含 `chatRoom()` 聊天室流程，也不包含 `p2p()` P2P 流程。

## 1. 入口

```text
MainViewModel.groupChat()
  -> CheckUserActivity(ChatMode.GROUP_MODE)
  -> CheckUserViewModel.toSfu(selectedUsers)
  -> CallerChatActivity(CALLEE_INFO_LIST)
```

被叫侧：

```text
notify_request_call
  -> MainViewModel.requestCall(bean)
  -> CalleeChatActivity(REQUEST_CALL)
```

群聊首邀和私聊的关键区别：选择人数大于 1 时，主叫发送 `req_invite_some_people`。

## 2. 通用回包和数据结构

通用 ack：

```json
{
  "code": 0,
  "data": {},
  "msg": "ok"
}
```

客户端以 `code == Constant.RESULT_OK` 判断成功，失败时使用 `msg`。

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

### ResInviteSomePeopleBean

`req_invite_some_people` 和 `req_invite_some_people_join_room` 成功 ack 的 `data`。

```json
{
  "callList": [
    { "...": "ClientInfoBean" }
  ],
  "busyList": [
    { "...": "ClientInfoBean" }
  ],
  "offlineOrNotExistsList": [
    { "userId": "10003" }
  ],
  "alreadyInRoomList": [
    { "...": "ClientInfoBean" }
  ],
  "roomId": "room_123"
}
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `callList` | `ClientInfoBean[]` | 实际可呼叫成员 |
| `busyList` | `ClientInfoBean[]` | 忙碌成员 |
| `offlineOrNotExistsList` | `{ userId: string }[]` | 离线或不存在成员 |
| `alreadyInRoomList` | `ClientInfoBean[]` | 已在房间内成员 |
| `roomId` | `String` | 房间号 |

### RequestCallBean

```json
{
  "inviteInfo": { "...": "ClientInfoBean" },
  "callList": [
    { "...": "ClientInfoBean" }
  ],
  "roomId": "room_123"
}
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `inviteInfo` | `ClientInfoBean` | 邀请人，即主叫 |
| `callList` | `ClientInfoBean[]?` | 主叫同时邀请的其他成员 |
| `roomId` | `String` | 房间号 |

### ResAlreadyInRoomBean

`req_accept_call` 成功 ack 的 `data`。

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

## 3. 群聊主叫流程

### 3.1 发起多人邀请

| 项 | 内容 |
| --- | --- |
| 客户端方法 | `CallSocketIoClient.reqInviteSomePeople(userIds)` |
| 事件名 | `req_invite_some_people` |
| 方向 | caller -> server |
| 传参 | 单个 JSON 对象 |

传参：

```json
{
  "userList": [
    { "userId": "10002" },
    { "userId": "10003" }
  ]
}
```

成功 ack：

```json
{
  "code": 0,
  "data": {
    "callList": [
      { "...": "ClientInfoBean" }
    ],
    "busyList": [
      { "...": "ClientInfoBean" }
    ],
    "offlineOrNotExistsList": [
      { "userId": "10003" }
    ],
    "alreadyInRoomList": [],
    "roomId": "room_123"
  },
  "msg": "ok"
}
```

主叫行为：

1. 保存 `roomId`。
2. 对 `callList` 中的成员先添加远端占位，`publishStreamUrl = null`。
3. 对 `busyList`、`offlineOrNotExistsList`、`alreadyInRoomList` 弹出提示。
4. 等待成员接听。

### 3.2 服务端通知被叫来电

| 项 | 内容 |
| --- | --- |
| 客户端回调 | `SignalEventCallback.requestCall(bean)` |
| 事件名 | `notify_request_call` |
| 方向 | server -> callees |
| 回参 | `RequestCallBean` |

`MainViewModel` 收到后打开 `CalleeChatActivity`。

### 3.3 主叫收到成员接听

| 项 | 内容 |
| --- | --- |
| 客户端回调 | `SignalEventCallback.acceptCall(bean)` |
| 事件名 | `notify_accept_call` |
| 方向 | server -> caller / room clients |
| 回参 | `AcceptCallBean` |

```json
{
  "userInfo": { "...": "ClientInfoBean" },
  "roomId": "room_123"
}
```

主叫停止铃声，UI 进入通话中，推本端 SRS 流，推流成功后发送 `req_publish_stream`。按当前代码实现，主叫每次收到 `notify_accept_call` 都会执行一次 `publishStream(publishStreamUrl)`。

### 3.4 通知本端推流地址

| 项 | 内容 |
| --- | --- |
| 客户端方法 | `CallSocketIoClient.reqPublishStream(roomId, publishStreamUrl)` |
| 事件名 | `req_publish_stream` |
| 方向 | client -> server |

```json
{
  "roomId": "room_123",
  "publishStreamUrl": "webrtc://host/app/stream"
}
```

### 3.5 收到拉流通知

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

客户端按 `userInfo.userId + userInfo.userType` 管理远端画面并拉流。

## 4. 群聊被叫流程

### 4.1 展示来电

被叫页从 intent 读取 `RequestCallBean`，保存 `roomId`，生成本端 `publishStreamUrl`，本地预览，展示来电 UI 并响铃。

### 4.2 被叫拒接

| 项 | 内容 |
| --- | --- |
| 客户端方法 | `CallSocketIoClient.reqRejectCall(roomId)` |
| 事件名 | `req_reject_call` |
| 方向 | callee -> server |
| 传参 | `roomId` 字符串 |

其他客户端收到 `notify_reject_call`：

```json
{
  "userInfo": { "...": "ClientInfoBean" },
  "callEnded": false,
  "roomId": "room_123"
}
```

`callEnded == true` 时结束通话页；否则只移除拒接成员。群聊通常是移除成员，房间继续存在。

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

被叫停止铃声，推本端 SRS 流，推流成功后发送 `req_publish_stream`，设置 `acceptedCall = true`，处理暂存的 `notify_play_stream`，并拉取 `alreadyInRoomList` 中已有成员的流。

### 4.4 被叫收到房间内其他事件

| 事件名 | 回参 | 已接听时行为 | 未接听时行为 |
| --- | --- | --- | --- |
| `notify_accept_call` | `AcceptCallBean` | 提示某成员接听 | 只更新 LiveData |
| `notify_play_stream` | `PlayStreamBean` | 立即拉流 | 暂存到 `tempPlayStreamList` |
| `notify_reject_call` | `RejectCallBean` | 移除成员或结束通话 | 从暂存列表移除 |
| `notify_hang_up` | `HangUpBean` | 移除成员或结束通话 | 从暂存列表移除 |
| `notify_offline_during_call` | `OfflineDuringCallBean` | 移除成员或结束通话 | 从暂存列表移除 |

## 5. 通话中继续邀请成员

主叫和已接听的被叫都可以添加成员。此时使用加入当前房间事件，不再使用首邀事件。

| 场景 | 事件名 | 方向 | 传参/回参 |
| --- | --- | --- | --- |
| 邀请单人进房间 | `req_invite_someone_join_room` | client -> server | `{ "userId": string, "roomId": string }`，ack `ResInviteeInfoBean` |
| 通知邀请单人进房间 | `notify_invite_someone_join_room` | server -> clients | `InviteSomeoneBean` |
| 邀请多人进房间 | `req_invite_some_people_join_room` | client -> server | `{ "userList": [{ "userId": string }], "roomId": string }`，ack `ResInviteSomePeopleBean` |
| 通知邀请多人进房间 | `notify_invite_some_people_join_room` | server -> clients | `InviteSomePeopleBean` |

## 6. 挂断、离线、状态重置

| 场景 | 事件名 | 方向 | 传参/回参 |
| --- | --- | --- | --- |
| 挂断 | `req_hang_up` | client -> server | `roomId`，ack `BaseResponseBean<Any>` |
| 通知挂断 | `notify_hang_up` | server -> clients | `HangUpBean` |
| 通话中离线 | `notify_offline_during_call` | server -> clients | `OfflineDuringCallBean` |
| 页面销毁重置 | `req_reset_status` | client -> server | 无传参，无 ack |

## 7. 事件速查表

| 场景 | 事件名 | 方向 | 传参/回参 |
| --- | --- | --- | --- |
| 发起群聊 | `req_invite_some_people` | caller -> server | `{ "userList": [{ "userId": string }] }`，ack `BaseResponseBean<ResInviteSomePeopleBean>` |
| 通知被叫来电 | `notify_request_call` | server -> callees | `RequestCallBean` |
| 被叫拒接 | `req_reject_call` | callee -> server | `roomId`，ack `BaseResponseBean<Any>` |
| 通知拒接 | `notify_reject_call` | server -> clients | `RejectCallBean` |
| 被叫接听 | `req_accept_call` | callee -> server | `roomId`，ack `BaseResponseBean<ResAlreadyInRoomBean>` |
| 通知接听 | `notify_accept_call` | server -> clients | `AcceptCallBean` |
| 通知推流地址 | `req_publish_stream` | client -> server | `{ "roomId": string, "publishStreamUrl": string }`，ack `BaseResponseBean<Any>` |
| 通知拉流 | `notify_play_stream` | server -> clients | `PlayStreamBean` |
| 邀请单人进当前房间 | `req_invite_someone_join_room` | client -> server | `{ "userId": string, "roomId": string }`，ack `BaseResponseBean<ResInviteeInfoBean>` |
| 邀请多人进当前房间 | `req_invite_some_people_join_room` | client -> server | `{ "userList": [{ "userId": string }], "roomId": string }`，ack `BaseResponseBean<ResInviteSomePeopleBean>` |
| 挂断 | `req_hang_up` | client -> server | `roomId`，ack `BaseResponseBean<Any>` |
| 通话中离线 | `notify_offline_during_call` | server -> clients | `OfflineDuringCallBean` |
| 状态重置 | `req_reset_status` | client -> server | 无 |

## 8. 代码来源

- `app/src/main/java/com/shencoder/srs_rtc_android_client/ui/main/MainViewModel.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/ui/check_user/CheckUserActivity.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/ui/check_user/CheckUserViewModel.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/widget/CheckUserView.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/ui/caller_chat/CallerChatActivity.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/ui/caller_chat/CallerChatViewModel.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/ui/callee_chat/CalleeChatActivity.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/ui/callee_chat/CalleeChatViewModel.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/helper/call/CallSocketIoClient.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/helper/call/SignalProtocols.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/helper/call/bean/*.kt`
