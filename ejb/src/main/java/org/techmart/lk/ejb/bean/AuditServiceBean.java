package org.techmart.lk.ejb.bean;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.techmart.lk.core.entity.AuditLog;
import org.techmart.lk.ejb.remote.AuditService;

@jakarta.interceptor.Interceptors(org.techmart.lk.ejb.interceptor.PerformanceInterceptor.class)
@Stateless
public class AuditServiceBean implements AuditService {

    @PersistenceContext(unitName = "TechMartPU")
    private EntityManager em;

    @Override
    public List<AuditLog> getLatestLogs(int maxResults) {
        return em.createQuery("SELECT a FROM AuditLog a ORDER BY a.timestamp DESC", AuditLog.class)
                 .setMaxResults(maxResults)
                 .getResultList();
    }
}
