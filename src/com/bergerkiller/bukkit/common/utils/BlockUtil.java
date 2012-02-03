package com.bergerkiller.bukkit.common.utils;

import java.util.ArrayList;

import net.minecraft.server.ChunkCoordinates;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.material.Attachable;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Rails;
import org.bukkit.material.Directional;

public class BlockUtil {
    
    /*
     * Prevents the need to read the lighting when using getState()
     * Can be a little bit faster :)
     */
    public static MaterialData getData(Block b) {
    	return b.getType().getNewData(b.getData());
    }
    public static int getBlockSteps(Location b1, Location b2, boolean checkY) {
    	int d = Math.abs(b1.getBlockX() - b2.getBlockX());
    	d += Math.abs(b1.getBlockZ() - b2.getBlockZ());
    	if (checkY) d +=  Math.abs(b1.getBlockY() - b2.getBlockY());
    	return d;
    }
    public static int getBlockSteps(Block b1, Block b2, boolean checkY) {
    	int d = Math.abs(b1.getX() - b2.getX());
    	d += Math.abs(b1.getZ() - b2.getZ());
    	if (checkY) d +=  Math.abs(b1.getY() - b2.getY());
    	return d;
    }
    
    public static boolean equals(Block block1, Block block2) {
    	if (block1 == null || block2 == null) return false;
    	if (block1 == block2) return true;
    	return block1.getX() == block2.getX() && block1.getZ() == block2.getZ()
    			&& block1.getY() == block2.getY() && block1.getWorld() == block2.getWorld();    	
    }
        
    public static Block[] getRelative(Block main, BlockFace... faces) {
    	if (main == null) return new Block[0];
    	Block[] rval = new Block[faces.length];
    	for (int i = 0; i < rval.length; i++) {
    		rval[i] = main.getRelative(faces[i]);
    	}
    	return rval;
    }
    public static ChunkCoordinates getCoordinates(final Block block) {
    	return new ChunkCoordinates(block.getX(), block.getY(), block.getZ());
    }
    public static Block getBlock(World world, ChunkCoordinates at) {
    	return world.getBlockAt(at.x, at.y, at.z);
    }
    
    public static Block getRailsBlockFromSign(final Block signblock) {
		//try to find out where the rails block is located
		Block above = signblock.getRelative(0, 2, 0);
		if (BlockUtil.isRails(above)) {
			return above;
		} else {
			//rail located above the attached face?
			BlockFace face = BlockUtil.getAttachedFace(signblock);
			above = signblock.getRelative(face.getModX(), 1, face.getModZ());
			if (BlockUtil.isRails(above)) {
				return above;
			} else {
				return null;
			}
		}
    }
    public static BlockFace getAttachedFace(Block attachable) {
    	MaterialData data = getData(attachable);
    	if (data != null && data instanceof Attachable) {
    		return ((Attachable) data).getAttachedFace();
    	}
    	return BlockFace.DOWN;
    }
    
	public static BlockFace getFacing(Block b) {
		MaterialData data = getData(b);
		if (data != null && data instanceof Directional) {
			return ((Directional) data).getFacing();
		} else {
			return BlockFace.NORTH;
		}
	}
	public static void setFacing(Block block, BlockFace facing) {
		org.bukkit.material.Sign sign = new org.bukkit.material.Sign();
		sign.setFacingDirection(facing);
		block.setData(sign.getData(), true);
	}
	
    public static Block getAttachedBlock(Block b) {
    	return b.getRelative(getAttachedFace(b));
    }
    
    public static void setLeversAroundBlock(Block block, boolean down) {
		for (Block b : BlockUtil.getRelative(block, FaceUtil.attachedFaces)) {
			BlockUtil.setLever(b, down);
		}
    }
    public static void setLever(Block lever, boolean down) {
    	if (lever.getTypeId() == Material.LEVER.getId()) {
			byte data = lever.getData();
	        int newData;
	        if (down) {
	        	newData = data | 0x8;
	        } else {
	        	newData = data & 0x7;
	        }
	        if (newData != data) {
	            lever.setData((byte) newData, true);
	        }
    	}
    }
    
