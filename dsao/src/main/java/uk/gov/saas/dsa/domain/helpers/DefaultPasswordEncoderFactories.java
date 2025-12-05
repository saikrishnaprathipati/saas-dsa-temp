package uk.gov.saas.dsa.domain.helpers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class DefaultPasswordEncoderFactories {

	private DefaultPasswordEncoderFactories(){
		 throw new IllegalStateException("Utility class");
	}

    public static PasswordEncoder createDelegatingPasswordEncoder() {

        String encodingId = "argon2";
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put(encodingId, new Argon2PasswordEncoder(16,16,1,2,2)); //15MiB = ~2Kibibytes

        DelegatingPasswordEncoder delegatingPasswordEncoder = new DelegatingPasswordEncoder(encodingId, encoders);
        delegatingPasswordEncoder.setDefaultPasswordEncoderForMatches(new LegacyPasswordEncoder());

        return delegatingPasswordEncoder;
    }

}
