package com.ukefu.webim.web.handler;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.QueryStringQueryBuilder.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

import com.ukefu.core.UKDataContext;
import com.ukefu.util.UKTools;
import com.ukefu.util.UKView;
import com.ukefu.webim.service.cache.CacheHelper;
import com.ukefu.webim.service.repository.TenantRepository;
import com.ukefu.webim.web.handler.api.rest.QueryParams;
import com.ukefu.webim.web.model.SystemConfig;
import com.ukefu.webim.web.model.Tenant;
import com.ukefu.webim.web.model.User;


@Controller
@SessionAttributes
public class Handler {
	@Autowired
	private TenantRepository tenantRes;
	
	public final static int PAGE_SIZE_BG = 1 ;
	public final static int PAGE_SIZE_TW = 20 ;
	public final static int PAGE_SIZE_FV = 50 ;
	public final static int PAGE_SIZE_HA = 100 ;
	
	private long starttime = System.currentTimeMillis();
	
	public User getUser(HttpServletRequest request){
		User user = (User) request.getSession(true).getAttribute(UKDataContext.USER_SESSION_NAME)  ;
		if(user==null){
			String authorization = request.getHeader("authorization") ;
			if(StringUtils.isBlank(authorization) && request.getCookies()!=null){
				for(Cookie cookie : request.getCookies()){
					if(cookie.getName().equals("authorization")){
						authorization = cookie.getValue() ; break ;
					}
				}
			}
			if(!StringUtils.isBlank(authorization)){
				user = (User) CacheHelper.getApiUserCacheBean().getCacheObject(authorization, UKDataContext.SYSTEM_ORGI) ;
			}
			if(user==null){
				user = new User();
				user.setId(UKTools.getContextID(request.getSession().getId())) ;
				user.setUsername(UKDataContext.GUEST_USER+"_"+UKTools.genIDByKey(user.getId())) ;
				user.setOrgi(UKDataContext.SYSTEM_ORGI);
				user.setSessionid(user.getId()) ;
			}
		}else{
			user.setSessionid(user.getId()) ;
		}
		return user ;
	}
	
	/**
	 * 
	 * @param queryBuilder
	 * @param request
	 */
	public BoolQueryBuilder search(BoolQueryBuilder queryBuilder , ModelMap map, HttpServletRequest request) {
		queryBuilder.must(termQuery("orgi", this.getOrgi(request))) ;
		if(!StringUtils.isBlank(request.getParameter("batid"))) {
			queryBuilder.must(termQuery("batid", request.getParameter("batid"))) ;
			map.put("batid", request.getParameter("batid")) ;
		}
		if(!StringUtils.isBlank(request.getParameter("q"))) {
			queryBuilder.must(QueryBuilders.boolQuery().must(new QueryStringQueryBuilder(request.getParameter("q")).defaultOperator(Operator.AND))) ;
			map.put("q", request.getParameter("q")) ;
		}
		return queryBuilder ;
	}
	
	public User getIMUser(HttpServletRequest request , String userid , String nickname){
		User user = (User) request.getSession(true).getAttribute(UKDataContext.IM_USER_SESSION_NAME)  ;
		if(user==null){
			user = new User();
			if(!StringUtils.isBlank(userid)){
				user.setId(userid) ;
			}else{
				user.setId(UKTools.getContextID(request.getSession().getId())) ;
			}
			if(!StringUtils.isBlank(nickname)){
				user.setUsername(nickname);
			}else{
				user.setUsername(UKDataContext.GUEST_USER+"_"+UKTools.genIDByKey(user.getId())) ;
			}
			user.setSessionid(user.getId()) ;
		}else{
			user.setSessionid(UKTools.getContextID(request.getSession().getId())) ;
		}
		return user ;
	}
	
	
	public void setUser(HttpServletRequest request , User user){
		request.getSession(true).removeAttribute(UKDataContext.USER_SESSION_NAME) ;
		request.getSession(true).setAttribute(UKDataContext.USER_SESSION_NAME , user) ;
	}
	

	/**
	 * 创建系统监控的 模板页面
	 * @param page
	 * @return
	 */
	public UKView createAdminTempletResponse(String page) {
		return new UKView("/admin/include/tpl" , page);
	}
	/**
	 * 创建系统监控的 模板页面
	 * @param page
	 * @return
	 */
	public UKView createAppsTempletResponse(String page) {
		return new UKView("/apps/include/tpl" , page);
	}
	
