package com.pdfgen.spring.managers;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	String [] loginUrls = new String [] {
			"/", "/error/**", "/login/create-user-form", "/login/create-user", "/login/login-page", "/error/**", "/login/logout-success", "/login/logout", "/login/logout-error"
	};

	//	@Autowired
	//	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private DataSource dataSource;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
		.antMatchers(loginUrls).permitAll()
		.antMatchers("/gen/**").authenticated()
		.antMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
		.and().csrf().disable().formLogin()
		.loginPage("/login/login-page").failureUrl("/error?code=bad-login")
		.defaultSuccessUrl("/success?code=good-login")
		.usernameParameter("name")
		.passwordParameter("password")
		.and().logout()
		.logoutRequestMatcher(new AntPathRequestMatcher("/login/logout"))
		.logoutSuccessUrl("/success?code=good-logout").deleteCookies("JSESSIONID")
		.invalidateHttpSession(true).and()
		.exceptionHandling()
		.accessDeniedPage("/error?code=access-denied")
		;
	}

	@Override
	public void configure(AuthenticationManagerBuilder builder)
			throws Exception {
		builder.userDetailsService(new MyUserDetailsService());
	}
	
	//@Override
	//protected void configure(AuthenticationManagerBuilder auth)
		//	throws Exception {
		//auth.inMemoryAuthentication().withUser("user").password("{noop}password").roles("ADMIN");
		//jdbcAuthentication()
		//.usersByUsernameQuery(usersQuery)
		//.authoritiesByUsernameQuery(rolesQuery)
		//.dataSource(dataSource)
		//		.passwordEncoder(bCryptPasswordEncoder)
		; 
	//}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web
		.ignoring()
		.antMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**");
	}
}