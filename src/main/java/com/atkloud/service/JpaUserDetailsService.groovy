package com.atkloud.service

import com.atkloud.domain.JpaUser
import com.atkloud.domain.SecUser
import com.atkloud.repository.SecUserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

import javax.transaction.Transactional

/**
 * Created by Antonio on 8/3/2015.
 */

@Service
@Transactional
class JpaUserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    @Autowired SecUserRepository secUserRepository

    UserDetails loadUserByUsername(String username, boolean loadRoles) throws UsernameNotFoundException {
        def user = secUserRepository.findOneByUsername(username)
        if (!user) {
            throw new UsernameNotFoundException('User not found')
        }
        Collection<GrantedAuthority> authorities = loadAuthorities(user, username, loadRoles)
        createUserDetails user, authorities
    }
	
	def loadPrincipal(String userName)
	{
		JpaUser jpaUser = null;
        def user = secUserRepository.findOneByUsername(userName);
        jpaUser = new JpaUser(user.getUsername(), user.getPassword(), user.getEnabled(), user.getAccountExpired(), user.getCredentialsExpired(), user.getAccountLocked(), user.getAuthorities(), user.getId())
		return jpaUser
	}
	
	def loadAll(){
		def jpaUsers = []
		def users = secUserRepository.findAll()
        users.each{ user ->
            JpaUser jUser = new JpaUser(user.getUsername(), user.getPassword(), user.getEnabled(), user.getAccountExpired(), user.getCredentialsExpired(), user.getAccountLocked(), user.getAuthorities(), user.getId())
            jpaUsers.add(jUser);
        }
		return jpaUsers
	}

    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        loadUserByUsername(username, true)
    }

    protected Collection<GrantedAuthority> loadAuthorities(user, String username, boolean loadRoles) {
        if (!loadRoles) {
            return []
        }
        Collection<?> userAuthorities = user.authorities
        def authorities = userAuthorities.collect { new SimpleGrantedAuthority(it.authority) }
        return authorities ?: []
    }

    protected UserDetails createUserDetails(SecUser user, Collection<GrantedAuthority> authorities) {
        new JpaUser(user.username, user.password, user.enabled, !user.accountExpired, !user.credentialsExpired, !user.accountLocked, authorities, user.id)
    }
}
