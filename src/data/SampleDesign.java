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

import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
* @author Zachary J. Ward (zward@hsph.harvard.edu)
* @version 2.0
*/

@XmlRootElement(name="SampleDesign")
public class SampleDesign {
	//data
	@XmlElement public Design curDesign;
	
	@XmlElement double meanCost, var, sd;
	@XmlElement public double trueTotal;
	@XmlElement double meanErrorAbs, meanErrorRel;
	@XmlElement double errorAbs,errorRel;
	@XmlElement double probWithin[]; //[1,5,10,25]
	@XmlElement double percentInsufficient;
	
	/**
	 * [x/y][Density outcome]
	 */
	@XmlElement public double kde[][];
	@XmlElement (name="FinalSampUnit", type=FinalSampUnit.class) public FinalSampUnit finalSample;
	
	//transient
	@XmlTransient JTextArea textArea;
	@XmlTransient SampUnit sampleTree;
	@XmlTransient Constraints constraints;
	@XmlTransient public MersenneTwisterFast generator;
	
	/**
	 * Cost of obtaining the sample
	 */
	@XmlTransient double sampleCost[];
	/**
	 * Estimated total outcome
	 */
	@XmlTransient double sampleTotal[];
	
	@XmlTransient int curProg=0;
	@XmlTransient int underSampledCounter;
	@XmlTransient boolean underSampled;
	
	@XmlTransient ArrayList<SampUnit> samples[];
	
	
	public SampleDesign(JTextArea textArea, SampUnit sampleTree, Constraints constraints, Design curDesign){
		this.textArea=textArea;
		this.sampleTree=sampleTree;
		this.constraints=constraints;
		this.curDesign=curDesign;
		trueTotal=0;
		calcTrueOutcome(sampleTree);
		underSampledCounter=0;
		generator=new MersenneTwisterFast();
	}
	
	public SampleDesign(){ //no-arg constructor
		
	}
 	
	private void calcTrueOutcome(SampUnit curNode){
		trueTotal+=curNode.outcome;
		int numStrata=curNode.children.length;
		for(int s=0; s<numStrata; s++){
			int numChildren=curNode.children[s].size();
			for(int i=0; i<numChildren; i++){
				calcTrueOutcome(curNode.children[s].get(i));
			}
		}
	}
	
	public void buildSampleWeights(SampUnit curNode, int level){
		int numStrata=curNode.children.length;
		curNode.childWeights=new double[numStrata][];
		curNode.totalChildWeights=new double[numStrata];
		for(int s=0; s<numStrata; s++){
			int numChildren=curNode.children[s].size();
			if(numChildren>0){
				curNode.childWeights[s]=new double[numChildren];
				if(curDesign.pps[level]){ //PPS
					curNode.totalChildWeights[s]=0;
					for(int i=0; i<numChildren; i++){
						curNode.totalChildWeights[s]+=curNode.children[s].get(i).pps;
					}
					for(int i=0; i<numChildren; i++){
						SampUnit child=curNode.children[s].get(i);
						curNode.childWeights[s][i]=child.pps/curNode.totalChildWeights[s];
						buildSampleWeights(child,level+1);
					}
				}
				else{ //Uniform
					for(int i=0; i<numChildren; i++){
						SampUnit child=curNode.children[s].get(i);
						curNode.childWeights[s][i]=1.0/(numChildren*1.0);
						buildSampleWeights(child,level+1);
					}
				}
			}
		}
	}
	
	
	public void estimateIPW(int numIterations){
		resetIPW(sampleTree,0);
		
		//Sample
		samples=new ArrayList[numIterations];
		textArea.append("|");
		curProg=0;
		for(int i=0; i<numIterations; i++){
			samples[i]=new ArrayList<SampUnit>();
			//Sample levels/facilities recursively
			underSampled=false;
			sampleNode(sampleTree,0,i);
			
			//Update progress
			double prog=i/(numIterations*1.0);
			int intProg=(int) (prog*100);
			if(intProg>curProg){
				curProg=intProg;
				if(curProg%10==0){textArea.append(".");}
			}
		}
		
		double denom=(numIterations*1.0);
		calcIPW(sampleTree,0,denom);
	}
	
