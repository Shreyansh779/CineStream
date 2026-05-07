import javax.swing.*;
import javax.swing.border.EmptyBorder;
import org.w3c.dom.events.MouseEvent;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import java.net.HttpURLConnection;

//Inheritance
public class MovieTicketBookingSystem extends JFrame {

    // --- UI THEME SETTINGS ---
    public static final Color BG_COLOR = new Color(20, 20, 20);
    public static final Color ACCENT_COLOR = new Color(229, 9, 20); // Netflix/BMS Red
    public static final Color ACCENT_HOVER = new Color(246, 18, 29);
    public static final Color TEXT_COLOR = Color.WHITE;
    public static final Color SECONDARY_BG = new Color(40, 40, 40);
    public static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 28);
    public static final Font NORMAL_FONT = new Font("SansSerif", Font.PLAIN, 16);
    public static final Font BOLD_FONT = new Font("SansSerif", Font.BOLD, 16);

    // --- APPLICATION STATE ---
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private User currentUser;
    private Mall selectedMall;
    private Movie selectedMovie;
    private List<String> selectedSeats;

    // --- IN-MEMORY DATABASE ---
    private static Map<String, User> usersDb = new HashMap<>();
    private static List<Mall> mallsDb = new ArrayList<>();

    public MovieTicketBookingSystem() {
        initDatabase();
        
        setTitle("CineStream - Movie Ticket Booking");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(BG_COLOR);

        // Add views to CardLayout
        mainPanel.add(createAuthPanel(), "Auth");
        mainPanel.add(new JPanel(), "Dashboard"); // Placeholder, generated dynamically
        mainPanel.add(new JPanel(), "Movies");    // Placeholder
        mainPanel.add(new JPanel(), "Seats");     // Placeholder
        mainPanel.add(new JPanel(), "Checkout");  // Placeholder

        add(mainPanel);
        cardLayout.show(mainPanel, "Auth");
    }

    // ==========================================
    // UI COMPONENTS & VIEWS
    // ==========================================

    private JPanel createAuthPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COLOR);
        
        JPanel card = new JPanel(new GridLayout(6, 1, 10, 10));
        card.setBackground(SECONDARY_BG);
        card.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("Sign In / Register", SwingConstants.CENTER);
        title.setFont(TITLE_FONT);
        title.setForeground(ACCENT_COLOR);

        JTextField userField = new JTextField();
        styleTextField(userField, "Username");
        
        JPasswordField passField = new JPasswordField();
        styleTextField(passField, "Password");

        JButton loginBtn = createStyledButton("Login");
        JButton regBtn = createStyledButton("Register New User");

        JLabel messageLabel = new JLabel("", SwingConstants.CENTER);
        messageLabel.setForeground(Color.YELLOW);

        loginBtn.addActionListener(e -> {
            String uname = userField.getText();
            String pwd = new String(passField.getPassword());
            if (usersDb.containsKey(uname) && usersDb.get(uname).getPassword().equals(pwd)) {
                currentUser = usersDb.get(uname);
                showDashboard();
            } else {
                messageLabel.setText("Invalid credentials!");
            }
        });

        regBtn.addActionListener(e -> {
            String uname = userField.getText();
            String pwd = new String(passField.getPassword());
            if (uname.isEmpty() || pwd.isEmpty()) {
                messageLabel.setText("Fields cannot be empty!");
            } else if (usersDb.containsKey(uname)) {
                messageLabel.setText("User already exists!");
            } else {
                usersDb.put(uname, new User(uname, pwd));
                messageLabel.setText("Registered! You can now login.");
            }
        });

        card.add(title);
        card.add(userField);
        card.add(passField);
        card.add(loginBtn);
        card.add(regBtn);
        card.add(messageLabel);

        panel.add(card);
        return panel;
    }

    private void showDashboard() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        panel.add(createHeader("Select a Cinema Mall"), BorderLayout.NORTH);

        JPanel mallGrid = new JPanel(new GridLayout(0, 2, 20, 20));
        mallGrid.setBackground(BG_COLOR);
        mallGrid.setBorder(new EmptyBorder(20, 50, 50, 50));

        for (Mall mall : mallsDb) {
            JPanel mallCard = new JPanel(new BorderLayout());
            mallCard.setBackground(SECONDARY_BG);
            mallCard.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));

            JLabel nameLabel = new JLabel(mall.getName(), SwingConstants.CENTER);
            nameLabel.setFont(TITLE_FONT);
            nameLabel.setForeground(TEXT_COLOR);
            nameLabel.setBorder(new EmptyBorder(30, 0, 30, 0));

            JButton selectBtn = createStyledButton("View Movies");
            selectBtn.addActionListener(e -> {
                selectedMall = mall;
                showMovies();
            });

            mallCard.add(nameLabel, BorderLayout.CENTER);
            mallCard.add(selectBtn, BorderLayout.SOUTH);
            mallGrid.add(mallCard);
        }

        panel.add(mallGrid, BorderLayout.CENTER);
        mainPanel.add(panel, "Dashboard");
        cardLayout.show(mainPanel, "Dashboard");
    }

    private void showMovies() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        panel.add(createHeader("Now Showing at " + selectedMall.getName()), BorderLayout.NORTH);

        JPanel movieGrid = new JPanel(new GridLayout(1, 0, 20, 20));
        movieGrid.setBackground(BG_COLOR);
        movieGrid.setBorder(new EmptyBorder(20, 50, 50, 50));

        for (Movie movie : selectedMall.getMovies()) {
            JPanel mCard = new JPanel(new BorderLayout(0, 10));
            mCard.setBackground(SECONDARY_BG);
            mCard.setBorder(new EmptyBorder(15, 15, 15, 15));

            /// Load Local Image
            JLabel poster = new JLabel("", SwingConstants.CENTER);
                try {
                    // This looks for the image relative to the project folder
                    ImageIcon icon = new ImageIcon(movie.getPosterPath());
                    Image img = icon.getImage().getScaledInstance(200, 300, Image.SCALE_SMOOTH);
                    poster.setIcon(new ImageIcon(img));
                } catch (Exception e) {
                    poster.setText("Image Not Found");
                    poster.setForeground(Color.GRAY);
                    poster.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                }

            JPanel infoPanel = new JPanel(new GridLayout(4, 1));
            infoPanel.setBackground(SECONDARY_BG);
            
            JLabel title = new JLabel(movie.getTitle(), SwingConstants.CENTER);
            title.setFont(BOLD_FONT);
            title.setForeground(Color.WHITE);
            
            JLabel screen = new JLabel("Screen: " + movie.getScreen(), SwingConstants.CENTER);
            screen.setForeground(Color.LIGHT_GRAY);
            
            JLabel price = new JLabel("Price: ₹" + movie.getPrice(), SwingConstants.CENTER);
            price.setForeground(Color.GREEN);
            price.setFont(BOLD_FONT);

            JButton bookBtn = createStyledButton("Book Tickets");
            bookBtn.addActionListener(e -> {
                selectedMovie = movie;
                showSeats();
            });

            infoPanel.add(title);
            infoPanel.add(screen);
            infoPanel.add(price);
            infoPanel.add(bookBtn);

            mCard.add(poster, BorderLayout.CENTER);
            mCard.add(infoPanel, BorderLayout.SOUTH);
            movieGrid.add(mCard);
        }

        panel.add(new JScrollPane(movieGrid), BorderLayout.CENTER);
        
        JButton backBtn = createStyledButton("Back to Malls");
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "Dashboard"));
        panel.add(backBtn, BorderLayout.SOUTH);

        mainPanel.add(panel, "Movies");
        cardLayout.show(mainPanel, "Movies");
    }

    private void showSeats() {
        selectedSeats = new ArrayList<>();
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        panel.add(createHeader(selectedMovie.getTitle() + " - Select Seats"), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(BG_COLOR);

        // Screen Area
        JLabel screenLabel = new JLabel("S C R E E N   T H I S   W A Y", SwingConstants.CENTER);
        screenLabel.setOpaque(true);
        screenLabel.setBackground(Color.DARK_GRAY);
        screenLabel.setForeground(Color.WHITE);
        screenLabel.setFont(BOLD_FONT);
        screenLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        centerPanel.add(screenLabel, BorderLayout.NORTH);

        JPanel seatGrid = new JPanel(new GridLayout(5, 8, 10, 10));
        seatGrid.setBackground(BG_COLOR);
        seatGrid.setBorder(new EmptyBorder(30, 80, 30, 80));

        boolean[][] seats = selectedMovie.getSeats();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 8; j++) {
                String seatName = (char) ('A' + i) + "" + (j + 1);
                JToggleButton seatBtn = new JToggleButton(seatName);
                seatBtn.setFont(NORMAL_FONT);
                seatBtn.setFocusPainted(false);
                seatBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

                if (seats[i][j]) {
                    seatBtn.setEnabled(false);
                    seatBtn.setBackground(Color.GRAY);
                } else {
                    seatBtn.setBackground(Color.WHITE);
                    seatBtn.setForeground(Color.BLACK);
                    seatBtn.addItemListener(e -> {
                        if (seatBtn.isSelected()) {
                            seatBtn.setBackground(ACCENT_COLOR);
                            seatBtn.setForeground(Color.WHITE);
                            selectedSeats.add(seatName);
                        } else {
                            seatBtn.setBackground(Color.WHITE);
                            seatBtn.setForeground(Color.BLACK);
                            selectedSeats.remove(seatName);
                        }
                    });
                }
                seatGrid.add(seatBtn);
            }
        }
        centerPanel.add(seatGrid, BorderLayout.CENTER);
        panel.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        bottomPanel.setBackground(BG_COLOR);
        bottomPanel.setBorder(new EmptyBorder(20, 50, 20, 50));

        JButton backBtn = createStyledButton("Back to Movies");
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "Movies"));
        
        JButton proceedBtn = createStyledButton("Proceed to Payment");
        proceedBtn.addActionListener(e -> {
            if (selectedSeats.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select at least one seat!");
            } else {
                showCheckout();
            }
        });

        bottomPanel.add(backBtn);
        bottomPanel.add(proceedBtn);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        mainPanel.add(panel, "Seats");
        cardLayout.show(mainPanel, "Seats");
    }

    private void showCheckout() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COLOR);

        JPanel receiptCard = new JPanel(new GridLayout(8, 1, 5, 5));
        receiptCard.setBackground(SECONDARY_BG);
        receiptCard.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("Booking Summary", SwingConstants.CENTER);
        title.setFont(TITLE_FONT);
        title.setForeground(ACCENT_COLOR);

        double total = selectedSeats.size() * selectedMovie.getPrice();
        double taxes = total * 0.18; // 18% GST
        double finalAmount = total + taxes;

        receiptCard.add(title);
        receiptCard.add(createWhiteLabel("User: " + currentUser.getUsername()));
        receiptCard.add(createWhiteLabel("Mall: " + selectedMall.getName()));
        receiptCard.add(createWhiteLabel("Movie: " + selectedMovie.getTitle() + " (" + selectedMovie.getScreen() + ")"));
        receiptCard.add(createWhiteLabel("Seats: " + String.join(", ", selectedSeats)));
        receiptCard.add(createWhiteLabel("Subtotal: ₹" + total));
        receiptCard.add(createWhiteLabel("Taxes (18%): ₹" + String.format("%.2f", taxes)));
        
        JLabel totalLabel = new JLabel("Grand Total: ₹" + String.format("%.2f", finalAmount));
        totalLabel.setFont(BOLD_FONT);
        totalLabel.setForeground(Color.GREEN);
        receiptCard.add(totalLabel);

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        btnPanel.setBackground(SECONDARY_BG);
        
        JButton cancelBtn = createStyledButton("Cancel");
        cancelBtn.setBackground(Color.DARK_GRAY);
        cancelBtn.addActionListener(e -> cardLayout.show(mainPanel, "Seats"));

        JButton payBtn = createStyledButton("Pay Now");
        payBtn.addActionListener(e -> {
            // Mark seats as booked
            for (String seat : selectedSeats) {
                int row = seat.charAt(0) - 'A';
                int col = Integer.parseInt(seat.substring(1)) - 1;
                selectedMovie.getSeats()[row][col] = true;
            }
            JOptionPane.showMessageDialog(this, "Payment Successful! Tickets Booked.", "Success", JOptionPane.INFORMATION_MESSAGE);
            showDashboard(); // Return home
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(payBtn);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(receiptCard, gbc);
        
        gbc.gridy = 1;
        gbc.insets = new Insets(20, 0, 0, 0);
        panel.add(btnPanel, gbc);

        mainPanel.add(panel, "Checkout");
        cardLayout.show(mainPanel, "Checkout");
    }

    // ==========================================
    // UTILITY METHODS & STYLING
    // ==========================================

    private JPanel createHeader(String text) {
        JPanel header = new JPanel();
        header.setBackground(BG_COLOR);
        header.setBorder(new EmptyBorder(20, 0, 20, 0));
        JLabel label = new JLabel(text);
        label.setFont(TITLE_FONT);
        label.setForeground(TEXT_COLOR);
        header.add(label);
        return header;
    }

    private JLabel createWhiteLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(NORMAL_FONT);
        return label;
    }

    private void styleTextField(JTextField field, String placeholder) {
        field.setFont(NORMAL_FONT);
        field.setBackground(new Color(50, 50, 50));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), 
                placeholder, 0, 0, NORMAL_FONT, Color.LIGHT_GRAY));
    }

    //Abstraction
    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(BOLD_FONT);
        btn.setBackground(ACCENT_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(200, 40));

        // Hover Animation logic
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                btn.setBackground(ACCENT_HOVER);
            }
            public void mouseExited(MouseEvent evt) {
                btn.setBackground(ACCENT_COLOR);
            }
        });
        return btn;
    }

    // ==========================================
    // DUMMY DATA INITIALIZATION
    // ==========================================

    private void initDatabase() {
        usersDb.put("admin", new User("admin", "admin123"));
        usersDb.put("user", new User("user", "password"));

        Mall mall1 = new Mall("Mall of Dehradun");
        mall1.addMovie(new Movie("Dune: Part Two", "images/dune2.jpg", 350.0, "Screen 1"));
        mall1.addMovie(new Movie("Oppenheimer", "images/oppen.jpg", 400.0, "IMAX Screen"));
        mall1.addMovie(new Movie("F1-The Movie", "images/f1.jpg", 250.0, "Screen 3"));

        Mall mall2 = new Mall("Pacific Mall Dehradun");
        mall2.addMovie(new Movie("Interstellar", "images/inter.jpg", 300.0, "Screen 2"));
        mall2.addMovie(new Movie("The Batman", "images/batman.jpg", 320.0, "Screen 4"));
       
        mallsDb.add(mall1);
        mallsDb.add(mall2);
    }

    // ==========================================
    // MAIN METHOD
    // ==========================================
    public static void main(String[] args) {
        // Set cross-platform look and feel for consistent UI
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new MovieTicketBookingSystem().setVisible(true);
        });
    }
}

// ==========================================
// OBJECT ORIENTED MODELS (Package-Private)
// ==========================================

class User {
    private String username;
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
}

class Mall {
    private String name;
    private List<Movie> movies;

    public Mall(String name) {
        this.name = name;
        this.movies = new ArrayList<>();
    }
    public void addMovie(Movie m) { this.movies.add(m); }
    public String getName() { return name; }
    public List<Movie> getMovies() { return movies; }
}

//Encapsulation
class Movie {
    private String title;
    private String posterPath;
    private double price;
    private String screen;
    private boolean[][] seats; // 5 rows, 8 columns. True = booked, False = available

    public Movie(String title, String posterPath, double price, String screen) {
        this.title = title;
        this.posterPath = posterPath;
        this.price = price;
        this.screen = screen;
        this.seats = new boolean[5][8]; 
        
        // Randomly pre-book a few seats for demonstration
        seats[0][1] = true;
        seats[2][4] = true;
        seats[2][5] = true;
    }

    public String getTitle() { return title; }
    public String getPosterPath() { return posterPath; }
    public double getPrice() { return price; }
    public String getScreen() { return screen; }
    public boolean[][] getSeats() { return seats; }
}
