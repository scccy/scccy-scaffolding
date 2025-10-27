package com.scccy.service.wechatwork.service;

import com.alibaba.fastjson2.JSONObject;
import com.scccy.service.wechatwork.aes.AesException;

import java.io.IOException;
import java.util.HashMap;

public interface WechatworkService {
//    获取token
    String getToken() throws IOException;

    String getChatToken() throws IOException;

    String getExteralContactUnionId(String EXTERNAL_USERID) throws IOException;

    String resolveExternalUseridOrSync(String userId, String unionId);

//    同步部门信息
    JSONObject syncDepartemt() throws IOException;

//    同步用户
    JSONObject syncUser(Integer depId) throws IOException;

//    同步外部用户
    Boolean addExteralContact(HashMap<String, Object> postParams);

    Boolean addAllExteralContact() throws IOException;

//    获取朋友圈信息
    JSONObject getMomentIds(HashMap<String, Object> postParams) throws IOException;

//    朋友圈活动信息
    JSONObject getMomentDetail(HashMap<String, Object> postParams) throws IOException;

//    群聊id
    JSONObject syncChatgroup(HashMap<String, Object> postParams) throws IOException;

//    群成员列表
    JSONObject syncChatGroupMember(HashMap<String, Object> postParams) throws IOException;

// 点赞详情
    Boolean getLikeDetail(String userId,String unionId) throws IOException;

// 评论详情
    Boolean getCommentDetail(String userId, String unionId) throws IOException;
// 好友关系
    Boolean getFriendRelation(String userId, String unionId);
// 群关系
    Boolean getGroupRelation(String userId, String unionId) throws IOException;

    // 点赞详情
    Boolean getLikeDetailByDep(String unionId);

//    // 评论详情
    Boolean getCommentDetailBydDep( String unionId);

//    // 好友关系
    Boolean getFriendRelationByDep(String unionId);
//    // 群关系
    Boolean getGroupRelationByDep(String unionId);


    String callBackGet(String msgSignature, String nonce, String timestamp, String echostr) throws AesException;


    void callBackPost(String msgSignature, String nonce, String timestamp, String xmlBody) throws AesException;

}
