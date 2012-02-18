import javax.swing.*; 
import javax.swing.border.*; 
import javax.swing.table.*;
import java.awt.*; 
import java.awt.event.*; 
 
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

import CLIPSJNI.*;

/* TBD module qualifier with find-all-facts */

/*

Notes:

This example creates just a single environment. If you create multiple environments,
call the destroy method when you no longer need the environment. This will free the
C data structures associated with the environment.

   clips = new Environment();
      .
      . 
      .
   clips.destroy();

Calling the clear, reset, load, loadFacts, run, eval, build, assertString,
and makeInstance methods can trigger CLIPS garbage collection. If you need
to retain access to a PrimitiveValue returned by a prior eval, assertString,
or makeInstance call, retain it and then release it after the call is made.

   PrimitiveValue pv1 = clips.eval("(myFunction foo)");
   pv1.retain();
   PrimitiveValue pv2 = clips.eval("(myFunction bar)");
      .
      .
      .
   pv1.release();

*/

class WineDemo implements ActionListener
  {  
   JFrame jfrm;
   
   DefaultTableModel wineList;
  
   JComboBox typeGoal1; 
   JComboBox typeGoal2; 
   JComboBox typeGoal3; 

   JTextField amountGoal1;
   JTextField amountGoal2;
   JTextField amountGoal3;
   
   JLabel jlab; 

   String preferredGoalDuration[] = { "Don't Care", "Long Term (> 8 years)", "Mid Term (3-8 years)", 
		   "Short Term (< 3 years)" }; 
   
   String preferredGoal1Choices[] = new String[4]; 
   String preferredGoal2Choices[] = new String[4]; 
   String preferredGoal3Choices[] = new String[4]; 
   
   ResourceBundle wineResources;

   Environment clips;
   
   boolean isExecuting = false;
   Thread executionThread;

   class WeightCellRenderer extends JProgressBar implements TableCellRenderer 
     {
      public WeightCellRenderer() 
        {
         super(JProgressBar.HORIZONTAL,0,100);
         setStringPainted(false);
        }
  
      public Component getTableCellRendererComponent(
        JTable table, 
        Object value,
        boolean isSelected, 
        boolean hasFocus, 
        int row, 
        int column) 
        { 
         setValue(((Number) value).intValue());
         return WeightCellRenderer.this; 
        }
     }
      
   /************/
   /* WineDemo */
   /************/
   WineDemo()
     {  
      try
        {
         wineResources = ResourceBundle.getBundle("resources.WineResources",Locale.getDefault());
        }
      catch (MissingResourceException mre)
        {
         mre.printStackTrace();
         return;
        }

      preferredGoal1Choices[0] = wineResources.getString("Don'tCare"); 
      preferredGoal1Choices[1] = wineResources.getString("LongTerm"); 
      preferredGoal1Choices[2] = wineResources.getString("MidTerm");
      preferredGoal1Choices[3] = wineResources.getString("ShortTerm");
      
      preferredGoal2Choices[0] = wineResources.getString("Don'tCare"); 
      preferredGoal2Choices[1] = wineResources.getString("LongTerm"); 
      preferredGoal2Choices[2] = wineResources.getString("MidTerm");
      preferredGoal2Choices[3] = wineResources.getString("ShortTerm");
      
      preferredGoal3Choices[0] = wineResources.getString("Don'tCare"); 
      preferredGoal3Choices[1] = wineResources.getString("LongTerm"); 
      preferredGoal3Choices[2] = wineResources.getString("MidTerm");
      preferredGoal3Choices[3] = wineResources.getString("ShortTerm");

      /*===================================*/
      /* Create a new JFrame container and */
      /* assign a layout manager to it.    */
      /*===================================*/
     
      jfrm = new JFrame(wineResources.getString("WineDemo"));          
      jfrm.getContentPane().setLayout(new BoxLayout(jfrm.getContentPane(),BoxLayout.Y_AXIS));
    
      /*=================================*/
      /* Give the frame an initial size. */
      /*=================================*/
     
      jfrm.setSize(480,390);  
  
      /*=============================================================*/
      /* Terminate the program when the user closes the application. */
      /*=============================================================*/
     
      jfrm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
 
      /*===============================*/
      /* Create the preferences panel. */
      /*===============================*/
      
      JPanel preferencesPanel = new JPanel(); 
      GridLayout theLayout = new GridLayout(3,2);
      preferencesPanel.setLayout(theLayout);   
      preferencesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                                                                 wineResources.getString("PreferencesTitle"),
                                                                 TitledBorder.CENTER,
                                                                 TitledBorder.ABOVE_TOP));
 
      preferencesPanel.add(new JLabel(wineResources.getString("ColorLabel")));
      typeGoal1 = new JComboBox(preferredGoalDuration); 
      preferencesPanel.add(typeGoal1);
      typeGoal1.addActionListener(this);
     
      preferencesPanel.add(new JLabel(wineResources.getString("BodyLabel")));
      typeGoal2 = new JComboBox(preferredGoalDuration); 
      preferencesPanel.add(typeGoal2);
      typeGoal2.addActionListener(this);

      preferencesPanel.add(new JLabel(wineResources.getString("SweetnessLabel")));
      typeGoal3 = new JComboBox(preferredGoalDuration); 
      preferencesPanel.add(typeGoal3);
      typeGoal3.addActionListener(this);

      /*========================*/
      /* Create the meal panel. */
      /*========================*/
     
      JPanel mealPanel = new JPanel(); 
      theLayout = new GridLayout(3,2);
      mealPanel.setLayout(theLayout);   
      mealPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                                                                 wineResources.getString("MealTitle"),
                                                                 TitledBorder.CENTER,
                                                                 TitledBorder.ABOVE_TOP));
      
      /*==============================================*/
      /* Create a panel including the preferences and */
      /* meal panels and add it to the content pane.  */
      /*==============================================*/

      JPanel choicesPanel = new JPanel(); 
      choicesPanel.setLayout(new FlowLayout());
      choicesPanel.add(preferencesPanel);
      
      jfrm.getContentPane().add(choicesPanel); 
 
      /*==================================*/
      /* Create the recommendation panel. */
      /*==================================*/

      wineList = new DefaultTableModel();

      wineList.setDataVector(new Object[][] { },
                             new Object[] { wineResources.getString("WineTitle"), 
                                            wineResources.getString("RecommendationTitle")});
         
      JTable table = 
         new JTable(wineList)
           {
            public boolean isCellEditable(int rowIndex,int vColIndex) 
              { return false; }
           };

      table.setCellSelectionEnabled(false); 

      WeightCellRenderer renderer = this.new WeightCellRenderer(); 
      renderer.setBackground(table.getBackground());

      table.getColumnModel().getColumn(1).setCellRenderer(renderer);

      JScrollPane pane = new JScrollPane(table);
    
      table.setPreferredScrollableViewportSize(new Dimension(450,210)); 
        
      /*===================================================*/
      /* Add the recommendation panel to the content pane. */
      /*===================================================*/

      jfrm.getContentPane().add(pane); 

      /*===================================================*/
      /* Initially select the first item in each ComboBox. */
      /*===================================================*/
       
      typeGoal1.setSelectedIndex(0); 
      typeGoal2.setSelectedIndex(0); 
      typeGoal3.setSelectedIndex(0); 
