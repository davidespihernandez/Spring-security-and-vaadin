package com.atkloud.repository


import groovy.transform.CompileStatic
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional
import com.atkloud.domain.SecRole

@CompileStatic
@Transactional
interface SecRoleRepository extends JpaRepository<SecRole, Long>{
	SecRole findOneById(Long id);
	SecRole findOneByAuthority(String authority);
}