	public void calculateCosts(int numIterations){
		sampleCost=new double[numIterations];
		sampleTotal=new double[numIterations];
		
		//Get samples
		for(int i=0; i<numIterations; i++){
			//Fixed cost of data collection at each level
			for(int j=0; j<curDesign.numLevels; j++){
				for(int s=0; s<curDesign.sampleInt[j].length; s++){
					sampleCost[i]+=constraints.fixedCost[j][s];
				}
			}
			//Get variable costs and outcomes
			int sampleSize=samples[i].size();
			for(int s=1; s<sampleSize; s++){ //skip root
				SampUnit curNode=samples[i].get(s);
				//Get variable cost of visiting this unit
				int level=curNode.level-1;
				int stratumIndex=0; //overall
				if(curDesign.stratified[level]){stratumIndex=curNode.stratumIndex+1;}
				if(constraints.useAverageCost[level][stratumIndex]){ //average
					sampleCost[i]+=constraints.averageCost[level][stratumIndex];
				}
				else{ //unit-specific
					sampleCost[i]+=curNode.cost; 
				}
				sampleTotal[i]+=curNode.outcome*curNode.ipw; //Get outcome
			}
			samples[i]=null; //remove sample
		}
		
		//Estimate KDE of outcomes
		kde=KernelSmooth.density(sampleTotal, 100);
		
		
		//Calculate variance of total cost
		meanCost=0;
		double meanX=0, meanX2=0;
		meanErrorAbs=0;
		probWithin=new double[4];
		double relThresh[]=new double[]{0.01,0.05,0.1,0.25};
		for(int i=0; i<numIterations; i++){
			meanCost+=sampleCost[i];
			meanX+=sampleTotal[i];
			meanX2+=(sampleTotal[i]*sampleTotal[i]);
			double error=Math.abs(sampleTotal[i]-trueTotal);
			meanErrorAbs+=error;
			double relError=error/trueTotal;
			for(int j=0; j<4; j++){
				if(relError<=relThresh[j]){
					probWithin[j]++;
				}
			}
		}
		meanX/=(numIterations*1.0);
		meanX2/=(numIterations*1.0);
		meanCost/=(numIterations*1.0);
		meanErrorAbs/=(numIterations*1.0);
		meanErrorRel=meanErrorAbs/trueTotal;
		errorAbs=Math.abs(meanX-trueTotal);
		errorRel=errorAbs/trueTotal;
		for(int j=0; j<4; j++){
			probWithin[j]/=(numIterations*1.0);
		}
		
		var=meanX2-meanX*meanX; //E[X^2]-E[X]^2
		sd=Math.sqrt(var);
		textArea.append("|");
		
		percentInsufficient=0;
		if(underSampledCounter>0){
			double frac=underSampledCounter/(numIterations*1.0);
			double percent=round(frac*100,2);
			percentInsufficient=percent;
			textArea.append(" Warning: "+percent+"% of samples were insufficient\n");
		}
		else{
			textArea.append("\n");
		}
	}
	
	private double round(double num, int decimals){
		double prec=Math.pow(10, decimals);
		double val=(Math.round(num*prec)/prec);
		return(val);
	}
	
	public void getFinalSample(){
		finalSample=new FinalSampUnit(sampleTree);
		sampleFinalNode(finalSample,sampleTree,0);
	}
	
	/**
	 * 
	 * @param Axis index, for chart
	 * @return
	 */
	public double getValue(int index){
		switch(index){
		case 0: return(meanCost);
		case 1: return(sd);
		case 2: return(meanErrorAbs);
		case 3: return(meanErrorRel);
		case 4: return(probWithin[0]);
		case 5: return(probWithin[1]);
		case 6: return(probWithin[2]);
		case 7: return(probWithin[3]);
		case 8: return(errorAbs);
		}
		return(-1); //fell through
	}
	
