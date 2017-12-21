package org.aggregateframework.sample.complexmodel.command.domain.entity;

import org.aggregateframework.entity.CompositeId;

/**
 * Created by changmingxie on 11/30/15.
 */
public class UserShardingId implements CompositeId {

    private static final long serialVersionUID = -4926393025658856070L;
    private Integer id;

    private Integer userId;

    public UserShardingId() {

    }

    public UserShardingId(Integer id, Integer userId) {
        this.id = id;
        this.userId = userId;
    }

    public UserShardingId(int userId) {
        this.userId = userId;
    }

    @Override
    public boolean isNewId() {
        return !(this.id != null && this.userId != null && this.id > 0 && this.userId > 0);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
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

        boolean isIdEquals = false;

        boolean isUserIdEquals = false;

        if ((this.id == null && that.id == null) || (this.id != null && this.id.equals(that.id))) {
            isIdEquals = true;
        }

        if ((this.userId == null && that.userId == null) || (this.userId != null && this.userId.equals(that.userId))) {
            isUserIdEquals = true;
        }

        return isIdEquals && isUserIdEquals;
    }


    @Override
    public int hashCode() {

        int hashCode = 17;

        hashCode += this.id == null ? 0 : this.id * 31;

        hashCode += this.userId == null ? 0 : this.userId * 31;

        return hashCode;
    }

}
