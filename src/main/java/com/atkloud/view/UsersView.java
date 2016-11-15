package com.atkloud.view;

import com.atkloud.domain.SecRole;
import com.atkloud.domain.SecUser;
import com.atkloud.domain.SecUser;
import com.atkloud.service.SecurityService;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.ItemSorter;
import com.vaadin.data.util.PropertyValueGenerator;
import com.vaadin.event.SelectionEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import com.vaadin.ui.Notification;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.themes.ValoTheme;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.management.*;
import java.util.List;

@SpringView
public class UsersView extends VerticalLayout implements View {

    SecurityService securityService;
    private BeanItemContainer<SecUser> secUserContainer;
    private Grid grid;

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
    private HorizontalLayout formLayout;
    private ComboBox roleCombobox;
    private Grid grantedRolesGrid;
    private BeanItemContainer<SecRole> secRoleContainer;

    @Autowired
    public UsersView(SecurityService securityService) {
        this.securityService = securityService;
    }

    private void configureGrid(){
        secUserContainer = new BeanItemContainer<>(SecUser.class);
        this.grid = new Grid(secUserContainer);
        grid.setSizeFull();
        grid.setResponsive(true);
        grid.setColumns("username", "userEmail", "firstName", "lastName", "phoneNumber");
        grid.setEditorEnabled(true);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addSelectionListener(this::select);
        grid.setEditorFieldGroup(new BeanFieldGroup<>(SecUser.class));
        grid.getEditorFieldGroup().addCommitHandler(new FieldGroup.CommitHandler() {
            @Override
            public void preCommit(FieldGroup.CommitEvent commitEvent) throws FieldGroup.CommitException {
                System.out.println("Pre edit!");
            }
            @Override
            public void postCommit(FieldGroup.CommitEvent commitEvent) throws FieldGroup.CommitException {
                // You can persist your data here
                SecUser editedUser = (SecUser)grid.getEditedItemId();
                securityService.updateSecUser(editedUser);
                Notification.show("User " + editedUser.getFullName() + " saved.");
            }
        });
    }

    @PostConstruct
    void init(){
        setMargin(true);
        setSpacing(true);
        setSizeFull();
        configureGrid();
        configureFormLayout();
        Label captionLabel = new Label("List of users");
        captionLabel.setStyleName(ValoTheme.LABEL_H2);
        captionLabel.setStyleName(ValoTheme.LABEL_COLORED);

        HorizontalLayout northLayout = new HorizontalLayout();
        northLayout.addComponent(captionLabel);
        northLayout.setWidth("100%");
        Button newUser = new Button("New user", clickEvent -> UI.getCurrent().getNavigator().navigateTo("secUserForm") );
        newUser.setIcon(FontAwesome.PLUS);
        newUser.addStyleName(ValoTheme.BUTTON_PRIMARY);
        northLayout.addComponent(newUser);
        northLayout.setComponentAlignment(newUser, Alignment.MIDDLE_RIGHT);
        northLayout.setComponentAlignment(captionLabel, Alignment.BOTTOM_LEFT);

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setSpacing(true);

        verticalLayout.addComponent(northLayout);
        verticalLayout.addComponent(grid);
        verticalLayout.addComponent(formLayout);

        verticalLayout.setExpandRatio(grid, 1.0f);
        addComponent(verticalLayout);

        refresh();
    }

