package com.vladmihalcea.hibernate.masterclass.laboratory.concurrency;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.OptimisticLockType;
import org.hibernate.annotations.OptimisticLocking;
import org.hibernate.annotations.SelectBeforeUpdate;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;

/**
 * OptimisticLockingVersionlessTest - Test to check optimistic checking using the dirty properties instead of a synthetic version column
 *
 * @author Vlad Mihalcea
 */
public class OptimisticLockingVersionlessTest extends AbstractTest {

    private Product product;

    @Before
    public void init() {
        super.init();
        product = doInTransaction(new TransactionCallable<Product>() {
            @Override
            public Product execute(Session session) {
                session.createQuery("delete from Product").executeUpdate();
                Product product = new Product();
                product.setId(1L);
                product.setName("TV");
                product.setDescription("Plasma TV");
                product.setPrice(BigDecimal.valueOf(199.99));
                product.setQuantity(7L);
                session.persist(product);
                return product;
            }
        });
    }

    @Test
    public void testVersionlessOptimisticLockingWhenMerging() {

        doInTransaction(new TransactionCallable<Object>() {
            @Override
            public Object execute(Session session) {
                Product _product = (Product) session.get(Product.class, 1L);
                _product.setPrice(BigDecimal.valueOf(21.22));
                LOGGER.info("Updating product price to {}", _product.getPrice());
                return null;
            }
        });

        product.setPrice(BigDecimal.ONE);
        doInTransaction(new TransactionCallable<Object>() {
            @Override
            public Object execute(Session session) {
                LOGGER.info("Merging product, price to be saved is {}", product.getPrice());
                session.merge(product);
                session.flush();
                return null;
            }
        });
    }

    @Test
    public void testVersionlessOptimisticLockingWhenReattaching() {

        doInTransaction(new TransactionCallable<Object>() {
            @Override
            public Object execute(Session session) {
                Product _product = (Product) session.get(Product.class, 1L);
                _product.setPrice(BigDecimal.valueOf(21.22));
                LOGGER.info("Updating product price to {}", _product.getPrice());
                return null;
            }
        });

        product.setPrice(BigDecimal.TEN);
        doInTransaction(new TransactionCallable<Object>() {
            @Override
            public Object execute(Session session) {
                LOGGER.info("Reattaching product, price to be saved is {}", product.getPrice());
                session.saveOrUpdate(product);
                session.flush();
                return null;
            }
        });
    }

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
                Product.class
        };
    }

    @Entity(name = "Product")
    @OptimisticLocking(type = OptimisticLockType.DIRTY)
    @DynamicUpdate
    @SelectBeforeUpdate(value = false)
    public static class Product {

        @Id
        private Long id;

        @Column(unique = true, nullable = false)
        private String name;

        @Column(nullable = false)
        private String description;

        @Column(nullable = false)
        private BigDecimal price;

        private long quantity;

        private int likes;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public long getQuantity() {
            return quantity;
        }

        public void setQuantity(long quantity) {
            this.quantity = quantity;
        }

        public int getLikes() {
            return likes;
        }

        public int incrementLikes() {
            return ++likes;
        }
    }
}
