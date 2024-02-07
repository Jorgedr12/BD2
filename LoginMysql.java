import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginMysql {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        String prg_user = "jorgedr@gmail.com";
        String prg_pwd  = "12345";
        byte[] hashed = getSHA(prg_pwd);
        String hashed64 = hexString(hashed);

        System.out.println("Hashed: "+hashed);
        System.out.println("Hashed64: "+hashed64);

        String URL = "jdbc:mysql://148.225.60.126/disneyplus?useSSL=false&useTimezone=true&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String db_user = "disney";
        String db_password = "Ma58toAa!YLtT9S9";

        Connection cnx = getConnection(URL, db_user, db_user, db_password, prg_user, hashed64);
        if (cnx != null) {
            System.out.println("Bienvenido " + prg_user + " a la base de datos de Disney");
        } else {
            System.out.println("Acceso denegado a " + prg_user + " a la base de datos de Disney");
        }
        try {
            cnx.close();
        } catch (Exception ex) {
            System.out.println("main: "+ex.getMessage());
        }
        
    }

    public static Connection getConnectionSQL(String url, String user, String db_user, String db_password, String prg_user, String prg_password) {
        Connection cnx = null;
        try {
            cnx = DriverManager.getConnection(url, user, db_password);
            System.out.println("Succesful connection to the database");
            String SQL = "SELECT username FROM user_Jorge where email = ? and password = SHA2(?,256)";

            PreparedStatement psu = cnx.prepareStatement(SQL);
            psu.setString(1, prg_user);
            psu.setString(2, prg_password);
            ResultSet rsu = psu.executeQuery();
            if (!rsu.next()) {
                cnx.close();
                cnx = null;
            } else {

            }
        } catch (Exception ex) {
            System.out.println("getConnection: "+ex.getMessage());
        }
        return cnx;

    }

    public static Connection getConnection(String url, String user, String db_user, String db_password, String prg_user, String prg_password) {
        Connection cnx = null;
        try {
            cnx = DriverManager.getConnection(url, user, db_password);
            System.out.println("Succesful connection to the database");
            String SQL = "SELECT username FROM user_Jorge where email = ? and password = ?";

            PreparedStatement psu = cnx.prepareStatement(SQL);
            psu.setString(1, prg_user);
            psu.setString(2, prg_password);
            ResultSet rsu = psu.executeQuery();
            if (!rsu.next()) {
                cnx.close();
                cnx = null;
            } else {

            }
        } catch (Exception ex) {
            System.out.println("getConnection: "+ex.getMessage());
        }
        return cnx;

    }
    public static byte[] getSHA(String input) throws NoSuchAlgorithmException {
        byte[] hash = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            System.out.println("getSHA: "+ex.getMessage());
        }
        return hash;
    }
    public static String hexString(byte[] hash) {
        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString(16));
        while (hexString.length()< 32){
            hexString.insert(0,'0');
        }
        return hexString.toString();
    }
}
