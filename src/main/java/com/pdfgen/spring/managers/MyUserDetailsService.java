package com.pdfgen.spring.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class MyUserDetailsService implements UserDetailsService {


	private static List<UserObject> users = new ArrayList<UserObject>();

	public MyUserDetailsService() {
		getAllUserDetails();
	}

	public static void getAllUserDetails() {
		ArrayList<MyUser> MyUsers = PdfDBManager.getAllUsers() ;
		for(MyUser user : MyUsers) {                 
			UserObject tempUserObj = new UserObject(user.getName(), user.getPassword(), PdfDBManager.getUserAuthority(user.getName())) ;
			users.add(tempUserObj) ;
		}
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<UserObject> user = users.stream()
				.filter(u -> u.name.equals(username))
				.findAny();
		if (!user.isPresent()) {
			throw new UsernameNotFoundException("User not found by name: " + username);
		}
		return toUserDetails(user.get());
	}

	private UserDetails toUserDetails(UserObject userObject) {
		return User.withUsername(userObject.name)
				.password("{noop}" + userObject.password)
				.roles(userObject.role).build();
	}

	private static class UserObject {
		private String name;
		private String password;
		private String role;

		public UserObject(String name, String password, String role) {
			this.name = name;
			this.password = password;
			this.role = role;
		}
	}
}