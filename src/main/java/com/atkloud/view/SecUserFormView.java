package com.atkloud.view;

import com.atkloud.domain.SecRole;
import com.atkloud.domain.SecUser;
import com.atkloud.service.SecurityService;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.ShortcutAction;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@SpringView(name = "secUserForm") // Root view
public class SecUserFormView extends VerticalLayout implements View {

    SecurityService securityService;

    @PropertyId("username")
    private TextField usernameTextField;

    @PropertyId("userEmail")
    private TextField userEmailTextField;

    @PropertyId("firstName")
    private TextField firstNameTextField;

    @PropertyId("lastName")
    private TextField lastNameTextField;

    @PropertyId("phoneNumber")
    private TextField phoneNumberTextField;

    private BeanFieldGroup<SecUser> binder = new BeanFieldGroup<>(SecUser.class);
    private VerticalLayout formLayout;
    private Grid rolesGrid;
    private BeanItemContainer<SecRole> secRoleContainer;

    @Autowired
    public SecUserFormView(SecurityService securityService) {
        this.securityService = securityService;
    }

    @PostConstruct
    void init(){
        setMargin(true);
        setSpacing(true);
        setSizeFull();
        configureForm();
        Label captionLabel = new Label("User info");
        captionLabel.setStyleName(ValoTheme.LABEL_H2);
        captionLabel.setStyleName(ValoTheme.LABEL_COLORED);

        HorizontalLayout northLayout = new HorizontalLayout();
        northLayout.addComponent(captionLabel);

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setSpacing(true);

        verticalLayout.addComponent(northLayout);
        verticalLayout.addComponent(formLayout);

        verticalLayout.setExpandRatio(formLayout, 1.0f);
        addComponent(verticalLayout);
    }

    private void configureForm() {
        //form
        binder.setItemDataSource(new SecUser());
        secRoleContainer = new BeanItemContainer<>(SecRole.class);
        formLayout = new VerticalLayout();
        formLayout.setSpacing(true);

        HorizontalLayout nameLayout = new HorizontalLayout();
        usernameTextField = new TextField("User");
        formLayout.addComponent(usernameTextField);

        firstNameTextField = new TextField("First name");
        nameLayout.addComponent(firstNameTextField);

        lastNameTextField = new TextField("Last name");
        nameLayout.addComponent(lastNameTextField);
        formLayout.addComponent(nameLayout);

        HorizontalLayout mailAndPhoneLayout = new HorizontalLayout();
        userEmailTextField = new TextField("Email");
        mailAndPhoneLayout.addComponent(userEmailTextField);

        phoneNumberTextField = new TextField("Phone number");
        mailAndPhoneLayout.addComponent(phoneNumberTextField);
        mailAndPhoneLayout.setExpandRatio(userEmailTextField, 1.0f);

        formLayout.addComponent(mailAndPhoneLayout);

        //roles grid
        VerticalLayout rolesLayout = new VerticalLayout();
        HorizontalLayout addRoleLayout = new HorizontalLayout();
        addRoleLayout.setWidth("100%");
        addRoleLayout.setSpacing(true);

        rolesLayout.addComponent(addRoleLayout);

        rolesGrid = new Grid(secRoleContainer);
        rolesGrid.setCaption("Roles granted");
        rolesGrid.setResponsive(true);
        rolesGrid.setColumns("description");
        rolesGrid.setSelectionMode(Grid.SelectionMode.MULTI);

        rolesLayout.addComponent(rolesGrid);
        rolesLayout.setExpandRatio(rolesGrid, 1.0f);

        formLayout.addComponent(rolesLayout);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        Button save = new Button("Create", event -> save(event));
        save.setIcon(FontAwesome.SAVE);
        save.addStyleName(ValoTheme.BUTTON_PRIMARY);
        buttonsLayout.addComponent(save);

        formLayout.addComponent(buttonsLayout);

        formLayout.setSpacing(true);
        nameLayout.setSpacing(true);
        mailAndPhoneLayout.setSpacing(true);
        buttonsLayout.setSpacing(true);
        rolesLayout.setSpacing(true);
        binder.bindMemberFields(this);
        refreshRoles();
    }

    private void save(Button.ClickEvent event) {
        try {
            binder.commit();
            SecUser secUser = binder.getItemDataSource().getBean();
            secUser = securityService.createSecUser(secUser);
            final List<SecRole> rolesToGrant = new ArrayList<>();
            rolesGrid.getSelectedRows().forEach( role -> rolesToGrant.add((SecRole)role));
            for(SecRole secRole: rolesToGrant){
                securityService.grantRole(secUser, secRole);
            }
            UI.getCurrent().getNavigator().navigateTo("users");
            Notification.show("Created");
        } catch (FieldGroup.CommitException ex) {
            Notification.show("Check fields " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        } catch (Exception ex) {
            Notification.show("Unexpected error: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }


    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        //NOP
    }

    private void refreshRoles() {
        secRoleContainer.removeAllItems();
        secRoleContainer.addAll(securityService.findAllSecRoles());
    }
}
