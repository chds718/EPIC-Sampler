/**
 * EPIC Sampler
 * Copyright (C) 2018-2019 Center for Health Decision Science, Harvard T.H. Chan School of Public Health
 *
 * This file is part of the EPIC Sampler. The EPIC Sampler is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The EPIC Sampler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with The EPIC Sampler.  If not, see <http://www.gnu.org/licenses/>.
 */

package data;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import gui.frmMain;

/**
* @author Zachary J. Ward (zward@hsph.harvard.edu)
* @version 2.0
*/

@XmlRootElement(name="Constraints")
public class Constraints{
	
	@XmlElement public int numLevels;
	
	/**
	 * 0=Simple, 1=PPS, 2=Both
	 */
	@XmlElement public int pps[];
	/**
	 * 0=No, 1=Yes, 2=Both
	 */
	@XmlElement public int stratified[];
	
	/**
	 * [Levels][Strata]
	 */
	@XmlElement public boolean sampleInt[][];
	@XmlElement public int minUnits[][], maxUnits[][];
	@XmlElement public double minPercent[][], maxPercent[][], stepPercent[][];
	@XmlElement public int floor[][], ceiling[][];
	
	//costs
	@XmlElement double fixedCost[][];
	@XmlElement boolean useAverageCost[][];
	@XmlElement double averageCost[][];
	
	@XmlElement public double minBudget, maxBudget;
	@XmlElement public int numIterations;
	@XmlElement public boolean seedSims;
	@XmlElement public int seed;
	
	//Constructor
	public Constraints(SampleFrame frame){
		numLevels=frame.numLevels;
		
		pps=new int[numLevels];
		stratified=new int[numLevels];
		
		sampleInt=new boolean[numLevels][];
		minUnits=new int[numLevels][];
		maxUnits=new int[numLevels][];
		minPercent=new double[numLevels][];
		maxPercent=new double[numLevels][];
		stepPercent=new double[numLevels][];
		floor=new int[numLevels][];
		ceiling=new int[numLevels][];
		
		fixedCost=new double[numLevels][];
		useAverageCost=new boolean[numLevels][];
		averageCost=new double[numLevels][];
	}
	
	public Constraints(){ //no-arg constructor
		
	}
	
