package sudokusolver;
import javax.swing.text.*;
//this filter is expressly for restricting input to digits 0-9
//and enforcing a one-character limit
//checking the integrety of the sudoku puzzle as a whole
//is handled by the event listener, not this filter

public class DigitFilter extends DocumentFilter {
    @Override
    public void insertString(FilterBypass fb, int offset, String text, 
            AttributeSet attr) throws BadLocationException {
        super.insertString(fb, offset, revise(text), attr);
    }
    @Override
    public void replace(FilterBypass fb, int offset, int length, String text,
                        AttributeSet attrs) throws BadLocationException {
    	 int currentLength = fb.getDocument().getLength();
    	 int overLimit = (currentLength + text.length()) - 1 - length;
         if (overLimit > 0) 
             text = text.substring(0, text.length() - overLimit);      
         super.replace(fb, offset, length, revise(text), attrs);
    }

    private String revise(String text) {
        StringBuilder builder = new StringBuilder(text);
        int index = 0;
        while (index < builder.length()) {
            if (accept(builder.charAt(index))) 
                index++; //only increment here
             else 
                builder.deleteCharAt(index);      
        }
        return builder.toString();
    }
    
    public boolean accept(final char c) {
        return Character.isDigit(c)&&(c!='0');
    }
}