    public static void setRails(Block rails, BlockFace from, BlockFace to) {
    	setRails(rails, FaceUtil.combine(from, to).getOppositeFace());
    }
    public static void setRails(Block rails, BlockFace direction) {
    	Material type = rails.getType();
    	if (type == Material.RAILS) {
    		if (direction == BlockFace.NORTH) {
    			direction = BlockFace.SOUTH;
    		} else if (direction == BlockFace.EAST) {
    			direction = BlockFace.WEST;
    		}
    		byte olddata = rails.getData();
    		Rails r = (Rails) type.getNewData(olddata);
    		r.setDirection(direction, r.isOnSlope());
    		byte newdata = r.getData();
    		if (olddata != newdata) {
        		rails.setData(newdata);
    		}
    	}
    }
        
    public static boolean isType(int material, int... types) {
    	return CommonUtil.contains(material, types);
    }
    public static boolean isType(Material material, Material... types) {
    	return CommonUtil.contains(material, types);
    }
    public static boolean isType(Block block, Material... types) {
    	return CommonUtil.contains(block.getType(), types);
    }
    public static boolean isType(Block block, int... types) {
    	return CommonUtil.contains(block.getTypeId(), types);
    }
    
	public static boolean isSign(Material material) {
		return isType(material, Material.WALL_SIGN, Material.SIGN_POST);
	}
    public static boolean isSign(Block b) {
    	return b == null ? false : isSign(b.getType());
    }
    public static boolean isRails(Material type) {
    	return isType(type, Material.RAILS, Material.POWERED_RAIL, Material.DETECTOR_RAIL);
    }
    public static boolean isRails(int type) {
    	return isType(type, Material.RAILS.getId(), Material.POWERED_RAIL.getId(), Material.DETECTOR_RAIL.getId());
    }
    public static boolean isRails(Block b) {
    	return b == null ? false : isRails(b.getTypeId());
    }
    
    public static <T extends MaterialData> T getData(Block block, Class<T> type) {
    	try {
    		return type.cast(getData(block));
    	} catch (Exception ex) {
    		return null;
    	}
    }
	public static <T extends BlockState> T getState(Block block, Class<T> type) {
    	try {
    		return type.cast(block.getState());
    	} catch (Exception ex) {
    		return null;
    	}
	}
	public static Rails getRails(Block railsblock) {
		return getData(railsblock, Rails.class);
	}
	public static Sign getSign(Block signblock) {
		return getState(signblock, Sign.class);
	}
		
	public static Block getRailsAttached(Block signblock) {
		Material type = signblock.getType();
		Block rail = null;
		if (type == Material.WALL_SIGN) {
			rail = getAttachedBlock(signblock).getRelative(BlockFace.UP);
			if (isRails(rail)) return rail;
		}
		if (isSign(type)) {
			rail = signblock.getRelative(0, 2, 0);
			if (isRails(rail)) return rail;
		}
		return null;
	}
	public static Block[] getSignsAttached(Block rails) {
		ArrayList<Block> rval = new ArrayList<Block>(3);
		Block under = rails.getRelative(0, -2, 0);
		if (BlockUtil.isSign(under)) rval.add(under);
		for (BlockFace face : FaceUtil.axis) {
			Block side = rails.getRelative(face.getModX(), -1, face.getModZ());
			if (!BlockUtil.isSign(side)) continue;
			if (BlockUtil.getAttachedFace(side) == face.getOppositeFace()) {
				rval.add(side);
			}
		}
		return rval.toArray(new Block[0]);
	}
		
	public static void breakBlock(Block block) {
		int x = block.getX();
		int y = block.getY();
		int z = block.getZ();
		net.minecraft.server.World world = ((CraftWorld) block.getWorld()).getHandle();
		net.minecraft.server.Block bb = net.minecraft.server.Block.byId[block.getTypeId()];
		if (bb != null) {
			try {
				bb.dropNaturally(world, x, y, z, block.getData(), 20, 0);
			} catch (Throwable t) {
			    t.printStackTrace();
			}
		}
        world.setTypeId(x, y, z, 0);
	}
}