/**
 * The package contains classes to generate an applet collaborative
 * Copyright (c) Dec 2007 C. Jara and F. Candelas
 * @author C. Jara and F. Candelas (http://www.aurova.ua.es).
 */

package org.colos.ejs.library.collaborative;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


/**
 * Connection interface in the student applet
 */

public class QuestionStudentTool {
	
	//Actions
	private static final String strAccept = "Accept";
	private static final String strCancel = "Cancel";

	//Object and default dimension
	private static QuestionStudentTool QUESTION_TOOL=null;
	private static Dimension defaultSize = new Dimension (300, 200);

	//Dimension frame
	private Dimension dim;
	
	//Simulation Object
	protected SimulationCollaborative sim;
	
	//Swing Components
	protected JFrame mainFrame;
	protected JButton acceptButton;
	protected JButton cancelButton;
	protected JLabel question, userLabel;
	protected JTextField userField;
	protected JPanel panel,panelUser;
    protected JTextArea statusArea;
    protected JScrollPane panelArea;
    
    //Connection Parameters
	protected ArrayList<String> _listParam = new ArrayList<String>();
    
    
    //Thread connection
    protected Receiver client;
    protected Vector<DataSocket> vector = new Vector<DataSocket>(); 
    
    
    
   /**
   * Constructor.
   */
    public QuestionStudentTool() {
	    this(defaultSize,null,null);
	}

    
    /**
    * Constructor with parameters
    * @param dim Dimension 
    * @param sim SimualationCollaborative Simulation for collaboration
    * @param _listP ArrayList List of parameters to connect with the master
    */
	public QuestionStudentTool(Dimension dim, SimulationCollaborative sim, ArrayList<String> _listP) {
	    this.dim = dim;
	    this.sim = sim;
	    this._listParam = _listP;
	    createGUI();
    }
	
	
	
	// ---------------------------
	// Methods to get the tool
	// ---------------------------
	/**
	* Method to get the interface
	* @param sim SimualationCollaborative Simulation for collaboration
	* @param _listP ArrayList List of parameters to connect with the master
	* @return QuestionStudentTool Object Interface
	*/
	protected static QuestionStudentTool getTool(SimulationCollaborative sim, ArrayList<String> _listP) 
    {
	   if (QUESTION_TOOL==null){QUESTION_TOOL= new QuestionStudentTool(defaultSize,sim,_listP);}
	   return QUESTION_TOOL;
	}

	
	/**
	* Method to get the interface
	* @param dim Dimension
	* @param sim SimualationCollaborative Simulation for collaboration
	* @param _listP ArrayList List of parameters to connect with the master
	* @return QuestionStudentTool Object Interface
	*/
	protected static QuestionStudentTool getTool(Dimension dim, SimulationCollaborative sim, ArrayList<String> _listP) 
	{
	   if (QUESTION_TOOL==null) QUESTION_TOOL = new QuestionStudentTool(dim,sim,_listP);
		   return QUESTION_TOOL;
	} 
	// ---------------------------
	// End Methods to get the tool
	// ---------------------------
	
	
	
	/**
	* Interface creation
	*/
	private void createGUI() 
	{
		//Frame
		JFrame.setDefaultLookAndFeelDecorated(false);
		mainFrame = new JFrame();
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mainFrame.setSize(this.dim);
        mainFrame.setResizable(false);
        
        //Buttons
        acceptButton = new JButton("Accept");
        acceptButton.setHorizontalAlignment(JButton.CENTER);
        acceptButton.setActionCommand(strAccept);
        
        cancelButton = new JButton("Cancel");
        cancelButton.setHorizontalAlignment(JButton.CENTER);
        cancelButton.setActionCommand(strCancel);
		cancelButton.setEnabled(false);
        
        //Labels
        question = new JLabel();
        question.setForeground(new Color (255,0,0));
        question.setFont(new Font("Times", Font.ITALIC, 13));
        question.setText("Do you want to take a virtual class?");
        question.setHorizontalAlignment(JLabel.CENTER);
        userLabel = new JLabel("User");
        userLabel.setHorizontalAlignment(JLabel.CENTER);
        
        //TextField
        userField = new JTextField();
        
        //Panel and Layout
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        //Add the controls to the panel
        panel.add(question, BorderLayout.CENTER);
        
        //PanelUser
        panelUser = new JPanel();
        panelUser.setLayout(new GridLayout(2,2));
        panelUser.add(userLabel);
        panelUser.add(userField);
        panelUser.add(acceptButton);
        panelUser.add(cancelButton);
        panelUser.setBorder(
        	      BorderFactory.createCompoundBorder(      
              		      		BorderFactory.createEmptyBorder(10,10,10,10),
              		      			panelUser.getBorder()));
        
        //Text Area
        statusArea = new JTextArea();
        statusArea.setFont(new Font("Courier", Font.BOLD, 10));
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);
        
