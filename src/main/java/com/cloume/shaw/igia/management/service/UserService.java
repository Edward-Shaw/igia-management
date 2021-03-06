package com.cloume.shaw.igia.management.service;

import java.util.Calendar;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.cloume.shaw.igia.common.resource.User;
import com.cloume.shaw.igia.common.utils.Const;
import com.cloume.shaw.igia.management.iservice.AbstractServiceBase;
import com.cloume.shaw.igia.management.iservice.IUserService;

@Service
public class UserService extends AbstractServiceBase implements IUserService {

	@Override
	public List<User> listByPage(boolean banned, String state, String time, int[] page) {
		if (page == null) {
			page = new int[2];
			page[0] = 0;
			page[1] = 20;
		}

		// 获取页码及每一页的长度
		int pageNumber = page[0];
		int pageSize = page[1];

		Query query = new Query();
		Criteria criterion = Criteria.where("state").ne(Const.STATE_DELETED);
		switch(state){
		case "true":
			criterion.and("banned").is(true);
			break;
		case "false":
			criterion.and("banned").is(false);
			break;
		default:
			break;
		}
		
		if (time.compareToIgnoreCase("default") != 0) {
			Calendar now = Calendar.getInstance();
			now.set(Calendar.HOUR_OF_DAY, 0);
			now.set(Calendar.SECOND, 0);
			now.set(Calendar.MINUTE, 0);
			now.set(Calendar.MILLISECOND, 0);
			long datetime;
			switch (time) {
			case "today":
				datetime = now.getTimeInMillis();
				criterion = criterion.and("createdTime").gte(datetime);
				break;
			case "yesterday":
				datetime = now.getTimeInMillis();
				now.add(Calendar.DATE, -1);
				long datetime1 = now.getTimeInMillis();
				criterion = criterion.and("createdTime").gte(datetime1).lte(datetime);
				break;
			case "this_week":
				now.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
				datetime = now.getTimeInMillis();
				criterion = criterion.and("createdTime").gte(datetime);
				break;
			case "this_month":
				now.set(Calendar.DAY_OF_MONTH, 1);
				datetime = now.getTimeInMillis();
				criterion = criterion.and("createdTime").gte(datetime);
				break;
			}
		}
		// 查询"state"不等于"STATE_DELETED"的样品，跳过前 pageNumber*pageSize条记录，返回之后的pageSize条记录
		query = Query.query(criterion).skip(pageNumber * pageSize).limit(pageSize);

		// 根据入库id倒序排列
		List<User> users = getMongoTemplate().find(query.with(new Sort(Direction.DESC, "_id")), User.class);
		
		return users;
	}

	@Override
	public User getUserById(String id) {
		
		Query query = new Query(Criteria.where("_id").is(id));
		
		User user = getMongoTemplate().findOne(query, User.class);
		
		return user;
	}

	@Override
	public User updateUserById(String id, User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long countUser(String state, String time) {
		
		Query query = new Query();
		Criteria criterion = Criteria.where("state").ne(Const.STATE_DELETED);
		switch(state){
		case "true":
			criterion.and("banned").is(true);
			break;
		case "false":
			criterion.and("banned").is(false);
			break;
		default:
			break;
		}
		
		if (time.compareToIgnoreCase("default") != 0) {
			Calendar now = Calendar.getInstance();
			now.set(Calendar.HOUR_OF_DAY, 0);
			now.set(Calendar.SECOND, 0);
			now.set(Calendar.MINUTE, 0);
			now.set(Calendar.MILLISECOND, 0);
			long datetime;
			switch (time) {
			case "today":
				datetime = now.getTimeInMillis();
				criterion = criterion.and("createdTime").gte(datetime);
				break;
			case "yesterday":
				datetime = now.getTimeInMillis();
				now.add(Calendar.DATE, -1);
				long datetime1 = now.getTimeInMillis();
				criterion = criterion.and("createdTime").gte(datetime1).lte(datetime);
				break;
			case "this_week":
				now.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
				datetime = now.getTimeInMillis();
				criterion = criterion.and("createdTime").gte(datetime);
				break;
			case "this_month":
				now.set(Calendar.DAY_OF_MONTH, 1);
				datetime = now.getTimeInMillis();
				criterion = criterion.and("createdTime").gte(datetime);
				break;
			}
		}
		
		// 根据入库id倒序排列
		long count = getMongoTemplate().count(query.with(new Sort(Direction.DESC, "_id")), User.class);
		
		return count;
	}

}
