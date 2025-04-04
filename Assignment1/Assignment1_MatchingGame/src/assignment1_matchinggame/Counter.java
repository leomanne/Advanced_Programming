/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package assignment1_matchinggame;

/**
 *
 * @author xab
*/

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JLabel;
import java.io.Serializable;

/**
 * Counter is a Bena that extends Jlabel and implements PropertyChangeListener
 * 
 */
public class Counter extends JLabel implements PropertyChangeListener, Serializable {

    private int count; // N_ of flips 

    //empty constructor
    public Counter() {
        super();
        count = 0;
        updateText();
    }

    /**
     * @effect increment the number of flips
     *
     */
    public void increment() {
        count++;
        updateText();
    }

    /**
     * @effect Reset the flips (new game or shuffle)
     */
    public void reset() {
        count = 0;
        updateText();
    }

    /**
     * @effect return count
     */
    public int getCount() {
        return count;
    }

    /**
     * @effect set the text of the label with the number of flips
     */
    private void updateText() {
        this.setText("Flips: " + count);
    }

    /**
     * @param evt 
     * @effect Handle the event received, if "flips" then increment the counter, else it reset it to 0
     * 
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String prop = evt.getPropertyName();
        if ("flip".equals(prop)) {
            increment();
        } else if ("restart".equals(prop)) {
            reset();
        }
    }
}
