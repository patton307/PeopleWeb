import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by landonkail on 11/4/15.
 */
public class PeopleTest {

    public static Connection startConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./test");
        People.createTable(conn);
        return conn;
    }

    public void endConnection(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("DROP TABLE people");
        conn.close();
    }

    @Test
    public void testPerson() throws SQLException {
        Connection conn = startConnection();
        People.insertPerson(conn, "Mike", "Smith", "mike@gmail.com", "America", "32456");
        Person person = People.selectPerson(conn, 1);
        endConnection(conn);

        assertTrue(person != null);
    }

    @Test
    public void testPeople() throws SQLException {
        Connection conn = startConnection();
        People.populateDatabase(conn);
        ArrayList<Person> people = People.selectAllPeople(conn, 1000, 0);
        endConnection(conn);

        assertTrue(people.size() > 0);
    }

}