package com.gmail.lopezitospriter.weedreloaded;

import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.entity.EntityCombustEvent;

public enum Activation{
	BURN;

	public Collection<LivingEntity> activate(Event event, String[] args){
		try{
			switch(this){
			case BURN:
				if(
						event instanceof EntityCombustEvent
						&& ((EntityCombustEvent) event).getEntity() instanceof Item
						&& (
								(
										((Item) ((EntityCombustEvent) event).getEntity()).getItemStack().getData() != null
										&& Weed.isTheSame(((Item) ((EntityCombustEvent) event).getEntity()).getItemStack().getType(), (byte) 0, Material.matchMaterial(args[0].split(":")[0]), Byte.parseByte(args[0].split(":")[1]))
										)
										|| (
												((Item) ((EntityCombustEvent) event).getEntity()).getItemStack().getData() != null
												&& Weed.isTheSame(((Item) ((EntityCombustEvent) event).getEntity()).getItemStack().getType(), ((Item) ((EntityCombustEvent) event).getEntity()).getItemStack().getData().getData(), Material.matchMaterial(args[0].split(":")[0]), Byte.parseByte(args[0].split(":")[1]))
												)
								)
						){
					if(Boolean.parseBoolean(args[2]))
						Weed.smoke(((EntityCombustEvent) event).getEntity().getLocation(), Integer.parseInt(args[1]));
					return Weed.getNearbyLivingEntities(((EntityCombustEvent) event).getEntity().getLocation(), Integer.parseInt(args[1]));
				}
				if(
						event instanceof BlockBurnEvent
						&& Weed.isTheSame(((BlockBurnEvent) event).getBlock().getType(), ((BlockBurnEvent) event).getBlock().getData(), Material.matchMaterial(args[0].split(":")[0]), Byte.parseByte(args[0].split(":")[1]))
						){
					if(Boolean.parseBoolean(args[2]))
						Weed.smoke(((BlockBurnEvent) event).getBlock().getLocation(), Integer.parseInt(args[1]));
					return Weed.getNearbyLivingEntities(((BlockBurnEvent) event).getBlock().getLocation(), Integer.parseInt(args[1]));
				}
				break;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String toString(){
		switch(this){
		case BURN:
			return "BURN";
		}
		return "UNKNOWN";
	}

	public static Activation parseActivation(String s){
		if(s.equals("BURN"))
			return BURN;
		return null;
	}
}