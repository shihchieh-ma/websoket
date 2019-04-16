# websoket
okhttp的websocket封装,添加重连机制,消息分发等

有两个模块,`websocket_core`进行连接以及重连的工作.`websocket_dispatch`提供创建`WsHelper`
对象的方法,`WsHelper`暴露连接断开发送消息的方法.且对状态和消息的进行分类处理回调.
</br>


##调用


**创建`WsHelper`**

	WsHelper wsHelper = new WsHelper.Builder()
                .url("")//ws地址,*必填
                .netStateService(getApplicationContext())//启动网络状态变化监听的服务,*必填
                .autoReconnect(true)//自动重连,默认true
                .autoReconnectTime(2000)//重连时间,默认失败2s后重连
                .pingSpace(2000)//发ping间隔,默认2s
                .enanbleLog(true)//打印日志,默认为true
                .callTimeOut(10)//连接整个过程的超时时长,默认20s
                .connectTimeOut(5)//连接超时时长,默认10s
                .readTimeOut(5)//读取超时时长,默认10s
                .wirteTimeOut(5)//写超时时长,默认10s
                .msgListener(this)//消息回调
                .signalEventListener(this)//信令回调
                .wsStateListener(this)//ws状态变更回调
                .build();


**连接**
	
	wsHelper.startWsConnect();//开始连接
	wsHelper.disWsConnect();//断开连接

**消息发送**

	wsHelper.sendMessage("消息,一般是json格式");
	

##回调

**OnWsStateListener**
	
	 /**
     * ws连接成功
     */
    void onWsOpen();

    /**
     * ws重连中
     */
    void onWsReconnecting();

    /**
     * ws连接失败
     *
     * @param errorInfo
     */
    void onWsFail(String errorInfo);


    /**
     * ws关闭中
     *
     * @param code
     * @param reason
     */
    void onWsClosing(int code, String reason);

    /**
     * ws连接关闭
     *
     * @param code
     * @param reason
     */
    void onWsClosed(int code, String reason);

**OnMsgListener**


	 /**
     * ws收到的消息(非信令,只是普通消息)
     *
     * @param msg
     */
    void onWsMessage(String msg);

**OnMsgListener**


	这个需要自定义回调,因为这个是根据业务变化的.
	比如收到某些和服务器约定的消息时需要做的事情,一般这个是不需要走onWsMessage回调
	只需要做好对应逻辑即可.
ps：


有些人问怎么确定发出去的消息对面成功收到了,发送消息的时候带一条唯一的msgid,网关在收到的
时候需要回复ack消息,只要回复的ack消息带上你这个msgid就行,这样根据ack的msgid即可断定消息对面已经拿到了.
