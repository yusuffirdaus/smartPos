/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ewallet.model;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Ade Saprudin
 */
public class PointReward implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String customerId;
    private Integer point;
    private Date createdDate;
    private Date expiredDate;

    public PointReward() {
    }

    public PointReward(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Integer getPoint() {
        return point;
    }

    public void setPoint(Integer point) {
        this.point = point;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(Date expiredDate) {
        this.expiredDate = expiredDate;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PointReward)) {
            return false;
        }
        PointReward other = (PointReward) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.ewallet.entity.PointReward[ id=" + id + " ]";
    }
    
}
