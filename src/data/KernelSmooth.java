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

/**
* @author Zachary J. Ward (zward@hsph.harvard.edu)
* @version 2.0
*/
public final class KernelSmooth{
	
	public static double[][] density(double data[], int n){
		int numX=data.length;
		double min=data[0], max=data[0];
		for(int i=0; i<numX; i++){
			min=Math.min(min, data[i]);
			max=Math.max(max, data[i]);
		}
		double range=max-min;
		min=min-range/10.0; max=max+range/10.0;
		range=max-min; //update range
		double nStep=range/((n-1)*1.0);
		//set up grid
		double density[][]=new double[2][n];
		
		//calculate standard deviation
		double eX=0, eX2=0;
		for(int i=0; i<numX; i++){
			eX+=data[i];
			eX2+=(data[i]*data[i]);
		}
		eX=eX/(numX*1.0);
		eX2=eX2/(numX*1.0);
		double var=eX2-eX*eX;
		double sd=Math.sqrt(var);
		double h=(4*Math.pow(sd, 5))/(3.0*n); //bandwidth
		h=Math.pow(h, 0.2);
		
		double norm=1.0/Math.sqrt(2.0*Math.PI); //normalizing constant
		
		for(int i=0; i<n; i++){
			double curX=min+nStep*i; //x location
			density[0][i]=curX;
			double curDensity=0;
			for(int j=0; j<numX; j++){
				double xStar=(curX-data[j])/h; //(x-x_i)/h
				curDensity+=norm*Math.exp(-0.5*xStar*xStar);
			}
			curDensity=curDensity/(n*h);
			density[1][i]=curDensity;
		}
		
		return(density);
	}
}