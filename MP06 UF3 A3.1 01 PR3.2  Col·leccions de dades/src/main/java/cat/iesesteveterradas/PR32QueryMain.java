package cat.iesesteveterradas;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.FindIterable;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.bson.Document;
import org.bson.conversions.Bson;
import com.mongodb.client.AggregateIterable;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class PR32QueryMain {
    public static void main(String[] args) {
        try (var mongoClient = MongoClients.create("mongodb://root:example@localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase("yourDatabaseName");
            MongoCollection<Document> collection = database.getCollection("yourCollectionName");

            // Calcular el promedio de ViewCount
            double averageViewCount = getAverageViewCount(collection);

            // Consultar preguntas con visitas mayor que la media
            List<Document> questionsAboveAverage = getQuestionsAboveAverage(collection, averageViewCount);
            generatePDF("data/out/informe1.pdf", questionsAboveAverage);

            // Consultar preguntas con letras específicas en el título
            List<String> letters = Arrays.asList("pug", "wig", "yak", "nap", "jig", "mug", "zap", "gag", "oaf", "elf");
            List<Document> questionsWithTitleLetters = getQuestionsWithTitleLetters(collection, letters);
            generatePDF("data/out/informe2.pdf", questionsWithTitleLetters);

        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    // Método para calcular el promedio de ViewCount
    private static double getAverageViewCount(MongoCollection<Document> collection) {
        List<Bson> pipeline = new ArrayList<>();
        pipeline.add(new Document("$group", new Document("_id", null).append("avgViewCount", new Document("$avg", "$ViewCount"))));
        AggregateIterable<Document> averageResult = collection.aggregate(pipeline);
        double averageViewCount = 0;
        for (Document doc : averageResult) {
            averageViewCount = doc.getDouble("avgViewCount");
        }
        return averageViewCount;
    }

    // Método para obtener preguntas con un número de visitas mayor que la media
    private static List<Document> getQuestionsAboveAverage(MongoCollection<Document> collection, double averageViewCount) {
        Bson greaterThanAverageQuery = new Document("ViewCount", new Document("$gt", averageViewCount));
        FindIterable<Document> result = collection.find(greaterThanAverageQuery);
        List<Document> questionsAboveAverage = new ArrayList<>();
        for (Document doc : result) {
            questionsAboveAverage.add(doc);
        }
        return questionsAboveAverage;
    }

    // Método para obtener preguntas cuyo título contiene al menos una de las letras proporcionadas
    private static List<Document> getQuestionsWithTitleLetters(MongoCollection<Document> collection, List<String> letters) {
        StringBuilder regexBuilder = new StringBuilder();
        regexBuilder.append("(");
        for (String letter : letters) {
            regexBuilder.append(letter).append("|");
        }
        regexBuilder.deleteCharAt(regexBuilder.length() - 1); // Eliminar el último '|'
        regexBuilder.append(")");

        Pattern pattern = Pattern.compile(regexBuilder.toString());
        Bson titleRegexQuery = new Document("Title", new Document("$regex", pattern));
        FindIterable<Document> result = collection.find(titleRegexQuery);
        List<Document> questionsWithTitleLetters = new ArrayList<>();
        for (Document doc : result) {
            questionsWithTitleLetters.add(doc);
        }
        return questionsWithTitleLetters;
    }

    // Método para generar un archivo PDF con los títulos de las preguntas
    private static void generatePDF(String filePath, List<Document> questions) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = null;
            try {
                contentStream = new PDPageContentStream(document, page);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.setLeading(14.5f);
                contentStream.newLineAtOffset(25, 750);

                int lineCount = 0;
                for (Document question : questions) {
                    if (lineCount % 45 == 0 && lineCount != 0) {
                        contentStream.endText();
                        contentStream.close();

                        page = new PDPage();
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        contentStream.beginText();
                        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                        contentStream.setLeading(14.5f);
                        contentStream.newLineAtOffset(25, 750);
                    }

                    String line = "Title " + (lineCount + 1) + ": " + question.getString("Title");
                    contentStream.showText(line);
                    contentStream.newLine();
                    lineCount++;
                }
                contentStream.endText();
            } finally {
                if (contentStream != null) {
                    contentStream.close();
                }
            }

            document.save(new File(filePath));
        }
    }
}
