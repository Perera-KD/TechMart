package org.techmart.lk.ejb.bean;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.PostActivate;
import jakarta.ejb.PrePassivate;
import jakarta.ejb.Remove;
import jakarta.ejb.Stateful;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.techmart.lk.ejb.remote.AdminSessionState;

@Stateful
public class AdminSessionStateBean implements AdminSessionState, Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private boolean loggedIn = false;
    private List<String> sessionAuditLog;

    @PostConstruct
    public void init() {
        System.out.println("[AdminSessionStateBean] Created for new client session!");
        sessionAuditLog = new ArrayList<>();
        sessionAuditLog.add("Session initialized");
    }

    @Override
    public void login(String username) {
        this.username = username;
        this.loggedIn = true;
        sessionAuditLog.add("Logged in as " + username);
    }

    @Override
    @Remove
    public void logout() {
        System.out.println("[AdminSessionStateBean] Logging out and removing stateful EJB instance.");
        this.username = null;
        this.loggedIn = false;
        this.sessionAuditLog.clear();
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isLoggedIn() {
        return loggedIn;
    }

    @Override
    public void addAuditAction(String action) {
        sessionAuditLog.add(action);
    }

    @Override
    public List<String> getAuditActions() {
        return new ArrayList<>(sessionAuditLog);
    }

    @PrePassivate
    public void passivate() {
        System.out.println("[AdminSessionStateBean] Passivating stateful session bean...");
    }

    @PostActivate
    public void activate() {
        System.out.println("[AdminSessionStateBean] Activating stateful session bean...");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("[AdminSessionStateBean] Destroying stateful session bean instance.");
    }
}
