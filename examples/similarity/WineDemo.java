import javax.swing.*; 
import javax.swing.border.*; 
import javax.swing.table.*;
import java.awt.*; 
import java.awt.event.*; 
 
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
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
 
JComboBox preferredMutualFund; 
   
   JLabel jlab; 

String preferredMutualFundChoices[] = new String[3];
   
   ResourceBundle wineResources;

   Environment clips;
  public ArrayList<MutualFund> mfList = new ArrayList<WineDemo.MutualFund>(); 
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

MutualFund mf = new MutualFund();
     
mf.name = "DSP Black Rock - Top 200 equity";
      mf.typeArr = new FundType[1];
      mf.typeArr[0] = FundType.equity;
      mf.capArr = new FundCap[1];
      mf.capArr[0] = FundCap.Large;
      mf.purposeArr = new FundPurpose[1];
      mf.purposeArr[0] = FundPurpose.ModerateGrowth;
      mf.sectorArr = new SectorType[2];
      mf.sectorArr[0] = SectorType.technology;
      mf.sectorArr[1] = SectorType.financial;
      mfList.add(mf);
      
      mf = new MutualFund();
      mf.name = "Franklin India Blue Chip";
      mf.typeArr = new FundType[1];
      mf.typeArr[0] = FundType.hybrid;
      mf.capArr = new FundCap[1];
      mf.capArr[0] = FundCap.Large;
      mf.purposeArr = new FundPurpose[1];
      mf.purposeArr[0] = FundPurpose.ModerateGrowth;
      mf.sectorArr = new SectorType[2];
      mf.sectorArr[0] = SectorType.services;
      mf.sectorArr[1] = SectorType.healthcare;
      mfList.add(mf); 
      
      preferredMutualFundChoices[0] = mfList.get(0).name;
      preferredMutualFundChoices[1] = mfList.get(1).name;

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
      preferredMutualFund = new JComboBox(preferredMutualFundChoices); 
      preferencesPanel.add(preferredMutualFund);
      preferredMutualFund.addActionListener(this);
 
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
        				"Performance", "Sector"}); 
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
      preferredMutualFund.setSelectedIndex(0); 
       
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
/***********/  
   private void runWine() throws Exception
     { 
      
      if (isExecuting) return;
      
      clips.reset();      
      
      int chosenIndex = preferredMutualFund.getSelectedIndex();
      
      MutualFund chosenFund;
      if (chosenIndex == -1) chosenFund = null;
      else chosenFund = this.mfList.get(chosenIndex);
      
      if (chosenFund != null){
    	  for (int i = 0; i < chosenFund.sectorArr.length; i++){
    		  SectorType sec = chosenFund.sectorArr[i];
    		  {clips.assertString("(attribute (name preferred-sector) (value " + sec.toString()+ "))");}
    	  } 
    	  for (int i = 0; i < chosenFund.typeArr.length; i++){
    		  FundType sec = chosenFund.typeArr[i];
    		  {clips.assertString("(attribute (name preferred-fundType) (value " + sec.toString()+ "))");}
    	  } 
      }
/*      
      if (item.equals("Equity"))   
        { clips.assertString("(attribute (name preferred-color) (value equity))"); }
      else if (item.equals("Hybrid"))   
        { clips.assertString("(attribute (name preferred-color) (value hybrid))"); }
      else
        { clips.assertString("(attribute (name preferred-color) (value unknown))"); }

      item = preferredBodyNames[preferredBody.getSelectedIndex()];
      if (item.equals("Small"))   
        { clips.assertString("(attribute (name preferred-body) (value small))"); }
      else if (item.equals("Medium"))   
        { clips.assertString("(attribute (name preferred-body) (value medium))"); }
      else if (item.equals("Large"))   
        { clips.assertString("(attribute (name preferred-body) (value large))"); }
      else
        { clips.assertString("(attribute (name preferred-body) (value unknown))"); }
 
      item = preferredGrowthnessNames[preferredGrowthness.getSelectedIndex()];
      if (item.equals("TaxPlanning"))   
        { clips.assertString("(attribute (name preferred-sweetness) (value taxplanning))"); }
      else if (item.equals("Medium"))   
        { clips.assertString("(attribute (name preferred-sweetness) (value debt))"); }
      else if (item.equals("Growth"))   
        { clips.assertString("(attribute (name preferred-sweetness) (value growth))"); }
      else
        { clips.assertString("(attribute (name preferred-sweetness) (value unknown))"); }

      item = mainCourseNames[mainCourse.getSelectedIndex()];
      if (item.equalsIgnoreCase("Retirement") ||
          item.equalsIgnoreCase("ChildMarriage") ||
          item.equalsIgnoreCase("LongTermGrowth"))
        { 
         clips.assertString("(attribute (name main-component) (value meat))"); 
         clips.assertString("(attribute (name has-turkey) (value no))");
        }
      else if (item.equals("Emergency"))   
        { 
         clips.assertString("(attribute (name main-component) (value poultry))"); 
         clips.assertString("(attribute (name has-turkey) (value yes))");
        }
      else if (item.equals("ChildEducation") ||
               item.equals("Business"))   
        { 
         clips.assertString("(attribute (name main-component) (value poultry))"); 
         clips.assertString("(attribute (name has-turkey) (value no))");
        }
      else if (item.equals("HigherEducation"))   
        { 
         clips.assertString("(attribute (name main-component) (value fish))"); 
         clips.assertString("(attribute (name has-turkey) (value no))");
        }
      else if (item.equals("Other"))   
        { 
         clips.assertString("(attribute (name main-component) (value unknown))"); 
         clips.assertString("(attribute (name has-turkey) (value no))");
        }
      else
        { 
         clips.assertString("(attribute (name main-component) (value unknown))"); 
         clips.assertString("(attribute (name has-turkey) (value unknown))");
        }

      item = sauceNames[sauce.getSelectedIndex()];
      if (item.equals("None"))   
        { clips.assertString("(attribute (name has-sauce) (value no))"); }
      else if (item.equals("Moderate"))   
        { 
         clips.assertString("(attribute (name has-sauce) (value yes))");
         clips.assertString("(attribute (name sauce) (value spicy))");
        }
      else if (item.equals("Bold"))   
        { 
         clips.assertString("(attribute (name has-sauce) (value yes))");
         clips.assertString("(attribute (name sauce) (value sweet))");
        }
      else if (item.equals("Aggressive"))   
        { 
         clips.assertString("(attribute (name has-sauce) (value yes))");
         clips.assertString("(attribute (name sauce) (value cream))");
        }
      else if (item.equals("Blind"))   
        { 
         clips.assertString("(attribute (name has-sauce) (value yes))");
         clips.assertString("(attribute (name sauce) (value unknown))");
        }
      else
        { 
         clips.assertString("(attribute (name has-sauce) (value unknown))");
         clips.assertString("(attribute (name sauce) (value unknown))");
        }

      item = flavorNames[flavor.getSelectedIndex()];
      if (item.equals("ShortTerm"))   
        { clips.assertString("(attribute (name tastinessa) (value delicate))"); }
      else if (item.equals("MidTerm")) 
        { clips.assertString("(attribute (name tastinessa) (value average))"); }
      else if (item.equals("LongTerm"))   
        { clips.assertString("(attribute (name tastinessa) (value strong))"); }
      else
        { clips.assertString("(attribute (name tastinessa) (value unknown))"); }
 */     
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
  
/*   private void runWine() throws Exception
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
     }*/
     
   /***************/
   /* updateWines */
   /***************/  
   private void updateWines() throws Exception
     { 
      String evalStr = "(WINES::get-wine-list)";
                                       
      PrimitiveValue pv = clips.eval(evalStr);
               
      wineList.setRowCount(0);

    ArrayList<FundInfo> fInfo = this.Portfolioize(pv);

      for (int i = 0; i < fInfo.size(); i++)
        {
         FundInfo fv = fInfo.get(i);

         double certainty = fv.ratio;

         String wineName = fv.name == null ? "" : fv.name;
         String sector = fv.sector == null ? "" : fv.sector;

         wineList.addRow(new Object[] { wineName + ":" + Double.toString(certainty), certainty, sector });
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

public enum SectorType {
           any,
           material,
           capitalGoods,
           conglomerates,
           construction,
           energy,
           financial,
           healthcare,
           services,
           technology,
           transportation,
           utilities
   }

   class FundInfo {
           String name;
           double ratio;
           ArrayList<SectorType> sectorList;
           String sector;

           public FundInfo(String name, double ratio, String sec){
                   this.name = name;
                   this.ratio = ratio;
                   this.sector = sec;
           }
   }
ArrayList<FundInfo> Portfolioize(PrimitiveValue funds) throws Exception{
           ArrayList<FundInfo> ret = new ArrayList<WineDemo.FundInfo>();
           /* 1. select the portfolio.
                  Rules: 1. Pick min 3 sectors.
                  2. Recommendation > 40%.

           */
           // 2. Decide the raio.
           for (int i = 0; i < funds.size(); i++)
       {
                   PrimitiveValue fv = funds.get(i);
		   PrimitiveValue val = fv.getFactSlot("performance");
		   if (SectorType.any.toString().equals(val.toString())) continue;
                   double certainty = val == null ? 0 : val.numberValue().doubleValue();
                   String wineName = fv.getFactSlot("value").stringValue();
                   String sectorSet = StripBraces(fv.getFactSlot("sector").toString());
		   if (SectorType.any.toString().equals(sectorSet)) continue;
                   FundInfo info = new FundInfo(wineName, certainty, sectorSet);
                   ret.add(info);
       }
           return ret;
   }

private static String StripBraces(String arg){
           if (arg.startsWith("(")){
                   arg = arg.substring(1);
                   if (arg.endsWith(")"))
                           arg = arg.substring(0, arg.length() - 1);
           }
          return arg;
   }
 
public enum FundType {
	   notinit,
	   equity,
	   hybrid
   }
   
   public enum FundCap {
	  NotInit,
	  Small,
	  Mid,
	  Large	  
   }
   
   public enum FundPurpose {
	  NotInit,
	  HighGrowth,
	  TaxPlanning,
	  ModerateGrowth
   }
   
   public class MutualFund {
	   public String name;
	   public FundType[] typeArr;
	   public FundCap[] capArr;
	   public FundPurpose[] purposeArr;
	   public double yoy;
	   public SectorType[] sectorArr;
   } 
  }
