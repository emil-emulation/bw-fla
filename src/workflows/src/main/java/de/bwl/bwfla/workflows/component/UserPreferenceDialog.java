package de.bwl.bwfla.workflows.component;

import java.util.Map;
import java.util.logging.Logger;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.FacesComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import de.bwl.bwfla.workflows.beans.common.UserPreferencesBean;
import de.bwl.bwfla.workflows.beans.common.XkbConfigRegistryHelper;

@ResourceDependencies({
	@ResourceDependency(library = "primefaces", name = "jquery/jquery.js"),
	@ResourceDependency(library = "primefaces", name = "primefaces.js")
})
@FacesComponent(value = UserPreferenceDialog.COMPONENT_TYPE)
public class UserPreferenceDialog extends UINamingContainer {
	public static final String COMPONENT_TYPE = "de.bwl.bwfla.workflows.component.UserPreferenceDialog";
		
	protected static final Logger log = Logger.getLogger(UserPreferenceDialog.class.getName());
	
	private static final String USER_PREF_COOKIE_NAME = "UserPreferences";
	private static final int USER_PREF_COOKIE_EXPIRES = 12*4*7*24*60*60; // preserve the cookie for 1 years
	
	private Map<String, String> models = XkbConfigRegistryHelper.getModels();
	private Map<String, String> layouts = XkbConfigRegistryHelper.getLayouts();
	
	private UserPreferencesBean userPrefs;
	
	public UserPreferenceDialog() {
		super();
		
		userPrefs = getUserPreferences();
		
		if (userPrefs == null) {
			userPrefs = new UserPreferencesBean();
		}
	}
	
	public static UserPreferencesBean getUserPreferences() {	
		Map<String, Object> requestCookieMap = FacesContext.getCurrentInstance().getExternalContext().getRequestCookieMap();
		Cookie cookie = (Cookie)requestCookieMap.get(USER_PREF_COOKIE_NAME);
		
		if (cookie == null) {
			return null;
		}
		
		Gson gson = new Gson();
		return gson.fromJson(cookie.getValue(), UserPreferencesBean.class);
	}
	
	public void updateUserPreferences() {
		Gson gson = new Gson();
		String cookieVal = gson.toJson(userPrefs);
		
		HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
		Cookie cookie = new Cookie(USER_PREF_COOKIE_NAME, cookieVal);
		cookie.setMaxAge(USER_PREF_COOKIE_EXPIRES);
		cookie.setPath("/");
		response.addCookie(cookie);
		
		log.info("updatedUserPreferences " + cookieVal);
	}

	public Map<String, String> getModels() {
		return models;
	}

	public void setModels(Map<String, String> models) {
		this.models = models;
	}

	public Map<String, String> getLayouts() {
		return layouts;
	}

	public void setLayouts(Map<String, String> layouts) {
		this.layouts = layouts;
	}

	public UserPreferencesBean getUserPrefs() {
		return userPrefs;
	}

	public void setUserPrefs(UserPreferencesBean userPrefs) {
		this.userPrefs = userPrefs;
	}
}