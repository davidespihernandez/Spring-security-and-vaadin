package com.atkloud

import com.atkloud.domain.SecRole;
import com.atkloud.domain.SecUser;
import com.atkloud.service.SecurityService
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Bootstrap implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);

    @Autowired
    SecurityService securityService

    @Override
    public void run(String... strings) throws Exception {
        logger.info("Bootstrap starts");
        def roles = [
                [authority: SecurityService.ROLE_ADMIN, description: "Administrator"],
                [authority: SecurityService.ROLE_ACCESS, description: "Application access"]
        ]
        roles.each{ Map roleInfo ->
            SecRole role = securityService.findSecRoleByAuthority(roleInfo.authority)
            if(role == null){
                role = securityService.createRole(roleInfo.authority, roleInfo.description)
            }
        }

        SecUser adminUser = securityService.findSecUserByUsername('admin')
        if(adminUser == null){
            adminUser = securityService.createSecUser(
                    username: 'admin',
                    password: 'admin2016',
                    userEmail: 'info@atkloud.com',
                    firstName: 'Administration',
                    lastName: "User",
                    address: 'Miami',
                    phoneNumber: '')
        }

        securityService.grantRole(adminUser, securityService.findSecRoleByAuthority(SecurityService.ROLE_ADMIN));
        securityService.grantRole(adminUser, securityService.findSecRoleByAuthority(SecurityService.ROLE_ACCESS));

        logger.info("Bootstrap ends");
    }
}

