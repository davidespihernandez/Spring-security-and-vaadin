package com.atkloud.repository

import com.atkloud.domain.SecUser
import groovy.transform.CompileStatic
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional

@CompileStatic
@Transactional
interface SecUserRepository extends JpaRepository<SecUser, Long>{
	SecUser findOneById(Long id);
	SecUser findOneByUsername(String username);
}
