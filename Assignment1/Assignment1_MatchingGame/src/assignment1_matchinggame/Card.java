/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package assignment1_matchinggame;

/**
 *
 * @author xab
 */
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import javax.swing.JButton;

/**
 * Card is a bean that extends JButton implementing PropertyChangeListener.
 * it has 2 variables that represent the value of the card hidden and the state (the state is the enum State with FACE_DOWN, FACE_UP, EXCLUDED)
 * Also has an index property to know where it is in the board.
 * Listens to the Board'sshuffledValues property changes to update when required
 */
public class Card extends JButton implements PropertyChangeListener {
    
    //enum to represent states
    public static enum State { FACE_DOWN, FACE_UP, EXCLUDED } 
    

    //used to handle the events shuffledValues or matched from the controller (registered as listener in board)
    private PropertyChangeSupport pcs= new PropertyChangeSupport(this);
    //used to fire the event state and value veto to the controller.
    private VetoableChangeSupport vcs= new VetoableChangeSupport(this);
    
 
     // index of the board grid
    private int index;  
    
    private int value;
    
    private State state;
    
   
    
    public Card() {
        this(-1);
    }
    
    /**
     * 
     * @param idx index of the card
     * constructor used to initialize a card to face down
     */
    public Card(int idx) {
        super();
        this.index = idx;
        this.value = 0;
        this.state = State.FACE_DOWN;
        
        updateAppearance();
        
        // adds a ActionListener to change the state on clcick
        this.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (state == State.FACE_DOWN) {
                    try {
                        setState(State.FACE_UP);
                    } catch (PropertyVetoException ex) {
                        // Cambio vetoato
                    }
                } else {
                    try {
                        setState(State.FACE_DOWN);
                    } catch (PropertyVetoException ex) {
                        // Cambio vetoato
                    }
                }
            }
        });
    }
    
    /**
     * 
     * @param idx index of the card
     * @param value initial value of the card
     */
    public Card(int idx, int value) {
        this(idx); //use the previous constructor
        try {
            setValue(value);
        } catch (PropertyVetoException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * 
     * @return index
     */
    public int getIndex() {
        return index;
    }
    
    
    /**
     * 
     * @return the value of the card 
     */
    public int getValue() {
        return value;
    }
    
    /**
     * 
     * @param newValue
     * @throws PropertyVetoException 
     * @effect set the new value of the card. first we sent a vetoable event "value", then a propertychange value to the listeners
     */
    public void setValue(int newValue) throws PropertyVetoException {
        int oldValue = this.value;
        if (oldValue == newValue) return;
        
        vcs.fireVetoableChange("value", oldValue, newValue);
        
        this.value = newValue; //if no exception then update the value
        
        pcs.firePropertyChange("value", oldValue, newValue); //let the listener know of the change
        
        if (state == State.FACE_UP || state == State.EXCLUDED) {
            setText(String.valueOf(newValue));
        }
    }
    
    /**
     * 
     * @return the state of the card
     */
    public State getState() {
        return state;
    }
    /**
     * 
     * @param newState
     * @throws PropertyVetoException 
     * @effect similar to setValue update the state of the card. first we sent a vetoable event "state", then a propertychange "state" to the listeners
     */
    public void setState(State newState) throws PropertyVetoException {
        State oldState = this.state;
        if (oldState == newState) return;

        vcs.fireVetoableChange("state", oldState, newState);
        this.state = newState; //if no exception then update the value
        pcs.firePropertyChange("state", oldState, newState);

        updateAppearance();

        //if card is going from a face down a face up we emit a property change "flip" to the listeners
        if (oldState != State.FACE_UP && newState == State.FACE_UP) {
            pcs.firePropertyChange("flip", false, true);
        }
    }

    
    /**
     * @effect update the background color and the text of the card
     */
    private void updateAppearance() {
        switch (state) {
            case FACE_DOWN:
                setBackground(Color.GREEN);
                setText("");
                break;
            case FACE_UP:
                setBackground(Color.WHITE);
                setText(String.valueOf(value));
                break;
            case EXCLUDED:
                setBackground(Color.RED);
                setText(String.valueOf(value));
                break;
        }
        setOpaque(true);
    }
    
    
    /**
     * 
     * @param l
     * @effect add a property change listener
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (pcs == null) {
            super.addPropertyChangeListener(l);
        } else {
            pcs.addPropertyChangeListener(l);
        }
    }
    
    /**
     * 
     * @param l
     * @effect remove a property change listener
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (pcs == null) {
            super.removePropertyChangeListener(l);
        } else {
            pcs.removePropertyChangeListener(l);
        }
    }
    
    /**
     * 
     * @param l
     * @effect add a veto listener
     */
    @Override
    public void addVetoableChangeListener(VetoableChangeListener l) {
        if (vcs == null) {
        } else {
            vcs.addVetoableChangeListener(l);
        }
    }
    
    /**
     * 
     * @param l
     * @effect remove a veto listener
     */
    @Override
    public void removeVetoableChangeListener(VetoableChangeListener l) {
        if (vcs == null) {
        } else {
            vcs.removeVetoableChangeListener(l);
        }
    }
    
    /**
     * @param evt
     * @effect it responds to the changes of shufflevalues sent by the board, it reset the value and the state.
     *         it respinds to the matched sent by tge controller. 
     * 
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if ("shuffledValues".equals(propName)) {
            int[] newArr = (int[]) evt.getNewValue();
            if (index >= 0 && index < newArr.length) {
                try {
                    setValue(newArr[index]);
                    setState(State.FACE_DOWN);
                } catch (PropertyVetoException ex) {
                }
            }
        } else if ("matched".equals(propName)) {
                //only for the face up card
                if (state == State.FACE_UP) {
                boolean isMatch = (Boolean) evt.getNewValue();
                try {
                    //if there is a match become excluded otherwise face down
                    setState(isMatch ? State.EXCLUDED : State.FACE_DOWN);
                } catch (PropertyVetoException ex) {
                        ex.printStackTrace();
                }
            }
        }
    }

    
    /**
     * @effect set the state of the card to face down
     */
    public void hideCard() {
        try {
            setState(State.FACE_DOWN);
        } catch (PropertyVetoException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * 
     * @return true if the state is face up, false otherwise
     */
    public boolean isFaceUp() {
        return state == State.FACE_UP;
    }
    /**
     * 
     * @return true if the state is excluded, false otherwise
     */
    public boolean isExcluded() {
        return state == State.EXCLUDED;
    }
}
