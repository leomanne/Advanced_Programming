/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package assignment1_matchinggame;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JOptionPane;

/**
 *
 * @author xab
 */
public class Board extends javax.swing.JFrame implements PropertyChangeListener {

    //for bound properties
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    // Number of pairs 
    public static final int N = 4;  
    // Array that changes on a shuffle action
    private int[] shuffledValues;   

    // Array of Card objects representing the game cards
    private Card[] cards;           
    // Best score (minimum number of flips)
    private int bestScore = Integer.MAX_VALUE;


    
    public Board() {
        super("Matching Pairs Game");
        initComponents();
        
        // Set layout for jPanel1: 2 rows, 4 columns
        jPanel1.setLayout(new GridLayout(2, 4, 10, 10));
        
        // Set window size, center location, and default close operation
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        // Initialize the array of cards and generate shuffled card values
        cards = new Card[2 * N];
        ArrayList<Integer> cardValues = generateCardValues();
        
        //for every card
        for (int i = 0; i < 2 * N; i++) {
            // Create a new Card with an index and its corresponding value
            Card card = new Card(i, cardValues.get(i));
            card.setPreferredSize(new Dimension(70, 100));

            // Add vetoable and property change listeners to the card for controlling property updates
            card.addVetoableChangeListener(controller2);
            card.addPropertyChangeListener(controller2);
            
            
            // Register the card with the controller for handling the "matched" event
            controller2.addMatchedListener(card);

            // The Card listens to the "shuffledValues" event fired by Board via the pcs property
            pcs.addPropertyChangeListener(card);
            
            // The controller listens to the "shuffledValues" event to manage reset behavior
            pcs.addPropertyChangeListener("shuffledValues", controller2);

            // The Counter listens to the "flip" event emitted by the Card to update flip count
            card.addPropertyChangeListener(counter1);
            
            // The Board listens to the "matched" event from the Card to update game status (e.g., "Match Found!" or "Mismatch!")
            card.addPropertyChangeListener(this);

            // Add the card to the cards array and to the panel (jPanel1)
            cards[i] = card;
            jPanel1.add(card);
        }

        // Configure the Shuffle button to restart the game
        jButton1.addActionListener(e -> shuffleGame());

        // Configure the Exit button to close the application with a confirmation dialog
        jButton2.setText("Exit");
        jButton2.addActionListener(e -> {
            int response = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to exit?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION
            );
            if (response == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        // Set the initial texts for the labels: jLabel1 shows the game state and jLabel2 shows the best score
        jLabel1.setText("Matching Game");
        jLabel2.setText("Challenge: --");

        pack();
        setVisible(true);
    }

    /**
     * @effect Generates card values and shuffle them
     */
    private ArrayList<Integer> generateCardValues() {
        ArrayList<Integer> values = new ArrayList<>();
        for (int i = 1; i <= N; i++) {
            values.add(i);
            values.add(i);
        }
        Collections.shuffle(values);
        return values;
    }

    /**
     * @effect Handles game restart (Shuffle). If the game is finished, updates the bestScore. it Fires the "shuffledValues" property change event with new values.
     * Hides all the cards, Resets the flip counter, Notifies the controller about the restart event.
     */
    private void shuffleGame() {
        if (gameFinished()){
            int flips = counter1.getCount();
            // Update bestScore if current number of flips is lower than bestScore
            if (bestScore == Integer.MAX_VALUE || flips < bestScore) {
                bestScore = flips;
            }
            jLabel1.setText("Game Completed in " + flips + " flips!");
            // Display "--" if bestScore has not yet been set, otherwise display the bestScore            
            jLabel2.setText("Challenge: " + (bestScore == Integer.MAX_VALUE ? "--" : bestScore));
        }

        // Generate new shuffled values 
        ArrayList<Integer> newValuesList = generateCardValues();
        int[] newValues = new int[newValuesList.size()];
        for (int i = 0; i < newValuesList.size(); i++) {
            newValues[i] = newValuesList.get(i);
        }
        // Fire the "shuffledValues" event to notify all Cards to update their values and state
        pcs.firePropertyChange("shuffledValues", null, newValues);

        // Enable reset mode, hide each card, and then disable reset mode in the controller
        controller2.setResetMode(true);
        for (Card c : cards) {
            c.hideCard();
        }
        controller2.setResetMode(false);

        jLabel1.setText("Game Restarted");
        counter1.reset();
    }




    /**
     * @effect Checks if the game is finished.
     * The game is finished when all cards are either face up or excluded.
     *
     * @return true if the game is finished; false otherwise.
     */
    private boolean gameFinished() {
        for (Card c : cards) {
            if (!c.isFaceUp() && !c.isExcluded()) {
                return false;
            }
        }
        return true;
    }

    /**
     * @effect Handles the "matched" property change event emitted by the Cards. 
     * Updates the status label to show "Match Found!" or "Mismatch!" and, if the game is finished,
     * updates the bestScore and displays the final flip count.
     *
     * @param evt the property change event.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("matched".equals(evt.getPropertyName())) {
            boolean match = (Boolean) evt.getNewValue();
            if (match) {
                jLabel1.setText("Match Found!");
            } else {
                jLabel1.setText("Mismatch!");
            }
            if (gameFinished()) {
                int flips = counter1.getCount();
                jLabel1.setText("Game Completed in " + flips + " flips!");
                if (flips < bestScore) {
                    bestScore = flips;
                }
                jLabel2.setText("Challenge: " + bestScore);
            }
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

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        controller2 = new assignment1_matchinggame.Controller();
        counter1 = new assignment1_matchinggame.Counter();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("jLabel1");

        jLabel2.setText("jLabel2");

        jButton1.setText("Shuffle");

        jButton2.setText("jButton2");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 216, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(105, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addGap(31, 31, 31)
                .addComponent(jLabel2)
                .addGap(32, 32, 32)
                .addComponent(jLabel1)
                .addGap(31, 31, 31)
                .addComponent(counter1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(jButton2)
                .addGap(73, 73, 73)
                .addComponent(controller2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(102, 102, 102))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jButton1)
                    .addComponent(jButton2)
                    .addComponent(controller2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(counter1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
            java.util.logging.Logger.getLogger(Board.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Board.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Board.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Board.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Board().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private assignment1_matchinggame.Controller controller2;
    private assignment1_matchinggame.Counter counter1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
