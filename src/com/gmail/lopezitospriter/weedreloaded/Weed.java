package com.gmail.lopezitospriter.weedreloaded;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Logger;

import net.minecraft.server.v1_4_R1.MobEffect;
import net.minecraft.server.v1_4_R1.Packet42RemoveMobEffect;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.craftbukkit.v1_4_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_4_R1.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.mcstats.BukkitMetrics;

public class Weed extends JavaPlugin{
	static class PI{
		public PotionEffectType type;
		public int duration;

		public PI(PotionEffectType type, int duration){
			this.type = type;
			this.duration = duration;
		}
	}

	private Logger log = Logger.getLogger("Minecraft");

	public static boolean debug = false;

	public static Weed plugin;

	private HashMap<String, Drug> drugs = null;

	private static HashMap<UUID, HashSet<DamageEffect>> damageEffects = new HashMap<UUID, HashSet<DamageEffect>>();
	private static HashMap<UUID, HashSet<PI>> immune = new HashMap<UUID, HashSet<PI>>();

	private Listener listener = new Listener(){
		@EventHandler
		public void onEntityCombust(EntityCombustEvent event){
			for(Player player: getServer().getOnlinePlayers()) {
			if(player.hasPermission("weed.use")) {
				if(event.isCancelled())
					return;
				for(Drug d : drugs.values())
					d.activate(event);
				}else{
					return;
				}
			}
		}
		
		@EventHandler
		public void onBlockBurn(BlockBurnEvent event){
			for(Player player: getServer().getOnlinePlayers()) {
			if(player.hasPermission("weed.use")) {
				if(event.isCancelled())
					return;
				for(Drug d : drugs.values())
					d.activate(event);
				}else{
					return;
				}
			}
		}
		
		@EventHandler
		public void onEntityDamage(EntityDamageEvent event){
			for(Player player: getServer().getOnlinePlayers()) {
			if(player.hasPermission("weed.use")) {
				if(event.isCancelled())
				return;
			if(event.getEntity() instanceof LivingEntity)
				if(damageEffects.containsKey(event.getEntity().getUniqueId()))
					for(DamageEffect effect : damageEffects.get(event.getEntity().getUniqueId()))
						if(effect.getCause().equals(event.getCause()))
							if(effect.getAmplifier().getResult(event.getDamage(), effect.getAmplitude()) == 0)
								event.setCancelled(true);
							else if(effect.getAmplifier().getResult(event.getDamage(), effect.getAmplitude()) < 0){
								event.setCancelled(true);
								if(((LivingEntity) event.getEntity()).getHealth() + -1 * effect.getAmplifier().getResult(event.getDamage(), effect.getAmplitude()) > ((LivingEntity) event.getEntity()).getMaxHealth())
									((LivingEntity) event.getEntity()).setHealth(((LivingEntity) event.getEntity()).getMaxHealth());
								else
									((LivingEntity) event.getEntity()).setHealth(((LivingEntity) event.getEntity()).getHealth() + -1 * effect.getAmplifier().getResult(event.getDamage(), effect.getAmplitude()));
							}else
								event.setDamage(effect.getAmplifier().getResult(event.getDamage(), effect.getAmplitude()));
			
		
				}else{
					return;
				}
			}
			}
	};


	
	/* works
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onNameTag(PlayerReceiveNameTagEvent event) {
	event.setTag(ChatColor.RED+event.getNamedPlayer().getName());
	}
	*/
	
	@Override
	public void onDisable(){
		drugs.clear();
		drugs = null;
		getServer().getScheduler().cancelTasks(this);
		log.info("[" + getDescription().getName() + "] v" + getDescription().getVersion() + " is disabled.");
	}

