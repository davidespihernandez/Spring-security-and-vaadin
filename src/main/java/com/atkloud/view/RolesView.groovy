package com.atkloud.view;

import com.atkloud.domain.SecRole;
import com.atkloud.domain.SecUser;
import com.atkloud.service.SecurityService;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@SpringView
public class RolesView extends VerticalLayout implements View {

    SecurityService securityService;
    Grid grid;

    @Autowired
    public RolesView(SecurityService securityService) {
        this.securityService = securityService;
        setMargin(true);
        setSpacing(true)
        setSizeFull();
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        this.grid = new Grid();
        grid.setSizeFull();
        grid.setCaption("List of roles");
        List<SecRole> allRoles = securityService.findAllSecRoles();
        grid.setContainerDataSource(new BeanItemContainer(SecRole.class, allRoles));
        grid.setResponsive(true);
        grid.setColumns("id", "authority", "description");
        grid.setEditorEnabled(true);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setEditorFieldGroup(new BeanFieldGroup<>(SecUser.class));
        grid.getEditorFieldGroup().addCommitHandler(new FieldGroup.CommitHandler() {
            @Override
            public void preCommit(FieldGroup.CommitEvent commitEvent) throws FieldGroup.CommitException {
                System.out.println("Pre edit!");
            }
            @Override
            public void postCommit(FieldGroup.CommitEvent commitEvent) throws FieldGroup.CommitException {
                // You can persist your data here
                SecRole editedRole = (SecRole)grid.getEditedItemId();
                securityService.updateSecRole(editedRole.getId(), editedRole.getAuthority(), editedRole.getDescription());
                Notification.show("Role " + editedRole.getAuthority() + " was edited.");
            }
        });
        Button button = new Button("New role",  (Button.ClickListener){ clickEvent ->
            println "on click new"
            UI.getCurrent().getNavigator().navigateTo("roleForm")
        });
        button.setIcon(FontAwesome.PLUS);
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.addComponent(grid);
        verticalLayout.setSizeFull()
        verticalLayout.setSpacing(true)
        verticalLayout.addComponent(button);
        verticalLayout.setExpandRatio(grid, 1.0f)
        addComponent(verticalLayout);
    }
}
