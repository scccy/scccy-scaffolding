package com.scccy.service.wechatwork.service;

public interface WechatworkEncyptService {
    Boolean getHasRoomChat(String unionId);
    void getAllData() throws Exception;

}