	@Override
	public void onEnable(){
	    try {
	        BukkitMetrics metrics = new BukkitMetrics(this);
	        metrics.start();
	    } catch (IOException e) {
	        // Failed to submit the stats :-(
	    }
		plugin = this;
		getServer().getPluginManager().registerEvents(listener, this);
		getServer().getScheduler().scheduleSyncRepeatingTask(
				this,
				new Runnable(){
					class IDD{
						public UUID id;
						public DamageCause cause;

						public IDD(UUID id, DamageCause cause){
							this.id = id;
							this.cause = cause;
						}
					}

					class IPP{
						public UUID id;
						public PotionEffectType effect;

						public IPP(UUID id, PotionEffectType effect){
							this.id = id;
							this.effect = effect;
						}
					}

					@Override
					public void run(){
						HashSet<IDD> r = new HashSet<IDD>();
						HashSet<IPP> i = new HashSet<IPP>();

						for(UUID id : damageEffects.keySet()){
							for(DamageEffect effect : damageEffects.get(id))
								if(effect.getDuration() - 1 > 0)
									effect.setDuration(effect.getDuration() - 1);
								else
									r.add(new IDD(id, effect.getCause()));
						}
						for(UUID id : immune.keySet()){
							for(PI p : immune.get(id))
								if(p.duration - 1 > 0)
									p.duration -= 1;
								else
									i.add(new IPP(id, p.type));
						}
						for(IDD id : r)
							removeDamageEffect(id.id, id.cause);
						for(IPP id : i)
							removeImmunePotionEffectType(id.id, id.effect);
						r.clear();
						i.clear();
						r = null;
						i = null;
						if(debug){
							System.out.println("----------------------DEBUG");
							System.out.println("------POTION");
							for(Player p : Bukkit.getOnlinePlayers())
								for(PotionEffect e : p.getActivePotionEffects())
									System.out.println(e.getType().getName() + " " + e.getDuration() / 20 + " " + e.getAmplifier());
							System.out.println("------DAMAGE");
							for(UUID id : damageEffects.keySet())
								for(DamageEffect effect : damageEffects.get(id))
									System.out.println(effect.getCause() + " " + effect.getDuration() + " " + effect.getAmplitude());
							System.out.println("------IMMUNE");
							for(UUID id : immune.keySet())
								for(PI a : immune.get(id))
									System.out.println(a.type.getName() + " " + a.duration);
									

							}

					}
				},
				20,
				20
				);
		load();

	    try {
	        BukkitMetrics metrics = new BukkitMetrics(this);
	        metrics.start();
	    } catch (IOException e) {
	        // Failed to submit the stats :-(
	    }
		log.info("[" + getDescription().getName() + "] v" + getDescription().getVersion() + " is enabled.");
	}

