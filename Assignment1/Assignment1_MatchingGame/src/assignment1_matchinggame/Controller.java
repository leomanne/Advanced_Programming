/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package assignment1_matchinggame;

/**
 *
 * @author xab
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import javax.swing.JLabel;
import javax.swing.Timer;

/**
 * Controller is a bean that extends JLabel
 * It implements VetoableChangeListener to block illegal flips
 * PropertyChangeListener to detect flips and handle matching
 * Also listens to Board's shuffle event to reset counters.
 */
public class Controller extends JLabel implements VetoableChangeListener, PropertyChangeListener {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    // N of board
    private int totalPairs = 4; //default
    // number of matched pairs
    private int pairsFound;      
    // total number of  flips
    private int flipsCount;            
    // best flips to complete a game
    private int bestScore;         
    // first and second card selected
    private Card firstCard;              
    private Card secondCard;   
    
    // labels showing flips count and best score
    private  JLabel counterLabel = null;   
    private  JLabel challengeLabel = null; 
    
    private Timer matchTimer;            // timer for 0.5s delay in checking match

    // Flag to allow forced resets
    private boolean resetMode = false;

    public Controller() {
    }

    public Controller(int totalPairs, JLabel counterLabel, JLabel challengeLabel) {
        super("Pairs found: 0");
        this.totalPairs = totalPairs;
        this.counterLabel = counterLabel;
        this.challengeLabel = challengeLabel;
        this.pairsFound = 0;
        this.flipsCount = 0;
        this.bestScore = 0;  // 0 since it is impossible is used to know wheter the game is over
        this.firstCard = null;
        this.secondCard = null;
        this.matchTimer = null;
    }

    /**
     * 
     * @param evt
     * @throws PropertyVetoException 
     * @effect block illegal flips except during shuffles since the force mode is enabled.
     */
    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if (!"state".equals(evt.getPropertyName())) return;

        Card.State oldS = (Card.State) evt.getOldValue();
        Card.State newS = (Card.State) evt.getNewValue();

           //if we are in reset mode we do not veto it
        if (!resetMode) {
            if (oldS == Card.State.FACE_UP && newS == Card.State.FACE_DOWN) {
                throw new PropertyVetoException("Cannot flip a face-up card down (user action).", evt);
            }
            if (oldS == Card.State.EXCLUDED && newS == Card.State.FACE_DOWN) {
                throw new PropertyVetoException("Cannot flip an excluded card down.", evt);
            }
            if (newS == Card.State.FACE_UP && firstCard != null && secondCard != null) {
                throw new PropertyVetoException("Cannot flip a third card up (two are already face up).", evt);
            }
        }
    }


    /**
     * 
     * @param evt 
     * @effect handle "shuffledValues" reset and card flips
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String prop = evt.getPropertyName();
        
        // If Board sends "shuffledValues", reset the round.
        if ("shuffledValues".equals(prop)) {
            resetMode = true;
            resetGame();
            resetMode = false;
            return;
        }

        // When a Card changes "state", process face up flips.
        if ("state".equals(prop)) {
            Card card = (Card) evt.getSource();
            Card.State newS = (Card.State) evt.getNewValue();
            if (newS == Card.State.FACE_UP) {
                flipsCount++;
                if (counterLabel != null) {
                    counterLabel.setText(String.valueOf(flipsCount));
                }
                if (firstCard == null) {
                    firstCard = card;
                } else if (secondCard == null) {
                    secondCard = card;
                    // When two cards are face-up, schedule a check after 0.5 seconds.
                    if (matchTimer != null) {
                        matchTimer.stop();
                    }
                    matchTimer = new Timer(500, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            checkMatch();
                        }
                    });
                    matchTimer.setRepeats(false);
                    matchTimer.start();
                }
            }
        }
    }

    
    /**
     * 
     * @param flag 
     * @effect set the reset mode to flag value
     */
    public void setResetMode(boolean flag) {
        this.resetMode = flag;
    }

    /**
     * @effect Checks whether the two face up cards match. If they match, the cards become EXCLUDED otherwise, they are turned back face down.
     * 
     */
    private void checkMatch() {
        if (firstCard == null || secondCard == null) return;

        boolean isMatch = (firstCard.getValue() == secondCard.getValue());
        if (isMatch) {
            pairsFound++;
            setText("Pairs Found: " + pairsFound);
        }

        //force the reset mode so that veto dont block the forced changes
        setResetMode(true);
        pcs.firePropertyChange("matched", null, isMatch);
        setResetMode(false);

        firstCard = null;
        secondCard = null;
    }


    /**
     * @effect Resets the game counters and temporary state when a new round is started.
     */
    private void resetGame() {
        pairsFound = 0;
        flipsCount = 0;
        this.setText("0");
        if (counterLabel != null) {
            counterLabel.setText("0");
        }
        if (matchTimer != null) {
            matchTimer.stop();
            matchTimer = null;
        }
        firstCard = null;
        secondCard = null;
    }
    /**
     * 
     * @param listener 
     * @effect add a listener 
     */
    public void addMatchedListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener("matched", listener);
    }

    /**
     * 
     * @param listener
     * @effect add a listener
     */
    public void removeMatchedListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener("matched", listener);
    }

}
