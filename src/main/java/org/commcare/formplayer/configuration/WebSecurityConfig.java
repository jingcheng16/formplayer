package org.commcare.formplayer.configuration;

import org.commcare.formplayer.auth.CommCareSessionAuthFilter;
import org.commcare.formplayer.auth.HmacAuthFilter;
import org.commcare.formplayer.services.FormSessionService;
import org.commcare.formplayer.services.HqUserDetailsService;
import org.commcare.formplayer.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private HqUserDetailsService userDetailsService;

    @Autowired
    private FormSessionService formSessionService;

    @Value("${commcarehq.formplayerAuthKey}")
    private String formplayerAuthKey;

    @Value("${formplayer.allowDoubleSlash:true}")
    private boolean allowDoubleSlash;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        disableDefaults(http);

        // no auth required for management endpoints
        // (these are bound to a separate HTTP port that is not publicly exposed)
        http.authorizeRequests().requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll();

        // not auth required
        http
            .authorizeRequests()
            .antMatchers("/serverup", "/favicon.ico")
            .permitAll();

        // relaxed auth (only require HMAC but not user details)
        http
            .authorizeRequests()
            .antMatchers("/validate_form")
            .access("isAuthenticated() or hasAuthority('" + Constants.AUTHORITY_COMMCARE + "')");

        // full auth required
        http.authorizeRequests().antMatchers("/**").authenticated();

        // configure auth filters
        http.addFilterAt(getHmacAuthFilter(), UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(sessionAuthFilter(), HmacAuthFilter.class);
        http.csrf()
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .ignoringRequestMatchers(new RequestHeaderRequestMatcher(Constants.HMAC_HEADER));
        http.cors();
    }

    private HmacAuthFilter getHmacAuthFilter() {
        return HmacAuthFilter.builder()
                .hmacKey(formplayerAuthKey)
                .formSessionService(formSessionService)
                .build();
    }

    private void disableDefaults(HttpSecurity http) throws Exception {
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.requestCache().disable();  // only needed for login workflow
        http.logout().disable();
        http.formLogin().disable();
        http.httpBasic().disable();
    }

    public CommCareSessionAuthFilter sessionAuthFilter() throws Exception {
        CommCareSessionAuthFilter filter = new CommCareSessionAuthFilter();
        filter.setAuthenticationManager(authenticationManagerBean());
        filter.setRequiresAuthenticationRequestMatcher(new CommCareSessionAuthFilter.SessionAuthRequestMatcher());
        return filter;
    }

    /**
     * Configure the authentication manager with a provider that will use the
     * ``HQUserDetailsService`` to authenticate the user.
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        PreAuthenticatedAuthenticationProvider provider = new PreAuthenticatedAuthenticationProvider();
        provider.setPreAuthenticatedUserDetailsService(userDetailsService);
        auth.authenticationProvider(provider);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.httpFirewall(allowDoubleSlashHttpFirewall());
    }

    /**
     * Temporary customization of the firewall to allow '//' in the URL.
     * This should be removed once all 3rd party hosters have been given an opportunity
     * to update their proxy configuration.
     */
    @Bean
    public HttpFirewall allowDoubleSlashHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedDoubleSlash(allowDoubleSlash);
        return firewall;
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