    private void configureFormLayout() {
        //form
        secRoleContainer = new BeanItemContainer<>(SecRole.class);
        formLayout = new HorizontalLayout();
        formLayout.setSpacing(true);
        formLayout.setVisible(false);
        VerticalLayout fieldsLayout = new VerticalLayout();

        HorizontalLayout nameLayout = new HorizontalLayout();
        usernameTextField = new TextField("User");
        fieldsLayout.addComponent(usernameTextField);

        firstNameTextField = new TextField("First name");
        nameLayout.addComponent(firstNameTextField);

        lastNameTextField = new TextField("Last name");
        nameLayout.addComponent(lastNameTextField);
        fieldsLayout.addComponent(nameLayout);

        HorizontalLayout mailAndPhoneLayout = new HorizontalLayout();
        userEmailTextField = new TextField("Email");
        mailAndPhoneLayout.addComponent(userEmailTextField);

        phoneNumberTextField = new TextField("Phone number");
        mailAndPhoneLayout.addComponent(phoneNumberTextField);
        mailAndPhoneLayout.setExpandRatio(userEmailTextField, 1.0f);

        fieldsLayout.addComponent(mailAndPhoneLayout);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        Button save = new Button("Save", event -> save(event));
        save.setIcon(FontAwesome.SAVE);
        save.addStyleName(ValoTheme.BUTTON_PRIMARY);
        buttonsLayout.addComponent(save);
        Button deleteRole = new Button("Delete", this::delete );
        deleteRole.setIcon(FontAwesome.TIMES);
        deleteRole.addStyleName(ValoTheme.BUTTON_DANGER);
        buttonsLayout.addComponent(deleteRole);

        fieldsLayout.addComponent(buttonsLayout);

        formLayout.addComponent(fieldsLayout);

        //roles grid
        VerticalLayout rolesLayout = new VerticalLayout();
        HorizontalLayout addRoleLayout = new HorizontalLayout();
        addRoleLayout.setWidth("100%");
        addRoleLayout.setSpacing(true);

        roleCombobox = new ComboBox("Roles", new BeanItemContainer<>(SecRole.class, securityService.findAllSecRoles()));
        roleCombobox.setItemCaptionPropertyId("description");
        roleCombobox.setNullSelectionAllowed(true);
        addRoleLayout.addComponent(roleCombobox);

        Button addRoleButton = new Button("Grant role", this::grantRole);
        addRoleButton.setIcon(FontAwesome.PLUS);
        addRoleLayout.addComponent(addRoleButton);
        addRoleLayout.setComponentAlignment(addRoleButton, Alignment.BOTTOM_LEFT);

        Button revokeSelectedButton = new Button("Revoke selected", this::revokeRoles);
        revokeSelectedButton.setIcon(FontAwesome.TIMES);
        revokeSelectedButton.addStyleName(ValoTheme.BUTTON_DANGER);
        addRoleLayout.addComponent(revokeSelectedButton);
        addRoleLayout.setComponentAlignment(revokeSelectedButton, Alignment.BOTTOM_LEFT);

        rolesLayout.addComponent(addRoleLayout);

        rolesLayout.addComponent(addRoleLayout);

		grantedRolesGrid = new Grid(secRoleContainer);
        grantedRolesGrid.setCaption("Roles granted");
        grantedRolesGrid.setSizeFull();
        grantedRolesGrid.setResponsive(true);
        grantedRolesGrid.setColumns("description");
        grantedRolesGrid.setSelectionMode(Grid.SelectionMode.MULTI);

		rolesLayout.addComponent(grantedRolesGrid);
        rolesLayout.setExpandRatio(grantedRolesGrid, 1.0f);

        formLayout.addComponent(rolesLayout);

        fieldsLayout.setSpacing(true);
        formLayout.setSpacing(true);
        nameLayout.setSpacing(true);
        mailAndPhoneLayout.setSpacing(true);
        buttonsLayout.setSpacing(true);
        rolesLayout.setSpacing(true);
        binder.bindMemberFields(this);
        refreshGrantedRoles();
    }

    private void grantRole(Button.ClickEvent event) {
        SecUser secUser = binder.getItemDataSource().getBean();
        SecRole role = (SecRole)roleCombobox.getValue();
        if(role!=null){
            securityService.grantRole(secUser, role);
            refreshGrantedRoles();
        }
    }

    private void revokeRoles(Button.ClickEvent event) {
        SecUser secUser = binder.getItemDataSource().getBean();
        if(secUser!=null){
            grantedRolesGrid.getSelectedRows().forEach( role -> securityService.revokeRole(secUser, (SecRole)role));
            refreshGrantedRoles();
        }
    }

    private void select(SelectionEvent event) {
        if (event.getSelected().isEmpty()) {
            formLayout.setVisible(false);
        } else {
            formLayout.setVisible(true);
            binder.setItemDataSource((SecUser) event.getSelected().iterator().next());
            refreshGrantedRoles();
        }
    }

    private void save(Button.ClickEvent event) {
        try {
            binder.commit();
            SecUser secUser = binder.getItemDataSource().getBean();
            securityService.updateSecUser(secUser);
            refresh();
        } catch (FieldGroup.CommitException ex) {
            Notification.show("Check fields", Notification.Type.ERROR_MESSAGE);
        } catch (Exception ex) {
            Notification.show("Unexpected error: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    private void delete(Button.ClickEvent event) {
        try {
            SecUser secUser = binder.getItemDataSource().getBean();
            securityService.deleteSecUser(secUser);
            Notification.show("User deleted!");
            refresh();
        } catch (Exception ex) {
            Notification.show("Unexpected error: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    private void refresh() {
        secUserContainer.removeAllItems();
        secUserContainer.addAll(securityService.findAllSecUsers());
        grid.select(null);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        //NOP
    }

    private void refreshGrantedRoles() {
        secRoleContainer.removeAllItems();
        SecUser secUser = (SecUser)grid.getSelectedRow();
        if(secUser!=null){
            secRoleContainer.addAll(securityService.findAllSecRoleBySecUser(secUser));
        }
    }

}
