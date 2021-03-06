package eu.carrade.amaury.Camelia.drawing.drawTools.tools;

import eu.carrade.amaury.Camelia.*;
import eu.carrade.amaury.Camelia.drawing.drawTools.core.*;
import eu.carrade.amaury.Camelia.drawing.whiteboard.*;
import eu.carrade.amaury.Camelia.game.*;
import org.bukkit.*;
import org.bukkit.inventory.*;

/*
 * This file is part of Camelia.
 *
 * Camelia is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Camelia is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Camelia.  If not, see <http://www.gnu.org/licenses/>.
 */
@ToolLocator(slot = 1)
public class SprayTool extends ContinuousDrawTool {

	private int strength = 1;

	private int i = 0;

	public SprayTool(Drawer drawer) {
		super(drawer);

		this.size = 3;
	}

	@Override
	public String getDisplayName() {
		return ChatColor.GREEN + "" + ChatColor.BOLD + "Aérographe";
	}

	@Override
	public String getDescription() {
		return ChatColor.GRAY + "Applique de la couleur aléatoirement dans une petite région, telle une bombe de peinture";
	}

	@Override
	public ItemStack getIcon(Drawer drawer) {
		return new ItemStack(Material.DEAD_BUSH);
	}

	@Override
	public void onRightClick(WhiteboardLocation targetOnScreen, Drawer drawer) {
		if (targetOnScreen == null) return;

		Camelia.getInstance().getWhiteboard().fillRandomly(targetOnScreen, 2 * size + 1, strength * 0.05, drawer.getColor(), this.mixColors);

		if (i < 5) {
			i++;
		} else {
			drawer.getPlayer().playSound(drawer.getPlayer().getLocation(), Sound.CAT_HISS, 0.05F, 2);
			i = 0;
		}
	}

	@Override
	public void onLeftClick(WhiteboardLocation targetOnScreen, Drawer drawer) {
		drawer.getPlayer().openInventory(Camelia.getInstance().getDrawingGuiManager().getSprayInventory(drawer));
	}

	public int getStrength() {
		return strength;
	}

	public void setStrength(int strength) {
		this.strength = strength;
	}
}
