import spark.ModelAndView;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.io.File;
import java.io.FileReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;


public class People {

    /////////////// START START START FACTORY METHODS     FACTORY METHODS    ///////////////////////////////

    public static void createTable(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("DROP TABLE IF EXISTS people");
        stmt.execute("CREATE TABLE IF NOT EXISTS people (id IDENTITY, first_name VARCHAR, last_name VARCHAR, country VARCHAR, email VARCHAR, ip VARCHAR)");
    }

    public static void insertPerson(Connection conn, String firstName, String lastName, String email, String country, String ip) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO people VALUES (NULL, ?, ?, ?, ?, ?)");
        stmt.setString(1, firstName);
        stmt.setString(2, lastName);
        stmt.setString(3, email);
        stmt.setString(4, country);
        stmt.setString(5, ip);
        stmt.execute();

    }

    public static Person selectPerson(Connection conn, int id) throws SQLException {
        Person person = null;
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM people WHERE id = ?");
        stmt.setInt(1, id);
        ResultSet results = stmt.executeQuery();
        if (results.next()) {
            person = new Person();
            person.id = results.getInt("id");
            person.firstName = results.getString("first_name");
            person.lastName = results.getString("last_name");
            person.email = results.getString("email");
            person.country = results.getString("country");
            person.ip = results.getString("ip");
        }
        return person;
    }

    public static void populateDatabase (Connection conn) throws SQLException {
        String fileContent = readFile("people.csv");
        String[] lines = fileContent.split("\n");

        for (String line : lines) {
            if (line == lines[0])
                continue;
            String[] columns = line.split(",");
            Person person = new Person(Integer.valueOf(columns[0]), columns[1], columns[2], columns[3], columns[4], columns[5]);
            insertPerson(conn, person.firstName, person.lastName, person.email, person.country, person.ip);
        }
    }

    public static ArrayList<Person> selectAllPeople (Connection conn, int limit, int offset) throws SQLException {
        ArrayList<Person> people = new ArrayList<>();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM people LIMIT ? OFFSET ?", offset);
        stmt.setInt(1, limit);
        stmt.setInt(2, offset);
        ResultSet results = stmt.executeQuery();
        while (results.next()) {
            Person person = new Person();
            person.id = results.getInt("id");
            person.firstName = results.getString("first_name");
            person.lastName = results.getString("last_name");
            person.country = results.getString("country");
            person.email = results.getString("email");
            person.email = results.getString("ip");

            people.add(person);
        }
        return people;
    }

    ////////////// END END END END END END END END END END END END  ////////////////////////////////////////////////////
    public static void main(String[] args) throws SQLException {
        // Create connection and Database "people"
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        createTable(conn);
        populateDatabase(conn);



        // START HTTP Routes  START
        Spark.get(
                "/",
                ((request, response) -> {
                    String count = request.queryParams("count");
                    int countNum;
                    boolean previousShow = true;
                    boolean nextShow = true;

                    // Setting up current count
                    if(count == null) {
                        countNum = 0;
                    } else {
                        countNum = Integer.valueOf(count);
                    }

                    // next page button setup / logic
                    int nextNum = countNum + 20;
                    int previousNum = countNum - 20;

                    HashMap map = new HashMap();
                    map.put("list", selectAllPeople(conn, 20, countNum));
                    map.put("count", countNum);
                    map.put("previous", previousNum);
                    map.put("next", nextNum);
                    map.put("previousShow", previousShow);
                    map.put("nextShow", nextShow);

                    return new ModelAndView(map, "people.html");
                }),

                new MustacheTemplateEngine()
        );

        // Individual Person Details
        Spark.get (
                "/person",
                ((request, response) -> {

                    String id = request.queryParams("id");
                    int idNum = Integer.valueOf(id);

                    Person person = selectPerson(conn, idNum);

                    HashMap m = new HashMap<>();
                    m.put("person", person);

                    return new ModelAndView(m, "person.html");
                }),
                new MustacheTemplateEngine()
        );

        // START - SEARCH BOX FEATURE - START
        Spark.get(
                "/results",
                ((request, response) -> {
                    String search = request.queryParams("search").toLowerCase();

                    ArrayList<Person> results = new ArrayList();

                    for (Person p : selectAllPeople(conn, 1000, 0)) {
                        if (p.firstName.toLowerCase().contains(search) || p.lastName.toLowerCase().contains(search)) {
                            results.add(p);
                        }
                    }
                    HashMap m = new HashMap();
                    m.put("results", results);
                    return new ModelAndView(m, "results.html");
                }),
                new MustacheTemplateEngine()

        );

    }
/////START FILE READ //////////////////////////////////////////////////////////////////
    static String readFile(String fileName) {
        File f = new File(fileName);
        try {
            FileReader fr = new FileReader(f);
            int fileSize = (int) f.length();
            char[] fileContent = new char[fileSize];
            fr.read(fileContent);
            return new String(fileContent);
        } catch (Exception e) {
            return null;
        }
    }
/////////END FILE READ /////////////////////////////////////////////////////////////
}
