package org.aggregateframework.test.command.domain.entity;

import java.io.Serializable;

/**
 * Created by changmingxie on 11/27/15.
 */
public class CompositeId implements Serializable {

    private int id;

    private int userId;


    @Override
    public boolean equals(Object obj) {

        if (null == obj) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        if (!getClass().equals(obj.getClass())) {
            return false;
        }

        CompositeId that = (CompositeId) obj;

        if (this.id == that.id && this.userId == that.userId) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {

        int hashCode = 17;

        hashCode += this.id * 31;

        hashCode += this.userId * 31;

        return hashCode;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
