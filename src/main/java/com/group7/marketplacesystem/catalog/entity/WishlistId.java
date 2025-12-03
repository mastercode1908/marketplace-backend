package com.group7.marketplacesystem.catalog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class WishlistId implements Serializable {
    private static final long serialVersionUID = 2185542499867536521L;
    @Column(name = "buyer_id", nullable = false)
    private Integer buyerId;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        WishlistId entity = (WishlistId) o;
        return Objects.equals(this.productId, entity.productId) &&
                Objects.equals(this.buyerId, entity.buyerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, buyerId);
    }

}