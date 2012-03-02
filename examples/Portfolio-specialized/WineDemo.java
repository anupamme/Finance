import javax.swing.*; 
import javax.swing.border.*; 
import javax.swing.table.*;
import java.awt.*; 
import java.awt.event.*; 
 
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.StringTokenizer;
import java.util.Set;
import java.util.HashSet;


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
  
   JTextField preferredColor; 
   JTextField preferredBody; 
   JTextField preferredGrowthness; 
   
   JComboBox mainCourse; 
   JComboBox riskappetite; 
   JComboBox flavor; 
   
   JLabel jlab; 

   String mainCourseChoices[] = new String[9];
   String riskappetiteChoices[] = new String[6];
   String flavorChoices[] = new String[10];

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
      
      mainCourseChoices[0] = "0"; 
      mainCourseChoices[1] = "5000"; 
      mainCourseChoices[2] = "10000"; 
      mainCourseChoices[3] = "15000"; 
      mainCourseChoices[4] = "20000"; 
      mainCourseChoices[5] = "25000"; 
      mainCourseChoices[6] = "30000"; 
      mainCourseChoices[7] = "35000"; 
      mainCourseChoices[8] = "40000"; 
   
      riskappetiteChoices[0] = "0"; 
      riskappetiteChoices[1] = "2"; 
      riskappetiteChoices[2] = "5"; 
      riskappetiteChoices[3] = "7"; 
      riskappetiteChoices[4] = "10"; 
      riskappetiteChoices[5] = "12"; 

      flavorChoices[0] = "0"; 
      flavorChoices[1] = "1";
      flavorChoices[2] = "2";
      flavorChoices[3] = "3";
      flavorChoices[4] = "4";
      flavorChoices[5] = "5";
      flavorChoices[6] = "6";
      flavorChoices[7] = "7";
      flavorChoices[8] = "8";
      flavorChoices[9] = "9";

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
      preferredColor = new JTextField("Interest Rate"); 
      preferencesPanel.add(preferredColor);
      preferredColor.addActionListener(this);
     
      preferencesPanel.add(new JLabel(wineResources.getString("BodyLabel")));
      preferredBody = new JTextField("Sample2"); 
      preferencesPanel.add(preferredBody);
      preferredBody.addActionListener(this);

      preferencesPanel.add(new JLabel(wineResources.getString("SweetnessLabel")));
      preferredGrowthness = new JTextField("Maturity Amount"); 
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
            
      

      item = mainCourseChoices[mainCourse.getSelectedIndex()];
      double monthly = Double.parseDouble(item);

      item = riskappetiteChoices[riskappetite.getSelectedIndex()];
      double increase = Double.parseDouble(item);

      item = flavorChoices[flavor.getSelectedIndex()];
      int years = Integer.parseInt(item);
      double rate = years + 2;
      String tmp = new Double(rate).toString();
      preferredColor.setText(tmp);
	double final1 = ComputeTotal(monthly, increase, years, rate);
	String rateStr = new Double(rate).toString();
	preferredGrowthness.setText(new Double(final1).toString());	
     	String str = "(attribute (name rate) (value " + rateStr + "))";
	preferredBody.setText(str);
	clips.assertString(str);
 
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
      
      ArrayList<FundInfo> fInfo = this.Portfolioize(pv);
      
      for (int i = 0; i < fInfo.size(); i++) 
        {
         FundInfo fv = fInfo.get(i);

         double certainty = fv.ratio; 
         
         String wineName = fv.name == null ? "" : fv.name;
         String sector = fv.sector == null ? "" : fv.sector;
                  
         wineList.addRow(new Object[] { wineName + ":" + Double.toString(certainty), certainty, sector });
        } 
/*
	for (int i = 0; i < pv.size(); i++) 
        {
         PrimitiveValue fv = pv.get(i);

         int certainty = fv.getFactSlot("certainty").numberValue().intValue(); 
         
         String wineName = fv.getFactSlot("value").stringValue();
         PrimitiveValue obj = fv.getFactSlot("sector");         
        String comm = ""; 
	if (obj != null)
		comm = obj.toString();
	
	wineList.addRow(new Object[] { wineName, new Integer(certainty), comm });
        }  
*/        
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
	int numFundsTotal = 5;
   ArrayList<FundInfo> Portfolioize(PrimitiveValue funds) throws Exception{
	   ArrayList<FundInfo> ret = new ArrayList<WineDemo.FundInfo>();
	   /* 1. select the portfolio.
	   	  Rules: 1. Pick min 3 sectors. 
	   	  2. Recommendation > 40%.
	   	   
	   */
	   ArrayList<PrimitiveValue> intermed = new ArrayList<PrimitiveValue>();
	   int selectedFunds = 0;
	   int totalCertainty = 0;
	   Set<SectorType> sectorsPresent = new HashSet<WineDemo.SectorType>();
	   for (int i = 0; i < funds.size(); i++){
		   if (selectedFunds == numFundsTotal)
			   break;
		   if (selectedFunds > numFundsTotal) throw new Exception("Total Sectors cannot be greater than 3: " + selectedFunds);
		   PrimitiveValue fv = funds.get(i);
		   int certainty = fv.getFactSlot("certainty").numberValue().intValue();
		   String sectorSet = StripBraces(fv.getFactSlot("sector").toString());
		   StringTokenizer st = new StringTokenizer(sectorSet, " ");
		   boolean isThere = false;
		   while(st.hasMoreTokens()){
			   String sector = st.nextToken();
			   SectorType secType = SectorType.valueOf(sector);
			  if (secType == SectorType.any) continue;
			   if (!sectorsPresent.contains(secType) || selectedFunds >= numFundsTotal){
				   sectorsPresent.add(secType);
				   isThere = true;
			   }
		   }
		   if (isThere){
			intermed.add(fv);
			totalCertainty += certainty;
			selectedFunds++;
		   }
	   }
	   // 2. Decide the raio.
	   for (int i = 0; i < intermed.size(); i++) 
       {
		   PrimitiveValue fv = intermed.get(i);
		   int certainty = fv.getFactSlot("certainty").numberValue().intValue(); 
		   String wineName = fv.getFactSlot("value").stringValue();
		   String sectorSet = StripBraces(fv.getFactSlot("sector").toString());
		   FundInfo info = new FundInfo(wineName, ((double)certainty*100)/((double)totalCertainty), sectorSet);
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

   private static double ComputeTotal(double monthly, double inc, int years, double rate){
	
	   double monthlyrate = rate/12.0;
	   double res = (monthly * 12 * (double) years) * ( 1 + rate/100.0) * (1 + (years * inc)/100.0);
	   return res;
   } 
  }
