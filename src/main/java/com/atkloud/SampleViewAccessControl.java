package com.atkloud;

import com.atkloud.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vaadin.spring.access.ViewAccessControl;
import com.vaadin.ui.UI;

/**
 * This demonstrates how you can control access to views.
 */
@Component
public class SampleViewAccessControl implements ViewAccessControl {

    @Autowired
    SecurityService securityService;

    @Override
    public boolean isAccessGranted(UI ui, String beanName) {
        if (beanName.equals("adminView")) {
//            System.out.println("Checking admin role");
            return securityService.hasRole("ROLE_ADMIN");
        } else {
//            System.out.println("Checking user role");
            return securityService.hasRole("ROLE_USER");
        }
    }
}
