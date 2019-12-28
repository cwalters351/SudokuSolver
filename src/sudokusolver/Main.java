package sudokusolver;
import java.awt.*;
import java.awt.event.*;
import java.io.*; 
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.*;  

public class Main {	
	static boolean attemptMode = false;
	static int Iholder; //to hold values for the text changed listener for the squares
	static int Jholder; //I doubt this is the ideal way to pass these values, but it works
	static int[][] board = {
			{0, 0, 0, 0, 0, 0, 0, 0, 0},  
            {0, 0, 0, 0, 0, 0, 0, 0, 0},  
            {0, 0, 0, 0, 0, 0, 0, 0, 0},  
            {0, 0, 0, 0, 0, 0, 0, 0, 0},  
            {0, 0, 0, 0, 0, 0, 0, 0, 0},  
            {0, 0, 0, 0, 0, 0, 0, 0, 0},  
            {0, 0, 0, 0, 0, 0, 0, 0, 0},  
            {0, 0, 0, 0, 0, 0, 0, 0, 0},  
            {0, 0, 0, 0, 0, 0, 0, 0, 0}};  
	static JFormattedTextField[][] squares = new JFormattedTextField[9][9];
	static JFrame f=new JFrame();//creating instance of JFrame
	static JButton at = new JButton("Attempt");
	static JButton cl = new JButton("Clear");
	public static void main(String[] args) {	
		int dvdrX = 10; //divider for sudoku board
		int dvdrY = 12; //divider for sudoku board
		Font sqFont = new Font("Arial", Font.BOLD, 20);

		for(int i = 0; i < 9; i++)//start with the sudoku squares
			for(int j = 0; j < 9; j++) {
				squares[i][j]=new JFormattedTextField();
				squares[i][j].setBounds(20 + 35*j +(j/3)*dvdrX, 20+40*i +(i/3)*dvdrY, 30,36); //formatted into subsquares
				//I know setbounds is generally bad
				//but for this application, it's pretty ideal
				squares[i][j].setFont(sqFont);
				squares[i][j].setVisible(true);
				Iholder=i; //For passing values, as per comments in definition
				Jholder=j;
				PlainDocument document = (PlainDocument) squares[i][j].getDocument();
			    document.setDocumentFilter(new DigitFilter()); //<--to ensure only a single digit is used
			    document.addDocumentListener(new DocumentListener() {//<--to ensure the puzzle is correct during user input
			    	 	public int x = Iholder; //<--so we can keep track
			    	 	public int y = Jholder; //<--of which text field triggered this
			    		public void changedUpdate(DocumentEvent e) {
			    		    changed();
			    		  }
			    		  public void removeUpdate(DocumentEvent e) {
			    		    changed();
			    		  }
			    		  public void insertUpdate(DocumentEvent e) {
			    		    changed();
			    		  }

			    		  public void changed() {
			    			  try {//make sure to ignore cases of \n, denoting an empty box
			    				  if((document.getText(0,1).length()==0)||(document.getText(0,1).compareTo("\n")==0)) {
			    					  //Handle empty square, update board to reflect
			    					  board[x][y]=0;
			    					  return;
			    				  }
			    				 int n = Integer.parseInt(document.getText(0, 1));
			    				 Runnable doVictory = new Runnable() { //must be threaded to work in conjunction
			                         @Override
			                         public void run() {at.setText("Victory!");}
			    				 };
			    				 Runnable doVictoryDlg = new Runnable() {
			                         @Override
			                         public void run() {JOptionPane.showMessageDialog(f, "You Won!");}
			    				 };
			    				 Runnable doAssist = new Runnable() {
			                         @Override
			                         public void run() {ClearSquare(x,y);}
			    				 };
			    				 Runnable doDialog = new Runnable() {
			                         @Override
			                         public void run() {JOptionPane.showMessageDialog(f, "That placement would break the sudoku puzzle");}
			    				 };
			    				if(!IsValid(n,x,y)) {//this entry breaks the sudoku puzzle
			    					SwingUtilities.invokeLater(doAssist);
			    					SwingUtilities.invokeLater(doDialog);
			    					
			    				}else {
			    					board[x][y]=n;//update board as well
			    				}
			    				//Victory Check
			    				if((attemptMode)&&(VictoryCheck())) {
			    					SwingUtilities.invokeLater(doVictory);
			    					SwingUtilities.invokeLater(doVictoryDlg);
			    				}
							} catch (HeadlessException e) {
								e.printStackTrace();
								return;
							} catch (BadLocationException e) {
								e.printStackTrace();
								return;
							}
			    		  }
			    		});
				f.add(squares[i][j]);
			}//End of edit field placement
				
		JButton b=new JButton("Solve");//creating instance of JButton  
		b.setBounds(135,410,100, 40);//setbounds is still bad, but provides nice alignment with the sudoku squares
		b.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				RunSolver();
			}
		});		
		f.add(b);//adding button in JFrame

		cl.setBounds(250,410,100, 40);//x axis, y axis, width, height
		cl.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ClearDisplay();
			}
		});
		f.add(cl);
		
		at.setBounds(20,410,100, 40);//x axis, y axis, width, height
		at.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ToggleMode();
			}
		});
		f.add(at);
		f.setMaximumSize(new Dimension(390,520));
		f.setSize(390,520);//400 width and 500 height  
		f.setLayout(null);//using no layout managers  
		f.setVisible(true);//making the frame visible
		
		JMenuBar menu = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		JMenuItem eMenuItem = new JMenuItem("Open");
	    eMenuItem.setMnemonic(KeyEvent.VK_E);
	    eMenuItem.setToolTipText("Exit application");
	    eMenuItem.addActionListener(new ActionListener() {

	    	 @Override
	    	 public void actionPerformed(ActionEvent arg0) {  //open file
					try {
						OpenFile();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
						return;
					} catch (IOException e) {
						e.printStackTrace();
						return;
					}
				}
			});
	     
	        fileMenu.add(eMenuItem);
	        menu.add(fileMenu);
	     f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	     f.setJMenuBar(menu);
	}
	public static void ToggleMode() {
		if(attemptMode) {			
			at.setText("Attempt"); //set captions
			cl.setText("Clear");		
			LoadSquares();
			ClearDisplay();//Clear display first, before turning off Attempt mode
			attemptMode=false;//or else all squares will be wiped
			for(int i=0;i<9;i++)
				for(int j=0;j<9;j++) {
					squares[i][j].setEditable(true);
					squares[i][j].setForeground(Color.BLACK);			
				}	
		}else {
			if(VictoryCheck()) {
				 JOptionPane.showMessageDialog(f, "Complete puzzle; there's nothing to solve!");
				 return;
			}
			attemptMode=true;
			at.setText("Give Up");
			cl.setText("Reset");
			for(int i=0;i<9;i++)
				for(int j=0;j<9;j++) 
					if(board[i][j]!=0) {
						squares[i][j].setForeground(Color.BLACK);
						squares[i][j].setEditable(false);
					}else
						squares[i][j].setForeground(Color.BLUE);
		}	
	}
	public static void OpenFile() throws IOException {
		JFileChooser chooser = new JFileChooser();
	    FileNameExtensionFilter filter = new FileNameExtensionFilter("Text files", "txt");
	    chooser.setFileFilter(filter);
	    int returnVal = chooser.showOpenDialog(f);
	    if(returnVal != JFileChooser.APPROVE_OPTION)//user cancelled dialog
	    	return;
	    //file reading
	    File file = new File(chooser.getSelectedFile().getAbsolutePath()); 
		BufferedReader br = new BufferedReader(new FileReader(file)); 
	    String st; 
	    int i = 0;
	    ClearDisplay(); //Make sure we write on a clean board
	    while ((i<9)&&(st = br.readLine()) != null) {
	    	//System.out.println(st); 
	    	String[] li = st.split(",");
	    	for(int j = 0;j<9;j++) {					
	    		if(li[j].startsWith("0", 0))
	    			squares[i][j].setText("");
	    		else
	    			squares[i][j].setText(li[j].trim()); //trim for sanitation
	    	  }
	    	  i++;
	      }
	      br.close();
	}
	
	public static void RunSolver() {
		if(!attemptMode) {//do not solve if user is attempting puzzle
			LoadSquares(); //load the board from gui textfields 
			if(Solve(0,0)) {
				//Solved
				DisplaySolution();
			}else 
				JOptionPane.showMessageDialog(f, "No solution exists for this puzzle");
		} else 
			JOptionPane.showMessageDialog(f, "Solving disabled while attempt underway");
	}
	public static void ClearSquare(int x, int y) {
		if((attemptMode)&(squares[x][y].isEditable()))
			squares[x][y].setForeground(Color.BLUE);
		else
			squares[x][y].setForeground(Color.BLACK);
		squares[x][y].setText("");
		board[x][y]=0;
	}
	public static void ClearDisplay() {
		if(!attemptMode)
			for(int i=0;i<9;i++) 
				for(int j=0;j<9;j++)
					ClearSquare(i,j);
		else{
			for(int i=0;i<9;i++) 
				for(int j=0;j<9;j++) {
					if(squares[i][j].isEditable()) {
						ClearSquare(i,j);
					squares[i][j].setForeground(Color.BLUE);
				}
			}
		}
	}
	public static void DisplaySolution() {
		for(int i=0;i<9;i++) 
			for(int j=0;j<9;j++) {
				if(squares[i][j].getText().length()==0) { //checking if empty square
					squares[i][j].setForeground(Color.GREEN);
					squares[i][j].setText(Integer.toString(board[i][j]));
					
				}
			}
				
	}
	public static void LoadSquares() {
		for(int i=0;i<9;i++)
			for(int j=0;j<9;j++) {
				if(squares[i][j].getText().length()==0) //since 0 are represented by blank fields
					board[i][j]=0;  //check for this case and set 0's manually
				else
					board[i][j] = Integer.parseInt(squares[i][j].getText());	
			}
	}
	public static boolean VictoryCheck() {
		//returns true if all squares are filled in attempt mode.
		boolean vFlag = true;//assume true, prove otherwise
		for(int i=0;i<9;i++)
			for(int j=0;j<9;j++)
				if(board[i][j]==0)
					vFlag=false;
		return vFlag;
	}
	public static boolean Solve(int x, int y) {
		boolean f; 
		if(y>8) {
			y=0;
			x++;
		}
		if(x>8)
			return true;
		if(board[x][y]!=0)//check to see if this square is empty
			f=Solve(x,y+1);//if it is, move on to the next square
		else {  //f is stored so a success can be passed back to the root of recursion
			for(int i = 1; i<10;i++) {
				if(IsValid(i,x,y)) {//check for validity
					board[x][y]=i;
					if(Solve(x,y+1))//do the next one
						return true;
					else
						board[x][y]=0;//reset when done
				}
			}//if nothing else is valid, return false.
			return false;
		}
		return f; //f will always be initialized by this line
	}
	public static boolean IsValid(int n, int x, int y) {
		int fx = 6;//start and end values for subsquares
		int lx = 9;//assume last square (bottom right)
		int fy = 6;//and check for lower ones below
		int ly = 9;
		for(int i = 0; i < 9; i++) { //check the row
			if((board[i][y] == n)&&(i!=x))  
				return false; //return false if value already used
		}
		for(int i = 0; i < 9; i++) {//check the column
			if((board[x][i] == n)&&(i!=y)) 
				return false;  //return false if value already used
		}
		//set up parameters for subsquare check
		if(x < 3) {
			fx = 0;
			lx = 3;
		}else if(x < 6) {
			fx = 3;
			lx = 6;
		}
		if(y < 3) {
			fy = 0;
			ly = 3;
		}else if(y < 6) {
			fy = 3;
			ly = 6;
		}
		//run the loop, with parameters in place
		for(int i = fx;i<lx;i++)
			for(int j = fy;j<ly;j++) 
				if((board[i][j] == n)&&(i!=x)&&(j!=y)) {
					return false;
			}		
		return true;
	}
}
