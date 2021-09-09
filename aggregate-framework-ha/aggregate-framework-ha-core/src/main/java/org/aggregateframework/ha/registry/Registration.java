package org.aggregateframework.ha.registry;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.aggregateframework.ha.registry.jdbc.JDBCRegistration;
import org.aggregateframework.ha.registry.jedis.JedisClusterRegistration;
import org.aggregateframework.ha.registry.jedis.JedisRegistration;
import org.aggregateframework.ha.registry.jedis.JedisShardedRegistration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 注册信息
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = JedisClusterRegistration.class, name = "jedis-cluster"),
        @JsonSubTypes.Type(value = JDBCRegistration.class, name = "jdbc"),
        @JsonSubTypes.Type(value = JedisRegistration.class, name = "jedis"),
        @JsonSubTypes.Type(value = JedisShardedRegistration.class, name = "jedis-sharded")
})
public abstract class Registration {


    /**
     * 应用名
     */
    private String application;

    /**
     * 维护者
     */
    private List<Owner> owners;
    /**
     * 元数据
     */
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * 领域
     */
    private String domain;


    public List<Owner> getOwners() {
        return owners;
    }

    public void setOwners(List<Owner> owners) {
        this.owners = owners;
    }
    
    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }


    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

}
