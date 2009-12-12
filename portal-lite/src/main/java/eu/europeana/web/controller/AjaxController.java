package eu.europeana.web.controller;

import eu.europeana.database.StaticInfoDao;
import eu.europeana.database.UserDao;
import eu.europeana.database.domain.CarouselItem;
import eu.europeana.database.domain.SavedItem;
import eu.europeana.database.domain.SavedSearch;
import eu.europeana.database.domain.SearchTerm;
import eu.europeana.database.domain.SocialTag;
import eu.europeana.database.domain.User;
import eu.europeana.query.DocType;
import eu.europeana.query.RequestLogger;
import eu.europeana.web.util.ControllerUtil;
import eu.europeana.web.util.EmailSender;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.util.Map;
import java.util.TreeMap;

/**
 * General Controller for all AJAX actions
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@Controller
public class AjaxController {

    protected Logger log = Logger.getLogger(getClass());
    private boolean debug = false; // true logs the exception to the xml response
    private boolean success = false;
    private String exceptionString = "";

    @Autowired
    private UserDao userDao;

    @Autowired
    private StaticInfoDao staticInfoDao;

    @Autowired
    private RequestLogger requestLogger;

    @Autowired
    @Qualifier("emailSenderForSendToFriend")
    private EmailSender friendEmailSender;

    @RequestMapping("/remove.ajax")
    public ModelAndView handleAjaxRemoveRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            if (!hasJavascriptInjection(request)) {
                success = processAjaxRemoveRequest(request);
            }
        }
        catch (Exception e) {
            handleAjaxException(e, response);
        }
        return createResponsePage(debug, success, exceptionString, response);
    }

    public Boolean processAjaxRemoveRequest(HttpServletRequest request) throws Exception {
        User user = ControllerUtil.getUser();
        String className = request.getParameter("className");
        String idString = request.getParameter("id");
        if (className == null || idString == null) {
            throw new IllegalArgumentException("Expected 'className' and 'id' parameters!");
        }
        Long id = Long.valueOf(idString);

        switch (findModifiable(className)) {
            case CAROUSEL_ITEM:
                user = staticInfoDao.removeCarouselItem(user, id);
                break;
            case SAVED_ITEM:
                user = userDao.removeSavedItem(user, id);
                break;
            case SAVED_SEARCH:
                user = userDao.removeSavedSearch(user, id);
                break;
            case SEARCH_TERM:
                user = staticInfoDao.removeSearchTerm(user, id);
                break;
            case SOCIAL_TAG:
                user = userDao.removeSocialTag(user, id);
                break;
            default:
                throw new IllegalArgumentException("Unhandled removable");
        }

        ControllerUtil.setUser(user);
        return true;
    }

    @RequestMapping("/save.ajax")
    public ModelAndView handleAjaxSaveRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            if (!hasJavascriptInjection(request)) {
                success = processAjaxSaveRequest(request);
            }
        }
        catch (Exception e) {
            handleAjaxException(e, response);
        }
        return createResponsePage(debug, success, exceptionString, response);
    }

    public boolean processAjaxSaveRequest(HttpServletRequest request) throws Exception {
        User user = ControllerUtil.getUser();
        String className = request.getParameter("className");
        String idString = request.getParameter("id");
        if (className == null) {
            throw new IllegalArgumentException("Expected 'className' parameter!");
        }

        switch (findModifiable(className)) {
            case CAROUSEL_ITEM:
                SavedItem savedItemForCarousel = userDao.fetchSavedItemById(Long.valueOf(idString));
                CarouselItem carouselItem = staticInfoDao.createCarouselItem(
                        savedItemForCarousel.getEuropeanaId(),
                        savedItemForCarousel.getId());
                if (carouselItem == null) {
                    return false;
                }
                break;
            case SAVED_ITEM:
                SavedItem savedItem = new SavedItem();
                savedItem.setTitle(getStringParameter("title", request));
                savedItem.setAuthor(getStringParameter("author", request));
                savedItem.setDocType(DocType.valueOf(getStringParameter("docType", request)));
                savedItem.setLanguage(ControllerUtil.getLocale(request));
                savedItem.setEuropeanaObject(getStringParameter("europeanaObject", request));
                user = userDao.addSavedItem(user, savedItem, getStringParameter("europeanaUri", request));
                break;
            case SAVED_SEARCH:
                SavedSearch savedSearch = new SavedSearch();
                savedSearch.setQuery(getStringParameter("query", request));
                savedSearch.setQueryString(URLDecoder.decode(getStringParameter("queryString", request), "utf-8"));
                savedSearch.setLanguage(ControllerUtil.getLocale(request));
                user = userDao.addSavedSearch(user, savedSearch);
                break;
            case SEARCH_TERM:
                SearchTerm searchTerm = staticInfoDao.addSearchTerm(Long.valueOf(idString));
                if (searchTerm == null) {
                    return false;
                }
                break;
            case SOCIAL_TAG:
                SocialTag socialTag = new SocialTag();
                socialTag.setTag(getStringParameter("tag", request));
                socialTag.setEuropeanaUri(getStringParameter("europeanaUri", request));
                socialTag.setDocType(DocType.valueOf(getStringParameter("docType", request)));
                socialTag.setEuropeanaObject(getStringParameter("europeanaObject", request));
                socialTag.setTitle(getStringParameter("title", request));
                socialTag.setLanguage(ControllerUtil.getLocale(request));
                user = userDao.addSocialTag(user, socialTag);
                break;
            default:
                throw new IllegalArgumentException("Unhandled removable");
        }

        ControllerUtil.setUser(user);
        return true;
    }

    // todo: finish this handler. + give back MAV object

    @RequestMapping("/email-to-friend.ajax")
    public ModelAndView handleSendToAFriendHandler(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            if (!hasJavascriptInjection(request)) {
                success = processSendToAFriendHandler(request);
            }
        }
        catch (Exception e) {
            handleAjaxException(e, response);
        }
        return createResponsePage(debug, success, exceptionString, response);
    }


    public boolean processSendToAFriendHandler(HttpServletRequest request) throws Exception {
        String emailAddress = getStringParameter("email", request);
        if (!ControllerUtil.validEmailAddress(emailAddress)) {
            throw new IllegalArgumentException("Email address invalid: [" + emailAddress + "]");
        }
        String uri = getStringParameter("uri", request);
        User user = ControllerUtil.getUser();
        Map<String, Object> model = new TreeMap<String, Object>();
        model.put("user", user);
        model.put("uri", uri);
        model.put("email", emailAddress);
        String subject = "A link from Europeana"; // replace with injection later
        friendEmailSender.sendEmail(emailAddress, user.getEmail(), subject, model);
        return true;
    }

    private void handleAjaxException(Exception e, HttpServletResponse response) {
        success = false;
        response.setStatus(400);
        exceptionString = getStackTrace(e);
        log.warn("Problem handling AJAX request", e);
    }

    private ModelAndView createResponsePage(boolean debug, boolean success, String exceptionString, HttpServletResponse response) {
        ModelAndView page = ControllerUtil.createModelAndViewPage("ajax");
        response.setContentType("text/xml");
        page.addObject("success", String.valueOf(success));
        page.addObject("exception", exceptionString);
        page.addObject("debug", debug);
        return page;
    }

    private Modifiable findModifiable(String className) {
        for (Modifiable modifiable : Modifiable.values()) {
            if (modifiable.matches(className)) {
                return modifiable;
            }
        }
        throw new IllegalArgumentException("Unable to find removable class with name " + className);
    }


    private enum Modifiable {
        SEARCH_TERM(SearchTerm.class),
        CAROUSEL_ITEM(CarouselItem.class),
        SAVED_ITEM(SavedItem.class),
        SAVED_SEARCH(SavedSearch.class),
        SOCIAL_TAG(SocialTag.class);

        private String className;

        private Modifiable(Class<?> clazz) {
            this.className = clazz.getName().substring(clazz.getName().lastIndexOf('.') + 1);
        }

        public boolean matches(String className) {
            return this.className.equals(className);
        }
    }


    protected String getStringParameter(String parameterName, HttpServletRequest request) {
        String stringValue = request.getParameter(parameterName);
        if (stringValue == null) {
            throw new IllegalArgumentException("Missing parameter: " + parameterName);
        }
        stringValue = stringValue.trim();
        return stringValue;
    }

    private String getStackTrace(Exception exception) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    // todo: write better javascript detection code

    private boolean hasJavascriptInjection(HttpServletRequest request) {
        boolean hasJavascript = false;
        Map map = request.getParameterMap();
        for (Object o : map.keySet()) {
            if (request.getParameter(String.valueOf(o)).contains("<")) {
                hasJavascript = true;
                log.warn("The request contains javascript so do not process this request");
                break;
            }
        }
        return hasJavascript;
    }

    public void setFriendEmailSender(EmailSender friendEmailSender) {
        this.friendEmailSender = friendEmailSender;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void setStaticInfoDao(StaticInfoDao staticInfoDao) {
        this.staticInfoDao = staticInfoDao;
    }

    public void setRequestLogger(RequestLogger requestLogger) {
        this.requestLogger = requestLogger;
    }

}