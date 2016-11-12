package com.atkloud.service

import com.atkloud.domain.SecRole
import com.atkloud.domain.SecUser
import com.atkloud.domain.SecUserSecRole
import com.atkloud.repository.SecRoleRepository
import com.atkloud.repository.SecUserRepository
import com.atkloud.repository.SecUserSecRoleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class SecurityService {
	static final String ROLE_ADMIN = "ROLE_ADMIN"
	static final String ROLE_USER = "ROLE_USER"

	@Autowired PasswordEncoder bcryptEncoder
	@Autowired SecUserRepository secUserRepository
	@Autowired SecRoleRepository secRoleRepository
	@Autowired SecUserSecRoleRepository secUserSecRoleRepository

	SecUser findSecUserByUsername(String userName){
		SecUser secUser = secUserRepository.findOneByUsername(userName)
		return secUser
	}

	SecUser findSecUserById(Long id){
		SecUser secUser = SecUser.get(id)
		return secUser
	}

	SecUser getPrincipal(){
		String userName = getAuthentication().name
		SecUser secUser = findSecUserByUsername(userName)
		return secUser
	}

	SecRole findSecRoleByAuthority(String authority){
		return(secRoleRepository.findOneByAuthority(authority))
	}

	SecRole createRole(String authority, String description){
		SecRole secRole = findSecRoleByAuthority(authority)
		if(secRole){
			return(secRole)
		}
		return(secRoleRepository.save(new SecRole(authority: authority, description: description)))
	}

	/**
	 * Get the currently logged in user's <code>Authentication</code>. If not authenticated
	 * and the AnonymousAuthenticationFilter is active (true by default) then the anonymous
	 * user's auth will be returned (AnonymousAuthenticationToken with username 'anonymousUser'
	 * unless overridden).
	 *
	 * @return the authentication
	 */
	Authentication getAuthentication() { SecurityContextHolder.context?.authentication }

	List<SecRole> findAllSecRoleBySecUser(SecUser secUser){
		List<SecUserSecRole> secUserSecRoles = secUserSecRoleRepository.findByIdSecUser(secUser)
		return(secUserSecRoles.secRole.findAll{ it.authority!=ROLE_ACCESS }.sort{ SecRole a, SecRole b -> a.getDescription() <=> b.getDescription() })
	}

	List<SecRole> findAllSecRoles(){
		return(SecRole.findAll().sort{ SecRole a, SecRole b -> a.getDescription() <=> b.getDescription() })
	}

	List<SecUser> findAllSecUsers(String filter=null){
		List<SecUser> allUsers = SecUser.findAll().sort { SecUser a, SecUser b -> a.getFullName()<=>b.getFullName() }
		if(filter && filter.trim()!=""){
			allUsers = allUsers.findAll{ it.getFullName().toUpperCase().contains(filter.toUpperCase()) }
		}
		return(allUsers)
	}

	SecUserSecRole grantRole(SecUser secUser, SecRole secRole){
		SecUserSecRole secUserSecRole = secUserSecRoleRepository.findOneByIdSecUserAndIdSecRole(secUser, secRole)
		if(secUserSecRole){
			return(secUserSecRole)
		}
		secUserSecRole = secUserSecRoleRepository.save(new SecUserSecRole(secUser: secUser, secRole: secRole))
		return(secUserSecRole)
	}

	SecUserSecRole grantRole(SecUser secUser, String authority){
		return(grantRole(secUser, findSecRoleByAuthority(authority)))
	}


	Boolean revokeRole(SecUser secUser, SecRole secRole){
		SecUserSecRole secUserSecRole = secUserSecRoleRepository.findOneByIdSecUserAndIdSecRole(secUser, secRole)
		if(secUserSecRole){
			secUserSecRoleRepository.delete(secUserSecRole)
			return(true)
		}
		return(false)
	}

	SecUserSecRole revokeRole(SecUser secUser, String authority){
		return(revokeRole(secUser, findSecRoleByAuthority(authority)))
	}

	Boolean hasRole(SecUser secUser, SecRole secRole){
		SecUserSecRole secUserSecRole = secUserSecRoleRepository.findOneByIdSecUserAndIdSecRole(secUser, secRole)
		if(secUserSecRole){
			return(true)
		}
		return(false)
	}

	Boolean hasRole(SecUser secUser, String authority){
		return(hasRole(secUser, findSecRoleByAuthority(authority)))
	}

	Boolean hasRole(String authority){
		return(hasRole(getPrincipal(), authority))
	}

	SecUser createSecUser(parameters){
		SecUser existingUser = findSecUserByUsername(parameters.username)
		if(existingUser){
			return(existingUser)
		}
		SecUser newUser = new SecUser(
				username: parameters.username,
				userEmail: parameters.userEmail,
				firstName: parameters.firstName,
				lastName: parameters.lastName,
				phoneNumber: parameters.phoneNumber
			)
		newUser.setEnabled(true)
		String newPassword = bcryptEncoder.encode(parameters.username)
		if(parameters.password && parameters.password.trim()!=""){
			newPassword = bcryptEncoder.encode(parameters.password)
		}
		newUser.setPassword(newPassword)
		newUser = secUserRepository.save(newUser)
		grantAndRevokeRoles(newUser, parameters.rolesToGrant, parameters.rolesToRevoke)
		grantRole(newUser, findSecRoleByAuthority(ROLE_USER))
		return(newUser)
	}

	SecUser updateSecUser(parameters){
		SecUser secUser = findSecUserById(parameters.id)
		if(!secUser){
			return(null)
		}
		secUser.setUserEmail(parameters.userEmail)
		secUser.setFirstName(parameters.firstName)
		secUser.setLastName(parameters.lastName)
		secUser.setPhoneNumber(parameters.phoneNumber)
		secUser = secUserRepository.save(secUser)
		grantAndRevokeRoles(secUser, parameters.rolesToGrant, parameters.rolesToRevoke)		
		return(secUser)
	}

	def changePassword(SecUser secUser, String password){
		String newPassword = bcryptEncoder.encode(password)
		secUser.setPassword(newPassword)
		return(secUserRepository.save(secUser))
	}

	def enableOrDisableSecRole(SecUser secUser, Boolean enabled){
		SecRole accessRole = findSecRoleByAuthority(ROLE_USER)
		if(secUser){
			if(!enabled){
				revokeRole(secUser, accessRole)
			}
			else{
				grantRole(secUser, accessRole)
			}
			secUser.setEnabled(enabled)
			secUserRepository.save(secUser)
		}
	}

	def grantAndRevokeRoles(SecUser secUser, List<SecRole> rolesToGrant, List<SecRole> rolesToRevoke){
		rolesToGrant.each{ SecRole secRole ->
			grantRole(secUser, secRole)
		}
		rolesToRevoke.each{ SecRole secRole ->
			revokeRole(secUser, secRole)
		}
	}

}
