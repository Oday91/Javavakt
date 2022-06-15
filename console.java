package Javavakt;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.swing.event.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.*;
import java.io.*;
import java.text.SimpleDateFormat;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.*;
import javax.swing.border.*;
import javax.swing.Timer;
import javax.swing.plaf.FontUIResource;

public class console extends JFrame implements TableModelListener, WindowListener, ClipboardOwner  {
	static final long serialVersionUID = 42L;
  
	private final static int screenwidth = Toolkit.getDefaultToolkit().getScreenSize().width;
	private final static int screenheight =Toolkit.getDefaultToolkit().getScreenSize().height;
  
	private String version = "Version = Jvakt Console 2.6"; 
  
	private JPanel topPanel;
  
	  private JTable table;
	  
	  private JScrollPane scrollPane;
	  
	  private JButton bu1;
	  
	  private JTableHeader header;
	  
	  private consoleDM wD;
	  
	  private Boolean swAuto = Boolean.valueOf(true);
	  
	  private Boolean swRed = Boolean.valueOf(true);
	  
	  private Boolean swDBopen = Boolean.valueOf(true);
	  
	  private Boolean swServer = Boolean.valueOf(true);
	  
	  private Boolean swDormant = Boolean.valueOf(true);
	  
	  private String jvhost = "127.0.0.1";
	  
	  private String jvport = "1956";
	  
	  private int port = 1956;
	  
	  private String cmdHst = "java -cp console.jar;postgresql.jar Jvakt.consoleHst";
	  
	  private String cmdSts = "java -cp console.jar;postgresql.jar Jvakt.consoleSts";
	  
	  private String cmdStat = "java -cp console.jar;postgresql.jar Jvakt.StatisticsChartLauncher";
	  
	  private int deselectCount = 0;
  
	  private int jvconnectCount = 0;
	  
	  private String infotxt;
	  
	  public static void main(String[] args) throws IOException {
		console mainFrame = new console();  // gör objekt av innevarande class 
		mainFrame.showWindow();
	  }
	  
	  
  
	  private void showWindow() {
		  pack();				// kallar på innevarande class metod pack som ärvts via Jframe 
		  setVisible(true); 	// kallar på innevarande class metod setVisible och nu visas fönster för användaren
	  }

	  public static BufferedImage makeAndGetContentPaneScreenshotFromJFrame(JFrame frame) {
		  
		  	
			Point p = new Point(frame.getLocation());
			Insets deco = frame.getInsets();
			int menuBarHeight = frame.getJMenuBar() == null ? 0:frame.getJMenuBar().getHeight();
			Point newPoint = new Point(p.x+deco.left, p.y+deco.top+menuBarHeight);
			
			Dimension d = frame.getPreferredSize();		
			Dimension newDimension = new Dimension(d.width-deco.left-deco.right, d.height-deco.bottom-deco.top-menuBarHeight);
			BufferedImage image = null;
			try {
				Robot robot = new Robot();
				image = robot.createScreenCapture(new Rectangle(newPoint, newDimension));
			} catch (AWTException e) {
				e.printStackTrace();
			}
			return image;
			
		}
	  
