package cat.iesesteveterradas;

import java.io.IOException;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.basex.api.client.ClientSession;
import org.basex.core.*;
import org.basex.core.cmd.*;


import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Insertar {
    private static final Logger logger = LoggerFactory.getLogger(Insertar.class);

    public static void main(String[] args) {
        // Connectar-se a MongoDB (substitueix amb la teva URI de connexió)
        try (var mongoClient = MongoClients.create("mongodb://root:example@localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase("yourDatabaseName");
            MongoCollection<Document> collection = database.getCollection("yourCollectionName");

            // Crear un nou document
            Document question = new Document("title", "How to use MongoDB with Java?")
                    .append("description", "I'm looking for examples on how to connect to MongoDB using Java.")
                    .append("tags", java.util.Arrays.asList("Java", "MongoDB", "Driver"))
                    .append("createdAt", new java.util.Date());

            // Inserir el document a la col·lecció
            collection.insertOne(question);

            System.out.println("Document inserted successfully");
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

}
