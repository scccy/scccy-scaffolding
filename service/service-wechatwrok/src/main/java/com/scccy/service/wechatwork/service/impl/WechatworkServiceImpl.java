package com.scccy.service.wechatwork.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.scccy.common.base.manager.OkHttpManager;
import com.scccy.service.wechatwork.aes.AesException;
import com.scccy.service.wechatwork.aes.WXBizMsgCrypt;
import com.scccy.service.wechatwork.config.WechatworkProperties;
import com.scccy.service.wechatwork.dao.repository.WechatworkExternalUserRepository;
import com.scccy.service.wechatwork.dao.repository.WechatworkGroupRepository;
import com.scccy.service.wechatwork.dao.service.WechatworkExternalUserMpService;
import com.scccy.service.wechatwork.dao.service.WechatworkGroupMpService;
import com.scccy.service.wechatwork.domain.jpa.WechatworkExternalUserIdJpa;
import com.scccy.service.wechatwork.domain.jpa.WechatworkExternalUserJpa;
import com.scccy.service.wechatwork.domain.jpa.WechatworkGroupIdJpa;
import com.scccy.service.wechatwork.domain.jpa.WechatworkGroupJpa;
import com.scccy.service.wechatwork.domain.mp.WechatworkExternalUserMp;
import com.scccy.service.wechatwork.domain.mp.WechatworkGroupMp;
import com.scccy.service.wechatwork.service.WechatworkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * 新实现：聚焦于基础能力的重构与规范化。
 * 说明：作为独立实现存在，避免与现有实现产生 Bean 冲突。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WechatworkServiceImpl implements WechatworkService {

    private static final String ACCESS_TOKEN_KEY_HEADER = "wechatwork:";
    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String ACCESS_TOKEN_KEY_CHAT = "access_token_chat";
    private static final String ACCESS_TOKEN_EXPIRE_KEY = "access_token_expire";
    private static final long TOKEN_EXPIRE_TIME = 7200L; // 秒
    private static final long REFRESH_THRESHOLD = 300L; // 秒

    private final RedisTemplate<String, String> redisTemplate;
    private final WechatworkProperties wechatworkProperties;

    private final WechatworkExternalUserMpService wechatworkExternalUserMpServiceImpl;
    private final WechatworkGroupMpService wechatworkGroupMpServiceImpl;
    private final OkHttpManager okHttpManager;

    private final WechatworkExternalUserRepository wechatworkExternalUserRepository;
    private final WechatworkGroupRepository wechatworkGroupRepository;




    // ---------- 令牌与基础 ----------

    @Override
    public String getToken() throws IOException {
        String cachedToken = redisTemplate.opsForValue().get(ACCESS_TOKEN_KEY_HEADER+ACCESS_TOKEN_KEY);
        if (cachedToken != null && !cachedToken.isEmpty()) {
            return cachedToken;
        }
        return refreshAccessToken(ACCESS_TOKEN_KEY_HEADER,ACCESS_TOKEN_KEY, wechatworkProperties.getCorSecret());
    }

    @Override
    public String getChatToken() throws IOException {
        String cachedToken = redisTemplate.opsForValue().get(ACCESS_TOKEN_KEY_HEADER+ACCESS_TOKEN_KEY_CHAT);
        if (cachedToken != null && !cachedToken.isEmpty()) {
            return cachedToken;
        }
        return refreshAccessToken(ACCESS_TOKEN_KEY_HEADER,ACCESS_TOKEN_KEY_CHAT,wechatworkProperties.getCoreSecretChat());
    }



    public String refreshAccessToken(String header,String tokenKey,String secret) throws IOException {
        String url =wechatworkProperties.getBaseUrl() + "/gettoken";
        Map<String, Object> headers = new HashMap<>();
        Map<String, Object> params = new HashMap<>();
        params.put("corpid", wechatworkProperties.getCorpID());
        params.put("corpsecret", secret);
        JSONObject result = okHttpManager.get(url, headers, params);
        String accessToken = result.getString("access_token");
        redisTemplate.opsForValue().set(header+tokenKey, accessToken, TOKEN_EXPIRE_TIME - REFRESH_THRESHOLD, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(ACCESS_TOKEN_EXPIRE_KEY, String.valueOf(System.currentTimeMillis() + TOKEN_EXPIRE_TIME * 1000));
        return accessToken;
    }

    // ---------- 组织架构同步 ----------

    @Override
    public JSONObject syncDepartemt() throws IOException {
        String url = buildUrlWithToken("/department/list");
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", getToken());
        return okHttpManager.get(url, null,params);
    }

    @Override
    public JSONObject syncUser(Integer depId) throws IOException {
        String url = wechatworkProperties.getBaseUrl() + "/user/list?";
        Map<String, Object> headers = new HashMap<>();
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", getToken());
        params.put("department_id", depId);
        JSONObject userList = okHttpManager.get(url, headers, params);
        JSONArray userlist = userList.getJSONArray("userlist");
        if (userlist != null) {
            for (int i = 0; i < userlist.size(); i++) {
                JSONObject user = userlist.getJSONObject(i);
                //客服号
//                todo: openFegin系统用户表修改信息
//                userService.insertOrEditUserInfo(user.getString("userid"), user.getString("name"), user.getString("status"));
            }
        }
        return userList;
    }

    // ---------- 好友/外部联系人 ----------
    @Override
    public Boolean addAllExteralContact() throws IOException {

        JSONArray userList = syncUser(152).getJSONArray("userlist");

        List<String> userIds = new ArrayList<>();
        HashMap<String, Object> stringObjectHashMap = new HashMap<>();

        for (int i = 0; i < userList.size(); i++) {
            JSONObject user = userList.getJSONObject(i);
            String userid = user.getString("userid");
            if (userid != null && !userid.isEmpty()) {
                userIds.add(userid);
            }
        }
        stringObjectHashMap.put("userid_list",userIds);
        stringObjectHashMap.put("limit",1000);
        return addExteralContact(stringObjectHashMap);
    }


    @Override
    public Boolean addExteralContact(HashMap<String, Object> postParams) {
        Map<String, Object> headers = new HashMap<>();
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("userid_list", postParams.get("userid_list"));
        requestParams.put("limit", postParams.get("limit"));

        String cursor = postParams.get("cursor") == null ? null : String.valueOf(postParams.get("cursor"));

        // 批量处理配置
        final int BATCH_SIZE = 2000; // 批量保存大小
        List<WechatworkExternalUserJpa> batchList = new ArrayList<>(BATCH_SIZE);
        int totalProcessed = 0;
        int totalSaved = 0;

        boolean success = true;
        String lastCursor;

        try {
            do {
                if (cursor != null && !cursor.isEmpty()) {
                    requestParams.put("cursor", cursor);
                } else {
                    requestParams.remove("cursor");
                }

                String url = buildUrlWithToken("/externalcontact/batch/get_by_user");
                JSONObject responseBody = okHttpManager.post(url, headers, requestParams);
                JSONArray externalContactList = responseBody.getJSONArray("external_contact_list");
                if (externalContactList != null && !externalContactList.isEmpty()) {
                    List<WechatworkExternalUserJpa> userWechats = IntStream
                            .range(0, externalContactList.size())
                            .mapToObj(externalContactList::getJSONObject)
                            .map(externalContactJson -> {
                                JSONObject externalContact = externalContactJson.getJSONObject("external_contact");
                                if (externalContact == null || externalContact.getInteger("type") == null || externalContact.getInteger("type") != 1) {
                                    return null;
                                }
                                String externalUserid = externalContact.getString("external_userid");
                                String unionid = externalContact.getString("unionid");
                                JSONObject followInfo = externalContactJson.getJSONObject("follow_info");
                                String userid = followInfo == null ? null : followInfo.getString("userid");

                                WechatworkExternalUserJpa userWechat = new WechatworkExternalUserJpa();
                                WechatworkExternalUserIdJpa wechatworkExternalUserIdJpa = new WechatworkExternalUserIdJpa();
                                wechatworkExternalUserIdJpa.setWechatworkExternalUserid(externalUserid);
                                wechatworkExternalUserIdJpa.setWechatworkUserId(userid);
                                userWechat.setId(wechatworkExternalUserIdJpa);
                                userWechat.setWechatworkUnionId(unionid);
                                userWechat.setUserId(userid);
                                return userWechat;
                            })
                            .filter(Objects::nonNull)
                            .toList();

                    // 添加到批量列表
                    batchList.addAll(userWechats);
                    totalProcessed += userWechats.size();

                    // 当批量列表达到指定大小时，执行批量保存
                    if (batchList.size() >= BATCH_SIZE) {
                        try {
                            wechatworkExternalUserRepository.saveAll(batchList);
                            totalSaved += batchList.size();
                            log.info("批量保存成功，本次保存数量: {}, 累计保存数量: {}", batchList.size(), totalSaved);
                            batchList.clear(); // 清空列表，释放内存
                        } catch (Exception e) {
                            log.error("批量保存失败，错误信息: {}", e.getMessage(), e);
                            success = false;
                            // 不要break，继续处理剩余数据
                        }
                    }
                }

                lastCursor = responseBody.getString("next_cursor");
                if (lastCursor == null || lastCursor.isEmpty() || (cursor != null && cursor.equals(lastCursor))) {
                    break;
                }
                cursor = lastCursor;

                // 添加进度日志
                log.info("已处理数据量: {}, 当前批次大小: {}", totalProcessed, batchList.size());

            } while (true);

            // 处理剩余的未保存数据
            if (!batchList.isEmpty()) {
                try {
                    wechatworkExternalUserRepository.saveAll(batchList);
                    totalSaved += batchList.size();
                    log.info("最终批量保存成功，本次保存数量: {}, 总保存数量: {}", batchList.size(), totalSaved);
                } catch (Exception e) {
                    log.error("最终批量保存失败，错误信息: {}", e.getMessage(), e);
                    success = false;
                }
            }

            log.info("外部联系人同步完成，总处理数量: {}, 总保存数量: {}", totalProcessed, totalSaved);

        } catch (Exception e) {
            log.error("外部联系人同步过程中发生异常: {}", e.getMessage(), e);
            success = false;
        }

        return success;
    }


    // 根据外部用户ID获取UnionId
    @Override
    public String getExteralContactUnionId(String EXTERNAL_USERID) throws IOException {
//todo: 需要用户模块
//        SysUser sysUser = userService.queryUcByExternalUserid(EXTERNAL_USERID);
//        if (sysUser!= null) {
//        return sysUser.getUnionId();
//        } else {
            Map<String, Object> headers = new HashMap<>();
            Map<String, Object> requestParams = new HashMap<>();
            String url = buildUrlWithToken("externalcontact/get");
            JSONObject responseBody = okHttpManager.get(url + "&external_userid=" + EXTERNAL_USERID, headers, requestParams);
            return responseBody.getJSONObject("external_contact").getString("unionid");
//        }
    }

    private Boolean synctExteralContact(HashMap<String, Object> postParams, String unionIdNew) {
        Map<String, Object> headers = new HashMap<>();
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("userid_list", postParams.get("userid_list"));
        requestParams.put("limit", postParams.getOrDefault("limit", 100));

        String cursor = postParams.get("cursor") == null ? null : String.valueOf(postParams.get("cursor"));
        String lastCursor;
        try {
            do {
                if (cursor != null && !cursor.isEmpty()) {
                    requestParams.put("cursor", cursor);
                } else {
                    requestParams.remove("cursor");
                }

                String url = buildUrlWithToken("/externalcontact/batch/get_by_user");
                JSONObject responseBody = okHttpManager.post(url,  headers, requestParams);

                JSONArray externalContactList = responseBody.getJSONArray("external_contact_list");
                if (externalContactList != null && !externalContactList.isEmpty()) {
                    boolean found = IntStream
                            .range(0, externalContactList.size())
                            .mapToObj(externalContactList::getJSONObject)
                            .map(obj -> obj.getJSONObject("external_contact"))
                            .filter(Objects::nonNull)
                            .filter(ec -> ec.getInteger("type") != null && ec.getInteger("type") == 1)
                            .map(ec -> ec.getString("unionid"))
                            .anyMatch(u -> unionIdNew != null && unionIdNew.equals(u));
                    if (found) {
                        return true;
                    }
                }

                lastCursor = responseBody.getString("next_cursor");
                if (lastCursor == null || lastCursor.isEmpty() || (cursor != null && cursor.equals(lastCursor))) {
                    break;
                }
                cursor = lastCursor;
            } while (true);
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    @Override
    public String resolveExternalUseridOrSync(String userId, String unionId) {
//        String externalUserid = wechatworkProperties.selectByUnionId(unionId);
        String wechatworkExternalUserid = wechatworkExternalUserMpServiceImpl.lambdaQuery().eq(WechatworkExternalUserMp::getWechatworkUnionId, unionId).getEntity().getWechatworkExternalUserid();
        if (wechatworkExternalUserid != null) {
            return wechatworkExternalUserid;
        }
        HashMap<String, Object> syncParams = new HashMap<>();
        ArrayList<String> userIdList = new ArrayList<>();
        userIdList.add(userId);
        syncParams.put("userid_list", userIdList);
        syncParams.put("limit", 100);
        addExteralContact(syncParams);
        return wechatworkGroupMpServiceImpl.lambdaQuery().eq(WechatworkGroupMp::getWechatworkExternalUnionId,unionId).getEntity().getWechatworkExternalUnionId();
    }

    // ---------- 朋友圈 ----------

    @Override
    public JSONObject getMomentIds(HashMap<String, Object> postParams) throws IOException {
        String url = buildUrlWithToken("/externalcontact/get_moment_list");
        return okHttpManager.post(url,null, postParams);
    }

    @Override
    public JSONObject getMomentDetail(HashMap<String, Object> postParams) throws IOException {
        String url = buildUrlWithToken("/externalcontact/get_moment_comments");
        return okHttpManager.post(url, null,postParams);
    }

    @Override
    public Boolean getLikeDetail(String userId, String unionId) throws IOException {
        long startTime = getTodayStartTimestamp();
        long endTime = getTodayEndTimestamp();

        HashMap<String, Object> params = new HashMap<>();
        params.put("start_time", startTime);
        params.put("end_time", endTime);
        params.put("creator", userId);
        params.put("filter_type", 2);
        params.put("cursor", "");
        params.put("limit", 100);

        String cursor = "";
        String lastCursor;
        do {
            if (cursor != null && !cursor.isEmpty()) {
                params.put("cursor", cursor);
            } else {
                params.remove("cursor");
            }

            JSONObject page = getMomentIds(params);
            JSONArray momentList = page.getJSONArray("moment_list");
            if (momentList != null && !momentList.isEmpty()) {
                for (int i = 0; i < momentList.size(); i++) {
                    JSONObject item = momentList.getJSONObject(i);
                    String momentId = item.getString("moment_id");

                    HashMap<String, Object> detailParams = new HashMap<>();
                    detailParams.put("moment_id", momentId);
                    detailParams.put("userid", userId);
                    JSONObject detail = getMomentDetail(detailParams);
                    JSONArray likeList = detail.getJSONArray("like_list");
                    if (likeList == null || likeList.isEmpty()) {
                        continue;
                    }

                    String externalUseridResolved = resolveExternalUseridOrSync(userId, unionId);
                    if (externalUseridResolved == null) {
                        continue;
                    }
                    boolean matched = likeList.stream().anyMatch(entry -> {
                        JSONObject like = (JSONObject) entry;
                        return externalUseridResolved.equals(like.getString("external_userid"));
                    });

                    if (matched) {
                        return true;
                    }
                }
            }

            lastCursor = page.getString("next_cursor");
            if (lastCursor == null) {
                lastCursor = page.getString("cursor");
            }
            if (lastCursor == null || lastCursor.isEmpty() || (cursor != null && cursor.equals(lastCursor))) {
                break;
            }
            cursor = lastCursor;
        } while (true);

        return false;
    }

    @Override
    public Boolean getCommentDetail(String userId, String unionId) throws IOException {
        long startTime = getTodayStartTimestamp();
        long endTime = getTodayEndTimestamp();

        HashMap<String, Object> params = new HashMap<>();
        params.put("start_time", startTime);
        params.put("end_time", endTime);
        params.put("creator", userId);
        params.put("filter_type", 2);
        params.put("cursor", "");
        params.put("limit", 100);

        String cursor = "";
        String lastCursor;
        do {
            if (cursor != null && !cursor.isEmpty()) {
                params.put("cursor", cursor);
            } else {
                params.remove("cursor");
            }

            JSONObject page = getMomentIds(params);
            JSONArray momentList = page.getJSONArray("moment_list");
            if (momentList != null && !momentList.isEmpty()) {
                for (int i = 0; i < momentList.size(); i++) {
                    JSONObject item = momentList.getJSONObject(i);
                    String momentId = item.getString("moment_id");

                    HashMap<String, Object> detailParams = new HashMap<>();
                    detailParams.put("moment_id", momentId);
                    detailParams.put("userid", userId);
                    JSONObject detail = getMomentDetail(detailParams);
                    JSONArray commentList = detail.getJSONArray("comment_list");
                    if (commentList == null || commentList.isEmpty()) {
                        continue;
                    }

                    String externalUseridResolved = resolveExternalUseridOrSync(userId, unionId);
                    if (externalUseridResolved == null) {
                        continue;
                    }
                    return commentList.stream().anyMatch(entry -> {
                        JSONObject comment = (JSONObject) entry;
                        System.out.println("执行成功:external_userid:" + comment.getString("external_userid"));
                        return externalUseridResolved.equals(comment.getString("external_userid"));
                    });
                }
            }

            lastCursor = page.getString("next_cursor");
            if (lastCursor == null) {
                lastCursor = page.getString("cursor");
            }
            if (lastCursor == null || lastCursor.isEmpty() || (cursor != null && cursor.equals(lastCursor))) {
                break;
            }
            cursor = lastCursor;
        } while (true);

        return false;
    }

    // ---------- 群聊 ----------

    @Override
    public JSONObject syncChatgroup(HashMap<String, Object> postParams) throws IOException {
        String url = buildUrlWithToken("/externalcontact/groupchat/list");
        return okHttpManager.post(url, postParams);
    }

    @Override
    public JSONObject syncChatGroupMember(HashMap<String, Object> postParams) throws IOException {
        String url = buildUrlWithToken("/externalcontact/groupchat/get");
        return okHttpManager.post(url, postParams);
    }

    @Override
    public Boolean getFriendRelation(String userId, String unionId) {
        HashMap<String, Object> params = new HashMap<>();
        ArrayList<String> useridList = new ArrayList<>();
        useridList.add(userId);
        params.put("userid_list", useridList);
        params.put("limit", 100);
        return synctExteralContact(params, unionId);
    }

    @Override
    public Boolean getGroupRelation(String userId, String unionId) throws IOException {
        HashMap<String, Object> params = new HashMap<>();
        HashMap<String, Object> useridMap = new HashMap<>();
        ArrayList<String> useridList = new ArrayList<>();
        useridList.add(userId);
        useridMap.put("userid_list", useridList);
        params.put("owner_filter", useridMap);
        params.put("limit",10);
        params.put("status_filter",0);
        String cursor = null;

        do {
            if (cursor != null && !cursor.isEmpty()) {
                params.put("cursor", cursor);
            } else {
                params.remove("cursor");
            }

            JSONObject page = syncChatgroup(params);
            JSONArray groupList = page.getJSONArray("group_chat_list");
            if (groupList != null) {
                boolean found = groupList.stream().map(JSONObject.class::cast).anyMatch(group -> {
                    String chatId = group.getString("chat_id");
                    HashMap<String, Object> detailParams = new HashMap<>();
                    detailParams.put("chat_id", chatId);
                    JSONObject groupDetail = null;
                    try {
                        groupDetail = syncChatGroupMember(detailParams);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    JSONObject groupChat = groupDetail.getJSONObject("group_chat");
                    if (groupChat == null) {
                        return false;
                    }
                    JSONArray memberList = groupChat.getJSONArray("member_list");
                    if (memberList == null) {
                        return false;
                    }
                    return memberList.stream()
                            .map(JSONObject.class::cast)
                            .filter(m -> m.getInteger("type") == 2)
                            .map(m -> m.getString("unionid"))
                            .anyMatch(unionId::equals);
                });
                if (found) {
                    return true;
                }
            }

            cursor = page.getString("cursor");
        } while (cursor != null && !cursor.isEmpty());

        return false;
    }

    @Override
    public Boolean getLikeDetailByDep(String unionId) {
        return getUserIds().stream()
                .anyMatch(uid -> {
                    try {
                        return getLikeDetail(uid, unionId);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Override
    public Boolean getCommentDetailBydDep(String unionId) {
        return getUserIds().stream()
                .anyMatch(uid -> {
                    try {
                        return getCommentDetail(uid, unionId);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Override
    public Boolean getFriendRelationByDep(String unionId) {
        return getUserIds().stream()
                .anyMatch(uid -> getFriendRelation(uid, unionId));
    }

    @Override
    public Boolean getGroupRelationByDep(String unionId) {
        return getUserIds().stream()
                .anyMatch(uid -> {
                    try {
                        return getGroupRelation(uid, unionId);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }



    // ---------- 回调处理 ----------

    public String callBackGet(String msgSignature, String nonce, String timestamp, String echostr) throws AesException {
        WXBizMsgCrypt wxcpt = new WXBizMsgCrypt(wechatworkProperties.getToken(), wechatworkProperties.getEncodingAESKey(), wechatworkProperties.getCorpID());
        try {
            return wxcpt.VerifyURL(msgSignature, timestamp, nonce, echostr);
        } catch (Exception e) {
            log.error( e.getMessage());
            return null;
        }
    }

    public void callBackPost(String msgSignature, String nonce, String timestamp, String xmlBody) throws AesException {
        WXBizMsgCrypt wxcpt = new WXBizMsgCrypt(wechatworkProperties.getToken(), wechatworkProperties.getEncodingAESKey(), wechatworkProperties.getCorpID());
        try {
            String sMsg = wxcpt.DecryptMsg(msgSignature, timestamp, nonce, xmlBody);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(sMsg.getBytes(StandardCharsets.UTF_8)));

            // 2. 获取根元素
            Element root = document.getDocumentElement();

            String event = root.getElementsByTagName("Event").item(0).getTextContent();
            String changeType = root.getElementsByTagName("ChangeType").item(0).getTextContent();


            switch (event) {
                case "change_external_chat":
                    // 处理外部会话变更事件
                    String groupId = root.getElementsByTagName("ChatId").item(0).getTextContent();
                    String updateDetail = root.getElementsByTagName("UpdateDetail").item(0).getTextContent();
                    WechatworkGroupJpa userGroup = new WechatworkGroupJpa();
                    WechatworkGroupIdJpa userGroupId = new WechatworkGroupIdJpa();
                    switch (updateDetail) {
                        case "add_member":
                            // 处理客户加入会话
                            System.out.println("客户加入群聊");
                            NodeList memChangeList = root.getElementsByTagName("MemChangeList");
                            Node item = memChangeList.item(0);
                            NodeList items = item.getChildNodes();
                            String memberId = items.item(0).getTextContent();
                            String memberUnionId = null;
                            try {
                                 memberUnionId = getExteralContactUnionId(memberId);
                            } catch (Exception e) {
                                log.error(e.getMessage());
                            }
//                            System.out.println("加入的成员ID: " + memberId + ", UnionId: " + memberUnionId);

                            userGroupId.setWechatworkGroupId(groupId);
                            userGroupId.setWechatworkExternalUserId(memberId);
                            userGroup.setId(userGroupId);
                            userGroup.setWechatworkExternalUnionId(memberUnionId);
                            wechatworkGroupRepository.save(userGroup);

                            // 在这里处理成员加入逻辑
                            break;
                        case "del_member":
                            // 处理客户离开会话

                            NodeList delMemChangeList = root.getElementsByTagName("MemChangeList");
                            Node delItem = delMemChangeList.item(0);
                            NodeList delItems = delItem.getChildNodes();
                            String delMemberId = delItems.item(0).getTextContent();
                            String delMemberUnionId = getExteralContactUnionId(delMemberId);
                            userGroupId.setWechatworkGroupId(groupId);
                            userGroupId.setWechatworkExternalUserId(delMemberUnionId);
                            userGroup.setId(userGroupId);
                            wechatworkGroupRepository.delete(userGroup);
                            // 在这里处理成员离开逻辑
                            break;
                        default:
                            System.out.println("未知的ChangeType: " + changeType);
                            System.out.println(sMsg);
                            break;
                    }
                    break;

                case "change_external_contact":

                    // 处理外部联系人变更事件
                    String userId = root.getElementsByTagName("UserID").item(0).getTextContent();
                    String externalUserID = root.getElementsByTagName("ExternalUserID").item(0).getTextContent();
                    String unionId = getExteralContactUnionId(externalUserID);
//                    UserWechat userWechat = new UserWechat();
                    WechatworkExternalUserJpa userWechat = new WechatworkExternalUserJpa();
                    WechatworkExternalUserIdJpa userWechatId = new WechatworkExternalUserIdJpa();
                    userWechatId.setWechatworkUserId(userId);
                    userWechatId.setWechatworkExternalUserid(externalUserID);
                    userWechat.setId(userWechatId);
                    userWechat.setWechatworkUnionId(unionId);


                    switch (changeType) {
                        case "add_external_contact":
                            System.out.println("添加外部联系人");
                            wechatworkExternalUserRepository.save(userWechat);
                            break;
                        case "del_external_contact":
                            System.out.println("删除外部联系人");
                            wechatworkExternalUserRepository.delete(userWechat);
                            break;
                        default:
                            System.out.println("未知的ChangeType: " + changeType);
                            break;
                    }
                    break;

                default:
                    System.out.println("未知的事件类型: " + event);
                    break;
            }


        } catch (Exception e) {
            log.error( e.getMessage());
        }
    }




    // ---------- 工具方法 ----------



    private String buildUrlWithToken(String endpoint) throws IOException {
        return wechatworkProperties.getBaseUrl() + endpoint + "?access_token=" + getToken();
    }

    private long getTodayStartTimestamp() {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        return today.atStartOfDay(ZoneId.systemDefault()).toInstant().getEpochSecond();
    }

    private long getTodayEndTimestamp() {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        return today.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
    }

    private List<String> getUserIds(){
//        Todo: 需要用户相关数据
//        return  userService.queryUserByQwUserid().stream()
//                .map(SysUser::getQwUserid)  // 把 SysUser 转换成 qID
//                .collect(Collectors.toList());  // 收集到 List<String>
        return new ArrayList<>();

    }
}


