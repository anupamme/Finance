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
  
   JComboBox preferredColor; 
   JComboBox preferredBody; 
   JComboBox preferredGrowthness; 

   JComboBox mainCourse; 
   JComboBox riskappetite; 
   JComboBox flavor; 
   
   JLabel jlab; 

   String preferredColorNames[] = { "Don't Care", "Equity", "Hybrid" }; 
   String preferredBodyNames[] = { "Don't Care", "Small", "Medium", "Large" }; 
   String preferredGrowthnessNames[] = { "Don't Care", "TaxPlanning", "Medium", "Growth" }; 
   
   String mainCourseNames[] = { "Don't Know", "Retirement", "ChildMarriage", "LongTermGrowth", "Emergency", "ChildEducation", "Business", "HigherEducation", "Other" };
   String riskappetiteNames[] = { "Don't Know", "RiskAverse", "Moderate", "Bold", "Aggressive", "Blind" };
   String flavorNames[] = { "Don't Know", "Short Term", "Mid Term", "Long Term" };
 
   String preferredColorChoices[] = new String[3]; 
   String preferredBodyChoices[] = new String[4]; 
   String preferredGrowthnessChoices[] = new String[4]; 
   
   String mainCourseChoices[] = new String[9];
   String riskappetiteChoices[] = new String[6];
   String flavorChoices[] = new String[4];

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

      preferredColorChoices[0] = wineResources.getString("Don'tCare"); 
      preferredColorChoices[1] = wineResources.getString("Equity"); 
      preferredColorChoices[2] = wineResources.getString("Hybrid"); 
      
      preferredBodyChoices[0] = wineResources.getString("Don'tCare"); 
      preferredBodyChoices[1] = wineResources.getString("Small"); 
      preferredBodyChoices[2] = wineResources.getString("MediumBody"); 
      preferredBodyChoices[3] = wineResources.getString("Large"); 

      preferredGrowthnessChoices[0] = wineResources.getString("Don'tCare"); 
      preferredGrowthnessChoices[1] = wineResources.getString("TaxPlanning"); 
      preferredGrowthnessChoices[2] = wineResources.getString("Debt"); 
      preferredGrowthnessChoices[3] = wineResources.getString("Growth"); 
      
      mainCourseChoices[0] = wineResources.getString("Don'tKnow"); 
      mainCourseChoices[1] = wineResources.getString("Retirement"); 
      mainCourseChoices[2] = wineResources.getString("ChildMarriage"); 
      mainCourseChoices[3] = wineResources.getString("LongTermGrowth"); 
      mainCourseChoices[4] = wineResources.getString("Emergency"); 
      mainCourseChoices[5] = wineResources.getString("ChildEducation"); 
      mainCourseChoices[6] = wineResources.getString("Business"); 
      mainCourseChoices[7] = wineResources.getString("HigherEducation"); 
      mainCourseChoices[8] = wineResources.getString("Other"); 
   
      riskappetiteChoices[0] = wineResources.getString("Don'tKnow"); 
      riskappetiteChoices[1] = wineResources.getString("RiskAverse"); 
      riskappetiteChoices[2] = wineResources.getString("Moderate"); 
      riskappetiteChoices[3] = wineResources.getString("Bold"); 
      riskappetiteChoices[4] = wineResources.getString("Aggressive"); 
      riskappetiteChoices[5] = wineResources.getString("Blind"); 

      flavorChoices[0] = wineResources.getString("Don'tKnow"); 
      flavorChoices[1] = wineResources.getString("ShortTerm"); 
      flavorChoices[2] = wineResources.getString("MidTerm"); 
      flavorChoices[3] = wineResources.getString("LongTerm"); 

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
      preferredColor = new JComboBox(preferredColorChoices); 
      preferencesPanel.add(preferredColor);
      preferredColor.addActionListener(this);
     
      preferencesPanel.add(new JLabel(wineResources.getString("BodyLabel")));
      preferredBody = new JComboBox(preferredBodyChoices); 
      preferencesPanel.add(preferredBody);
      preferredBody.addActionListener(this);

      preferencesPanel.add(new JLabel(wineResources.getString("SweetnessLabel")));
      preferredGrowthness = new JComboBox(preferredGrowthnessChoices); 
      preferencesPanel.add(preferredGrowthness);
      preferredGrowthness.addActionListener(this);

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
 
      mealPanel.add(new JLabel(wineResources.getString("MainCourseLabel")));
      mainCourse = new JComboBox(mainCourseChoices); 
      mealPanel.add(mainCourse);
      mainCourse.addActionListener(this);
    
      mealPanel.add(new JLabel(wineResources.getString("SauceLabel")));
      riskappetite = new JComboBox(riskappetiteChoices); 
      mealPanel.add(riskappetite);
      riskappetite.addActionListener(this);

      mealPanel.add(new JLabel(wineResources.getString("FlavorLabel")));
      flavor = new JComboBox(flavorChoices); 
      mealPanel.add(flavor);
      flavor.addActionListener(this);
      
      /*==============================================*/
      /* Create a panel including the preferences and */
      /* meal panels and add it to the content pane.  */
      /*==============================================*/

      JPanel choicesPanel = new JPanel(); 
      choicesPanel.setLayout(new FlowLayout());
      choicesPanel.add(preferencesPanel);
      choicesPanel.add(mealPanel);
      
      jfrm.getContentPane().add(choicesPanel); 
 
      /*==================================*/
      /* Create the recommendation panel. */
      /*==================================*/

      wineList = new DefaultTableModel();

      wineList.setDataVector(new Object[][] { },
                             new Object[] { wineResources.getString("WineTitle"), 
                                            wineResources.getString("RecommendationTitle"),
					    "Comments"});
         
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
       
      preferredColor.setSelectedIndex(0); 
      preferredBody.setSelectedIndex(0); 
      preferredGrowthness.setSelectedIndex(0); 
      mainCourse.setSelectedIndex(0);
      riskappetite.setSelectedIndex(0);
      flavor.setSelectedIndex(0);

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
            
      item = preferredColorNames[preferredColor.getSelectedIndex()];
      
      if (item.equals("Equity"))   
        { clips.assertString("(attribute (name preferred-color) (value equity))"); }
      else if (item.equals("Hybrid"))   
        { clips.assertString("(attribute (name preferred-color) (value hybrid))"); }
      else
        { clips.assertString("(attribute (name preferred-color) (value unknown))"); }

      item = preferredBodyNames[preferredBody.getSelectedIndex()];
      if (item.equals("Small"))   
        { clips.assertString("(attribute (name preferred-cap) (value small))"); }
      else if (item.equals("Medium"))   
        { clips.assertString("(attribute (name preferred-cap) (value medium))"); }
      else if (item.equals("Large"))   
        { clips.assertString("(attribute (name preferred-cap) (value large))"); }
      else
        { clips.assertString("(attribute (name preferred-cap) (value unknown))"); }
 
      item = preferredGrowthnessNames[preferredGrowthness.getSelectedIndex()];
      if (item.equals("TaxPlanning"))   
        { clips.assertString("(attribute (name preferred-purpose) (value taxplanning))"); }
      else if (item.equals("Medium"))   
        { clips.assertString("(attribute (name preferred-purpose) (value debt))"); }
      else if (item.equals("Growth"))   
        { clips.assertString("(attribute (name preferred-purpose) (value growth))"); }
      else
        { clips.assertString("(attribute (name preferred-purpose) (value unknown))"); }

      item = mainCourseNames[mainCourse.getSelectedIndex()];
      if (item.equalsIgnoreCase("Retirement") ||
          item.equalsIgnoreCase("ChildMarriage") ||
          item.equalsIgnoreCase("LongTermGrowth"))
        { 
         clips.assertString("(attribute (name main-goal) (value goallongterm))"); 
         clips.assertString("(attribute (name has-emergency) (value no))");
        }
      else if (item.equals("Emergency"))   
        { 
         clips.assertString("(attribute (name main-goal) (value goalmidterm))"); 
         clips.assertString("(attribute (name has-emergency) (value yes))");
        }
      else if (item.equals("ChildEducation") ||
               item.equals("Business"))   
        { 
         clips.assertString("(attribute (name main-goal) (value goalmidterm))"); 
         clips.assertString("(attribute (name has-emergency) (value no))");
        }
      else if (item.equals("HigherEducation"))   
        { 
         clips.assertString("(attribute (name main-goal) (value goalshortterm))"); 
         clips.assertString("(attribute (name has-emergency) (value no))");
        }
      else if (item.equals("Other"))   
        { 
         clips.assertString("(attribute (name main-goal) (value unknown))"); 
         clips.assertString("(attribute (name has-emergency) (value no))");
        }
      else
        { 
         clips.assertString("(attribute (name main-goal) (value unknown))"); 
         clips.assertString("(attribute (name has-emergency) (value unknown))");
        }

      item = riskappetiteNames[riskappetite.getSelectedIndex()];
      if (item.equals("None"))   
        { clips.assertString("(attribute (name has-riskappetite) (value no))"); }
      else if (item.equals("Moderate"))   
        { 
         clips.assertString("(attribute (name has-riskappetite) (value yes))");
         clips.assertString("(attribute (name riskappetite) (value moderate))");
        }
      else if (item.equals("Bold"))   
        { 
         clips.assertString("(attribute (name has-riskappetite) (value yes))");
         clips.assertString("(attribute (name riskappetite) (value sweet))");
        }
      else if (item.equals("Aggressive"))   
        { 
         clips.assertString("(attribute (name has-riskappetite) (value yes))");
         clips.assertString("(attribute (name riskappetite) (value aggressive))");
        }
      else if (item.equals("Blind"))   
        { 
         clips.assertString("(attribute (name has-riskappetite) (value yes))");
         clips.assertString("(attribute (name riskappetite) (value blind))");
        }
      else
        { 
         clips.assertString("(attribute (name has-riskappetite) (value unknown))");
         clips.assertString("(attribute (name riskappetite) (value unknown))");
        }

      item = flavorNames[flavor.getSelectedIndex()];
      if (item.equals("ShortTerm"))   
        { clips.assertString("(attribute (name horizon) (value short))"); }
      else if (item.equals("MidTerm")) 
        { clips.assertString("(attribute (name horizon) (value mid))"); }
      else if (item.equals("LongTerm"))   
        { clips.assertString("(attribute (name horizon) (value long))"); }
      else
        { clips.assertString("(attribute (name horizon) (value unknown))"); }
      
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
         PrimitiveValue obj = fv.getFactSlot("comments");         
        String comm = ""; 
	if (obj != null)
		comm = obj.toString();
	
	wineList.addRow(new Object[] { wineName, new Integer(certainty), comm });
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
