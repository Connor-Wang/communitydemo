package com.wcaaotr.community.Util;

import com.github.pagehelper.PageInfo;

/**
 * @author Connor
 * @create 2021-07-04-18:46
 */
public class RedisPageUtil {

    public static PageInfo getPageInfo(int total, int pageSize, int pageNum) {
        PageInfo pageInfo = new PageInfo();
        pageInfo.setTotal(total);
        int pages = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
        pageInfo.setPages(pages);
        pageInfo.setPageNum(pageNum);
        return pageInfo;
    }

}
