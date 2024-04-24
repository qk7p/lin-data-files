/*
 * Copyright © 2004-2024 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.datapack.handlers.bypasshandlers;

import static com.l2jserver.gameserver.config.Configuration.character;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.gameserver.data.xml.impl.SkillTreesData;
import com.l2jserver.gameserver.handler.IBypassHandler;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.base.ClassId;
import com.l2jserver.gameserver.model.events.EventDispatcher;
import com.l2jserver.gameserver.model.events.impl.character.player.PlayerLearnSkillRequested;
import com.l2jserver.gameserver.network.serverpackets.ActionFailed;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * Learn Skill.
 * @author Zoey76
 * @version 2.6.3.0
 */
public class LearnSkill implements IBypassHandler {
	
	private static final Logger LOG = LoggerFactory.getLogger(LearnSkill.class);
	
	private static final String[] COMMANDS = {
		"SkillList",
		"learn_skill"
	};
	
	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target) {
		if (!(target instanceof L2NpcInstance npc)) {
			return false;
		}
		
		// L2J Custom.
		if (character().skillLearn()) {
			showCustomLearnSkill(command, activeChar, npc);
			return true;
		}
		
		if (command.equals(COMMANDS[0])) {
			L2NpcInstance.showSkillList(activeChar, npc, activeChar.getClassId());
		} else if (command.equals(COMMANDS[1])) {
			EventDispatcher.getInstance().notifyEventAsync(new PlayerLearnSkillRequested(activeChar), target);
		}
		return true;
	}
	
	@Override
	public String[] getBypassList() {
		return COMMANDS;
	}
	
	private static void showCustomLearnSkill(String command, L2PcInstance player, L2NpcInstance npc) {
		try {
			final var id = command.substring(9).trim();
			if (id.length() != 0) {
				L2NpcInstance.showSkillList(player, npc, ClassId.getClassId(Integer.parseInt(id)));
			} else {
				boolean own_class = false;
				final var classesToTeach = npc.getClassesToTeach();
				for (var cid : classesToTeach) {
					if (cid.equalsOrChildOf(player.getClassId())) {
						own_class = true;
						break;
					}
				}
				
				final var text = new StringBuilder("<html><body><center>Skill learning:</center><br>");
				if (!own_class) {
					text.append("Skills of your class are the easiest to learn.<br>")
						.append("Skills of another class of your race are a little harder.<br>")
						.append("Skills for classes of another race are extremely difficult.<br>")
						.append("But the hardest of all to learn are the ")
						.append(player.getClassId().isMage() ? "fighter" : "mage")
						.append("skills!<br>");
				}
				
				// make a list of classes
				if (!classesToTeach.isEmpty()) {
					int count = 0;
					var classCheck = player.getClassId();
					
					while ((count == 0) && (classCheck != null)) {
						for (var cid : classesToTeach) {
							if (cid.level() > classCheck.level()) {
								continue;
							}
							
							if (SkillTreesData.getInstance().getAvailableSkills(player, cid, false, false).isEmpty()) {
								continue;
							}
							
							text.append("<a action=\"bypass -h npc_%objectId%_SkillList ")
								.append(cid.getId())
								.append("\">Learn ")
								.append(cid)
								.append("'s class Skills</a><br>\n");
							count++;
						}
						classCheck = classCheck.getParent();
					}
					classCheck = null;
				} else {
					text.append("No Skills.<br>");
				}
				text.append("</body></html>");
				
				final var html = new NpcHtmlMessage(npc.getObjectId());
				html.setHtml(text.toString());
				html.replace("%objectId%", String.valueOf(npc.getObjectId()));
				player.sendPacket(html);
				
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		} catch (Exception ex) {
			LOG.warn("Exception using bypass!", ex);
		}
	}
}
