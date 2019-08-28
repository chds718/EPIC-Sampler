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

package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.Icon;


/**
* @author Zachary J. Ward (zward@hsph.harvard.edu)
* @version 2.0
*/

public class ScaledIcon implements Icon{
	Image image;
	BufferedImage imageDisabled;
	int width, height;
	boolean enabled;
	
	//to paint
	Component c;
	Graphics g; 
	int x, y;
	
	//constructor
	public ScaledIcon(String basePath, int width, int height, boolean enabled) {
		try {
		this.width=width;
		this.height=height;
		this.enabled=enabled;
		
		int screenRes=Toolkit.getDefaultToolkit().getScreenResolution();
		int res=16; //16 pixels default
		if(screenRes>120) {res=128;}
		String path=basePath+"_"+res+".png";
		
		if(enabled==true) {
			image=Toolkit.getDefaultToolkit().getImage(frmMain.class.getResource(path));
		}
		else {
			imageDisabled = ImageIO.read(frmMain.class.getResource(path));
			int w=imageDisabled.getWidth();
			int h=imageDisabled.getHeight();

			//convert to grayscale
			for(int y = 0; y < h; y++){
				for(int x = 0; x < w; x++){

					Color c = new Color(imageDisabled.getRGB(x, y),true);

					int red = (int) (c.getRed() * 0.299);
					int green = (int) (c.getGreen() * 0.587);
					int blue = (int) (c.getBlue() * 0.114);
					int alpha = (int) (c.getAlpha() * 0.5);
					Color newColor = new Color(
							red + green + blue,
							red + green + blue,
							red + green + blue,
							alpha);
					imageDisabled.setRGB(x, y, newColor.getRGB());

				}
			}
		}

		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		if(enabled) {
			g.drawImage(image, x, y, width, height, null);
		}
		else {
			g.drawImage(imageDisabled, x, y, width, height, null);
		}	
		c.repaint();
		c.revalidate();
	}

	@Override
	public int getIconWidth() {
		return width;
	}

	@Override
	public int getIconHeight() {
		return height;
	}
	
	
	
	
}