package com.sgl.hms.hosp.service;

import com.sgl.hms.model.hosp.Schedule;
import com.sgl.hms.vo.hosp.ScheduleOrderVo;
import com.sgl.hms.vo.hosp.ScheduleQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ScheduleService {
    //上传排班信息接口
    void save(Map<String, Object> paramMap);

    //查询排班信息
    Page<Schedule> getPageSchedule(int pageNum, int pageSize, ScheduleQueryVo scheduleQueryVo);

    //删除排班信息
    void removeSchedule(String hoscode, String hosScheduleId);

    //根据医院编号 和 科室编号 ，查询排班规则数据
    Map<String, Object> getRuleSchedule(long pageNum, long pageSize, String hoscode, String depcode);

    //根据医院编号 、科室编号和工作日期，查询排班详细信息
    List<Schedule> getDetailSchedule(String hoscode, String depcode, String workDate);

    //获取可预约排班数据
    Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode);

    //根据排班id获取排班数据
    Schedule getById(String scheduleId);

    //根据排班id获取预约下单数据
    ScheduleOrderVo getScheduleOrderVo(String scheduleId);

    /**
     * 修改排班,用于mq
     */
    void update(Schedule schedule);
}
