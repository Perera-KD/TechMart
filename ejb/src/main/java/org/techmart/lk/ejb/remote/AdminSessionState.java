package org.techmart.lk.ejb.remote;

import jakarta.ejb.Remote;
import java.util.List;

@Remote
public interface AdminSessionState {
    void login(String username);
    void logout();
    String getUsername();
    boolean isLoggedIn();
    void addAuditAction(String action);
    List<String> getAuditActions();
}
