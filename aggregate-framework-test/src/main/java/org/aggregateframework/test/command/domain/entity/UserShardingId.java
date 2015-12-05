package org.aggregateframework.test.command.domain.entity;

import org.aggregateframework.entity.CompositeId;

/**
 * Created by changmingxie on 11/30/15.
 */
public class UserShardingId implements CompositeId {

    private int id;

    private int userId;

    public UserShardingId() {

    }

    public UserShardingId(int userId) {
        this.userId = userId;
    }

    @Override
    public boolean isNewId() {
        return !(id > 0 && userId > 0);
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

        UserShardingId that = (UserShardingId) obj;

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
}