	public console() throws IOException {
			setTitle(version);	  	
		  	ImageIcon img = new ImageIcon("console.png");
		    setIconImage(img.getImage());
		    getProps();
		    this.port = Integer.parseInt(this.jvport);
			// get 1/2 of the height, and 5/6 of the width
			int height = screenheight * 1 / 2;
		 	int width = screenwidth * 5 / 6;
		    int x = (int) ((screenwidth  - width) / 2);
		    int y = (int) ((screenheight - height) / 2);
		    this.setPreferredSize(new Dimension(width, height)); // set the jframe height and width
		    this.setLocation(x,y);
			this.setBackground(Color.gray);
			this.setUndecorated(false);
	    
			buildMenu();
			buildContentpane();
			
			Timer timer = new Timer(2500, new ActionListener() {
		    	public void actionPerformed(ActionEvent e) {
		    		if (console.this.deselectCount > 10) {
		    			console.this.table.getSelectionModel().clearSelection();
		    			console.this.deselectCount = 0;
		            } 
		            console.this.deselectCount = console.this.deselectCount + 1;
		            if (console.this.swAuto.booleanValue()) {
		            	console.this.jvconnectCount = console.this.jvconnectCount + 1;
		            	if (console.this.jvconnectCount > 5 || !console.this.swServer.booleanValue()) {
		            		console.this.jvconnectCount = 0;
		            		try {
		            			console.this.swServer = Boolean.valueOf(true);
		            			SendMsg jm = new SendMsg(console.this.jvhost, console.this.port);
		            			String oSts = jm.open();
		            			if (oSts.startsWith("failed")) {
		            				console.this.swServer = Boolean.valueOf(false); 
		            			}
		            			if (oSts.startsWith("DORMANT")) {
		            				console.this.swDormant = Boolean.valueOf(true);
		            			} else {
		            				console.this.swDormant = Boolean.valueOf(false);
		            			} 
		            			jm.close();
		            		} catch (NullPointerException npe2) {
		            			console.this.swServer = Boolean.valueOf(false);
		            			System.out.println("-- Rpt Failed 2 --" + npe2);
		            		} 
		            	} 
		            	console.this.swDBopen = Boolean.valueOf(console.this.wD.refreshData());
		            	console.this.setBu1Color();
		            	if (console.this.swRed.booleanValue()) {
		            		console.this.scrollPane.setBorder(new LineBorder(Color.RED));
		            	} else {
		            		console.this.scrollPane.setBorder(new LineBorder(Color.CYAN));
		            	} 
		            	console.this.swRed = Boolean.valueOf(!console.this.swRed.booleanValue());
		            	console.this.scrollPane.setVerticalScrollBarPolicy(21);
			            console.this.scrollPane.validate();
			            console.this.scrollPane.repaint();
			            console.this.scrollPane.setVerticalScrollBarPolicy(22);
			            console.this.revalidate();
			            console.this.repaint();
		            } 
		    	}
			});
	    
	    	timer.start();
	    	
	  }
  