	private void load(){
		drugs = new HashMap<String, Drug>();
		if(!getDataFolder().exists())
			getDataFolder().mkdir();
		try{
			getConfig().load(new File(getDataFolder(), "Drugs.yml"));
			debug = getConfig().getBoolean("Debug");
			for(String id : getConfig().getConfigurationSection("").getKeys(false))
				if(!id.equals("Debug")){
					HashSet<WEffect> effects = new HashSet<WEffect>();

					for(String delay : getConfig().getConfigurationSection(id + ".effects").getKeys(false)){
						for(String effect : getConfig().getConfigurationSection(id + ".effects." + delay).getKeys(false)){
							String s = getConfig().getString(id + ".effects." + delay + "." + effect);

							if(effect.startsWith("potion_"))
								effects.add(new WEffectPotion(PotionEffectType.getByName(effect.replace("potion_", "")), Integer.parseInt(delay), Integer.parseInt(s.split(" ")[0]), Integer.parseInt(s.split(" ")[1].substring(1)), Integer.parseInt(s.split(" ")[2]), Amplifier.parseAmplifier(s.split(" ")[1].charAt(0))));
							else if(effect.startsWith("damage_"))
								effects.add(new WEffectDamage(DamageCause.valueOf(effect.replace("damage_", "")), Integer.parseInt(delay), Integer.parseInt(s.split(" ")[0]), Integer.parseInt(s.split(" ")[1].substring(1)), Amplifier.parseAmplifier(s.split(" ")[1].charAt(0))));
							else
								log.warning("Invalid effect:" + effect);
							s = null;
						}
					}
					if(effects.size() > 0)
						drugs.put(id, new Drug(Activation.parseActivation(getConfig().getString(id + ".activation")), getConfig().getString(id + ".activation_argument").split(" "), effects));
				}
		}catch(FileNotFoundException e){
			log.info("[" + getDescription().getName() + "] Creating configuration file due to first time usage.");
			try{
				new File(getDataFolder(), "Drugs.yml").createNewFile();
				getConfig().load(new File(getDataFolder(), "Drugs.yml"));
				getConfig().set("Debug", debug);
				getConfig().set("Weed.activation", Activation.BURN.toString());
				getConfig().set("Weed.activation_argument", "LONG_GRASS:1 5 true");
				getConfig().set("Weed.effects.0.potion_SLOW", "58 =0 0");
				getConfig().set("Weed.effects.0.potion_CONFUSION", "58 =0 0");
				getConfig().set("Weed.effects.0.potion_POISON", "58 =0 0");
				getConfig().set("Weed.effects.0.potion_SPEED", "60 +2 6");
				getConfig().set("Weed.effects.10.potion_JUMP", "50 +2 10");
				getConfig().set("Weed.effects.10.damage_FALL", "60 =0");
				getConfig().set("Weed.effects.60.potion_SLOW", "15 +2 4");
				getConfig().set("Weed.effects.60.potion_CONFUSION", "22 +1 10");
				getConfig().set("Weed.effects.60.potion_POISON", "5 +1 4");
				getConfig().set("Weed.effects.80.potion_CONFUSION", "10 +2 10");
				try{
					getConfig().save(new File(getDataFolder(), "Drugs.yml"));
					load();
				}catch (IOException e2){
					e2.printStackTrace();
				}
			}catch(Exception e1){
				e1.printStackTrace();
			}
		}catch (IOException e){
			log.warning("[" + getDescription().getName() + "] Cannot load configuration: " + e);
		}catch (InvalidConfigurationException e){
			log.warning("[" + getDescription().getName() + "] Cannot read configuration: " + e);
		}
		if(!new File(getDataFolder(), "Instructions.txt").exists() || (new File(getDataFolder(), "Instructions.txt").exists() && !new File(getDataFolder(), "Instructions.txt").isFile())){
			InputStream is = null;
			OutputStream os = null;

			log.info("[" + getDescription().getName() + "] Creating information file due to first time usage.");
			try{
				new File(getDataFolder(), "Instructions.txt").createNewFile();
				is = this.getClass().getResourceAsStream("Instructions.txt");
				os = new FileOutputStream(new File(getDataFolder(), "Instructions.txt"), true);        
				byte[] buffer = new byte[1024];
				int len;

				while ((len = is.read(buffer)) > 0)
					os.write(buffer, 0, len);
				is.close();
				os.close(); 
			}catch(Exception e){
				log.warning("[" + getDescription().getName() + "] Cannot save information: " + e);
			}finally{
				is = null;
				os = null;
			}
		}
	}

	public static void smoke(Location location, double distance){
		for(double x = location.getX() - distance; x <= location.getX() + distance; x++)
			for(double y = location.getY() - distance; y <= location.getY() + distance; y++)
				for(double z = location.getZ() - distance; z <= location.getZ() + distance; z++)
					if(getDistance(location, location.getWorld().getBlockAt((int) x, (int) y, (int) z).getLocation()) <= distance)
						location.getWorld().playEffect(location.getWorld().getBlockAt((int) x, (int) y, (int) z).getLocation(), Effect.SMOKE, 0);
	}

	public static double getDistance(Location l1, Location l2){
		if(!l1.getWorld().equals(l2.getWorld()))
			return -1;
		return Math.sqrt(Math.pow(l1.getX() - l2.getX(), 2) + Math.pow(l1.getY() - l2.getY(), 2) + Math.pow(l1.getZ() - l2.getZ(), 2));
	}

	public static int getPotionEffectAmplitude(LivingEntity entity, PotionEffectType effect){
		if(entity != null && !entity.isDead())
			for(PotionEffect e : entity.getActivePotionEffects())
				if(e.getType().equals(effect))
					return e.getAmplifier();
		return 0;
	}

	public static int getPotionEffectDuration(LivingEntity entity, PotionEffectType effect){
		if(entity != null && !entity.isDead())
			for(PotionEffect e : entity.getActivePotionEffects())
				if(e.getType().equals(effect))
					return e.getDuration();
		return 0;
	}