	public ArrayList<String> parse(frmMain mainForm){
		//Parse constraints
		ArrayList<String> errors=new ArrayList<String>();
		
		//Level sampling
		for(int i=0; i<numLevels; i++){
			//stratified
			String str=(String) mainForm.tableLevelDesign.getValueAt(i, 1);
			if(str.equals("Simple")){stratified[i]=0;}
			else if(str.equals("Stratified")){stratified[i]=1;}
			else if(str.equals("Simple OR Stratified")){stratified[i]=2;}
			//probability
			str=(String) mainForm.tableLevelDesign.getValueAt(i, 2);
			if(str.equals("Equal")){pps[i]=0;}
			else if(str.equals("Proportional to size (PPS)")){pps[i]=1;}
			else if(str.equals("Equal OR PPS")){pps[i]=2;}
		}
		
		//Number of units
		int r=0;
		for(int i=0; i<numLevels; i++){
			int numStrata=1; //overall only
			if(stratified[i]>0){ //stratified
				numStrata=mainForm.curProject.sampleFrame.levels[i].numStrata+1;
			}
						
			sampleInt[i]=new boolean[numStrata];
			minUnits[i]=new int[numStrata]; maxUnits[i]=new int[numStrata];
			minPercent[i]=new double[numStrata]; maxPercent[i]=new double[numStrata];
			stepPercent[i]=new double[numStrata];
			floor[i]=new int[numStrata]; ceiling[i]=new int[numStrata];
			
			//parse values
			int startS=0;
			if(stratified[i]==1){ //stratified only, skip overall
				startS=1;
			}
			for(int s=startS; s<numStrata; s++){
				String curLS="Level: "+mainForm.tableUnitBounds.getValueAt(r, 0)+", Stratum: "+mainForm.tableUnitBounds.getValueAt(r, 1);
				
				String str=(String) mainForm.tableUnitBounds.getValueAt(r, 2);
				if(str==null || str.isEmpty()){
					errors.add("Search Type - "+curLS);
				}
				else{
					if(str.matches("Count")){
						sampleInt[i][s]=true;
						try{
							minUnits[i][s]=Integer.parseInt((String) mainForm.tableUnitBounds.getValueAt(r, 3));
						} catch(Exception e){
							errors.add("Min Units - "+curLS);
						}
						try{
							maxUnits[i][s]=Integer.parseInt((String) mainForm.tableUnitBounds.getValueAt(r, 4));
						} catch(Exception e){
							errors.add("Max Units - "+curLS);
						}
						
						if(minUnits[i][s]<0){errors.add("Invalid Min Units - "+curLS);}
						if(maxUnits[i][s]<minUnits[i][s]){errors.add("Invalid Max Units - "+curLS);}
					}
					else if(str.matches("Proportion")){
						sampleInt[i][s]=false;
						try{
							minPercent[i][s]=Double.parseDouble((String) mainForm.tableUnitBounds.getValueAt(r, 3));
						} catch(Exception e){
							errors.add("Min Units - "+curLS);
						}
						try{
							maxPercent[i][s]=Double.parseDouble((String) mainForm.tableUnitBounds.getValueAt(r, 4));
						} catch(Exception e){
							errors.add("Max Units - "+curLS);
						}
						
						if(minPercent[i][s]<0 || minPercent[i][s]>1.0){errors.add("Invalid Min Units - "+curLS);}
						if(maxPercent[i][s]<0 || maxPercent[i][s]>1.0){errors.add("Invalid Max Units - "+curLS);}
						if(maxPercent[i][s]<minPercent[i][s]){errors.add("Invalid Max Units - "+curLS);}
						
						//optional fields
						//step size
						str=(String) mainForm.tableUnitBounds.getValueAt(r, 5); 
						if(str!=null && str.isEmpty()==false){ //parse value
							try{
								stepPercent[i][s]=Double.parseDouble(str);
							} catch(Exception e){
								errors.add("Step Size - "+curLS);
							}
							if(stepPercent[i][s]<=0 || stepPercent[i][s]>1.0){errors.add("Step Size - "+curLS);}
						}
						else{ //set to default of 4-5 intervals
							stepPercent[i][s]=(maxPercent[i][s]-minPercent[i][s])/4.0;
						}
						
						//floor
						floor[i][s]=-1;
						str=(String) mainForm.tableUnitBounds.getValueAt(r, 6); 
						if(str!=null && str.isEmpty()==false){
							try{
								floor[i][s]=Integer.parseInt(str);
							} catch(Exception e){
								errors.add("Floor - "+curLS);
							}
							if(floor[i][s]<0){errors.add("Floor - "+curLS);}
						}
						//ceiling
						ceiling[i][s]=-1;
						str=(String) mainForm.tableUnitBounds.getValueAt(r, 7);
						if(str!=null && str.isEmpty()==false){
							try{
								ceiling[i][s]=Integer.parseInt(str);
							} catch(Exception e){
								errors.add("Ceiling - "+curLS);
							}
							if(ceiling[i][s]<0){errors.add("Ceiling - "+curLS);}
							if(ceiling[i][s]<floor[i][s]){errors.add("Ceiling - "+curLS);}
						}
						
						
					}
				}
				r++;
			} //end strata loop
		} //end level loop
		
		//Data collection costs
		r=0;
		for(int i=0; i<numLevels; i++){
			int numStrata=1; //overall only
			if(stratified[i]>0){ //stratified
				numStrata=mainForm.curProject.sampleFrame.levels[i].numStrata+1;
			}
			fixedCost[i]=new double[numStrata];
			useAverageCost[i]=new boolean[numStrata];
			averageCost[i]=new double[numStrata];
			
			//parse values
			int startS=0;
			if(stratified[i]==1){ //stratified only
				startS=1;
			}
			for(int s=startS; s<numStrata; s++){
				String curLS="Level: "+mainForm.tableUnitCosts.getValueAt(r, 0)+", Stratum: "+mainForm.tableUnitBounds.getValueAt(r, 1);
				
				try{
					fixedCost[i][s]=Double.parseDouble((String) mainForm.tableUnitCosts.getValueAt(r, 2));
				}
				catch(Exception e){
					errors.add("Fixed Cost - "+curLS);
				}
				if(fixedCost[i][s]<0){
					errors.add("Fixed Cost - "+curLS);
				}
								
				String str=(String) mainForm.tableUnitCosts.getValueAt(r, 3);
				if(str.equals("Average")){
					useAverageCost[i][s]=true;
					
					String cost=(String) mainForm.tableUnitCosts.getValueAt(r, 4);
					try{
						averageCost[i][s]=Double.parseDouble(cost);
					} catch(Exception e){
						errors.add("Variable Cost - "+curLS);
					}
					if(averageCost[i][s]<0){
						errors.add("Variable Cost - "+curLS);
					}
				}
				else{
					useAverageCost[i][s]=false;
				}
				
				r++;
			} //end strata loop
		} //end level loop


		//Budget constraint
		try{
			String str=mainForm.textBudgetMin.getText().replaceAll(",", ""); //strip commas
			minBudget=Double.parseDouble(str);
		} catch(Exception e){
			errors.add("Budget constraint: Min");
		}
		try{
			String str=mainForm.textBudgetMax.getText().replaceAll(",", ""); //strip commas
			maxBudget=Double.parseDouble(str);
		} catch(Exception e){
			errors.add("Budget constraint: Max");
		}
		
		//Num iterations
		try{
			String str=mainForm.textNumSim.getText().replaceAll(",", ""); //strip commas
			numIterations=Integer.parseInt(str);
		} catch(Exception e){
			errors.add("# of simulations");
		}
		//Seed
		seedSims=mainForm.chckbxSeedSimulations.isSelected();
		if(seedSims){
			try{
				seed=Integer.parseInt(mainForm.textSeed.getText());
			}catch(Exception ex){
				errors.add("Invalid seed!");
			}
		}
		
		return(errors);
	}
			