	public void addToTable(DefaultTableModel model, int num, SampleFrame frame){
		int r=model.getRowCount();
		model.addRow(new Object[]{null});
		
		//design
		model.setValueAt(num+1, r, 0);
		//strata
		String strata=getType(curDesign.stratified[0]);
		for(int i=1; i<curDesign.numLevels; i++){
			strata+="-"+getType(curDesign.stratified[i]);
		}
		model.setValueAt(strata, r, 1);
		//pps
		String pps=getPPS(curDesign.pps[0]);
		for(int i=1; i<curDesign.numLevels; i++){
			pps+="-"+getPPS(curDesign.pps[i]);
		}
		model.setValueAt(pps, r, 2);
		//units
		for(int i=0; i<curDesign.numLevels; i++){
			if(curDesign.stratified[i]==false){ //overall
				if(curDesign.sampleInt[i][0]){model.setValueAt(curDesign.sampleNums[i][0], r, 3+i);}
				else{model.setValueAt(curDesign.samplePercents[i][0]*100+"%", r, 3+i);}
			}
			else{
				String strat="";
				for(int s=0; s<curDesign.sampleInt[i].length; s++){
					if(s>0){strat+=" ";}
					strat+=frame.levels[i].strata.get(s)+": ";
					if(curDesign.sampleInt[i][s]){strat+=curDesign.sampleNums[i][s];}
					else{strat+=curDesign.samplePercents[i][s]*100+"%";}
				}
				model.setValueAt(strat,r,3+i);
			}
		}
		//results
		int c=3+curDesign.numLevels;
		model.setValueAt(round(meanCost,2), r, c); c++;
		model.setValueAt(round(sd,2), r, c); c++;
		model.setValueAt(round(meanErrorAbs,2), r, c); c++;
		model.setValueAt(round(meanErrorRel*100,2), r, c); c++;
		for(int i=0; i<4; i++){
			model.setValueAt(round(probWithin[i]*100,2), r, c); c++;
		}
		model.setValueAt(percentInsufficient, r, c); c++;
	}
	
	private String getType(boolean strata){
		if(strata==false){return("Simple");}
		else{return("Stratified");}
	}
	
	private String getPPS(boolean pps){
		if(pps==false){return("Equal");}
		else{return("PPS");}
	}
	
	private void resetIPW(SampUnit curNode, int level){
		curNode.numSampled=0;
		curNode.ipw=1;
		int numStrata=curNode.children.length;
		for(int s=0; s<numStrata; s++){
			int numChildren=curNode.children[s].size();
			for(int i=0; i<numChildren; i++){
				resetIPW(curNode.children[s].get(i),level+1);
			}
		}
	}
	
	private void calcIPW(SampUnit curNode, int level, double denom){
		if(curNode.numSampled!=0){
			double wt=curNode.numSampled/denom;
			curNode.ipw=1.0/wt; //IPW
		}
		else{ //Avoid div/0
			curNode.ipw=1.0;
		}
		int numStrata=curNode.children.length;
		for(int s=0; s<numStrata; s++){
			int numChildren=curNode.children[s].size();
			for(int i=0; i<numChildren; i++){
				calcIPW(curNode.children[s].get(i),level+1,denom);
			}
		}
	}
	
