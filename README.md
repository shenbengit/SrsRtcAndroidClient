# SrsRtcAndroidClient
基于[SRS](https://github.com/ossrs/srs)视频服务器实现简易音视频通话系统——Android客户端

## 系统组成
- 信令服务器[srs-rtc-server](https://github.com/shenbengit/srs-rtc-server) 
- Android客户端[SrsRtcAndroidClient](https://github.com/shenbengit/SrsRtcAndroidClient) 
- Web客户端[srs-rtc-web-client](https://github.com/shenbengit/srs-rtc-web-client) （功能开发中...）
## 功能特点
- 支持用户注册、登录
- 支持私聊
- 支持群聊
- 支持聊天室
- 支持私聊、群聊中继续添加会见人

## 运行效果

## Getting Started

### 依赖环境
- srs-rtc-server用户注册、信令服务，部署步骤详见[srs-rtc-server](https://github.com/shenbengit/srs-rtc-server)。
- SRS视频服务器，部署步骤详见[SRS-Wiki](https://github.com/ossrs/srs/wiki/v4_CN_Home#getting-started)，启用WebRTC。

### 配置文件修改
修改[Constant.kt](https://github.com/shenbengit/SrsRtcAndroidClient/blob/master/app/src/main/java/com/shencoder/srs_rtc_android_client/constant/Constant.kt)；
- 信令服务相关参数

```koltin
/**
 * 信令服务相关
 */
object SIGNAL {
    /**
     * 信令服务地址
     * ip或域名
     */
    const val SERVER_ADDRESS = "192.168.10.185"

    /**
     * api请求http端口
     */
    const val API_HTTP_PORT = 9898

    /**
     * api请求https端口
     */
    const val API_HTTPS_PORT = 9899

    /**
     * socket.io http端口
     */
    const val SOCKET_IO_IP_HTTP_PORT = 9998

    /**
     * socket.io https端口
     */
    const val SOCKET_IO_IP_HTTPS_PORT = 9999
}
```
- SRS服务相关参数
```kotlin
/**
 * SRS服务相关
 */
object SRS {
    /**
     * SRS服务地址
     * ip或域名
     */
    const val SERVER_ADDRESS = "192.168.10.185"

    /**
     * SRS服务api请求http端口
     */
    const val API_HTTP_PORT = 1985

    /**
     * SRS服务api请求https端口
     */
    const val API_HTTPS_PORT = 1990
}
```

## [LICENSE](https://github.com/shenbengit/SrsRtcAndroidClient/blob/master/LICENSE)