/*      mainCourse.setSelectedIndex(0);
      sauce.setSelectedIndex(0);
      flavor.setSelectedIndex(0);
*/
      /*========================*/
      /* Load the wine program. */
      /*========================*/
      
      clips = new Environment();
      
      clips.load("winedemo.clp");
      
      try
        { runWine(); }
      catch (Exception e)
        { e.printStackTrace(); }
       
      /*====================*/
      /* Display the frame. */
      /*====================*/

      jfrm.pack();
      jfrm.setVisible(true);  
     }  
 
   /*########################*/
   /* ActionListener Methods */
   /*########################*/

   /*******************/
   /* actionPerformed */
   /*******************/  
   public void actionPerformed(
     ActionEvent ae) 
     { 
      if (clips == null) return;
      
      try
        { runWine(); }
      catch (Exception e)
        { e.printStackTrace(); }
     }
     
   /***********/
   /* runWine */
   /***********/  
   private void runWine() throws Exception
     { 
      String item;
      
      if (isExecuting) return;
      
      clips.reset();      
            
      item = preferredGoal1Choices[typeGoal1.getSelectedIndex()];
      
      if (item.equals("LongTerm"))   
        { clips.assertString("(attribute (name preferred-goal1) (value longterm))"); }
      else if (item.equals("MidTerm"))   
        { clips.assertString("(attribute (name preferred-goal1) (value midterm))"); }
      else if (item.equals("ShortTerm"))   
      { clips.assertString("(attribute (name preferred-goal1) (value shortterm))"); }
      else
        { clips.assertString("(attribute (name preferred-goal1) (value unknown))"); }

      item = preferredGoal2Choices[typeGoal2.getSelectedIndex()];
      
      if (item.equals("LongTerm"))   
      { clips.assertString("(attribute (name preferred-goal2) (value longterm))"); }
      else if (item.equals("MidTerm"))   
      { clips.assertString("(attribute (name preferred-goal2) (value midterm))"); }
      else if (item.equals("ShortTerm"))   
      { clips.assertString("(attribute (name preferred-goal2) (value shortterm))"); }
      else
      { clips.assertString("(attribute (name preferred-goal2) (value unknown))"); }
 
      item = preferredGoal3Choices[typeGoal3.getSelectedIndex()];
      if (item.equals("LongTerm"))   
      { clips.assertString("(attribute (name preferred-goal3) (value longterm))"); }
    else if (item.equals("MidTerm"))   
      { clips.assertString("(attribute (name preferred-goal3) (value midterm))"); }
    else if (item.equals("ShortTerm"))   
    { clips.assertString("(attribute (name preferred-goal3) (value shortterm))"); }
    else
      { clips.assertString("(attribute (name preferred-goal3) (value unknown))"); }

      Runnable runThread = 
         new Runnable()
           {
            public void run()
              {
               clips.run();
               
               SwingUtilities.invokeLater(
                  new Runnable()
                    {
                     public void run()
                       {
                        try 
                          { updateWines(); }
                        catch (Exception e)
                          { e.printStackTrace(); }
                       }
                    });
              }
           };
      
      isExecuting = true;
      
      executionThread = new Thread(runThread);
      
      executionThread.start();
     }
     
   /***************/
   /* updateWines */
   /***************/  
   private void updateWines() throws Exception
     { 
      String evalStr = "(WINES::get-wine-list)";
                                       
      PrimitiveValue pv = clips.eval(evalStr);
               
      wineList.setRowCount(0);
      
      for (int i = 0; i < pv.size(); i++) 
        {
         PrimitiveValue fv = pv.get(i);

         int certainty = fv.getFactSlot("certainty").numberValue().intValue(); 
         
         String wineName = fv.getFactSlot("value").stringValue();
                  
         wineList.addRow(new Object[] { wineName, new Integer(certainty) });
        }  
        
      jfrm.pack();
      
      executionThread = null;
      
      isExecuting = false;
     }     
     
   /********/
   /* main */
   /********/  
   public static void main(String args[])
     {  
      /*===================================================*/
      /* Create the frame on the event dispatching thread. */
      /*===================================================*/
      
      SwingUtilities.invokeLater(
        new Runnable() 
          {  
           public void run() { new WineDemo(); }  
          });   
     }  
  }
