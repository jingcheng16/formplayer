package org.commcare.formplayer.beans.menus;

import org.commcare.formplayer.beans.NotificationMessage;
import org.commcare.modern.session.SessionWrapper;
import org.commcare.util.screen.ScreenUtils;

import java.util.HashMap;

/**
 * Base class for responses being sent to the front end. Params are: title - self explanatory
 * notification - A message String and error boolean to be displayed by frontend sholdAutoSubmit - A
 * boolean to indicate if the frontend should auto-submit the form clearSession - True if the front
 * end should redirect to the app home NOTE: clearSession causes all other displayables to be
 * disregarded
 */
public class BaseResponseBean extends LocationRelevantResponseBean {
    protected NotificationMessage notification;
    protected String title;
    protected boolean clearSession;
    protected boolean shouldAutoSubmit;
    private String appId;
    private String appVersion;
    private String[] selections;
    private HashMap<String, String> translations;
    private String smartLinkRedirect;

    public BaseResponseBean() {
    }

    public BaseResponseBean(String title, NotificationMessage notification, boolean clearSession) {
        this.title = title;
        this.notification = notification;
        this.clearSession = clearSession;
        this.shouldAutoSubmit = false;
    }

    protected void processTitle(SessionWrapper session) {
        setTitle(ScreenUtils.getBestTitle(session));
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSmartLinkRedirect() {
        return smartLinkRedirect;
    }

    public void setSmartLinkRedirect(String smartLinkRedirect) {
        this.smartLinkRedirect = smartLinkRedirect;
    }

    public NotificationMessage getNotification() {
        return notification;
    }

    public void setNotification(NotificationMessage notification) {
        this.notification = notification;
    }

    public boolean getShouldAutoSubmit() {
        return shouldAutoSubmit;
    }

    public void setShouldAutoSubmit(boolean shouldAutoSubmit) {
        this.shouldAutoSubmit = shouldAutoSubmit;
    }

    public boolean isclearSession() {
        return clearSession;
    }

    public void setClearSession(boolean clearSession) {
        this.clearSession = clearSession;
    }

    @Override
    public String toString() {
        return "BaseResponseBean [title=" + title + ", " +
                "notification=" + notification + ", " +
                "clearSession=" + clearSession + ", " +
                "shouldAutoSubmit=" + shouldAutoSubmit + ", " +
                "selections=" + selections + "]";
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String[] getSelections() {
        return selections;
    }

    public void setSelections(String[] selections) {
        this.selections = selections;
    }

    public HashMap<String, String> getTranslations() {
        return translations;
    }

    public void setTranslations(HashMap<String, String> translations) {
        this.translations = translations;
    }

    public void addToTranslation(String key, String value) {
        if (this.translations == null) {
            this.translations = new HashMap<String, String>();
        }
        this.translations.put(key, value);
    }
}
