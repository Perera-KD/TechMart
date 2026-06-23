package org.techmart.lk.ejb.bean;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.DependsOn;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.techmart.lk.core.entity.User;
import org.techmart.lk.core.entity.Warehouse;
import org.techmart.lk.core.entity.Product;

@Singleton
@Startup
@DependsOn("DatabaseConfig")
public class DatabaseSeederBean {

    @PersistenceContext(unitName = "TechMartPU")
    private EntityManager em;

    @PostConstruct
    public void init() {
        System.out.println("[DatabaseSeeder] Seeding database tables...");
        try {
            seedUsers();
            seedWarehouses();
            seedProducts();
            System.out.println("[DatabaseSeeder] Seeding completed successfully!");
        } catch (Exception e) {
            System.err.println("[DatabaseSeeder] Failed to seed database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void seedUsers() {
        Long count = em.createQuery("SELECT COUNT(u) FROM User u", Long.class).getSingleResult();
        if (count == 0) {
            System.out.println("[DatabaseSeeder] Inserting default admin account...");
            User admin = new User("admin", "admin123", "ADMIN");
            em.persist(admin);
        }
    }

    private void seedWarehouses() {
        Long count = em.createQuery("SELECT COUNT(w) FROM Warehouse w", Long.class).getSingleResult();
        if (count == 0) {
            System.out.println("[DatabaseSeeder] Inserting default warehouses...");
            em.persist(new Warehouse("Warehouse Colombo", "Colombo 03"));
            em.persist(new Warehouse("Warehouse Kandy", "Kandy Central"));
            em.persist(new Warehouse("Warehouse Galle", "Galle Fort"));
        }
    }

    private void seedProducts() {
        Long count = em.createQuery("SELECT COUNT(p) FROM Product p", Long.class).getSingleResult();
        if (count == 0) {
            System.out.println("[DatabaseSeeder] Inserting default product inventory...");
            // Query warehouses to assign warehouse IDs
            Warehouse w1 = em.createQuery("SELECT w FROM Warehouse w WHERE w.name = 'Warehouse Colombo'", Warehouse.class).getSingleResult();
            Warehouse w2 = em.createQuery("SELECT w FROM Warehouse w WHERE w.name = 'Warehouse Kandy'", Warehouse.class).getSingleResult();
            Warehouse w3 = em.createQuery("SELECT w FROM Warehouse w WHERE w.name = 'Warehouse Galle'", Warehouse.class).getSingleResult();

            em.persist(new Product("EliteBook Laptop", "High-performance business laptop", 150000.0, 50, w1.getId()));
            em.persist(new Product("Galaxy Pro Smartphone", "Next-gen flagship smartphone", 85000.0, 120, w2.getId()));
            em.persist(new Product("Noise Cancelling Headphones", "Premium over-ear headphones", 15000.0, 300, w1.getId()));
            em.persist(new Product("SmartFit Watch", "Fitness tracker and smart watch", 25000.0, 80, w3.getId()));
        }
    }
}
