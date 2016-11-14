package com.atkloud.view

import com.atkloud.domain.SecRole
import com.atkloud.service.SecurityService
import com.vaadin.data.fieldgroup.BeanFieldGroup
import com.vaadin.data.fieldgroup.FieldGroup
import com.vaadin.data.fieldgroup.PropertyId
import com.vaadin.event.ShortcutAction
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener
import com.vaadin.server.FontAwesome
import com.vaadin.spring.annotation.SpringView
import com.vaadin.ui.*
import com.vaadin.ui.themes.ValoTheme
import org.springframework.beans.factory.annotation.Autowired

import javax.annotation.PostConstruct

@SpringView(name = "roleForm")
class SecRoleFormView extends VerticalLayout implements View {
    SecurityService securityService

    @PropertyId("authority")
    TextField authorityField
    @PropertyId("description")
    TextField descriptionField

    @Autowired
    public SecRoleFormView(SecurityService securityService) {
        this.securityService = securityService;
    }

    @PostConstruct
    void init(){
        setMargin(true);
        setSizeFull();
        FormLayout content = new FormLayout();
//        content.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);

        authorityField = new TextField("Authority");
        content.addComponent(authorityField);

        descriptionField = new TextField("Description");
        content.addComponent(descriptionField);

        BeanFieldGroup binder = new BeanFieldGroup<>(SecRole.class);
        binder.setItemDataSource(new SecRole(authority: "", description: ""));
        binder.bindMemberFields(this)

        Button ok = new Button("Create");
        ok.setIcon(FontAwesome.SAVE)
        ok.addClickListener((Button.ClickListener) { clickEvent ->
            try {
                binder.commit();
                securityService.createRole(authorityField.getValue(), descriptionField.getValue())
                Notification.show("Saved!");
                getUI().getNavigator().navigateTo("roles")
            } catch (FieldGroup.CommitException ex) {
                Notification.show("Check fields", Notification.Type.ERROR_MESSAGE);
            } catch (Exception ex) {
                Notification.show("Unexpected error: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            }
        });
        ok.setClickShortcut(ShortcutAction.KeyCode.ENTER)
        content.addComponent(ok);
        Label titleLabel = new Label("Create new role")
        titleLabel.addStyleName(ValoTheme.LABEL_H2);
        titleLabel.addStyleName(ValoTheme.LABEL_COLORED);
        addComponent(titleLabel)
        addComponent(content)
        setExpandRatio(content, 1.0f)
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
    }

}
