# 聊天室加入 / 退出 Socket.IO 信令

范围：`MainViewModel.chatRoom()` 进入聊天室页面后的流程，重点整理 `req_join_chat_room` 和 `req_leave_chat_room`，以及对应的服务端通知。

## 1. 入口流程

```text
MainViewModel.chatRoom()
  -> EnterRoomIdActivity
  -> ChatRoomActivity
  -> ChatRoomViewModel.joinChatRoom()
  -> CallSocketIoClient.reqJoinChatRoom(roomId)
```

退出流程：

```text
ChatRoomActivity
  -> CallLayout.hangUpCall()
  -> ChatRoomViewModel.leaveChatRoom()
  -> CallSocketIoClient.reqLeaveChatRoom(roomId)
```

## 2. 公共回包结构

聊天室相关请求的 ack 都走 `BaseResponseBean<T>`：

```json
{
  "code": 0,
  "data": {},
  "msg": "ok"
}
```

判定规则：

- `code == Constant.RESULT_OK` 认为成功
- 失败时读取 `msg`

## 3. 相关数据结构

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

字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `createdAt` | `String` | 创建时间 |
| `id` | `Long` | 服务端主键 |
| `userId` | `String` | 用户 ID |
| `userType` | `String` | 用户类型 |
| `username` | `String` | 用户名 |

### ResAlreadyInRoomBean

`req_join_chat_room` 成功后的 `data`：

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

字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `alreadyInRoomList` | `ResClientStreamBean[]` | 房间里已存在的成员流 |
| `roomId` | `String` | 房间号 |

`ResClientStreamBean`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `userInfo` | `ClientInfoBean` | 成员信息 |
| `publishStreamUrl` | `String?` | 推流地址，`null` 表示已在房间但尚未推流 |

### JoinChatRoomBean / LeaveChatRoomBean

服务端通知使用同样的结构：

```json
{
  "userInfo": { "...": "ClientInfoBean" },
  "roomId": "room_123"
}
```

## 4. 加入聊天室

### 4.1 请求

| 项 | 内容 |
| --- | --- |
| 方法 | `CallSocketIoClient.reqJoinChatRoom(roomId)` |
| 事件名 | `req_join_chat_room` |
| 方向 | client -> server |
| 传参 | `arrayOf(roomId)`，单个字符串 |

示例：

```json
"room_123"
```

### 4.2 ack 回包

| 项 | 内容 |
| --- | --- |
| 返回类型 | `BaseResponseBean<ResAlreadyInRoomBean>` |
| 成功数据 | `alreadyInRoomList`、`roomId` |
| 失败数据 | `code`、`msg` |

成功示例：

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

### 4.3 服务端主动通知有人进入聊天室

| 项 | 内容 |
| --- | --- |
| 事件名 | `notify_join_chat_room` |
| 方向 | server -> clients |
| 回参 | `JoinChatRoomBean` |
| 处理入口 | `SignalEventCallback.joinChatRoom(bean)` |

作用：

- `ChatRoomViewModel` 把 `bean.userInfo` 写入 `joinChatRoomLiveData`
- `ChatRoomActivity` 收到后把新成员加入播放列表

### 4.4 加入后的本地行为

当前实现里，`req_join_chat_room` 成功后还会：

1. 遍历 `alreadyInRoomList`
2. 对每个已在房间内但已推流的成员调用 `addPlayStream(...)`
3. 本地预览推流成功后，再发送 `req_publish_stream`
4. 服务端随后广播 `notify_play_stream`

`notify_play_stream` 的结构：

```json
{
  "userInfo": { "...": "ClientInfoBean" },
  "publishStreamUrl": "webrtc://host/app/stream",
  "roomId": "room_123"
}
```

## 5. 退出聊天室

### 5.1 请求

| 项 | 内容 |
| --- | --- |
| 方法 | `CallSocketIoClient.reqLeaveChatRoom(roomId)` |
| 事件名 | `req_leave_chat_room` |
| 方向 | client -> server |
| 传参 | `arrayOf(roomId)`，单个字符串 |

示例：

```json
"room_123"
```

### 5.2 ack 回包

| 项 | 内容 |
| --- | --- |
| 返回类型 | `BaseResponseBean<Any>` |
| 成功判断 | `code == Constant.RESULT_OK` |
| 失败数据 | `code`、`msg` |

成功后客户端只使用成功码，不读取 `data`。

### 5.3 服务端主动通知有人离开聊天室

| 项 | 内容 |
| --- | --- |
| 事件名 | `notify_leave_chat_room` |
| 方向 | server -> clients |
| 回参 | `LeaveChatRoomBean` |
| 处理入口 | `SignalEventCallback.leaveChatRoom(bean)` |

作用：

- `ChatRoomViewModel` 把 `bean.userInfo` 写入 `leaveChatRoomLiveData`
- `ChatRoomActivity` 收到后从播放列表移除该成员

### 5.4 本地退出行为

当前实现里，点击挂断后：

1. `CallLayout.hangUpCall()` 触发 `ChatRoomViewModel.leaveChatRoom()`
2. 退出成功后先 toast
3. 延迟返回上一页

## 6. 代码来源

- `app/src/main/java/com/shencoder/srs_rtc_android_client/ui/main/MainViewModel.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/ui/chat_room/EnterRoomIdActivity.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/ui/chat_room/ChatRoomActivity.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/ui/chat_room/ChatRoomViewModel.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/helper/call/CallSocketIoClient.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/helper/call/SignalProtocols.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/helper/call/SignalEventCallback.kt`
- `app/src/main/java/com/shencoder/srs_rtc_android_client/helper/call/bean/*.kt`
