package mongo.base;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

// object
// using collection
@Document(collection=Config.CUSTOMER_COLLECTION)
public class Customer {

    // MongoDB _id
    @Id
    private String id;
    private String firstName;
    private String lastName;
    
    public Customer() {}
    
    public Customer(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
    
    public Customer(Builder builder) {
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
    }

    public String getFirstName() {
        return firstName;
    }

   
    public String getLastName() {
        return lastName;
    }
    
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String toString(){
        return id + "::" + firstName + "::" + lastName;
    }

    public static class Builder {

        private String firstName;
        private String lastName;
        
        public Builder() {}

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }
        
        public Customer build() {
            return new Customer(this);
        }
        
    }

    
    
}
