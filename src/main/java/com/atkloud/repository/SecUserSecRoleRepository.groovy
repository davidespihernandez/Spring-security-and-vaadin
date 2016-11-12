package com.atkloud.repository

import groovy.transform.CompileStatic
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional
import com.atkloud.domain.SecRole
import com.atkloud.domain.SecUser
import com.atkloud.domain.SecUserSecRole

@CompileStatic
@Transactional
interface SecUserSecRoleRepository extends JpaRepository<SecUserSecRole, Long>{
	
	List<SecUserSecRole> findByIdSecUser(SecUser secUser);
	List<SecUserSecRole> findByIdSecRole(SecRole secRole);
	SecUserSecRole findOneByIdSecUserAndIdSecRole(SecUser secUser, SecRole secRole);
	Long deleteByIdSecUserAndIdSecRole(SecUser secUser, SecRole secRole);
}
