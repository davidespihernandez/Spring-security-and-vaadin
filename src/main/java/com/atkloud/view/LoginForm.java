package com.atkloud.view;

import com.vaadin.event.ShortcutAction;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

public class LoginForm extends VerticalLayout {

    public LoginForm(LoginCallback callback) {
        setMargin(true);
        setSpacing(true);
        setSizeFull();

        VerticalLayout centerLayout = new VerticalLayout();
        centerLayout.setSizeUndefined();
        centerLayout.setMargin(true);
        centerLayout.setSpacing(true);


        TextField username = new TextField("Username");
        centerLayout.addComponent(username);

        PasswordField password = new PasswordField("Password");
        centerLayout.addComponent(password);

        Button login = new Button("Login", evt -> {
            String pword = password.getValue();
            password.setValue("");
            if (!callback.login(username.getValue(), pword)) {
                Notification.show("Login failed");
                username.focus();
            }
        });
        login.setClickShortcut(ShortcutAction.KeyCode.ENTER);
        login.setIcon(FontAwesome.SIGN_IN);
        login.addStyleName(ValoTheme.BUTTON_PRIMARY);
        centerLayout.addComponent(login);
        addComponent(centerLayout);
        setComponentAlignment(centerLayout, Alignment.MIDDLE_CENTER);
    }

    @FunctionalInterface
    public interface LoginCallback {
        boolean login(String username, String password);
    }
}
