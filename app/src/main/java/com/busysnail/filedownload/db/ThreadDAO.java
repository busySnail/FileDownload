package com.busysnail.filedownload.db;
import com.busysnail.filedownload.entity.ThreadInfo;

import java.util.List;

/**ThreadInfo数据访问接口
 *
 */


public interface ThreadDAO {

    /**
     * 插入线程消息
     * @param threadInfo
     */
    void insertThread(ThreadInfo threadInfo);

    /**
     * 删除线程
     * @param url
     */
    void deleteThread(String url);

    /**
     * 更新线程下载进度
     * @param url
     * @param thread_id
     * @param finishded
     */
    void updateThread(String url,int thread_id,long finishded);

    /**
     * 查询文件的线程信息
     * @param url
     * @return
     */
    List<ThreadInfo> getThreads(String url);

    /**
     * 线程信息是否存在
     * @param url
     * @param thread_id
     * @return
     */
    boolean isExists(String url,int thread_id);
}
