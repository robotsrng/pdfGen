package com.pdfgen.spring.managers;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	String [] publicUrls = new String [] {
			"/pGen/**"
	};

	String [] loginUrls = new String [] {
			"/", "/login/*"
	};

	//	@Autowired
	//	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private DataSource dataSource;

	//@Value("${pdfgen.queries.users-query}")
	//private String usersQuery;

	//@Value("${pdfgen.queries.roles-query}")
	//private String rolesQuery;

	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
		.antMatchers(loginUrls).permitAll()
		.antMatchers("/admin/**").hasAuthority("ADMIN").anyRequest()
		.authenticated().and().csrf().disable().formLogin()
		.loginPage("/login/login-page").failureUrl("/login/login-error")
		.defaultSuccessUrl("/login/login-success")
		.usernameParameter("name")
		.passwordParameter("password")
		.and().logout()
		.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
		.logoutSuccessUrl("/login/logout-success").deleteCookies("JSESSIONID")
		.invalidateHttpSession(true).and().exceptionHandling()
		.accessDeniedPage("/access-denied")
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