package com.atkloud.view;

import com.atkloud.domain.SecUser;
import com.atkloud.domain.SecUser;
import com.atkloud.service.SecurityService;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.SelectionEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import com.vaadin.ui.Notification;
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
    private Combobox roleCombobox;
    private Grid grantedRolesGrid;

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
        Button newUser = new Button("New user", clickEvent -> UI.getCurrent().getNavigator().navigateTo("userForm") );
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
        formLayout = new HorizontalLayout();
        formLayout.setSpacing(true);
        formLayout.setVisible(false);
        VerticalLayout fieldsLayout = new VerticalLayout();

        HorizontalLayout nameLayout = new HorizontalLayout();
        usernameTextField = new TextField("User");
        nameLayout.addComponent(usernameTextField);

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

        roleCombobox = new ComboBox("Roles", new BeanItemContainer<>(SecRole.class, securityService.findAllSecRoles()));
        roleCombobox.setItemCaptionPropertyId("description");
        roleCombobox.setNullSelectionAllowed(true);
        rolesLayout.addComponent(roleCombobox)

		grantedRolesGrid = new Grid();
		grantedRolesGrid.setSizeFull();
        grantedRolesGrid.setResponsive(true);
        grantedRolesGrid.setColumns("description");
        grantedRolesGrid.setEditorEnabled(true);
        grantedRolesGrid.setSelectionMode(Grid.SelectionMode.MULTIPLE);


		rolesLayout.addComponent(grantedRolesGrid);
        rolesLayout.setExpandRatio(grantedRolesGrid, 1.0f);

        formLayout.addComponent(rolesLayout);

        formLayout.setComponentAlignment(save, Alignment.BOTTOM_LEFT);
        formLayout.setComponentAlignment(deleteRole, Alignment.BOTTOM_RIGHT);

        binder.bindMemberFields(this);

    }

    private void select(SelectionEvent event) {
        if (event.getSelected().isEmpty()) {
            formLayout.setVisible(false);
        } else {
            formLayout.setVisible(true);
            binder.setItemDataSource((SecUser) event.getSelected().iterator().next());
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
            Notification.show("Role deleted!");
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
}