	private void sampleNode(SampUnit curNode, int level, int iteration){
		curNode.numSampled++;
		samples[iteration].add(curNode);
			
		if(level<curDesign.numLevels){ //not last level
			int numStrata=curNode.children.length;

			if(curDesign.stratified[level]){ //stratified
				for(int s=0; s<numStrata; s++){
					int numChildren=curNode.children[s].size();
					if(numChildren>0){
						double curWeights[]=new double[numChildren];
						for(int i=0; i<numChildren; i++){
							curWeights[i]=curNode.childWeights[s][i];
						}

						int targetSample=curDesign.sampleNums[level][s];
						if(curDesign.sampleInt[level][s]==false){ //percentage
							targetSample=(int) Math.round(curDesign.samplePercents[level][s]*numChildren);
							//check bounds
							if(curDesign.floor[level][s]!=-1 && targetSample<curDesign.floor[level][s]){
								targetSample=curDesign.floor[level][s];
							}
							if(curDesign.ceiling[level][s]!=-1 && targetSample>curDesign.ceiling[level][s]){
								targetSample=curDesign.ceiling[level][s];
							}
						}
												
						if(numChildren<=targetSample){ //get all children
							if(underSampled==false && numChildren<targetSample){
								underSampled=true;
								underSampledCounter++;
							}
							for(int i=0; i<numChildren; i++){
								SampUnit child=curNode.children[s].get(i);
								sampleNode(child,level+1,iteration);
							}
						}
						else{
							for(int i=0; i<targetSample; i++){
								double cumWeights[]=makeCumulative(curWeights);
								double rand=generator.nextDouble();
								int index=0;
								while(rand>cumWeights[index]){index++;}
								curWeights[index]=0; //Remove from sample frame
								SampUnit child=curNode.children[s].get(index);
								sampleNode(child,level+1,iteration);
							}
						}
					}
				}
			}
			else{ //Not stratified
				int childCounts[]=new int[numStrata];
				double childWeights[][]=new double[numStrata][];
				int totalChildren=0;
				for(int s=0; s<numStrata; s++){
					int numChildren=curNode.children[s].size();
					childCounts[s]=numChildren;
					totalChildren+=numChildren;
					childWeights[s]=new double[numChildren];
					for(int i=0; i<numChildren; i++){
						childWeights[s][i]=curNode.childWeights[s][i];
					}
				}
				double curWeights[]=new double[totalChildren];
				int strataIndex[]=new int[totalChildren];
				int childIndex[]=new int[totalChildren];
				int index=0;
				for(int s=0; s<numStrata; s++){
					int numChildren=curNode.children[s].size();
					for(int i=0; i<numChildren; i++){
						curWeights[index]=childWeights[s][i];
						strataIndex[index]=s;
						childIndex[index]=i;
						index++;	
					}
				}

				int targetSample=curDesign.sampleNums[level][0];
				if(curDesign.sampleInt[level][0]==false){
					targetSample=(int) Math.round(curDesign.samplePercents[level][0]*totalChildren);
					//check bounds
					if(curDesign.floor[level][0]!=-1 && targetSample<curDesign.floor[level][0]){
						targetSample=curDesign.floor[level][0];
					}
					if(curDesign.ceiling[level][0]!=-1 && targetSample>curDesign.ceiling[level][0]){
						targetSample=curDesign.ceiling[level][0];
					}
				}
				
				if(totalChildren<=targetSample){ //get all children
					if(underSampled==false && totalChildren<targetSample){
						underSampled=true;
						underSampledCounter++;
					}
					for(int s=0; s<numStrata; s++){
						int numChildren=curNode.children[s].size();
						for(int i=0; i<numChildren; i++){
							SampUnit child=curNode.children[s].get(i);
							sampleNode(child,level+1,iteration);
						}
					}
				}
				else{
					for(int i=0; i<targetSample; i++){
						double cumWeights[]=makeCumulative(curWeights);
						double rand=generator.nextDouble();
						index=0;
						while(rand>cumWeights[index]){index++;}
						curWeights[index]=0; //Remove from sample frame
						int s=strataIndex[index];
						int c=childIndex[index];
						SampUnit child=curNode.children[s].get(c);
						sampleNode(child,level+1,iteration);
					}
				}
			}
		}
	}
	