	public void display(frmMain mainForm){
		SampleFrame curFrame=mainForm.curProject.sampleFrame;
		
		//Level sampling
		mainForm.modelLevelDesign.setRowCount(numLevels);
		for(int i=0; i<numLevels; i++){
			//level name
			mainForm.modelLevelDesign.setValueAt(curFrame.levels[i].name, i, 0);
			
			//stratified
			if(stratified[i]==0){mainForm.modelLevelDesign.setValueAt("Simple", i, 1);}
			else if(stratified[i]==1){mainForm.modelLevelDesign.setValueAt("Stratified", i, 1);}
			else if(stratified[i]==2){mainForm.modelLevelDesign.setValueAt("Simple OR Stratified", i, 1);}
			
			//probability
			if(pps[i]==0){mainForm.modelLevelDesign.setValueAt("Equal", i, 2);}
			else if(pps[i]==1){mainForm.modelLevelDesign.setValueAt("Proportional to size (PPS)", i, 2);}
			else if(pps[i]==2){mainForm.modelLevelDesign.setValueAt("Equal OR PPS", i, 2);}
		}
		
		//Number of units
		int r=0;
		mainForm.modelUnitBounds.setRowCount(0);
		for(int i=0; i<numLevels; i++){
			int numStrata=1; //overall only
			if(stratified[i]>0){ //stratified
				numStrata=curFrame.levels[i].numStrata+1;
			}
			String strataNames[]=new String[numStrata];
			strataNames[0]="[Overall]";
			for(int s=1; s<numStrata; s++){strataNames[s]=curFrame.levels[i].strata.get(s-1);}
			
			//display values
			int startS=0;
			if(stratified[i]==1){ //stratified only, skip overall
				startS=1;
			}
			for(int s=startS; s<numStrata; s++){
				mainForm.modelUnitBounds.addRow(new Object[]{null});
				mainForm.modelUnitBounds.setValueAt(curFrame.levels[i].name, r, 0);
				mainForm.modelUnitBounds.setValueAt(strataNames[s], r, 1);
				
				if(sampleInt[i][s]==true){ //Count
					mainForm.modelUnitBounds.setValueAt("Count", r, 2);
					mainForm.modelUnitBounds.setValueAt(minUnits[i][s]+"", r, 3);
					mainForm.modelUnitBounds.setValueAt(maxUnits[i][s]+"", r, 4);
					mainForm.modelUnitBounds.setValueAt("---", r, 5);
					mainForm.modelUnitBounds.setValueAt("---", r, 6);
					mainForm.modelUnitBounds.setValueAt("---", r, 7);
				}
				else{
					mainForm.modelUnitBounds.setValueAt("Proportion", r, 2);
					mainForm.modelUnitBounds.setValueAt(minPercent[i][s]+"", r, 3);
					mainForm.modelUnitBounds.setValueAt(maxPercent[i][s]+"", r, 4);
					//optional fields
					mainForm.modelUnitBounds.setValueAt(stepPercent[i][s]+"", r, 5);
					if(floor[i][s]!=-1){
						mainForm.modelUnitBounds.setValueAt(floor[i][s]+"", r, 6);
					}
					if(ceiling[i][s]!=-1){
						mainForm.modelUnitBounds.setValueAt(ceiling[i][s]+"", r, 7);
					}
				}
				r++;
			} //end strata loop
		} //end level loop
		
		//Data collection costs
		r=0;
		mainForm.modelUnitCosts.setRowCount(0);
		for(int i=0; i<numLevels; i++){
			int numStrata=1; //overall only
			if(stratified[i]>0){ //stratified
				numStrata=mainForm.curProject.sampleFrame.levels[i].numStrata+1;
			}
			String strataNames[]=new String[numStrata];
			strataNames[0]="[Overall]";
			for(int s=1; s<numStrata; s++){strataNames[s]=curFrame.levels[i].strata.get(s-1);}
						
			//parse values
			int startS=0;
			if(stratified[i]==1){ //stratified only
				startS=1;
			}
			for(int s=startS; s<numStrata; s++){
				mainForm.modelUnitCosts.addRow(new Object[]{null});
				mainForm.modelUnitCosts.setValueAt(curFrame.levels[i].name, r, 0);
				mainForm.modelUnitCosts.setValueAt(strataNames[s], r, 1);
				
				mainForm.modelUnitCosts.setValueAt(fixedCost[i][s]+"", r, 2);
				if(useAverageCost[i][s]==true){
					mainForm.modelUnitCosts.setValueAt("Average", r, 3);
					mainForm.modelUnitCosts.setValueAt(averageCost[i][s]+"", r, 4);
				}
				else{
					mainForm.modelUnitCosts.setValueAt("Unit-Specific", r, 3);
					mainForm.modelUnitCosts.setValueAt("---", r, 4);
				}
				r++;
			} //end strata loop
		} //end level loop


		//Budget constraint
		mainForm.textBudgetMin.setText(minBudget+"");
		mainForm.textBudgetMax.setText(maxBudget+"");
				
		//Num iterations
		mainForm.textNumSim.setText(numIterations+"");
		//Seed
		mainForm.chckbxSeedSimulations.setSelected(seedSims);
		if(seedSims){
			mainForm.textSeed.setText(seed+"");
		}
	}
}