	public static void addPotionEffect(LivingEntity entity, PotionEffect effect){
		if(entity != null && !entity.isDead())
			if(effect.getAmplifier() > 0){
				if(!isImmuneFromPotionEffectType(entity, effect.getType()))
					if(effect.getDuration() > 0){
						removePotionEffect(entity, effect.getType());
						entity.addPotionEffect(effect, true);
					}else{
						int i = getPotionEffectDuration(entity, effect.getType());

						removePotionEffect(entity, effect.getType());
						entity.addPotionEffect(new PotionEffect(effect.getType(), i, effect.getAmplifier()), true);
					}
			}else{
				if(effect.getDuration() > 0){
					addImmunePotionEffectType(entity, effect.getType(), effect.getDuration() / 20);
				}
				removePotionEffect(entity, effect.getType());
			}
	}

	public static void removePotionEffect(LivingEntity entity, PotionEffectType effect){
		for (Player p : entity.getLocation().getWorld().getPlayers())
			((CraftPlayer) p).getHandle().playerConnection
					.sendPacket(new Packet42RemoveMobEffect(entity
							.getEntityId(), new MobEffect(effect.getId(), 0, 0)));
		((CraftLivingEntity) entity).getHandle().getDataWatcher()
				.watch(8, Integer.valueOf(0));
	}

	public static int getDamageEffectAmplitude(LivingEntity entity, DamageCause cause){
		if(damageEffects.containsKey(entity.getUniqueId()))
			for(DamageEffect effect : damageEffects.get(entity.getUniqueId()))
				if(effect.getCause().equals(cause))
					return effect.getAmplitude();
		return 0;
	}

	public static int getDamageEffectDuration(LivingEntity entity, DamageCause cause){
		if(damageEffects.containsKey(entity.getUniqueId()))
			for(DamageEffect effect : damageEffects.get(entity.getUniqueId()))
				if(effect.getCause().equals(cause))
					return effect.getDuration();
		return 0;
	}

	public static void addDamageEffect(LivingEntity entity, DamageEffect effect){
		removeDamageEffect(entity, effect.getCause());
		if(!damageEffects.containsKey(entity.getUniqueId()))
			damageEffects.put(entity.getUniqueId(), new HashSet<DamageEffect>());
		damageEffects.get(entity.getUniqueId()).add(effect);
	}

	public static void removeDamageEffect(LivingEntity entity, DamageCause cause){
		removeDamageEffect(entity.getUniqueId(), cause);
	}

	public static void removeDamageEffect(UUID id, DamageCause cause){
		if(damageEffects.containsKey(id)){
			ArrayList<DamageEffect> c = new ArrayList<DamageEffect>();

			for(DamageEffect effect : damageEffects.get(id))
				if(effect.getCause().equals(cause))
					c.add(effect);
			damageEffects.get(id).removeAll(c);
			c.clear();
			c = null;
			if(!(damageEffects.get(id).size() > 0))
				damageEffects.remove(id);
		}
	}

	public static void addImmunePotionEffectType(LivingEntity entity, PotionEffectType effect, int duration){
		removeImmunePotionEffectType(entity.getUniqueId(), effect);
		if(!immune.containsKey(entity.getUniqueId()))
			immune.put(entity.getUniqueId(), new HashSet<PI>());
		immune.get(entity.getUniqueId()).add(new PI(effect, duration));
	}

	public static void removeImmunePotionEffectType(UUID id, PotionEffectType effect){
		if(immune.containsKey(id)){
			ArrayList<PI> c = new ArrayList<PI>();

			for(PI e : immune.get(id))
				if(e.type.equals(effect))
					c.add(e);
			immune.get(id).removeAll(c);
			c.clear();
			c = null;
			if(!(immune.get(id).size() > 0))
				immune.remove(id);
		}
	}

	public static boolean isImmuneFromPotionEffectType(LivingEntity entity, PotionEffectType effect){
		if(immune.containsKey(entity.getUniqueId())){
			for(PI p : immune.get(entity.getUniqueId()))
				if(p.type.equals(effect))
					return true;
		}
		return false;
	}

	public static ArrayList<LivingEntity> getNearbyLivingEntities(Location location, double distance){
		Collection<LivingEntity> list = location.getWorld().getEntitiesByClass(LivingEntity.class);
		ArrayList<LivingEntity> n = new ArrayList<LivingEntity>();

		for(LivingEntity e : list)
			if(getDistance(location , e.getLocation()) <= distance){
				n.add(e);
			}
		list.clear();
		list = null;
		return n;
	}

	public static boolean isTheSame(Material m1, byte b1, Material m2, byte b2){
		return m1.equals(m2) && b1 == b2;
	}
}