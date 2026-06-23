package org.techmart.lk.ejb.remote;

import jakarta.ejb.Remote;
import java.util.List;
import org.techmart.lk.core.entity.AuditLog;

@Remote
public interface AuditService {
    List<AuditLog> getLatestLogs(int maxResults);
}
