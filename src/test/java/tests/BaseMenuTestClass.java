package tests;

import application.MenuController;
import auth.HqAuth;
import beans.InstallRequestBean;
import beans.MenuSelectBean;
import beans.MenuSelectRepeater;
import beans.SessionNavigationBean;
import beans.menus.CommandListResponseBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import install.FormplayerConfigEngine;
import objects.SerializableMenuSession;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commcare.api.persistence.SqlSandboxUtils;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import repo.MenuRepo;
import repo.SessionRepo;
import services.InstallService;
import services.RestoreService;
import services.XFormService;
import util.Constants;
import utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Created by willpride on 4/13/16.
 */
public class BaseMenuTestClass {
    protected MockMvc mockMvc;

    @Autowired
    protected MenuRepo menuRepoMock;

    @Autowired
    protected SessionRepo sessionRepoMock;

    @Autowired
    protected XFormService xFormServiceMock;

    @Autowired
    protected RestoreService restoreServiceMock;

    @Autowired
    protected InstallService installService;

    @InjectMocks
    MenuController menuController;

    protected ObjectMapper mapper;

    final protected SerializableMenuSession serializableMenuSession = new SerializableMenuSession();

    protected String urlPrepend(String string){
        return "/" + string;
    }

    Log log = LogFactory.getLog(BaseMenuTestClass.class);

    @Before
    public void setUp() throws IOException {
        Mockito.reset(sessionRepoMock);
        Mockito.reset(xFormServiceMock);
        Mockito.reset(restoreServiceMock);
        Mockito.reset(menuRepoMock);
        Mockito.reset(installService);
        MockitoAnnotations.initMocks(this);
        mapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(menuController).build();
        when(restoreServiceMock.getRestoreXml(anyString(), any(HqAuth.class)))
                .thenReturn(FileUtils.getFile(this.getClass(), "test_restore.xml"));
        setupMenuMock();
        setupInstallServiceMock();
    }

    private String resolveAppId(String ref){
        log.info("Test resolving hack ref: " + ref);
        String appId = ref.substring(ref.indexOf("app_id=") + "app_id=".length(),
                ref.indexOf("#hack"));
        log.info("Got appId: " + ref);
        if(appId.equals("305d31c1457d6a41232fb52ecff038ff")){
            ref = "apps/basic2/profile.ccpr";
        }
        log.info("Resolved ref: " + ref);
        return ref;
    }

    protected void setupInstallServiceMock() throws IOException {
        try {
            doAnswer(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                    try {
                        Object[] args = invocationOnMock.getArguments();
                        String ref = (String) args[0];
                        if(ref.contains("#hack=commcare.ccz")){
                            ref = resolveAppId(ref);
                        }
                        String username = (String) args[1];
                        FormplayerConfigEngine engine = new FormplayerConfigEngine(username, "dbs");
                        String absolutePath = getTestResourcePath(ref);
                        if (absolutePath.endsWith(".ccpr")) {
                            engine.initFromLocalFileResource(absolutePath);
                        } else if (absolutePath.endsWith(".ccz")) {
                            engine.initFromArchive(absolutePath);
                        } else {
                            throw new RuntimeException("Can't install with reference: " + absolutePath);
                        }
                        engine.initEnvironment();
                        return engine;
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw e;
                    }
                }
            }).when(installService).configureApplication(anyString(), anyString(), anyString());
        } catch(Exception e){
            // don't think we need error handling for mocking
            e.printStackTrace();
        }
    }

    private void setupMenuMock() {
        when(menuRepoMock.find(anyString())).thenReturn(serializableMenuSession);
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                SerializableMenuSession toBeSaved = (SerializableMenuSession) args[0];
                serializableMenuSession.setActions(toBeSaved.getActions());
                serializableMenuSession.setUsername(toBeSaved.getUsername());
                serializableMenuSession.setDomain(toBeSaved.getDomain());
                serializableMenuSession.setActions(toBeSaved.getActions());
                serializableMenuSession.setSessionId(toBeSaved.getSessionId());
                serializableMenuSession.setInstallReference(toBeSaved.getInstallReference());
                serializableMenuSession.setPassword(toBeSaved.getPassword());
                serializableMenuSession.setSerializedCommCareSession(toBeSaved.getSerializedCommCareSession());
                serializableMenuSession.setCurrentSelection(toBeSaved.getCurrentSelection());
                return null;
            }
        }).when(menuRepoMock).save(any(SerializableMenuSession.class));
    }

    protected String getTestResourcePath(String resourcePath){
        URL url = this.getClass().getClassLoader().getResource(resourcePath);
        File file = new File(url.getPath());
        return file.getAbsolutePath();
    }

    public JSONObject selectMenuRepeat(String requestPath, String sessionId) throws Exception {
        MenuSelectRepeater menuSelectRepeater = mapper.readValue
                (FileUtils.getFile(this.getClass(), requestPath), MenuSelectRepeater.class);
        menuSelectRepeater.setSessionId(sessionId);
        ResultActions selectResult = mockMvc.perform(
                post(urlPrepend(Constants.URL_MENU_SELECT_REPEATER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(menuSelectRepeater)));
        String resultString = selectResult.andReturn().getResponse().getContentAsString();
        JSONObject ret = new JSONObject(resultString);
        return ret;
    }

    public JSONObject sessionNavigate(String requestPath) throws Exception {
        SessionNavigationBean sessionNavigationBean = mapper.readValue
                (FileUtils.getFile(this.getClass(), requestPath), SessionNavigationBean.class);
        ResultActions selectResult = mockMvc.perform(
                post(urlPrepend(Constants.URL_MENU_NAVIGATION))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(sessionNavigationBean)));
        String resultString = selectResult.andReturn().getResponse().getContentAsString();
        JSONObject ret = new JSONObject(resultString);
        return ret;
    }

    public JSONObject sessionNavigate(String[] selections) throws Exception {
        SessionNavigationBean sessionNavigationBean = new SessionNavigationBean();
        sessionNavigationBean.setSelections(selections);
        ResultActions selectResult = mockMvc.perform(
                post(urlPrepend(Constants.URL_MENU_NAVIGATION))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(sessionNavigationBean)));
        String resultString = selectResult.andReturn().getResponse().getContentAsString();
        JSONObject ret = new JSONObject(resultString);
        return ret;
    }

    public CommandListResponseBean doInstall(String requestPath) throws Exception {
        InstallRequestBean installRequestBean = mapper.readValue
                (FileUtils.getFile(this.getClass(), requestPath), InstallRequestBean.class);
        ResultActions installResult = mockMvc.perform(
                post(urlPrepend(Constants.URL_INSTALL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(installRequestBean)));
        String installResultString = installResult.andReturn().getResponse().getContentAsString();
        CommandListResponseBean menuResponseBean = mapper.readValue(installResultString,
                CommandListResponseBean.class);
        return menuResponseBean;
    }
}