  private void buildContentpane() throws IOException {
	  this.topPanel = new JPanel();
	    this.topPanel.setLayout(new BorderLayout());
	    getContentPane().add(this.topPanel);
	    this.wD = new consoleDM();
	    this.table = new JTable(this.wD);
	    this.header = this.table.getTableHeader();
	    this.header.setBackground(Color.LIGHT_GRAY);
	    this.bu1 = new JButton();
	    System.out.println("screenHeightWidth :" + screenheight + " " + screenwidth);
	    if (screenheight > 1200) {
	      this.table.setRowHeight(this.table.getRowHeight() * 2);
	      this.header.setFont(new FontUIResource("Dialog", 0, this.table.getRowHeight()));
	      this.bu1.setFont(new FontUIResource("Dialog", 0, this.table.getRowHeight()));
	    } else if (screenheight > 1080) {
	      this.table.setRowHeight(this.table.getRowHeight() * 1, 5);
	      this.header.setFont(new FontUIResource("Dialog", 0, this.table.getRowHeight()));
	      this.bu1.setFont(new FontUIResource("Dialog", 0, this.table.getRowHeight()));
	    } 
	    this.swServer = Boolean.valueOf(true);
	    try {
	      SendMsg jm = new SendMsg(this.jvhost, this.port);
	      String oSts = jm.open();
	      if (oSts.startsWith("failed"))
	        this.swServer = Boolean.valueOf(false); 
	      if (oSts.startsWith("DORMANT")) {
	        this.swDormant = Boolean.valueOf(true);
	      } else {
	        this.swDormant = Boolean.valueOf(false);
	      } 
	      jm.close();
	    } catch (NullPointerException npe2) {
	      this.swServer = Boolean.valueOf(false);
	      System.out.println("-- Rpt Failed 1 --" + npe2);
	    } 
	    this.swDBopen = Boolean.valueOf(this.wD.refreshData());
	    setBu1Color();
	    this.bu1.addActionListener(new ActionListener() {
	          public void actionPerformed(ActionEvent e) {
	            console.this.swAuto = Boolean.valueOf(!console.this.swAuto.booleanValue());
	            console.this.swDBopen = Boolean.valueOf(console.this.wD.refreshData());
	            console.this.setBu1Color();
	          }
	        });
	    this.table.setSelectionMode(2);
	    ListSelectionModel rowSM = this.table.getSelectionModel();
	    rowSM.addListSelectionListener(new ListSelectionListener() {
	          public void valueChanged(ListSelectionEvent e) {
	            if (e.getValueIsAdjusting())
	              return; 
	            ListSelectionModel lsm = (ListSelectionModel)e.getSource();
	            if (!lsm.isSelectionEmpty())
	              console.this.deselectCount = 0; 
	          }
	        });
	    this.table.getModel().addTableModelListener(this);
	    consoleCR cr = new consoleCR();
	    for (int i = 0; i <= 7; i++)
	      this.table.getColumn(this.table.getColumnName(i)).setCellRenderer(cr); 
	    this.scrollPane = new JScrollPane(this.table);
	    this.table.setAutoResizeMode(2);
	    TableColumn column = null;
	    column = this.table.getColumnModel().getColumn(0);
	    column.setPreferredWidth(400);
	    column.setMaxWidth(1100);
	    column = this.table.getColumnModel().getColumn(1);
	    column.setPreferredWidth(35);
	    column.setMaxWidth(65);
	    column = this.table.getColumnModel().getColumn(2);
	    column.setPreferredWidth(30);
	    column.setMaxWidth(65);
	    column = this.table.getColumnModel().getColumn(3);
	    column.setPreferredWidth(255);
	    column.setMaxWidth(895);
	    column = this.table.getColumnModel().getColumn(4);
	    column.setPreferredWidth(255);
	    column.setMaxWidth(895);
	    column = this.table.getColumnModel().getColumn(5);
	    column.setPreferredWidth(70);
	    column.setMaxWidth(420);
	    column = this.table.getColumnModel().getColumn(6);
	    column.setPreferredWidth(800);
	    column.setMaxWidth(2800);
	    column = this.table.getColumnModel().getColumn(7);
	    column.setPreferredWidth(200);
	    column.setMaxWidth(950);
	    this.scrollPane.setVerticalScrollBarPolicy(22);
	    this.topPanel.add(this.scrollPane, "Center");
	    this.topPanel.add(this.bu1, "North");
	    addWindowListener(this);
	
}

