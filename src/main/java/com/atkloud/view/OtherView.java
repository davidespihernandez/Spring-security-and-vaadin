package com.atkloud.view;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

@SpringView(name = "other") // Root view
public class OtherView extends VerticalLayout implements View {

    public OtherView() {
        setMargin(true);
        addComponent(new Label("Create your content here..."));
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        // NOP
    }
}