	/**
	 * 创建系统监控的 模板页面
	 * @param page
	 * @return
	 */
	public UKView createEntIMTempletResponse(String page) {
		return new UKView("/apps/entim/include/tpl" , page);
	}
	
	public UKView createRequestPageTempletResponse(String page) {
		return new UKView(page);
	}
	
	/**
	 * 
	 * @param data
	 * @return
	 */
	public ModelAndView request(UKView data) {
    	return new ModelAndView(data.getTemplet()!=null ? data.getTemplet(): data.getPage() , "data", data) ;
    }

	public int getP(HttpServletRequest request) {
		int page = 0;
		String p = request.getParameter("p") ;
		if(!StringUtils.isBlank(p) && p.matches("[\\d]*")){
			page = Integer.parseInt(p) ;
			if(page > 0){
				page = page - 1 ;
			}
		}
		return page;
	}
	
	public int getPs(HttpServletRequest request) {
		int pagesize = PAGE_SIZE_TW;
		String ps = request.getParameter("ps") ;
		if(!StringUtils.isBlank(ps) && ps.matches("[\\d]*")){
			pagesize = Integer.parseInt(ps) ;
		}
		return pagesize;
	}
	
	public int getP(QueryParams params) {
		int page = 0;
		if(params!=null && !StringUtils.isBlank(params.getP()) && params.getP().matches("[\\d]*")){
			page = Integer.parseInt(params.getP()) ;
			if(page > 0){
				page = page - 1 ;
			}
		}
		return page;
	}
	
	public int getPs(QueryParams params) {
		int pagesize = PAGE_SIZE_TW;
		if(params != null && !StringUtils.isBlank(params.getPs()) && params.getPs().matches("[\\d]*")){
			pagesize = Integer.parseInt(params.getPs()) ;
		}
		return pagesize;
	}
	
	
	public int get50Ps(HttpServletRequest request) {
		int pagesize = PAGE_SIZE_FV;
		String ps = request.getParameter("ps") ;
		if(!StringUtils.isBlank(ps) && ps.matches("[\\d]*")){
			pagesize = Integer.parseInt(ps) ;
		}
		return pagesize;
	}
	
	public String getOrgi(HttpServletRequest request){	
		return getUser(request).getOrgi();
	}
	/**
	 * 机构id
	 * @param request
	 * @return
	 */
	public String getOrgid(HttpServletRequest request){	
		User u = getUser(request);
		return u.getOrgid();
	}
	
	public Tenant getTenant(HttpServletRequest request){
		return tenantRes.findById(getOrgi(request));
	}
	/**
	 * 根据是否租户共享获取orgi
	 * @param request
	 * @return
	 */
	public String getOrgiByTenantshare(HttpServletRequest request){	
		SystemConfig systemConfig = UKTools.getSystemConfig();
		if(systemConfig!=null&&systemConfig.isEnabletneant()&&systemConfig.isTenantshare()) {
			User user = this.getUser(request) ;
			return user.getOrgid();
    	}
		return getOrgi(request);
	}
	
	/**
	 * 判断是否租户共享
	 * @return
	 */
	public boolean isTenantshare(){	
		SystemConfig systemConfig = UKTools.getSystemConfig();
		if(systemConfig!=null&&systemConfig.isEnabletneant()&&systemConfig.isTenantshare()) {
			return true;
    	}
		return false;
	}
	
	/**
	 * 判断是否多租户
	 * @return
	 */
	public boolean isEnabletneant(){	
		SystemConfig systemConfig = UKTools.getSystemConfig();
		if(systemConfig!=null&&systemConfig.isEnabletneant()) {
			return true;
    	}
		return false;
	}
	/**
	 * 判断是否多租户
	 * @return
	 */
	public boolean isTenantconsole(){	
		SystemConfig systemConfig = UKTools.getSystemConfig();
		if(systemConfig!=null&&systemConfig.isEnabletneant()&&systemConfig.isTenantconsole()) {
			return true;
    	}
		return false;
	}

	public long getStarttime() {
		return starttime;
	}

	public void setStarttime(long starttime) {
		this.starttime = starttime;
	}
}
