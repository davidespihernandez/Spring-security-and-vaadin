package com.atkloud.domain

import groovy.transform.EqualsAndHashCode
import org.springframework.beans.factory.annotation.Autowired
import com.atkloud.repository.SecUserSecRoleRepository

import javax.persistence.*

@Entity //@IdClass(SecUserSecRolePK.class)
@Table(name = "sec_user_sec_role",
    uniqueConstraints=@UniqueConstraint(columnNames=["sec_user_id", "sec_role_id"])
)
class SecUserSecRole implements Serializable{

	@EmbeddedId
	private SecUserSecRolePK id = new SecUserSecRolePK();

	public getSecUser(){ 
		id.secUser
	}
	
	public getSecRole(){ 
		id.secRole
	}
	
	public setSecUser(SecUser secUser){
		id.secUser = secUser
	}
	
	public setSecRole(SecRole secRole){
		id.secRole = secRole
	}

}

@Embeddable
@EqualsAndHashCode
public class SecUserSecRolePK implements Serializable {
	@ManyToOne
	@JoinColumn(name = "sec_user_id", referencedColumnName = "id", nullable = false)
	SecUser secUser

	@ManyToOne
	@JoinColumn(name = "sec_role_id", referencedColumnName = "id", nullable = false)
	SecRole secRole
}