  private void buildMenu() {
	  	JMenu menu1 = new JMenu("File");
		JMenu menu2 = new JMenu("Edit");
		JMenu menu3 = new JMenu("View");
		JMenu menu4 = new JMenu("Tools");
		JMenu menu5 = new JMenu("Help");
		JMenuBar menuBar = new JMenuBar();
		
		JMenuItem screenShot = new JMenuItem("Take Screenshot");
		screenShot.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
		screenShot.addActionListener(screenShot());
		
		JMenuItem exit = new JMenuItem("Exit");
		exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK));
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
		}});
		
		JMenuItem delRow = new JMenuItem("Delete Selected Row");
		delRow.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.	VK_DELETE , 0));
		delRow.addActionListener(delRow());
		
		JMenuItem toggleDormant = new JMenuItem("Toggle System Active/dormant");
		toggleDormant.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0));
		toggleDormant.addActionListener(toggleDormant());
		
		JMenuItem clearSel = new JMenuItem("Unselect row");
		clearSel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
		clearSel.addActionListener(clearSel());
		
		JMenuItem increaseH = new JMenuItem("Increase Font");
		increaseH.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
		increaseH.addActionListener(increaseH());
		
		JMenuItem decreaseH = new JMenuItem("Decrease Font");
		decreaseH.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
		decreaseH.addActionListener(decreaseH());
			
		JMenuItem getInfo = new JMenuItem("Enter info text");
		getInfo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0));
		getInfo.addActionListener(getInfo());
		
		JMenuItem showLine = new JMenuItem("Show line information");
		showLine.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
		showLine.addActionListener(showLine());
		
		JMenuItem strStat = new JMenuItem("Show Statistik");
		strStat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0));
		strStat.addActionListener(strStat());
		
		JMenuItem strHst = new JMenuItem("Show History");
		strHst.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		strHst.addActionListener(strHst());
		
		JMenuItem strSts = new JMenuItem("Status Table");
		strSts.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
		strSts.addActionListener(strSts());
		
		JMenuItem showHelp = new JMenuItem("Show Help");
		showHelp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		showHelp.addActionListener(showHelp());
		
		JMenuItem about = new JMenuItem("About");
		about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
		about.addActionListener(about());
		
		menu1.add(screenShot);
		menu1.add(exit);
		menu2.add(delRow);
		menu2.add(toggleDormant);
		menu3.add(clearSel);
		menu3.add(increaseH);
		menu3.add(decreaseH);
		menu4.add(getInfo);
		menu4.add(showLine);
		menu4.add(strStat);
		menu4.add(strHst);
		menu4.add(strSts);
		menu5.add(showHelp);
		menu5.add(about);
		menuBar.add(menu1);
		menuBar.add(menu2);
		menuBar.add(menu3);
		menuBar.add(menu4);
		menuBar.add(menu5);
		this.setJMenuBar(menuBar);
	
}

  public void tableChanged(TableModelEvent e) {
	    int row = e.getFirstRow();
	    int column = e.getColumn();
	    TableModel model = (TableModel)e.getSource();
	    String data = (String)model.getValueAt(row, column);
	    String ls = "Workout tableChanged " + row + " " + column + " " + data;
	    System.out.println(ls);
  }
  
  public void setBu1Color() {
	  String txt = "";
	    if (this.swAuto.booleanValue()) {
	      this.bu1.setBackground(Color.GRAY);
	      txt = "Auto Update ON.";
	    } else {
	      this.bu1.setBackground(Color.yellow);
	      txt = "Auto Update OFF.";
	    } 
	    if (!this.swDBopen.booleanValue()) {
	      this.bu1.setBackground(Color.RED);
	      txt = String.valueOf(txt) + "  No connection with DB. ";
	    } 
	    if (!this.swServer.booleanValue()) {
	      this.bu1.setBackground(Color.RED);
	      txt = String.valueOf(txt) + "  No connection with JvaktServer. ";
	    } else if (this.swDormant.booleanValue()) {
	      this.bu1.setBackground(Color.ORANGE);
	      txt = String.valueOf(txt) + "  System DORMANT.";
	    } else {
	      txt = String.valueOf(txt) + "  System ACTIVE.";
	    } 
	    this.bu1.setText(txt);
	 }
  
  private AbstractAction screenShot() {
	  AbstractAction save = new AbstractAction() {
	        static final long serialVersionUID = 43L;
	        public void actionPerformed(ActionEvent e) {
	        	Object[] options = {"Clipboard", "Save to a file"};
	        	int n = JOptionPane.showOptionDialog(console.this,
	        		    "Where would you like to save screenshot?",
	        		    "Screenshot taken",
	        		    JOptionPane.YES_NO_OPTION,
	        		    JOptionPane.QUESTION_MESSAGE,
	        		    null, options, options[0]    //do not use a custom Icon
	        		      //the titles of buttons
	        		    ); //default button title
	        	try {
					BufferedImage screenCaputure = makeAndGetContentPaneScreenshotFromJFrame(console.this);
					
					if(n == JOptionPane.YES_OPTION) {
						TransferableImage trans = new TransferableImage( screenCaputure );
			            Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
			            c.setContents( trans, console.this );
			            
					}
					else if(n == JOptionPane.NO_OPTION) {
						save(screenCaputure);
					}
	        	}catch (IOException e1) {
					e1.printStackTrace();
				}
	        }
	      };
	    return save;
  }
   
  private void save(BufferedImage bi) throws IOException {
	  SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm");  
	  Date date = new Date();  
	  String now = formatter.format(date); 
	  FileDialog chooser = new FileDialog(this,
	        "Use a .png, .gif or .jpg extension", FileDialog.SAVE);
	  chooser.setFile("Jvakt_" + now + ".png");
	  chooser.setVisible(true);
	  
	  String name = "";
	  if (chooser.getFile() != null) {
		  name = chooser.getDirectory() + chooser.getFile();
	  }
	  
	  try { 
			File outputfile = new File(name.contains(".png") ? name:name+".png"); 
			ImageIO.write(bi, "png", outputfile); 
	  } catch (IOException e) { e.getStackTrace();}
	  
	  
	  
	}
  
  
  private AbstractAction showHelp() {
    AbstractAction save = new AbstractAction() {
        static final long serialVersionUID = 43L;
        
        public void actionPerformed(ActionEvent e) {
          JOptionPane.showMessageDialog(console.this.getContentPane(), 
              "F1 : Help \nF2 : About Jvakt \nF3 : Increase font size \nF4 : Decrease font size \nF5 : History \nF6 : Status table \nF7 : Show row \nF8 : Toggle System active / dormant \nF9 : Enter info text \nF10 : Statistics \nF11 : Take screenshot  \n\nDEL : delete rows \nESC : Unselect\n\nThis app shows the filtered reports/messages sent to the Jvakt server. OK messages of types 'R', 'T' and 'S' remains in the database.\nThe upper bar acts a button to stop/start the automatic update. \nIt will also show the status of the server and database.\n\nFields: \nId = The Id if the message. \nPrio = Prio 30 and higher is meant for office hours and messages will remain in the console. No mail or SMS.\n       Below 30 is important and might trigger SMS and/or mail depending on chkday/chktim \n       Prio 10 or less is very important and will trigger SMS and/or mail 24/7. \ntype = 'S' means a check that rptday is updated 'today'. The check is made once a day at the time in the chkday and chktim fields. \n           When read and acted upon, the row may be selected and removed with the DEL button.\n           If not manually deleted it will be automatically removed the next time the check sends an OK report. Usually the next day.\ntype = 'R' means a check that rptdat is updated at least every 20 minute. The check starts from the time in chkday and chktim fields.\n           The message will disappear automatically when the issue is resolved. \ntype = 'T' means no tome-out checks are made.\n           When read and acted upon the line may be selected and removed with the DEL button.\n           It will be automatically removed the next time the check sends an OK report.\n           When or if this will happen is unknown.\ntype = 'I' means impromptu messages. \n           The 'I' type will not remain in the status table and can not be prepared in advance.\n           When read and acted upon the row must be selected and removed with the DEL button.\nCreDate = The inital time the message arrived the the console.\nConDate = The latest time the message was updated. \nStatus  = ERR, INFO, OK or TOut.\n          TOut means the agent has stopped sending the expected status reports. This applied only to types 'S' and 'R'. \nbody = Contains the text sent by the agent\nagent = Contains the host name and IP address where the agent is executed.", 
              
              "Jvakt Help", 
              1);
        }
      };
    return save;
  }
  
  private AbstractAction about() {
	    AbstractAction save = new AbstractAction() {
	        static final long serialVersionUID = 43L;
	        
	        public void actionPerformed(ActionEvent e) {
	          JOptionPane.showMessageDialog(console.this.getContentPane(), 
	        			 version
	     				+ "\n This program is made by Michael Ekdal"
	     				+ "\n Contact Person : Oday Abusamra"
	     				+ "\n Mail : oday.abusamra@perstorp.com" 
	     				+"\n All rights reserved for PerstorpAB"
	     						,"Jvakt Help",
	              1);
	        }
	      };
	    return save;
	  }
  
  private AbstractAction getInfo() {
    AbstractAction save = new AbstractAction() {
        static final long serialVersionUID = 53L;
        
        public void actionPerformed(ActionEvent e) {
          console.this.table.getSelectionModel().clearSelection();
          console.this.infotxt = JOptionPane.showInputDialog(console.this.getContentPane(), 
              "Enter information text to be sent to the console\n", 
              "Jvakt Info", 
              3);
          if (console.this.infotxt != null && console.this.infotxt.length() > 0) {
            System.out.println("*** infotxt: " + console.this.infotxt);
            try {
              Message jmsg = new Message();
              SendMsg jm = new SendMsg(console.this.jvhost, console.this.port);
              System.out.println(jm.open());
              jmsg.setId("INFO-to-console");
              jmsg.setType("I");
              jmsg.setRptsts("INFO");
              jmsg.setBody(console.this.infotxt);
              jmsg.setAgent("GUI");
              if (jm.sendMsg(jmsg)) {
                System.out.println("-- Rpt Delivered 5 --");
              } else {
                System.out.println("-- Rpt Failed 5 --");
              } 
              jm.close();
            } catch (Exception e2) {
              System.err.println(e2);
              System.err.println(e2.getMessage());
            } 
          } 
        }
      };
    return save;
  }
  
  private AbstractAction showLine() {
    AbstractAction save = new AbstractAction() {
        static final long serialVersionUID = 44L;
        
        public void actionPerformed(ActionEvent e) {
          console.this.table.editingCanceled((ChangeEvent)null);
          console.this.table.editingStopped((ChangeEvent)null);
          int[] selectedRow = console.this.table.getSelectedRows();
          System.out.println("ShowLine: " + selectedRow.length);
          try {
            for (int i = 0; i < selectedRow.length; i++) {
              Object ValueId = console.this.table.getValueAt(selectedRow[i], console.this.table.getColumnModel().getColumnIndex("Id"));
              String id = (String)ValueId;
              if (id != null) {
                ValueId = console.this.table.getValueAt(selectedRow[i], console.this.table.getColumnModel().getColumnIndex("Prio"));
                int prio = ((Integer)ValueId).intValue();
                ValueId = console.this.table.getValueAt(selectedRow[i], console.this.table.getColumnModel().getColumnIndex("Type"));
                String type = (String)ValueId;
                ValueId = console.this.table.getValueAt(selectedRow[i], console.this.table.getColumnModel().getColumnIndex("CreDate"));
                String credate = (String)ValueId;
                ValueId = console.this.table.getValueAt(selectedRow[i], console.this.table.getColumnModel().getColumnIndex("ConDate"));
                String condate = (String)ValueId;
                ValueId = console.this.table.getValueAt(selectedRow[i], console.this.table.getColumnModel().getColumnIndex("Status"));
                String status = (String)ValueId;
                ValueId = console.this.table.getValueAt(selectedRow[i], console.this.table.getColumnModel().getColumnIndex("Body"));
                String body = (String)ValueId;
                ValueId = console.this.table.getValueAt(selectedRow[i], console.this.table.getColumnModel().getColumnIndex("Agent"));
                String agent = (String)ValueId;
                JOptionPane.showMessageDialog(console.this.getContentPane(), 
                    "- ID (the id of the message. Together with prio it makes an unique id) -\n" + id + " \n\n" + 
                    "- Prio (the priority, part of the unique id. Below 30 trigger email and SMS text) -\n" + prio + "\n\n" + 
                    "- Type (R=repeated, S=scheduled, I=immediate/impromptu, T=permanent with no time-out checks) -\n" + type + "\n\n" + 
                    "- CreDate (the date it appeared in the console) -\n" + credate + "\n\n" + 
                    "- ConDate (the date it updated in the console) -\n" + condate + "\n\n" + 
                    "- Status (OK, INFO, TOut or ERR) -\n" + status + "\n\n" + 
                    "- Body (any text) -\n" + body + "\n\n" + 
                    "- Agent (description of the reporting agent) -\n" + agent, 
                    
                    "Jvakt Show line", 
                    1);
              } 
            } 
          } catch (Exception e2) {
            System.err.println(e2);
            System.err.println(e2.getMessage());
          } 
          console.this.table.getSelectionModel().clearSelection();
        }
      };
    return save;
  }
  
  private AbstractAction clearSel() {
    AbstractAction save = new AbstractAction() {
        static final long serialVersionUID = 45L;
        
        public void actionPerformed(ActionEvent e) {
          console.this.table.getSelectionModel().clearSelection();
        }
      };
    return save;
  }
  
  private AbstractAction increaseH() {
    AbstractAction save = new AbstractAction() {
        static final long serialVersionUID = 52L;
        
        public void actionPerformed(ActionEvent e) {
          if (console.this.table.getRowHeight() < 100) {
            console.this.table.setRowHeight(console.this.table.getRowHeight() + 1);
            console.this.header.setFont(new FontUIResource("Dialog", 0, console.this.table.getRowHeight()));
            console.this.bu1.setFont(new FontUIResource("Dialog", 0, console.this.table.getRowHeight()));
          } 
        }
      };
    return save;
  }
  
  private AbstractAction decreaseH() {
    AbstractAction save = new AbstractAction() {
        static final long serialVersionUID = 46L;
        
        public void actionPerformed(ActionEvent e) {
          if (console.this.table.getRowHeight() > 10) {
            console.this.table.setRowHeight(console.this.table.getRowHeight() - 1);
            console.this.header.setFont(new FontUIResource("Dialog", 0, console.this.table.getRowHeight()));
            console.this.bu1.setFont(new FontUIResource("Dialog", 0, console.this.table.getRowHeight()));
          } 
        }
      };
    return save;
  }
  
  private AbstractAction delRow() {
    AbstractAction save = new AbstractAction() {
        static final long serialVersionUID = 47L;
        
        public void actionPerformed(ActionEvent e) {
          console.this.table.editingCanceled((ChangeEvent)null);
          console.this.table.editingStopped((ChangeEvent)null);
          int[] selectedRow = console.this.table.getSelectedRows();
          try {
            for (int i = 0; i < selectedRow.length; i++) {
              Message jmsg = new Message();
              SendMsg jm = new SendMsg(console.this.jvhost, console.this.port);
              System.out.println("Response opening connection to Jvakt server: " + jm.open());
              Object ValueId = console.this.table.getValueAt(selectedRow[i], console.this.table.getColumnModel().getColumnIndex("Id"));
              jmsg.setId(ValueId.toString());
              jmsg.setRptsts("OK");
              ValueId = console.this.table.getValueAt(selectedRow[i], console.this.table.getColumnModel().getColumnIndex("Body"));
              jmsg.setBody(ValueId.toString());
              ValueId = console.this.table.getValueAt(selectedRow[i], console.this.table.getColumnModel().getColumnIndex("Prio"));
              jmsg.setPrio(Integer.parseInt(ValueId.toString()));
              jmsg.setType("D");
              jmsg.setAgent("GUI");
              if (jm.sendMsg(jmsg)) {
                System.out.println("-- Rpt Delivered 3 --");
              } else {
                System.out.println("-- Rpt Failed 3 --");
              } 
              jm.close();
            } 
          } catch (Exception e2) {
            System.err.println(e2);
            System.err.println(e2.getMessage());
          } 
          console.this.table.getSelectionModel().clearSelection();
        }
      };
    return save;
  }
  
  private AbstractAction toggleDormant() {
    AbstractAction save = new AbstractAction() {
        static final long serialVersionUID = 48L;
        
        public void actionPerformed(ActionEvent e) {
          Object[] options = { "OK", "Cancel" };
          int n = JOptionPane.showOptionDialog(null, "Do you want to toggle System active / dormant?", "Toggle active / dormant", 
              -1, 2, 
              null, options, options[0]);
          if (n == 1) {
            System.out.println("-- Cancel Toggle dormant ---");
          } else {
            try {
              Message jmsg = new Message();
              SendMsg jm = new SendMsg(console.this.jvhost, console.this.port);
              System.out.println(jm.open());
              jmsg.setId("Jvakt");
              if (console.this.swDormant.booleanValue()) {
                jmsg.setType("Active");
              } else {
                jmsg.setType("Dormant");
              } 
              jmsg.setAgent("GUI");
              if (jm.sendMsg(jmsg)) {
                System.out.println("-- Rpt Delivered --");
              } else {
                System.out.println("-- Rpt Failed 4 --");
              } 
              jm.close();
            } catch (Exception e2) {
              System.err.println(e2);
              System.err.println(e2.getMessage());
            } 
          } 
        }
      };
    return save;
  }
  
  private AbstractAction strHst() {
    AbstractAction save = new AbstractAction() {
        static final long serialVersionUID = 49L;
        
        public void actionPerformed(ActionEvent e) {
          try {
            Runtime.getRuntime().exec(console.this.cmdHst);
          } catch (IOException e1) {
            System.err.println(e1);
            System.err.println(e1.getMessage());
          } 
        }
      };
    return save;
  }
  
  private AbstractAction strStat() {
    AbstractAction save = new AbstractAction() {
        static final long serialVersionUID = 49L;
        
        public void actionPerformed(ActionEvent e) {
          try {
            Runtime.getRuntime().exec(console.this.cmdStat);
          } catch (IOException e1) {
            System.err.println(e1);
            System.err.println(e1.getMessage());
          } 
        }
      };
    return save;
  }
  
  private AbstractAction strSts() {
    AbstractAction save = new AbstractAction() {
        static final long serialVersionUID = 50L;
        
        public void actionPerformed(ActionEvent e) {
          try {
            Runtime.getRuntime().exec(console.this.cmdSts);
          } catch (IOException e1) {
            System.err.println(e1);
            System.err.println(e1.getMessage());
          } 
        }
      };
    return save;
  }
  
  public void windowClosing(WindowEvent e) {
    this.wD.closeDB();
    System.exit(0);
  }
  
  void getProps() {
    Properties prop = new Properties();
    InputStream input = null;
    try {
      input = new FileInputStream("console.properties");
      prop.load(input);
      this.jvport = prop.getProperty("jvport");
      this.jvhost = prop.getProperty("jvhost");
      this.cmdHst = prop.getProperty("cmdHst");
      this.cmdSts = prop.getProperty("cmdSts");
      this.cmdStat = prop.getProperty("cmdStat");
      input.close();
    } catch (IOException iOException) {}
  }
  
  public void windowClosed(WindowEvent e) {}
  
  public void windowOpened(WindowEvent e) {}
  
  public void windowIconified(WindowEvent e) {}
  
  public void windowDeiconified(WindowEvent e) {}
  
  public void windowActivated(WindowEvent e) {}
  
  public void windowDeactivated(WindowEvent e) {}



@Override
public void lostOwnership(Clipboard clipboard, Transferable contents) {
	System.out.println("Lost ownership");
	
}


private class TransferableImage implements Transferable {

    Image i;

    public TransferableImage( Image i ) {
        this.i = i;
    }

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		DataFlavor[] flavors = new DataFlavor[ 1 ];
        flavors[ 0 ] = DataFlavor.imageFlavor;
        return flavors;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		DataFlavor[] flavors = getTransferDataFlavors();
        for ( int i = 0; i < flavors.length; i++ ) {
            if ( flavor.equals( flavors[ i ] ) ) {
                return true;
            }
        }

        return false;
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		 if ( flavor.equals( DataFlavor.imageFlavor ) && i != null ) {
	            return i;
	        }
	        else {
	            throw new UnsupportedFlavorException( flavor );
	        }
	}
}


}
