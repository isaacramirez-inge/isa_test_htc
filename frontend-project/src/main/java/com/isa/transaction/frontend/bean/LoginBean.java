package com.isa.transaction.frontend.bean;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import java.io.IOException;
import java.util.ResourceBundle;
import jakarta.faces.context.ExternalContext;

@Named("loginBean")
@SessionScoped
public class LoginBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
    private boolean loggedIn = false;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public String login() {
        if ("admin".equals(username) && "admin".equals(password)) {
            loggedIn = true;
            return "login-success";
        } else {
            String message = getMessageFromBundle("login.failed");
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
            return "login-failure";
        }
    }

    public String logout() {
        loggedIn = false;
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        
        // Invalidate the session
        externalContext.invalidateSession();
        
        // Redirect to login page with a parameter to show logout message
        try {
            externalContext.redirect(externalContext.getRequestContextPath() + "/login.xhtml?faces-redirect=true&logout=true");
            context.responseComplete(); // Prevent JSF from performing a server-side forward
        } catch (IOException e) {
            // Log the error
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Error during logout", "An error occurred while trying to log out."));
        }
        return null; // Navigation is handled by the redirect
    }
    
    private String getMessageFromBundle(String key) {
        FacesContext context = FacesContext.getCurrentInstance();
        ResourceBundle bundle = context.getApplication()
            .getResourceBundle(context, "msgs");
        return bundle.getString(key);
    }
}
