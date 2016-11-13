package com.atkloud.view

import com.atkloud.service.SecurityService
import com.vaadin.data.fieldgroup.FieldGroup
import com.vaadin.data.fieldgroup.PropertyId
import com.vaadin.data.util.ObjectProperty
import com.vaadin.data.util.PropertysetItem
import com.vaadin.data.validator.NullValidator
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener
import com.vaadin.server.FontAwesome
import com.vaadin.spring.annotation.SpringView
import com.vaadin.ui.*
import org.springframework.beans.factory.annotation.Autowired

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
        setMargin(true);
        setSizeFull();
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        FormLayout content = new FormLayout();

        authorityField = new TextField("Authority");
        authorityField.setRequired(true);
        authorityField.addValidator(new NullValidator("Must be given", false));
        authorityField.setValidationVisible(true)
        content.addComponent(authorityField);

        descriptionField = new TextField("Description");
        descriptionField.setRequired(true);
        descriptionField.addValidator(new NullValidator("Must be given", false));
        descriptionField.setValidationVisible(true)
        content.addComponent(descriptionField);

        final PropertysetItem item = new PropertysetItem();
        item.addItemProperty("authority", new ObjectProperty<String>(""));
        item.addItemProperty("description",  new ObjectProperty<String>(""));
        FieldGroup binder = new FieldGroup(item);
        binder.bindMemberFields(this)

        // Trivial logic for closing the sub-window
        Button ok = new Button("Create");
        ok.setIcon(FontAwesome.SAVE)
        ok.addClickListener((Button.ClickListener) { clickEvent ->
            try {
                binder.commit();
                securityService.createRole(authorityField.getValue(), descriptionField.getValue())
                Notification.show("Saved!");
                getUI().getNavigator().navigateTo("roles")
            } catch (FieldGroup.CommitException e) {
                Notification.show("Check fields!");
            }
        });
        content.addComponent(ok);
        content.setImmediate(true)
        setCaption("New role")
        addComponent(new Label("Create new role"))
        addComponent(content)
        setExpandRatio(content, 1.0f)
    }

}
