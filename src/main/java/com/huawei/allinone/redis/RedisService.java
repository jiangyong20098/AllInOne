package com.huawei.allinone.redis;

import java.util.List;

public interface RedisService {
    /**
     * 新增一条userid用户在搜索栏的历史记录
     *
     * @param searchkey 输入的关键词
     * @param userid
     */
    int insertSearchHistoryByUserId(String userid, String searchkey);

    /**
     * 删除个人历史数据
     *
     * @param searchkey 输入的关键词
     * @param userid
     */
    Long delSearchHistoryByUserId(String userid, String searchkey);

    /**
     * 获取个人历史数据列表
     *
     * @param userid
     */
    List<String> getSearchHistoryByUserId(String userid);

    /**
     * 新增热词搜索记录，将用户输入的热词存储
     *
     * @param searchkey 输入的关键词
     */
    int insertScoreByUserId(String searchkey);

    /**
     * 根据searchkey搜索其相关最热的前十名
     * (如果searchkey为null空，则返回redis存储的前十最热词条)
     *
     * @param searchkey 输入的关键词
     */
    List<String> getHotList(String searchkey);

    /**
     * 每次点击给相关词searchkey热度 +1
     *
     * @param searchkey 输入的关键词
     */
    int incrementScore(String searchkey);
}
