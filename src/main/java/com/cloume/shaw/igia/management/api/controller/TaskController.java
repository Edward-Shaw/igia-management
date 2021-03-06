package com.cloume.shaw.igia.management.api.controller;

import java.util.Calendar;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.cloume.shaw.igia.common.controller.AbstractController;
import com.cloume.shaw.igia.common.resource.Task;
import com.cloume.shaw.igia.common.rest.RestResponse;
import com.cloume.shaw.igia.common.utils.Const;
import com.cloume.shaw.igia.common.utils.Updater;
import com.cloume.shaw.igia.management.iservice.ITaskService;

@RestController
@RequestMapping(value = "/api/task")
public class TaskController extends AbstractController{
	
	@Autowired
	private ITaskService taskService;
	
	@RequestMapping(method = RequestMethod.PUT)
	public RestResponse<Task> addNewTask(@RequestBody Map<String, Object> body){
		
		System.out.println(body);
		
		Task task = new Updater<Task>(new Task()).update(body, (key, value)->{
			if(key.compareToIgnoreCase("state") == 0){
				switch(value.toString()){
				case "unpublished":
					value = Const.STATE_UNPUBLISHED;
					break;
				case "published":
					value = Const.STATE_PUBLISHED;
					break;
				default:
					value = Const.STATE_UNKNOWN;
				}
			}
			return value;
		});
		task.setCreatedTime(System.currentTimeMillis());
		task.setCode(generateTaskCode());
		
		//TODO:set course code from course name
		
		taskService.addNewTask(task);
		
		return RestResponse.good(task);
	}
	
	/**
	 * 更新作业信息
	 * @param id
	 * @param body
	 * @return
	 */
	@RequestMapping(value = "/{code}", method = RequestMethod.POST)
	public RestResponse<Task> updateTask(@PathVariable("code") String code,
			@RequestBody Map<String, Object> body){
		
		Task task = getMongoTemplate().findOne(Query.query(Criteria.where("code").is(code)), Task.class);
		if(task == null){
			return RestResponse.bad(-1, "Task not found: " + code);
		}
		
		task = new Updater<Task>(task).update(body, (key, value)->{
			return value;
		});
		
		getMongoTemplate().save(task);
		
		return RestResponse.good(task);
	}
	
	/**
	 * delete task by code.
	 * @param code
	 * @return
	 */
	@RequestMapping(value = "/{code}", method = RequestMethod.DELETE)
	public RestResponse<Task> deleteTaskByCode(@PathVariable("code") String code){
		
		Task task = getMongoTemplate().findOne(Query.query(Criteria.where("code").is(code)), Task.class);
		if(task == null){
			return RestResponse.bad(-1, "Task not found: " + code);
		}
		
		task.setState(Const.STATE_DELETED);
		
		getMongoTemplate().save(task);
		
		return RestResponse.good(task);
	}
	
	/**
	 * get task detail by code.
	 * @param code
	 * @return
	 */
	@RequestMapping(value = "/{code}", method = RequestMethod.GET)
	public RestResponse<Task> getTaskDetailByCode(@PathVariable("code") String code){
		Task task = getMongoTemplate().findOne(Query.query(Criteria.where("code").is(code)), Task.class);
		if(task == null){
			return RestResponse.bad(-1, "Task not found: " + code);
		}
		
		return RestResponse.good(task);
	}
	
	/**
	 * code = YYMM(年月)-作业数量序号(位数根据数据库中实际用户数量进行动态变更).
	 * 
	 * @return
	 */
	private String generateTaskCode() {
		Calendar now = Calendar.getInstance();
		String time = (now.get(Calendar.YEAR) + "").substring(2) + String.format("%02d", now.get(Calendar.MONTH) + 1);
		final String prefix = "T-" + time;
		final String pattern = prefix + "-\\d{1,5}";

		String code = "";
		synchronized (this) {
			long count = getMongoTemplate().count(
					Query.query(Criteria.where("code").regex(pattern)), Task.class,
					"task") + 1;
			code = prefix + "-" + count;
		}

		return code;
	}
}
