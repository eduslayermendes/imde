package com.example.ImageHandling.config;

import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;


import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

    @Value( "${app.security.roles.invoice.upload}" )
    private String[] uploadAllowedRoles;

    @Value( "${app.security.roles.invoices.readAll}" )
    private String[] readInvoicesAllowedRoles;

    @Value( "${app.security.roles.invoices.export}" )
    private String[] exportAllowedRoles;

    @Value( "${app.security.roles.invoices.edit}" )
    private String[] editMetadataAllowedRoles;

    @Value( "${app.security.roles.invoice.delete}" )
    private String[] deleteInvoicesAllowedRoles;

    @Value( "${app.security.roles.layouts.all}" )
    private String[] layoutsAllowedRoles;

    @Value( "${app.security.roles.issuers.all}" )
    private String[] issuersAllowedRoles;

    @Value( "${app.security.roles.auditlogs.all}" )
    private String[] logsAllowedRoles;

    @Value( "${app.security.roles.costCenters.all}" )
    private String[] costCentersAllowedRoles ;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
        SimpleAuthorityMapper authorityMapper = new SimpleAuthorityMapper();
        authorityMapper.setPrefix("");
        keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(authorityMapper);

        auth.authenticationProvider(keycloakAuthenticationProvider);
    }

    @Bean
    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http.csrf().disable()
            .sessionManagement()
            .sessionCreationPolicy( SessionCreationPolicy.STATELESS )
            .and()
            .authorizeRequests()
            .antMatchers("/api-docs/**", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
            .antMatchers( "/api/upload/**" ).hasAnyAuthority( uploadAllowedRoles )
            .antMatchers("/api/export/**").hasAnyAuthority(exportAllowedRoles)
            .antMatchers( "/api/**/edit-invoice-metadata" ).hasAnyAuthority(editMetadataAllowedRoles)
            .anyRequest().authenticated()
            .and().cors();
    }

    @Bean("deleteInvoicesAllowedRoles")
    public String[] deleteAllowedRoles() {
        return deleteInvoicesAllowedRoles;
    }

    @Bean("layoutsAllowedRoles")
    public String[] layoutsAllowedRoles() {
        return layoutsAllowedRoles;
    }

    @Bean("issuersAllowedRoles")
    public String[] issuersAllowedRoles() {
        return issuersAllowedRoles;
    }

    @Bean("logsAllowedRoles")
    public String[] logsAllowedRoles() {
        return logsAllowedRoles;
    }

    @Bean("costCentersAllowedRoles")
    public String[] costCentersAllowedRoles() {
        return costCentersAllowedRoles;
    }

    @Bean("readInvoicesAllowedRoles")
    public String[] readInvoicesAllowedRoles() {
        return readInvoicesAllowedRoles;
    }
}



