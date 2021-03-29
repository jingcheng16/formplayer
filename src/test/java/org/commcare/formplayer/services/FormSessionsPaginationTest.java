package org.commcare.formplayer.services;

import com.google.common.collect.ImmutableMap;
import org.commcare.formplayer.beans.FormsSessionsRequestBean;
import org.commcare.formplayer.objects.FormSessionListView;
import org.commcare.formplayer.objects.SerializableFormSession;
import org.commcare.formplayer.repo.FormSessionRepo;
import org.commcare.formplayer.repo.FormplayerBaseJpaRepoImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

@ContextConfiguration
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnableJpaAuditing
public class FormSessionsPaginationTest {

    @Autowired
    FormSessionService formSessionService;

    @Autowired
    private FormSessionRepo formSessionRepo;

    @Test
    public void testPagination() {
        int totalNumberOfForms = 18;
        for (int i = 0; i < totalNumberOfForms; i++) {
            SerializableFormSession session = new SerializableFormSession(
                    "domain", "appId", "momo", "momo", "restoreAsCaseId",
                    "/a/domain/receiver", null, "More momo", true, "en", false,
                    ImmutableMap.of("a", "1", "b", "2"),
                    null
            );
            formSessionService.saveSession(session);
        }
        FormsSessionsRequestBean formsSessionsRequestBean = new FormsSessionsRequestBean();
        formsSessionsRequestBean.setUsername("momo");
        formsSessionsRequestBean.setDomain("domain");
        formsSessionsRequestBean.setRestoreAs("momo");
        formsSessionsRequestBean.setPageSize(5);
        formsSessionsRequestBean.setOffset(15);
        List<FormSessionListView> formSessions = formSessionService.getSessionsForUser("momo", formsSessionsRequestBean);
        assert formSessions.size() == 3;
        formSessionService.purge();
    }

    // only include the service under test and it's dependencies
    // This should not be necessary but we're using older versions of junit and spring
    @ComponentScan(
            basePackageClasses = {FormSessionService.class, FormSessionRepo.class},
            useDefaultFilters = false,
            includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {FormSessionService.class, FormSessionRepo.class})
    )
    @EnableJpaRepositories(
            basePackages = {"org.commcare.formplayer.repo"},
            repositoryBaseClass = FormplayerBaseJpaRepoImpl.class
    )
    @EntityScan("org.commcare.formplayer.objects")
    @EnableCaching
    @Configuration
    public static class FormSessionServicePaginationTestConfig {
    }
}
