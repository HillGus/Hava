package annotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private TesteRepository repository;

	public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {

		System.out.println("Username");
		System.out.println(s);

		Test user = this.repository.findByNome(s);

		if (user == null)
			throw new UsernameNotFoundException("AAAAAAAAAA");

		return new User(user.getNome(), user.getSenha(), new ArrayList());
	}
}
