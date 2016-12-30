package mongo.base;

import java.net.UnknownHostException;
import java.util.List;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.mongodb.MongoClient;

public class Main {

    
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        try {
            MongoClient mongo = new MongoClient(Config.MONGO_HOST, Config.MONGO_PORT);
            MongoOperations mongoOps = new MongoTemplate(mongo, Config.DB_NAME);
            Customer customer = new Customer.Builder().firstName("Jiang").lastName("Zhao").build();
            mongoOps.insert(customer);
            customer = new Customer.Builder().firstName("Cheng").lastName("SC").build();
            mongoOps.insert(customer);
            customer = mongoOps.findOne(new Query(Criteria.where("firstName").is("Jiang")), Customer.class);
            System.out.println(customer);
            List<Customer> customers = mongoOps.find(new Query(Criteria.where("firstName").is("Jiang")), Customer.class);
            System.out.println("Have " + customers.size() + " customers.");
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
