package org.aggregateframework.ha.registry;


/**
 * Created by Lee on 2020/7/22 16:43.
 * aggregate-framework
 */
public class Owner {

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    private String name;
    private String email;
    
    
}
