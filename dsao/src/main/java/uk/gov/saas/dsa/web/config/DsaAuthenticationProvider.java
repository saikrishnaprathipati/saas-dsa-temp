package uk.gov.saas.dsa.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import uk.gov.saas.dsa.domain.DsaAdvisor;
import uk.gov.saas.dsa.domain.UserPersonalDetails;
import uk.gov.saas.dsa.persistence.UserRepository;
import uk.gov.saas.dsa.service.AdvisorLoginService;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DsaAuthenticationProvider implements AuthenticationProvider {

	@Autowired
	private AdvisorLoginService advisorLoginService;
	@Autowired
	private UserRepository userRepo;
	private final String regex = "^(.+)@(.+)$";

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String username = authentication.getName();
		Object password = authentication.getCredentials();
		return new UsernamePasswordAuthenticationToken(username, password, getGrantedAuthorities(username));
	}

	private List<GrantedAuthority> getGrantedAuthorities(String username) {
		Pattern pattern = Pattern.compile(regex);
		Matcher mat = pattern.matcher(username);
		List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
		if(mat.matches()){
			DsaAdvisor dsaAdvisor = advisorLoginService.findAdvisorByEmail(username);
			if (null != dsaAdvisor) {
				grantedAuthorities.add(new SimpleGrantedAuthority("ROLE".concat(",").concat(dsaAdvisor.getRoleName())));
				grantedAuthorities.add(new SimpleGrantedAuthority("TEAM_EMAIL".concat(",").concat(dsaAdvisor.getTeamEmail())));
				grantedAuthorities.add(new SimpleGrantedAuthority("INSTITUTION".concat(",").concat(dsaAdvisor.getInstitution())));
				grantedAuthorities.add(new SimpleGrantedAuthority("USER_ID".concat(",").concat(dsaAdvisor.getUserId())));
			}
		} else {
			UserPersonalDetails user = userRepo.findByUserId(username);
			if (null != user) {
				grantedAuthorities.add(new SimpleGrantedAuthority("ROLE".concat(",").concat("STUDENT")));
				grantedAuthorities.add(new SimpleGrantedAuthority("EMAIL".concat(",").concat(user.getEmailAddress())));
				grantedAuthorities.add(new SimpleGrantedAuthority("NAME".concat(",").concat(user.getSurname().concat(user.getForename()))));
				grantedAuthorities.add(new SimpleGrantedAuthority("USER_ID".concat(",").concat(user.getUserId())));
			}
		}
		return grantedAuthorities;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return true;
	}
}
