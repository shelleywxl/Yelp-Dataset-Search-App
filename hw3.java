
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author xueluwu
 */
public class hw3 extends javax.swing.JFrame {

    Connection con = null;
    public static final String DBURL = "jdbc:oracle:thin:@localhost:1522:ORCL";
    public static final String DBUSER = "hr";
    public static final String DBPASSWORD = "hr";
    
    public static String sql_query = "";
    ArrayList<String> selected_main_categories = new ArrayList<String>();
    ArrayList<String> selected_sub_categories = new ArrayList<String>();
    ArrayList<String> selected_attributes = new ArrayList<String>();
    
    // Main categories
    String[] main_categories = {"Active Life", "Arts & Entertainment", "Automotive", "Car Rental","Cafes","Beauty & Spas","Convenience Stores","Dentists","Doctors","Drugstores","Department Stores","Education","Event Planning & Services","Flowers & Gifts","Food","Health & Medical","Home Services","Home & Garden","Hospitals","Hotels & Travel","Hardware Stores","Grocery","Medical Centers","Nurseries & Gardening","Nightlife","Restaurants","Shopping","Transportation"};
    
    /**
     * Creates new form hw3
     */
    public hw3() {
        initComponents();
        
        try {
        // connect
            Class.forName("oracle.jdbc.driver.OracleDriver");
            con = DriverManager.getConnection(DBURL, DBUSER, DBPASSWORD);
            System.out.println("Successfully connect to the driver");
            
            // Show main categories (add checkboxes to JPanel to jScrollPane)
            JPanel business_main_category_jPane = new JPanel();
            business_main_category_jPane.setLayout(new BoxLayout(business_main_category_jPane, BoxLayout.Y_AXIS));
            for (int i = 0; i < main_categories.length; ++i) {
                JCheckBox main_category_checkbox = new JCheckBox(main_categories[i]);
                business_main_category_jPane.add(main_category_checkbox);
                business_main_category_jPane.revalidate();
                business_main_category_jPane.repaint();

                main_category_checkbox.addMouseListener(new MouseListener(){
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        JCheckBox main_category_checkbox = (JCheckBox) e.getSource();
                        String curr_main_category = main_category_checkbox.getText();
                        if (main_category_checkbox.isSelected()) {
                            selected_main_categories.add(curr_main_category);
                        }
                        else {
                            selected_main_categories.remove(curr_main_category);
                        }
                        System.out.println("*** DEBUG selected main categories: " + selected_main_categories.toString());
                        showSubCategories();
                        showAttributes();
                    }
                    @Override
                    public void mousePressed(MouseEvent e) {
                    }
                    @Override
                    public void mouseReleased(MouseEvent e) {
                    }
                    @Override
                    public void mouseEntered(MouseEvent e) {
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                    }
                });
            }
            business_main_category_jScrollPane.setViewportView(business_main_category_jPane);
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // After selecting the main categories, show corresponding subcategories
    public void showSubCategories() {
        try {
            //business_sub_category_jScrollPane.removeAll();
            selected_sub_categories.clear();

            // Get a list of corresponding subcategories of the selected main categories
            PreparedStatement selectSubcategoryPreparedStatement = null;
            ResultSet resultSet = null;
            ArrayList<String> sub_category_list = new ArrayList();
            String and_or = (String) business_search_for_jComboBox.getSelectedItem();
            String intersect_union = "INTERSECT";
            if (and_or.equals("AND")) {
                intersect_union = "INTERSECT";
            }
            else {
                intersect_union = "UNION";
            }
            
            if (selected_main_categories.size() > 0) {
                String statement_sql = "SELECT DISTINCT BC.sub_category FROM Business_Category BC WHERE BC.business_id IN (\n";
                for (int i = 0; i < selected_main_categories.size(); ++i) {
                    String index = Integer.toString(i);
                    statement_sql += "(SELECT BC" + index + ".business_id FROM Business_Category BC" + index + "\n";
                    statement_sql += "WHERE BC" + index + ".main_category = '" + selected_main_categories.get(i) + "')";
                    statement_sql += "\n" + intersect_union + "\n";
                }
                // remove the "intersect_union" in the end
                if (intersect_union.equals("INTERSECT")) {
                    statement_sql = statement_sql.substring(0, statement_sql.length() - 11) + ")";
                } else {
                    statement_sql = statement_sql.substring(0, statement_sql.length() - 7) + ")";
                }
                System.out.println("DEBUG: statement_sql: " + statement_sql);
                
                selectSubcategoryPreparedStatement = con.prepareStatement(statement_sql);
                resultSet = selectSubcategoryPreparedStatement.executeQuery();
                while (resultSet.next()) {
                    String sub_category = resultSet.getString(resultSet.findColumn("sub_category"));
                    if (sub_category != null){
                        sub_category_list.add(sub_category);
                    }
                }
                System.out.println("DEBUG: sub_category_list: " + sub_category_list.toString());
                resultSet.close();
            }
            Collections.sort(sub_category_list);
            
            // Show checkboxes for subcategories
            JPanel business_sub_category_jPane = new JPanel();
            business_sub_category_jPane.setLayout(new BoxLayout(business_sub_category_jPane, BoxLayout.Y_AXIS));
            for (int i = 0; i < sub_category_list.size(); ++i) {
                JCheckBox sub_category_checkbox = new JCheckBox(sub_category_list.get(i));
                business_sub_category_jPane.add(sub_category_checkbox);
                sub_category_checkbox.addMouseListener(new MouseListener() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        JCheckBox business_sub_category_checkbox = (JCheckBox) e.getSource();
                        String curr_sub_category = business_sub_category_checkbox.getText();
                        if (business_sub_category_checkbox.isSelected()) {
                            selected_sub_categories.add(curr_sub_category);
                        }
                        else {
                            selected_sub_categories.remove(curr_sub_category);
                        }
                        showAttributes();
                    }
                    @Override
                    public void mousePressed(MouseEvent e) {
                    }
                    @Override
                    public void mouseReleased(MouseEvent e) {
                    }
                    @Override
                    public void mouseEntered(MouseEvent e) {
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                    }
                });
            }
            business_sub_category_jPane.updateUI();
            business_sub_category_jScrollPane.setViewportView(business_sub_category_jPane);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }    
    }

    // After selecting the sub categories, show corresponding attributes
    public void showAttributes() {
        try {
            selected_attributes.clear();
            // Get a list of corresponding attributes of the selected main and sub categories
            PreparedStatement selectAttributePreparedStatement = null;
            ResultSet resultSet = null;
            ArrayList<String> attribute_list = new ArrayList();
            String and_or = (String) business_search_for_jComboBox.getSelectedItem();
            String intersect_union = "INTERSECT";
            if (and_or.equals("AND")) {
                intersect_union = "INTERSECT";
            }
            else {
                intersect_union = "UNION";
            }
            
            if (selected_sub_categories.size() > 0) {
                String statement_sql = "SELECT DISTINCT BA.attributes FROM Business_Category BC, Business_Attributes BA WHERE BC.business_id = BA.business_id AND BC.business_id IN (\n";
                int main_categories_size = selected_main_categories.size();
                int sub_categories_size = selected_sub_categories.size();
                for (int i = 0; i < main_categories_size; ++i) {
                    String index = Integer.toString(i);
                    statement_sql += "(SELECT BC1" + index + ".business_id FROM Business_Category BC1" + index;
                    statement_sql += " WHERE BC1" + index + ".main_category = '" + selected_main_categories.get(i) + "')";
                    statement_sql += "\n" + intersect_union + "\n";
                }
                if (intersect_union.equals("INTERSECT")) {
                    statement_sql = statement_sql.substring(0, statement_sql.length() - 11) + ") AND BC.business_id IN (";
                }
                else {
                    statement_sql = statement_sql.substring(0, statement_sql.length() - 7) + ") AND BC.business_id IN (";
                }
                for (int j = 0; j < sub_categories_size; ++j) {
                    String index = Integer.toString(j) + Integer.toString(j);
                    statement_sql += "(SELECT BC2" + index + ".business_id FROM Business_Category BC2" + index;
                    statement_sql += " WHERE BC2" + index + ".sub_category = '" + selected_sub_categories.get(j) + "')";
                    statement_sql += "\n" + intersect_union + "\n";
                }
                // remove the "\nintersect_union\n" in the end
                if (intersect_union.equals("INTERSECT")) {
                    statement_sql = statement_sql.substring(0, statement_sql.length() - 11) + ")";
                } else {
                    statement_sql = statement_sql.substring(0, statement_sql.length() - 7) + ")";
                }
                
                System.out.println("DEBUG: " + statement_sql);
                
                selectAttributePreparedStatement = con.prepareStatement(statement_sql);
                resultSet = selectAttributePreparedStatement.executeQuery();
                while (resultSet.next()) {
                    String attribute = resultSet.getString(resultSet.findColumn("attributes"));
                    if (attribute != null){
                        attribute_list.add(attribute);
                    }
                }
                resultSet.close();
            }
            System.out.println("attribute_list: " + attribute_list.toString());
            Collections.sort(attribute_list);
            
            // Show checkboxes for attributes
            JPanel business_attribute_jPane = new JPanel();
            business_attribute_jPane.setLayout(new BoxLayout(business_attribute_jPane, BoxLayout.Y_AXIS));
            for (int i = 0; i < attribute_list.size(); ++i) {
                JCheckBox attribute_checkbox = new JCheckBox(attribute_list.get(i));
                business_attribute_jPane.add(attribute_checkbox);
                attribute_checkbox.addMouseListener(new MouseListener() {
                @Override
                    public void mouseClicked(MouseEvent e) {
                        JCheckBox business_attribute_checkbox = (JCheckBox) e.getSource();
                        String curr_attribute = business_attribute_checkbox.getText();
                        if (business_attribute_checkbox.isSelected()) {
                            selected_attributes.add(curr_attribute);
                        }
                        else {
                            selected_attributes.remove(curr_attribute);
                        }
                    }
                    @Override
                    public void mousePressed(MouseEvent e) {
                    }
                    @Override
                    public void mouseReleased(MouseEvent e) {
                    }
                    @Override
                    public void mouseEntered(MouseEvent e) {
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                    }
                });
            }
            business_attribute_jPane.updateUI();
            business_attribute_jScrollPane.setViewportView(business_attribute_jPane);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // ============== Return a string for sql statement with review restrictions
    public String getReviewRestriction(String and_or) {
        // qualification 1: Date
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date from_date = business_from_jXDatePicker.getDate();
        String review_from_date = "";
        if (from_date != null) {
            review_from_date = dateFormat.format(from_date);
        }
        Date to_date = business_to_jXDatePicker.getDate();
        String review_to_date = "";
        if (to_date != null) {
            review_to_date = dateFormat.format(to_date);
        }
        // qualification 2: star
        String star_operation = (String) business_star_jComboBox.getSelectedItem();
        String star_value = (String) business_star_jTextField.getText();
        // qualification 3: votes
        String votes_operation = (String) business_votes_jComboBox.getSelectedItem();
        String votes_value = (String) business_votes_jTextField.getText();
        
        // No restrictions in review section
        if (review_from_date.trim().equals("") && review_to_date.trim().equals("") && star_value.trim().equals("") && votes_value.trim().equals("")) {
            return "";
        }
        
        String statement_sql = "B.business_id IN (SELECT R.business_id FROM Review R";
        // review date qualification
        if (! review_from_date.equals("")) {
            statement_sql += " WHERE R.review_date >= to_timestamp('" + review_from_date + "','yyyy-MM-dd')";
            if (! review_to_date.equals("")) {
                statement_sql += " AND R.review_date <= to_timestamp('" + review_to_date + "','yyyy-MM-dd')";
            }
        }
        else if (! review_to_date.equals("")) {
            statement_sql += " WHERE R.review_date <= to_timestamp('" + review_to_date + "','yyyy-MM-dd')";
        }
        // star and votes qualifications to put on groups
        if (! star_value.equals("")) {
            statement_sql += " GROUP BY R.business_id HAVING AVG(R.stars)" + star_operation + star_value;
            if (! votes_value.equals("")) {
                statement_sql += and_or + " SUM((R.votes))" + votes_operation + votes_value;
            }
        }
        else if (! votes_value.equals("")) {
            statement_sql += " GROUP BY R.business_id HAVING SUM((R.votes))" + votes_operation + votes_value;
        }
        // B.business_id IN (.....")"
        statement_sql += ")";
        return statement_sql;
    }
    
    // Show reviews of the selected business in jTable
    public void showBusinessReview(String selected_business_id) {
        try {
            PreparedStatement selectReviewPreparedStatement = null;
            ResultSet resultSet = null;
            String statement_sql = "SELECT U.user_name, R.review_text FROM Review R, Users U \n";
            //String statement_sql = "SELECT U.user_name, R.review_id FROM Review R, Users U\n";
            statement_sql += "WHERE R.user_id = U.user_id AND R.business_id = '" + selected_business_id + "'";
            // show sql query
            System.out.println("DEBUG: query: " + statement_sql);
            business_query_jTextArea.setText(statement_sql);
            selectReviewPreparedStatement = con.prepareStatement(statement_sql);
            resultSet = selectReviewPreparedStatement.executeQuery();
            // load resultSet data in table
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Author");
            model.addColumn("Review Text");
            //model.addColumn("Review ID");
            while (resultSet.next()) {
                model.addRow(new Object[]{resultSet.getString(1), resultSet.getString(2)});
            }
            business_result_review_jTable.setModel(model);            
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Show reviews of the selected user in jTable
    public void showUserReview(String selected_user_id) {
        try {
            PreparedStatement selectReviewPreparedStatement = null;
            ResultSet resultSet = null;
            
            String statement_sql = "SELECT B.business_name, R.review_text FROM Review R, Business B\n";
            statement_sql += "WHERE R.business_id = B.business_id AND R.user_id = '" + selected_user_id + "'";
            // show sql query
            System.out.println("DEBUG: query: " + statement_sql);
            users_query_jTextArea.setText(statement_sql);
            selectReviewPreparedStatement = con.prepareStatement(statement_sql);
            resultSet = selectReviewPreparedStatement.executeQuery();
            // load resultSet data in table
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("For Business");
            model.addColumn("Review Text");
            while (resultSet.next()) {
                model.addRow(new Object[]{resultSet.getString(1), resultSet.getString(2)});
            }
            users_result_review_jTable.setModel(model);            
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }        
            

    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        main_jTabbedPane = new javax.swing.JTabbedPane();
        business_jPanel = new javax.swing.JPanel();
        business_search_for_jLabel = new javax.swing.JLabel();
        business_search_for_jComboBox = new javax.swing.JComboBox<>();
        business_search_for_between_jLabel = new javax.swing.JLabel();
        business_main_category_jLabel = new javax.swing.JLabel();
        business_sub_category_jLabel = new javax.swing.JLabel();
        business_attribute_jLabel = new javax.swing.JLabel();
        business_main_category_jScrollPane = new javax.swing.JScrollPane();
        business_sub_category_jScrollPane = new javax.swing.JScrollPane();
        business_attribute_jScrollPane = new javax.swing.JScrollPane();
        business_result_jLabel = new javax.swing.JLabel();
        business_execute_jButton = new javax.swing.JButton();
        business_query_jScrollPane = new javax.swing.JScrollPane();
        business_query_jTextArea = new javax.swing.JTextArea();
        business_review_jLabel = new javax.swing.JLabel();
        business_review_jPanel = new javax.swing.JPanel();
        business_from_jLabel = new javax.swing.JLabel();
        business_from_jXDatePicker = new org.jdesktop.swingx.JXDatePicker();
        business_to_jXDatePicker = new org.jdesktop.swingx.JXDatePicker();
        business_to_jLabel = new javax.swing.JLabel();
        business_star_jLabel = new javax.swing.JLabel();
        business_star_jComboBox = new javax.swing.JComboBox<>();
        business_star_jTextField = new javax.swing.JTextField();
        business_votes_jLabel = new javax.swing.JLabel();
        business_votes_jComboBox = new javax.swing.JComboBox<>();
        business_votes_jTextField = new javax.swing.JTextField();
        business_result_business_jLabel = new javax.swing.JLabel();
        business_result_review_jScrollPane = new javax.swing.JScrollPane();
        business_result_review_jTable = new javax.swing.JTable();
        jLabel13 = new javax.swing.JLabel();
        business_result_business_jScrollPane = new javax.swing.JScrollPane();
        business_result_business_jTable = new javax.swing.JTable();
        users_jPanel = new javax.swing.JPanel();
        users_search_for_jLabel = new javax.swing.JLabel();
        users_search_for_jComboBox = new javax.swing.JComboBox<>();
        users_serach_for_between_jLabel = new javax.swing.JLabel();
        users_result_jLabel1 = new javax.swing.JLabel();
        users_result_users_jLabel = new javax.swing.JLabel();
        users_result_users_jScrollPane = new javax.swing.JScrollPane();
        users_result_jTable = new javax.swing.JTable();
        users_result_review_jLabel = new javax.swing.JLabel();
        users_result_review_jScrollPane = new javax.swing.JScrollPane();
        users_result_review_jTable = new javax.swing.JTable();
        users_query_jScrollPane = new javax.swing.JScrollPane();
        users_query_jTextArea = new javax.swing.JTextArea();
        users_execute_jButton = new javax.swing.JButton();
        users_search_jPanel = new javax.swing.JPanel();
        users_since_jLabel = new javax.swing.JLabel();
        users_review_count_jLabel = new javax.swing.JLabel();
        users_friends_jLabel = new javax.swing.JLabel();
        users_stars_jLabel = new javax.swing.JLabel();
        users_votes_jLabel = new javax.swing.JLabel();
        users_since_jXDatePicker = new org.jdesktop.swingx.JXDatePicker();
        users_review_count_jComboBox = new javax.swing.JComboBox<>();
        users_friends_jComboBox = new javax.swing.JComboBox<>();
        users_stars_jComboBox = new javax.swing.JComboBox<>();
        users_votes_jComboBox = new javax.swing.JComboBox<>();
        users_review_count_jTextField = new javax.swing.JTextField();
        users_stars_jTextField = new javax.swing.JTextField();
        users_votes_jTextField = new javax.swing.JTextField();
        users_friends_jTextField = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        main_jTabbedPane.setFont(new java.awt.Font("Arial Black", 0, 14)); // NOI18N

        business_jPanel.setBackground(new java.awt.Color(255, 255, 255));

        business_search_for_jLabel.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        business_search_for_jLabel.setText("Search for");

        business_search_for_jComboBox.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        business_search_for_jComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "AND", "OR" }));

        business_search_for_between_jLabel.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        business_search_for_between_jLabel.setText("between attributes");

        business_main_category_jLabel.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        business_main_category_jLabel.setForeground(new java.awt.Color(196, 18, 0));
        business_main_category_jLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        business_main_category_jLabel.setText("Category");

        business_sub_category_jLabel.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        business_sub_category_jLabel.setForeground(new java.awt.Color(196, 18, 0));
        business_sub_category_jLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        business_sub_category_jLabel.setText("Sub-category");

        business_attribute_jLabel.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        business_attribute_jLabel.setForeground(new java.awt.Color(196, 18, 0));
        business_attribute_jLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        business_attribute_jLabel.setText("Attribute");

        business_main_category_jScrollPane.setMaximumSize(new java.awt.Dimension(1, 32767));

        business_result_jLabel.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        business_result_jLabel.setForeground(new java.awt.Color(196, 18, 0));
        business_result_jLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        business_result_jLabel.setText("Result");

        business_execute_jButton.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        business_execute_jButton.setForeground(new java.awt.Color(196, 18, 0));
        business_execute_jButton.setLabel("Execute Query");
        business_execute_jButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                business_execute_jButtonActionPerformed(evt);
            }
        });

        business_query_jTextArea.setColumns(20);
        business_query_jTextArea.setRows(5);
        business_query_jScrollPane.setViewportView(business_query_jTextArea);

        business_review_jLabel.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        business_review_jLabel.setForeground(new java.awt.Color(196, 18, 0));
        business_review_jLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        business_review_jLabel.setText("Review");

        business_review_jPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));

        business_from_jLabel.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        business_from_jLabel.setText("from (yy-M-d)");

        business_to_jLabel.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        business_to_jLabel.setText("to (yy-M-d)");

        business_star_jLabel.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        business_star_jLabel.setText("star");

        business_star_jComboBox.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        business_star_jComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "=", ">", "<" }));

        business_star_jTextField.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        business_star_jTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                business_star_jTextFieldActionPerformed(evt);
            }
        });

        business_votes_jLabel.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        business_votes_jLabel.setText("votes");

        business_votes_jComboBox.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        business_votes_jComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "=", ">", "<" }));

        business_votes_jTextField.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        business_votes_jTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                business_votes_jTextFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout business_review_jPanelLayout = new javax.swing.GroupLayout(business_review_jPanel);
        business_review_jPanel.setLayout(business_review_jPanelLayout);
        business_review_jPanelLayout.setHorizontalGroup(
            business_review_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(business_review_jPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(business_review_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(business_from_jLabel)
                    .addComponent(business_to_jLabel))
                .addGroup(business_review_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(business_review_jPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(business_star_jLabel)
                        .addGap(52, 52, 52)
                        .addComponent(business_votes_jLabel)
                        .addGap(59, 59, 59))
                    .addGroup(business_review_jPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(business_review_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(business_review_jPanelLayout.createSequentialGroup()
                                .addComponent(business_to_jXDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(business_star_jTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(business_votes_jTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(35, 35, 35))
                            .addGroup(business_review_jPanelLayout.createSequentialGroup()
                                .addComponent(business_from_jXDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(business_star_jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(12, 12, 12)
                                .addComponent(business_votes_jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))))
        );
        business_review_jPanelLayout.setVerticalGroup(
            business_review_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(business_review_jPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(business_review_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, business_review_jPanelLayout.createSequentialGroup()
                        .addGroup(business_review_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(business_votes_jLabel)
                            .addComponent(business_star_jLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(business_review_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(business_votes_jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(business_star_jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(business_from_jXDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(business_from_jLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(business_review_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(business_votes_jTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(business_star_jTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(business_to_jXDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(business_to_jLabel))
                .addContainerGap())
        );

        business_result_business_jLabel.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        business_result_business_jLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        business_result_business_jLabel.setText("Business");

        business_result_review_jScrollPane.setPreferredSize(new java.awt.Dimension(0, 0));

        business_result_review_jTable.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        business_result_review_jTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        business_result_review_jScrollPane.setViewportView(business_result_review_jTable);

        jLabel13.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setText("Review");

        business_result_business_jScrollPane.setPreferredSize(new java.awt.Dimension(0, 0));

        business_result_business_jTable.setBackground(new java.awt.Color(240, 240, 240));
        business_result_business_jTable.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        business_result_business_jTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        business_result_business_jScrollPane.setViewportView(business_result_business_jTable);

        javax.swing.GroupLayout business_jPanelLayout = new javax.swing.GroupLayout(business_jPanel);
        business_jPanel.setLayout(business_jPanelLayout);
        business_jPanelLayout.setHorizontalGroup(
            business_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(business_jPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(business_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(business_jPanelLayout.createSequentialGroup()
                        .addComponent(business_search_for_jLabel)
                        .addGap(2, 2, 2)
                        .addComponent(business_search_for_jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(business_search_for_between_jLabel)
                        .addGap(150, 150, 150)
                        .addComponent(business_result_jLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 317, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(business_jPanelLayout.createSequentialGroup()
                        .addGroup(business_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(business_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(business_query_jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                                .addGroup(business_jPanelLayout.createSequentialGroup()
                                    .addGap(124, 124, 124)
                                    .addComponent(business_execute_jButton))
                                .addComponent(business_review_jLabel))
                            .addGroup(business_jPanelLayout.createSequentialGroup()
                                .addGroup(business_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(business_main_category_jScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(business_main_category_jLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(business_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(business_jPanelLayout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(business_sub_category_jLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, business_jPanelLayout.createSequentialGroup()
                                        .addGap(6, 6, 6)
                                        .addComponent(business_sub_category_jScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGroup(business_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(business_jPanelLayout.createSequentialGroup()
                                        .addGap(16, 16, 16)
                                        .addComponent(business_attribute_jLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(business_jPanelLayout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(business_attribute_jScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addComponent(business_review_jPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(business_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(business_jPanelLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(business_result_business_jLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 319, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(business_jPanelLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(business_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(business_result_review_jScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                    .addComponent(business_result_business_jScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))))))
                .addContainerGap())
        );
        business_jPanelLayout.setVerticalGroup(
            business_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(business_jPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(business_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(business_result_jLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(business_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(business_search_for_jLabel)
                        .addComponent(business_search_for_jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(business_search_for_between_jLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(business_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(business_main_category_jLabel)
                    .addComponent(business_sub_category_jLabel)
                    .addComponent(business_attribute_jLabel)
                    .addComponent(business_result_business_jLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(business_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(business_jPanelLayout.createSequentialGroup()
                        .addGroup(business_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(business_result_business_jScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 283, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(business_attribute_jScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(business_result_review_jScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(business_jPanelLayout.createSequentialGroup()
                        .addGroup(business_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(business_sub_category_jScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(business_main_category_jScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(business_review_jLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(business_review_jPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(22, 22, 22)
                        .addComponent(business_query_jScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(business_execute_jButton)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        main_jTabbedPane.addTab("Business", business_jPanel);
        business_jPanel.getAccessibleContext().setAccessibleName("Business");

        users_search_for_jLabel.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        users_search_for_jLabel.setText("Search for");

        users_search_for_jComboBox.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        users_search_for_jComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "AND", "OR" }));

        users_serach_for_between_jLabel.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        users_serach_for_between_jLabel.setText("between attributes");

        users_result_jLabel1.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        users_result_jLabel1.setForeground(new java.awt.Color(196, 18, 0));
        users_result_jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        users_result_jLabel1.setText("Result");

        users_result_users_jLabel.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        users_result_users_jLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        users_result_users_jLabel.setText("Users");

        users_result_users_jScrollPane.setPreferredSize(new java.awt.Dimension(0, 0));

        users_result_jTable.setBackground(new java.awt.Color(240, 240, 240));
        users_result_jTable.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        users_result_jTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        users_result_users_jScrollPane.setViewportView(users_result_jTable);

        users_result_review_jLabel.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        users_result_review_jLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        users_result_review_jLabel.setText("Review");

        users_result_review_jScrollPane.setPreferredSize(new java.awt.Dimension(0, 0));

        users_result_review_jTable.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        users_result_review_jTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        users_result_review_jScrollPane.setViewportView(users_result_review_jTable);

        users_query_jTextArea.setColumns(20);
        users_query_jTextArea.setRows(5);
        users_query_jScrollPane.setViewportView(users_query_jTextArea);

        users_execute_jButton.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        users_execute_jButton.setForeground(new java.awt.Color(196, 18, 0));
        users_execute_jButton.setLabel("Execute Query");
        users_execute_jButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                users_execute_jButtonActionPerformed(evt);
            }
        });

        users_since_jLabel.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        users_since_jLabel.setForeground(new java.awt.Color(196, 18, 0));
        users_since_jLabel.setText("Member Since (yy-M-d)");

        users_review_count_jLabel.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        users_review_count_jLabel.setForeground(new java.awt.Color(196, 18, 0));
        users_review_count_jLabel.setText("Review Count");

        users_friends_jLabel.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        users_friends_jLabel.setForeground(new java.awt.Color(196, 18, 0));
        users_friends_jLabel.setText("Number of Friends");

        users_stars_jLabel.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        users_stars_jLabel.setForeground(new java.awt.Color(196, 18, 0));
        users_stars_jLabel.setText("Average Stars");

        users_votes_jLabel.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        users_votes_jLabel.setForeground(new java.awt.Color(196, 18, 0));
        users_votes_jLabel.setText("Number of Votes");

        users_review_count_jComboBox.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        users_review_count_jComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "=", ">", "<" }));

        users_friends_jComboBox.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        users_friends_jComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "=", ">", "<" }));

        users_stars_jComboBox.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        users_stars_jComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "=", ">", "<" }));

        users_votes_jComboBox.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        users_votes_jComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "=", ">", "<" }));

        users_review_count_jTextField.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        users_review_count_jTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                users_review_count_jTextFieldActionPerformed(evt);
            }
        });

        users_stars_jTextField.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        users_stars_jTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                users_stars_jTextFieldActionPerformed(evt);
            }
        });

        users_votes_jTextField.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        users_votes_jTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                users_votes_jTextFieldActionPerformed(evt);
            }
        });

        users_friends_jTextField.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        users_friends_jTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                users_friends_jTextFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout users_search_jPanelLayout = new javax.swing.GroupLayout(users_search_jPanel);
        users_search_jPanel.setLayout(users_search_jPanelLayout);
        users_search_jPanelLayout.setHorizontalGroup(
            users_search_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(users_search_jPanelLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(users_search_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(users_since_jLabel)
                    .addGroup(users_search_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(users_search_jPanelLayout.createSequentialGroup()
                            .addComponent(users_votes_jLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(users_votes_jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(users_search_jPanelLayout.createSequentialGroup()
                            .addComponent(users_stars_jLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(users_stars_jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(users_search_jPanelLayout.createSequentialGroup()
                            .addComponent(users_review_count_jLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(users_review_count_jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(users_search_jPanelLayout.createSequentialGroup()
                            .addComponent(users_friends_jLabel)
                            .addGap(18, 18, 18)
                            .addComponent(users_friends_jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(20, 20, 20)
                .addGroup(users_search_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(users_search_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(users_review_count_jTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(users_stars_jTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(users_votes_jTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(users_friends_jTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(users_since_jXDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        users_search_jPanelLayout.setVerticalGroup(
            users_search_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(users_search_jPanelLayout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addGroup(users_search_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(users_since_jLabel)
                    .addComponent(users_since_jXDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32)
                .addGroup(users_search_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(users_review_count_jLabel)
                    .addComponent(users_review_count_jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(users_review_count_jTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addGroup(users_search_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(users_friends_jLabel)
                    .addComponent(users_friends_jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(users_friends_jTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addGroup(users_search_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(users_stars_jLabel)
                    .addComponent(users_stars_jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(users_stars_jTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addGroup(users_search_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(users_votes_jLabel)
                    .addComponent(users_votes_jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(users_votes_jTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(67, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout users_jPanelLayout = new javax.swing.GroupLayout(users_jPanel);
        users_jPanel.setLayout(users_jPanelLayout);
        users_jPanelLayout.setHorizontalGroup(
            users_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(users_jPanelLayout.createSequentialGroup()
                .addGroup(users_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(users_jPanelLayout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(users_search_for_jLabel)
                        .addGap(2, 2, 2)
                        .addComponent(users_search_for_jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(users_serach_for_between_jLabel))
                    .addGroup(users_jPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(users_search_jPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(users_jPanelLayout.createSequentialGroup()
                        .addGap(114, 114, 114)
                        .addComponent(users_execute_jButton)))
                .addContainerGap(380, Short.MAX_VALUE))
            .addGroup(users_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(users_jPanelLayout.createSequentialGroup()
                    .addGap(7, 7, 7)
                    .addComponent(users_query_jScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 347, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(15, 15, 15)
                    .addGroup(users_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(users_result_users_jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE)
                        .addComponent(users_result_review_jScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 347, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(users_result_review_jLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 347, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(users_result_users_jLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(users_result_jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addContainerGap(15, Short.MAX_VALUE)))
        );
        users_jPanelLayout.setVerticalGroup(
            users_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(users_jPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(users_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(users_search_for_jLabel)
                    .addComponent(users_search_for_jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(users_serach_for_between_jLabel))
                .addGap(11, 11, 11)
                .addComponent(users_search_jPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 170, Short.MAX_VALUE)
                .addComponent(users_execute_jButton)
                .addGap(18, 18, 18))
            .addGroup(users_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(users_jPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(users_result_jLabel1)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(users_result_users_jLabel)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(users_jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(users_jPanelLayout.createSequentialGroup()
                            .addComponent(users_result_users_jScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 283, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(8, 8, 8)
                            .addComponent(users_result_review_jLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(users_result_review_jScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(users_jPanelLayout.createSequentialGroup()
                            .addGap(346, 346, 346)
                            .addComponent(users_query_jScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        main_jTabbedPane.addTab("  User  ", users_jPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(main_jTabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 737, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(main_jTabbedPane)
        );

        main_jTabbedPane.getAccessibleContext().setAccessibleName("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Business execute query
    private void business_execute_jButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_business_execute_jButtonActionPerformed
        try {
            System.out.println("DEBUG: selected_main_categories: " + selected_main_categories.toString());
            System.out.println("DEBUG: selected_sub_categories: " + selected_sub_categories.toString());
            System.out.println("DEBUG: selected_attributes: " + selected_attributes.toString());
            
            PreparedStatement selectBusinessPreparedStatement = null;
            ResultSet resultSet = null;
            String and_or = (String) business_search_for_jComboBox.getSelectedItem();
            String intersect_union = "INTERSECT";
            if (and_or.equals("OR")) {
                intersect_union = "UNION";
            }
            String statement_sql = "SELECT DISTINCT B.business_name, B.city, B.state, B.stars, B.business_id \nFROM Business B ";
            // main category qualification
            if (!selected_main_categories.isEmpty()) {
                statement_sql += "\nWHERE B.business_id IN (";
                // main categories qualifications
                for (int i = 0; i < selected_main_categories.size(); ++i) {
                    String idx = Integer.toString(i);
                    statement_sql += "(SELECT BC1" + idx + ".business_id FROM Business_Category BC1" + idx;
                    statement_sql += " WHERE BC1" + idx + ".main_category = '" + selected_main_categories.get(i) + "') " + intersect_union + " ";
                }
                // remove the " intersect_union " in the end
                if (intersect_union.equals("INTERSECT")) {
                    statement_sql = statement_sql.substring(0, statement_sql.length() - 11) + ")";
                } else {
                    statement_sql = statement_sql.substring(0, statement_sql.length() - 7) + ")";
                }
            }
            // sub categories qualifications
            if (!selected_sub_categories.isEmpty()) {
                statement_sql += "AND B.business_id IN (";
                for (int j = 0; j < selected_sub_categories.size(); ++j) {
                    String idx = Integer.toString(j);
                    statement_sql += "(SELECT BC2" + idx + ".business_id FROM Business_Category BC2" + idx;
                    statement_sql += " WHERE BC2" + idx + ".sub_category = '" + selected_sub_categories.get(j) + "') " + intersect_union + " ";
                }
                // remove the " intersect_union " in the end
                if (intersect_union.equals("INTERSECT")) {
                    statement_sql = statement_sql.substring(0, statement_sql.length() - 11) + ")";
                } else {
                    statement_sql = statement_sql.substring(0, statement_sql.length() - 7) + ")";
                }
            }
            // attributes qualifications
            if (!selected_attributes.isEmpty()) {
                statement_sql += " \nAND B.business_id IN (";
                for (int k = 0; k < selected_attributes.size(); ++k) {
                    String idx = Integer.toString(k);
                    statement_sql += "(SELECT BA1" + idx + ".business_id FROM Business_Attributes BA1" + idx;
                    statement_sql += " WHERE BA1" + idx + ".attributes = '" + selected_attributes.get(k) + "') " + intersect_union + " ";
                }
                // remove the "and_or " in the end
                if (intersect_union.equals("INTERSECT")) {
                    statement_sql = statement_sql.substring(0, statement_sql.length() - 11) + ")";
                } else {
                    statement_sql = statement_sql.substring(0, statement_sql.length() - 7) + ")";
                }
            }
            // review qualifications
            String review_statement = getReviewRestriction(and_or);
            if (!review_statement.equals("")) {
                if (selected_main_categories.isEmpty()) {
                    statement_sql += "\nWHERE " + review_statement;
                }
                else {
                    statement_sql += "\nAND " + review_statement;
                }
            }
            
            // show sql query
            System.out.println("DEBUG: statement_sql: " + statement_sql);
            business_query_jTextArea.setText(statement_sql);
            selectBusinessPreparedStatement = con.prepareStatement(statement_sql);
            resultSet = selectBusinessPreparedStatement.executeQuery();
            //System.out.println(resultSet);
            
            // load resultSet data in table
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Business Name");
            model.addColumn("City");
            model.addColumn("State");
            model.addColumn("Stars");
            model.addColumn("business_id");
            int count = 0;
            while (resultSet.next()) {
                count ++;
                model.addRow(new Object[]{resultSet.getString(1), resultSet.getString(2), resultSet.getString(3), resultSet.getDouble(4), resultSet.getString(5)});
            }
            System.out.println("count" + count);
            business_result_business_jTable.setModel(model);
            // hiding business_id
            business_result_business_jTable.removeColumn(business_result_business_jTable.getColumnModel().getColumn(4));
            business_result_business_jTable.setVisible(true);
                        
            // To get review
            business_result_business_jTable.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    JTable clicked_table = (JTable) e.getSource();
                    int selected_row = clicked_table.getSelectedRow();
                    String selected_business_id = business_result_business_jTable.getModel().getValueAt(selected_row, 4).toString();
                    System.out.println("DEBUG: selected business_id: " + selected_business_id);
                    showBusinessReview(selected_business_id);
                }
            });   
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_business_execute_jButtonActionPerformed

    private void business_star_jTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_business_star_jTextFieldActionPerformed

    }//GEN-LAST:event_business_star_jTextFieldActionPerformed

    // Users execute query
    private void users_execute_jButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_users_execute_jButtonActionPerformed
        try {
            PreparedStatement selectUsersPreparedStatement = null;
            ResultSet resultSet = null;
            String and_or = (String) users_search_for_jComboBox.getSelectedItem();
            
            String statement_sql = "SELECT U.user_name, U.yelping_since, U.average_stars, U.user_id FROM Users U";
            // qualification 1: member since Date
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date since_date = users_since_jXDatePicker.getDate();
            String yelping_since_date = "";
            if (since_date != null) {
                yelping_since_date = dateFormat.format(since_date);
            }
            // qualification 2: review count
            String review_count_operation = (String) users_review_count_jComboBox.getSelectedItem();
            String review_count_value = (String) users_review_count_jTextField.getText();
            // qualification 3: number of friends
            String number_of_friends_operation = (String) users_friends_jComboBox.getSelectedItem();
            String number_of_friends_value = (String) users_friends_jTextField.getText();
            // qualification 4: average stars
            String average_stars_operation = (String) users_stars_jComboBox.getSelectedItem();
            String average_stars_value = (String) users_stars_jTextField.getText();
            // qualification 5: number o fvotes
            String number_of_votes_operation = (String) users_votes_jComboBox.getSelectedItem();
            String number_of_votes_value = (String) users_votes_jTextField.getText();
            // where qualifications
            if (!yelping_since_date.equals("") || !review_count_value.equals("") || !number_of_friends_value.equals("") || !average_stars_value.equals("") || !number_of_votes_value.equals("")) {
                statement_sql += " WHERE ";
                if (!yelping_since_date.equals("")) {                
                    statement_sql += "U.yelping_since >= to_timestamp('" + yelping_since_date + "','yyyy-MM-dd') " + and_or + " ";
                }
                if (!review_count_value.equals("")) {
                    statement_sql += "U.review_count" + review_count_operation + review_count_value + " " + and_or + " ";
                }
                if (!number_of_friends_value.equals("")) {
                    statement_sql += "U.number_of_friends" + number_of_friends_operation + number_of_friends_value + " " + and_or + " ";
                }
                if (!average_stars_value.equals("")) {
                    statement_sql += "U.average_stars" + average_stars_operation + average_stars_value + " " + and_or + " ";
                }
                if (!number_of_votes_value.equals("")) {
                    statement_sql += "(U.useful_votes + U.funny_votes + U.cool_votes)" + number_of_votes_operation + number_of_votes_value + " " + and_or + " ";
                }
                // remove the "and_or " in the end
                if (and_or.equals("AND")) {
                    statement_sql = statement_sql.substring(0, statement_sql.length() - 4);
                } else {
                    statement_sql = statement_sql.substring(0, statement_sql.length() - 3);
                }
            }
            
            // show sql query
            System.out.println("DUBUG: statement_sql: " + statement_sql);
            users_query_jTextArea.setText(statement_sql);
            selectUsersPreparedStatement = con.prepareStatement(statement_sql);
            resultSet = selectUsersPreparedStatement.executeQuery();
            
            // load resultSet data in table
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("User Name");
            model.addColumn("Member Since");
            model.addColumn("Average Stars");
            model.addColumn("user_id");
            while (resultSet.next()) {
                model.addRow(new Object[]{resultSet.getString(1), resultSet.getString(2).substring(0,10), resultSet.getDouble(3), resultSet.getString(4)});
            }
            users_result_jTable.setModel(model);
            // hiding user_id
            users_result_jTable.removeColumn(users_result_jTable.getColumnModel().getColumn(3));
            users_result_jTable.setVisible(true);
            
            // To get review
            users_result_jTable.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    JTable clicked_table = (JTable) e.getSource();
                    int selected_row = clicked_table.getSelectedRow();
                    String selected_user_id = users_result_jTable.getModel().getValueAt(selected_row, 3).toString();
                    System.out.println("DEBUG: selected user_id: " + selected_user_id);
                    showUserReview(selected_user_id);
                }
            });
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_users_execute_jButtonActionPerformed

    private void users_review_count_jTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_users_review_count_jTextFieldActionPerformed
       
    }//GEN-LAST:event_users_review_count_jTextFieldActionPerformed

    private void users_stars_jTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_users_stars_jTextFieldActionPerformed
        
    }//GEN-LAST:event_users_stars_jTextFieldActionPerformed

    private void users_votes_jTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_users_votes_jTextFieldActionPerformed
       
    }//GEN-LAST:event_users_votes_jTextFieldActionPerformed

    private void users_friends_jTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_users_friends_jTextFieldActionPerformed
        
    }//GEN-LAST:event_users_friends_jTextFieldActionPerformed

    private void business_votes_jTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_business_votes_jTextFieldActionPerformed

    }//GEN-LAST:event_business_votes_jTextFieldActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(hw3.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(hw3.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(hw3.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(hw3.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new hw3().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel business_attribute_jLabel;
    private javax.swing.JScrollPane business_attribute_jScrollPane;
    private javax.swing.JButton business_execute_jButton;
    private javax.swing.JLabel business_from_jLabel;
    private org.jdesktop.swingx.JXDatePicker business_from_jXDatePicker;
    private javax.swing.JPanel business_jPanel;
    private javax.swing.JLabel business_main_category_jLabel;
    private javax.swing.JScrollPane business_main_category_jScrollPane;
    private javax.swing.JScrollPane business_query_jScrollPane;
    private javax.swing.JTextArea business_query_jTextArea;
    private javax.swing.JLabel business_result_business_jLabel;
    private javax.swing.JScrollPane business_result_business_jScrollPane;
    private javax.swing.JTable business_result_business_jTable;
    private javax.swing.JLabel business_result_jLabel;
    private javax.swing.JScrollPane business_result_review_jScrollPane;
    private javax.swing.JTable business_result_review_jTable;
    private javax.swing.JLabel business_review_jLabel;
    private javax.swing.JPanel business_review_jPanel;
    private javax.swing.JLabel business_search_for_between_jLabel;
    private javax.swing.JComboBox<String> business_search_for_jComboBox;
    private javax.swing.JLabel business_search_for_jLabel;
    private javax.swing.JComboBox<String> business_star_jComboBox;
    private javax.swing.JLabel business_star_jLabel;
    private javax.swing.JTextField business_star_jTextField;
    private javax.swing.JLabel business_sub_category_jLabel;
    private javax.swing.JScrollPane business_sub_category_jScrollPane;
    private javax.swing.JLabel business_to_jLabel;
    private org.jdesktop.swingx.JXDatePicker business_to_jXDatePicker;
    private javax.swing.JComboBox<String> business_votes_jComboBox;
    private javax.swing.JLabel business_votes_jLabel;
    private javax.swing.JTextField business_votes_jTextField;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JTabbedPane main_jTabbedPane;
    private javax.swing.JButton users_execute_jButton;
    private javax.swing.JComboBox<String> users_friends_jComboBox;
    private javax.swing.JLabel users_friends_jLabel;
    private javax.swing.JTextField users_friends_jTextField;
    private javax.swing.JPanel users_jPanel;
    private javax.swing.JScrollPane users_query_jScrollPane;
    private javax.swing.JTextArea users_query_jTextArea;
    private javax.swing.JLabel users_result_jLabel1;
    private javax.swing.JTable users_result_jTable;
    private javax.swing.JLabel users_result_review_jLabel;
    private javax.swing.JScrollPane users_result_review_jScrollPane;
    private javax.swing.JTable users_result_review_jTable;
    private javax.swing.JLabel users_result_users_jLabel;
    private javax.swing.JScrollPane users_result_users_jScrollPane;
    private javax.swing.JComboBox<String> users_review_count_jComboBox;
    private javax.swing.JLabel users_review_count_jLabel;
    private javax.swing.JTextField users_review_count_jTextField;
    private javax.swing.JComboBox<String> users_search_for_jComboBox;
    private javax.swing.JLabel users_search_for_jLabel;
    private javax.swing.JPanel users_search_jPanel;
    private javax.swing.JLabel users_serach_for_between_jLabel;
    private javax.swing.JLabel users_since_jLabel;
    private org.jdesktop.swingx.JXDatePicker users_since_jXDatePicker;
    private javax.swing.JComboBox<String> users_stars_jComboBox;
    private javax.swing.JLabel users_stars_jLabel;
    private javax.swing.JTextField users_stars_jTextField;
    private javax.swing.JComboBox<String> users_votes_jComboBox;
    private javax.swing.JLabel users_votes_jLabel;
    private javax.swing.JTextField users_votes_jTextField;
    // End of variables declaration//GEN-END:variables
}
