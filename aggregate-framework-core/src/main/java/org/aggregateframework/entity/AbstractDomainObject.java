package org.aggregateframework.entity;

import java.io.Serializable;

/**
 * User: changming.xie
 * Date: 14-6-25
 * Time: 下午1:38
 */
public abstract class AbstractDomainObject<ID extends Serializable> implements DomainObject<ID> {


    private static final long serialVersionUID = -1026311526086575034L;

    private boolean isNew;

    @Override
    public boolean isNew() {
        return isNewId() || isNew;
    }

    protected void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    @Override
    public String toString() {

        return String.format("Entity of type %s with id: %s", this.getClass().getName(), getId());
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
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

        AbstractDomainObject<?> that = (AbstractDomainObject<?>) obj;

        return null == this.getId() ? false : this.getId().equals(that.getId());
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        int hashCode = 17;

        hashCode += null == getId() ? 0 : getId().hashCode() * 31;

        return hashCode;
    }

    private boolean isNewId() {

        ID id = getId();

        if (id == null) {
            return true;
        }

        if (id instanceof CompositeId) {
            return ((CompositeId) id).isNewId();
        }

        return false;
    }
}
