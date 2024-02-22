/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author dii
 */
class Menu {
    int level;
    String type;
    String menu;
    String menu_text;
    ArrayList<Menu> sub_menu;
    public Menu(int level, String type, String menu, String menu_text) {
        this.level = level;
        this.type = type; //USER or ADMIN
        this.menu = menu;
        this.menu_text = menu_text; //Display text    
        this.sub_menu = null;
    }
    public void addsubMenu(Menu parent, Menu child) {
        if (parent!=null) {
            if( parent.sub_menu!=null) {
                parent.sub_menu.add(child);
            } else {
                parent.sub_menu = new ArrayList<>();
                parent.sub_menu.add(child);
            }
        }
    }
}    

public class LoginMysql {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws NoSuchAlgorithmException {
        // TODO code application logic here
        String prg_user ;
        Scanner scan = new Scanner(System.in);
        System.out.print("Usuario:");
        prg_user = scan.nextLine();
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
        ArrayList<Menu> mainMenu = getOptionMenu(cnx, user);
        Menu parent = mainMenu.get(0);
        for (Menu menu : mainMenu) {    
            if (menu.level%10==0) {
                parent = menu;
            } else {
                parent.addsubMenu(parent, menu);
            }
        }
        String option = "0";

        do {
            showMenu(mainMenu);
            option = getMenu(mainMenu);
            System.out.println("Option:"+option);
            executeMenuOption(cnx, option,user);
            
        } while (!option.equals("0"));
        
    }
    public static void executeMenuOption(Connection cnx,String option, String user){
        String SQL;
        switch (option) {
            case "SHOW_RATING":
                SQL = "SELECT * FROM rating_Jorge";
                listRecords(cnx,SQL);
                break;
            case "ADD_RATING":
                addRating(cnx,user);
                break;
            case "LIST_SHOWS":
                SQL = "SELECT username,email,role FROM user_Jorge";
                listRecords(cnx,SQL);
                break;
            
            case "TOP_RATINGS":
                SQL = "SELECT * FROM `vista_jorge_ratings`";
                listRecords(cnx,SQL);
                break;
            
            case "TOPTEN_RATING":
                SQL = "SELECT * FROM `vista_jorge_ratings` LIMIT 0,10";
                listRecords(cnx,SQL);
                break;

            case "LIST_USERS":
                SQL = "SELECT username,email,role FROM user_Jorge";
                listRecords(cnx,SQL);
                break;
            case "SHOW_LOG":
                SQL = "SELECT * FROM zlog_jorge ORDER BY id DESC LIMIT 0,10";
                listRecords(cnx,SQL);
                break;
        }
    }
    public static void addRating(Connection cnx, String user){
        Scanner scan = new Scanner(System.in);
        System.out.print("Busca show o película:");
        String show = scan.nextLine();
        show = "%"+show+"%";
        String SQL = "SELECT show_id,title FROM shows WHERE title LIKE ? ORDER BY title";
        try {
            PreparedStatement ps = cnx.prepareStatement(SQL);
            ps.setString(1, show);
            SQL = ps.toString();
            SQL = SQL.substring(SQL.indexOf(":")+1);
            System.out.println("SQL: " + SQL);
            listRecords(cnx,SQL);  
            System.out.println("USER: "+user);
            SQL = "SELECT ID FROM users WHERE email = ?";
            ps = cnx.prepareStatement(SQL);
            ps.setString(1, user);  
            ResultSet rs = ps.executeQuery();
            int id = 0;
            if (rs.next()) {
                id = rs.getInt(1);
            }
            System.out.println("ID: "+id);
            System.out.println("Show a calificar");
            show = scan.nextLine();
            System.out.println("Calificacion (0 a 5):");
            int rating = scan.nextInt();
            System.out.println("Comentario:");
            String comentario = scan.nextLine();
            comentario = scan.nextLine();
            SQL = "INSERT INTO rating_Jorge (user_id,show,rating,comments) VALUES (?,?,?,?)";
            ps = cnx.prepareStatement(SQL);
            ps.setInt(1, id);
            ps.setString(2, show);
            ps.setInt(3, rating);
            ps.setString(4, comentario);
            System.err.println(ps.toString());
            int r = ps.executeUpdate(SQL);
            System.out.println("Inserted records:"+r);

        } catch (Exception ex) {
            System.out.println("addRating:"+ex.getMessage());
        }

    }

    public static void listRecords(Connection cnx, String SQL){
        try{
            PreparedStatement ps = cnx.prepareStatement(SQL);
            ResultSet rs = ps.executeQuery();
            displayRecords(rs);
        } catch (Exception ex) {
            System.out.println("listUsers:"+ex.getMessage());
        }
    }
    public static void displayRecords(ResultSet rs) throws SQLException {
        try{
            ResultSetMetaData rsmd = rs.getMetaData();
            int count = rsmd.getColumnCount();
            for (int i = 1; i <= count; i++) {
                System.out.print(rsmd.getColumnName(i) + " ");
            }
            System.out.println();
            while (rs.next()) {
                for (int i = 1; i <= count; i++) {
                    System.out.print(rs.getString(i) + " ");
                }
                System.out.println();
            }
            System.out.println();
        } catch (Exception ex) {
            System.out.println("displeyRecords:"+ex.getMessage());
        }
    }
    public static void showMenu(ArrayList<Menu> menuList){
        int i = 0;
        for (Menu menu : menuList) {
            if (menu.level%10==0) {
                System.out.println("**"+menu.menu_text+"**");
                //showMenu(menu.sub_menu);
            } else {
                System.out.println(i+":"+menu.menu_text);
            }
            i++;
        }
    }
    public static String getMenu(ArrayList<Menu> menuList){
        Scanner scan = new Scanner(System.in);
        System.out.println("0:Salir");
        System.out.print("Elige opción:");
        String option = scan.nextLine();
        if (!option.equals("0")) {
            int idx = Integer.parseInt(option);
            option = menuList.get(idx).menu;
        }
        return option;
    }
    public static ArrayList<Menu> getOptionMenu(Connection cnx, String user) {
        ArrayList<Menu> menu_list = new ArrayList<Menu>();
        String query_user = "SELECT * FROM menu_Jorge,user_Jorge WHERE  user_Jorge.email = ? AND menu_Jorge.user_role = user_Jorge.role";
        String query_admin= "SELECT * FROM menu_Jorge,user_Jorge WHERE  user_Jorge.email = ? AND menu_Jorge.user_role = user_Jorge.role";
        String query ="";
        query = query_user;
        
        try {
            PreparedStatement ps = cnx.prepareStatement(query);
            ps.setString(1,user);
            //System.out.println(ps.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()== true) {
                String type = rs.getString(1);
                int level =   rs.getInt(3);
                String menu = rs.getString(2);
                String menu_text = rs.getString(4);
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
    
    
    public static Connection getConnection(String URL, String db_user, 
                            String db_password, String prg_user,
                            String prg_password){
        Connection cnx = null;
        try {
            cnx = DriverManager.getConnection(URL, db_user, db_password);
            System.out.println("Succesful connection to DB");
            String SQL = "SELECT username FROM user_bob WHERE email=? AND password=?";
            PreparedStatement psu =cnx.prepareStatement(SQL);
            psu.setString(1, prg_user);
            psu.setString(2, prg_password);
            ResultSet rsu = psu.executeQuery();
            if (!rsu.next()) {
                cnx.close();
                cnx = null;
            }
        } catch (Exception ex) {
            System.out.println("getConnection():"+ex.getMessage());
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