	private void sampleFinalNode(FinalSampUnit parentNode, SampUnit curNode, int level){
					
		if(level<curDesign.numLevels){ //not last level
			int numStrata=curNode.children.length;

			if(curDesign.stratified[level]){ //stratified
				for(int s=0; s<numStrata; s++){
					int numChildren=curNode.children[s].size();
					if(numChildren>0){
						double curWeights[]=new double[numChildren];
						for(int i=0; i<numChildren; i++){
							curWeights[i]=curNode.childWeights[s][i];
						}

						int targetSample=curDesign.sampleNums[level][s];
						if(curDesign.sampleInt[level][s]==false){ //percentage
							targetSample=(int) Math.round(curDesign.samplePercents[level][s]*numChildren);
							//check bounds
							if(curDesign.floor[level][s]!=-1 && targetSample<curDesign.floor[level][s]){
								targetSample=curDesign.floor[level][s];
							}
							if(curDesign.ceiling[level][s]!=-1 && targetSample>curDesign.ceiling[level][s]){
								targetSample=curDesign.ceiling[level][s];
							}
						}
												
						if(numChildren<=targetSample){ //get all children
							for(int i=0; i<numChildren; i++){
								SampUnit child=curNode.children[s].get(i);
								FinalSampUnit finalChild=new FinalSampUnit(child);
								parentNode.children.add(finalChild);
								sampleFinalNode(finalChild,child,level+1);
							}
						}
						else{
							for(int i=0; i<targetSample; i++){
								double cumWeights[]=makeCumulative(curWeights);
								double rand=generator.nextDouble();
								int index=0;
								while(rand>cumWeights[index]){index++;}
								curWeights[index]=0; //Remove from sample frame
								SampUnit child=curNode.children[s].get(index);
								FinalSampUnit finalChild=new FinalSampUnit(child);
								parentNode.children.add(finalChild);
								sampleFinalNode(finalChild,child,level+1);
							}
						}
					}
				}
			}
			else{ //Not stratified
				int childCounts[]=new int[numStrata];
				double childWeights[][]=new double[numStrata][];
				int totalChildren=0;
				for(int s=0; s<numStrata; s++){
					int numChildren=curNode.children[s].size();
					childCounts[s]=numChildren;
					totalChildren+=numChildren;
					childWeights[s]=new double[numChildren];
					for(int i=0; i<numChildren; i++){
						childWeights[s][i]=curNode.childWeights[s][i];
					}
				}
				double curWeights[]=new double[totalChildren];
				int strataIndex[]=new int[totalChildren];
				int childIndex[]=new int[totalChildren];
				int index=0;
				for(int s=0; s<numStrata; s++){
					int numChildren=curNode.children[s].size();
					for(int i=0; i<numChildren; i++){
						curWeights[index]=childWeights[s][i];
						strataIndex[index]=s;
						childIndex[index]=i;
						index++;	
					}
				}

				int targetSample=curDesign.sampleNums[level][0];
				if(curDesign.sampleInt[level][0]==false){
					targetSample=(int) Math.round(curDesign.samplePercents[level][0]*totalChildren);
					//check bounds
					if(curDesign.floor[level][0]!=-1 && targetSample<curDesign.floor[level][0]){
						targetSample=curDesign.floor[level][0];
					}
					if(curDesign.ceiling[level][0]!=-1 && targetSample>curDesign.ceiling[level][0]){
						targetSample=curDesign.ceiling[level][0];
					}
				}
				
				if(totalChildren<=targetSample){ //get all children
					for(int s=0; s<numStrata; s++){
						int numChildren=curNode.children[s].size();
						for(int i=0; i<numChildren; i++){
							SampUnit child=curNode.children[s].get(i);
							FinalSampUnit finalChild=new FinalSampUnit(child);
							parentNode.children.add(finalChild);
							sampleFinalNode(finalChild,child,level+1);
						}
					}
				}
				else{
					for(int i=0; i<targetSample; i++){
						double cumWeights[]=makeCumulative(curWeights);
						double rand=generator.nextDouble();
						index=0;
						while(rand>cumWeights[index]){index++;}
						curWeights[index]=0; //Remove from sample frame
						int s=strataIndex[index];
						int c=childIndex[index];
						SampUnit child=curNode.children[s].get(c);
						FinalSampUnit finalChild=new FinalSampUnit(child);
						parentNode.children.add(finalChild);
						sampleFinalNode(finalChild,child,level+1);
					}
				}
			}
		}
	}
	
	private double[] makeCumulative(double weights[]){
		int num=weights.length;
		double sumWeights=0;
		double cumWeights[]=new double[num];
		for(int i=0; i<num; i++){sumWeights+=weights[i];}
		cumWeights[0]=weights[0]/sumWeights;
		for(int i=1; i<num-1; i++){
			cumWeights[i]=cumWeights[i-1]+(weights[i]/sumWeights);
		}
		cumWeights[num-1]=1.0;
		return(cumWeights);
	}
	
}