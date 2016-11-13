package com.atkloud;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

import com.atkloud.service.BackendService;
import com.atkloud.service.SecurityService;
import com.atkloud.view.*;
import com.vaadin.server.FontAwesome;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vaadin.annotations.Theme;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.navigator.SpringViewProvider;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

@SpringUI
// No @Push annotation, we are going to enable it programmatically when the user logs on
@Theme(ValoTheme.THEME_NAME) // Looks nicer
public class MainUI extends UI {

    @Autowired AuthenticationManager authenticationManager;

    @Autowired BackendService backendService;

    @Autowired SpringViewProvider viewProvider;

    @Autowired ErrorView errorView;

    @Autowired SecurityService securityService;

    private Label timeAndUser;

    private Timer timer;

    @Override
    protected void init(VaadinRequest request) {
        getPage().setTitle("Vaadin and Spring Security Demo - Hybrid Security");
        if (SecurityUtils.isLoggedIn()) {
            showMain();
        } else {
            showLogin();
        }
    }

    private void showLogin() {
        setContent(new com.atkloud.view.LoginForm(this::login));
    }

    private void showMain() {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.setSizeFull();

        MenuBar menuBar = new MenuBar();
        menuBar.setWidth(100.0f, Unit.PERCENTAGE);
        MenuBar.MenuItem adminItem = menuBar.addItem("Admin", FontAwesome.COG, null);
        adminItem.addItem("Roles", FontAwesome.GROUP, event -> { getNavigator().navigateTo("roles"); } );
        adminItem.addItem("Users", FontAwesome.USERS, event -> { getNavigator().navigateTo("users"); } );
        MenuBar.MenuItem userItem = menuBar.addItem("User option", FontAwesome.USER, event -> { getNavigator().navigateTo(""); });
        menuBar.addItem("Logout", FontAwesome.SIGN_OUT, event -> { logout(); } );
        layout.addComponent(menuBar);

        Panel viewContainer = new Panel();
        viewContainer.setSizeFull();
        layout.addComponent(viewContainer);
        layout.setExpandRatio(viewContainer, 1.0f);

        setContent(layout);
        setErrorHandler(this::handleError);

        Navigator navigator = new Navigator(this, viewContainer);
        navigator.addProvider(viewProvider);
        navigator.setErrorView(errorView);
        viewProvider.setAccessDeniedViewClass(AccessDeniedView.class);
    }

    @Override
    public void detach() {
        super.detach();
    }

    private boolean login(String username, String password) {
        try {
            Authentication token = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(username, password));
            // Reinitialize the session to protect against session fixation attacks. This does not work
            // with websocket communication.
            VaadinService.reinitializeSession(VaadinService.getCurrentRequest());
            SecurityContextHolder.getContext().setAuthentication(token);
            // Now when the session is reinitialized, we can enable websocket communication. Or we could have just
            // used WEBSOCKET_XHR and skipped this step completely.
            getPushConfiguration().setTransport(Transport.WEBSOCKET);
            getPushConfiguration().setPushMode(PushMode.AUTOMATIC);
            // Show the main UI
            showMain();
            return true;
        } catch (AuthenticationException ex) {
            return false;
        }
    }

    private void logout() {
        getPage().reload();
        getSession().close();
    }

    private void handleError(com.vaadin.server.ErrorEvent event) {
        Throwable t = DefaultErrorHandler.findRelevantThrowable(event.getThrowable());
        if (t instanceof AccessDeniedException) {
            Notification.show("You do not have permission to perform this operation",
                Notification.Type.WARNING_MESSAGE);
        } else {
            DefaultErrorHandler.doDefault(event);
        }
    }
}
