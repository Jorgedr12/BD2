import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.plaf.nimbus.State;

import com.mysql.cj.Query;

class Menu {
    int level;
    String type;
    String menu;
    String menu_text;
    String query;

    public Menu(int level, String type, String menu, String menu_text){
        this.level = level;
        this.type = type; // User or Admin
        this.menu = menu;
        this.menu_text = menu_text; // Display text
    }
}

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

        Connection cnx = getConnectionSQL(URL, db_user, db_user, db_password, prg_user, prg_pwd);
        if (cnx != null) {
            System.out.println("Bienvenido " + prg_user + " a la base de datos de Disney");
            mainCycle(cnx, prg_user);
        } else {
            System.out.println("Acceso denegado a " + prg_user + " a la base de datos de Disney");
        }
        try {
            cnx.close();
        } catch (Exception ex) {
            System.out.println("main: "+ex.getMessage());
        }
        
    }
    public static void mainCycle(Connection cnx, String user) {
        ArrayList<Menu> menuPrincipal = getOptionMenu(cnx, "user");
        String option = "0";
        do {
            option = showMenu(menuPrincipal);
        } while (option.equals("0") == false);
    }

    public static String showMenu(ArrayList<Menu> menuList) {
        int i = 1;
        for (Menu menu : menuList) {
            System.out.println(i + ":" + menu.menu_text);
            i++;
        }

        Scanner scan = new Scanner(System.in);
        System.out.println("Seleccione una opción:");
        String option = scan.nextLine();

        if (!option.equals("0")) {
            int idx = Integer.parseInt(option);
            option = menuList.get(idx).menu;
        }
        return option;

    }

    public static ArrayList<Menu> getOptionMenu(Connection cnx, String role) {
        ArrayList<Menu> menu_list = new ArrayList<Menu>();
        String query_user = "SELECT * FROM `menu_Jorge` WHERE level IN (10,20) AND menu_Jorge.user_role ='USER';";
        String query_admin= "SELECT * FROM menu_Jorge, user_Jorge WHERE user_Jorge.email = 'jorgedr@gmail.com'AND menu_Jorge.user_role = user_Jorge.role;";
        String query ="";
        if (role.equals("USER")) {
            query = query_user;
        } else {
            query = query_admin;
        }
        try {
            PreparedStatement ps = cnx.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()== true) {
                String type = rs.getString(0);
                int level =   rs.getInt(1);
                String menu = rs.getString(2);
                String menu_text = rs.getString(3);
                Menu menu_row = new Menu(level,type,menu,menu_text);
                menu_list.add(menu_row);
            }
            
        } catch (Exception ex) {
            System.out.println("getOptionMenu:"+ex.getMessage());
        }
        return menu_list;
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