        //ScrollPane
        panelArea = new JScrollPane(statusArea);
        panelArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panelArea.setPreferredSize(new Dimension(150,80));
        panelArea.setBorder(
  	      BorderFactory.createCompoundBorder(
    		       BorderFactory.createCompoundBorder(
      		         BorderFactory.createTitledBorder("Communication channel"),
        		      BorderFactory.createEmptyBorder(5,5,5,5)),
          		 panelArea.getBorder()));
       
        //Control Listener
        MyActionListener myListener = new MyActionListener(mainFrame,statusArea,userField);
        acceptButton.addActionListener(myListener);
        cancelButton.addActionListener(myListener);
        
        mainFrame.getContentPane().add(panel, BorderLayout.NORTH);
        mainFrame.getContentPane().add(panelUser, BorderLayout.CENTER);
        mainFrame.getContentPane().add(panelArea, BorderLayout.SOUTH);
        
        Dimension d = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        mainFrame.setLocation((d.width-defaultSize.width)/2,(d.height-defaultSize.height)/2);
	}

	
	// ---------------------------
	// Protected Methods
	// ---------------------------
	/**
	* Set the text in the JTextArea of the interface
	* @param text String
	*/
	protected void setText(String text){
		statusArea.append("\n");
		statusArea.append(text);
	}
	
	
	/**
	* Get the Client Thread
	* @return Receiver The Thread receiver
	*/
	protected Receiver getReceiver(){
		return client;
	}
	
	
	/**
	* Set visible the interface
	* @param boolean True: visible the interface, False: not visible the interface
	*/
	protected void setVisible(boolean visible) 
	{
		mainFrame.setVisible(visible);
    }
	// ---------------------------
	// End Protected Methods
	// ---------------------------
	
	
	
	// --------------------------------------------
    // Private classes to manage the control events
	// --------------------------------------------
	private class MyActionListener implements ActionListener
	{
	    JTextArea _panelArea;
	    	
	    MyActionListener(JFrame frame, JTextArea area, JTextField userField){
	    	this._panelArea = area;
	    }
	    public void actionPerformed(ActionEvent e) 
	    {
	    	String action = e.getActionCommand();
	    	if(action.equals(QuestionStudentTool.strAccept)){
	    		if(userField.getText().equals("")){
	    			JOptionPane.showMessageDialog(mainFrame,"Student name","Error in the name", JOptionPane.ERROR_MESSAGE);
	   			}
	    		else{
    				_panelArea.setText("Connecting with the teacher..");
    				acceptButton.setEnabled(false);
    				cancelButton.setEnabled(true);
    				userField.setEditable(false);
    				sim.disconnectControls();
    				client = new Receiver(vector, userField.getText(), QuestionStudentTool.QUESTION_TOOL);
    				client.connect(_listParam.get(0), Integer.valueOf(_listParam.get(1)));
    				System.out.println(_listParam.get(0)+ " "+Integer.valueOf(_listParam.get(1)));
    				client.setPriority(Thread.MAX_PRIORITY);
    				client.start();
	   			}
	   		}
	   		else if(action.equals(QuestionStudentTool.strCancel)){
	   			_panelArea.append("\nDisconnecting...");
    			client.disconnect();
				userField.setEditable(true);
				cancelButton.setEnabled(false);
				acceptButton.setEnabled(true);
				//Chalk
				if(sim.getChalk())
					sim.setChalk(false);
			
				sim.connectControls();
	    	}
	   	}
	}
	// --------------------------------------------
    // Private classes to manage the control events
	// --------------------------------------------
	
}
