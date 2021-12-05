/*
 * Copyright Â© 2004-2021 L2J DataPack
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
package com.l2jserver.datapack.handlers.effecthandlers.instant;

import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.conditions.Condition;
import com.l2jserver.gameserver.model.effects.AbstractEffect;
import com.l2jserver.gameserver.model.effects.L2EffectType;
import com.l2jserver.gameserver.model.skills.BuffInfo;
import com.l2jserver.gameserver.model.zone.ZoneId;
import com.l2jserver.gameserver.network.serverpackets.UserInfo;

/**
 * Nevit's Hourglass effect implementation.
 * @author Maneco2
 * @since 2.6.3.0
 */
public final class NevitHourglass extends AbstractEffect {
	
	public NevitHourglass(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params) {
		super(attachCond, applyCond, set, params);
	}
	
	@Override
	public boolean isInstant() {
		return true;
	}
	
	@Override
	public L2EffectType getEffectType() {
		return L2EffectType.BONUS_TIME_LIMIT_UP;
	}
	
	@Override
	public void onStart(BuffInfo info) {
		if ((info.getEffected() != null) && info.getEffected().isPlayer()) {
			info.getEffected().getActingPlayer().startHourglassEffect();
			info.getEffected().getActingPlayer().sendPacket(new UserInfo(info.getEffected().getActingPlayer()));
		}
	}
	
	@Override
	public void onExit(BuffInfo info) {
		if (!info.getEffected().getActingPlayer().isInsideZone(ZoneId.PEACE)) {
			info.getEffected().getActingPlayer().stopHourglassEffect();
		}
	}
}
