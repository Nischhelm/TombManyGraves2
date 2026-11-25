package com.m4thg33k.tombmanygraves.friends;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.m4thg33k.tombmanygraves.Names;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

public class FriendData extends WorldSavedData {

    private static final String NAME = Names.MODID + "_friends";
    private static final String OWNER = "Owner";
    private static final String FRIENDS = "Friends";

    private final Map<UUID, Set<UUID>> friendList = new HashMap<>();

    public FriendData(String name)
    {
        super(name);
    }

    public static FriendData get(MinecraftServer server)
    {
        MapStorage storage = server.getEntityWorld().getMapStorage();
        FriendData data = (FriendData) storage.getOrLoadData(FriendData.class, NAME);
        if (data == null)
        {
            data = new FriendData(NAME);
            storage.setData(NAME, data);
        }
        return data;
    }

    public Set<UUID> getFriends(UUID owner)
    {
        return friendList.computeIfAbsent(owner, k -> new HashSet<>());
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        friendList.clear();
        NBTTagList owners = nbt.getTagList(FRIENDS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < owners.tagCount(); i++)
        {
            NBTTagCompound entry = owners.getCompoundTagAt(i);
            Set<UUID> friends = getFriends(NBTUtil.getUUIDFromTag(entry.getCompoundTag(OWNER)));
            NBTTagList friendTags = entry.getTagList(FRIENDS, Constants.NBT.TAG_COMPOUND);
            for (int j = 0; j < friendTags.tagCount(); j++)
            {
                friends.add(NBTUtil.getUUIDFromTag(friendTags.getCompoundTagAt(j)));
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        NBTTagList owners = new NBTTagList();
        for (Map.Entry<UUID, Set<UUID>> entry : friendList.entrySet())
        {
            if (entry.getValue().isEmpty())
            {
                continue;
            }
            NBTTagCompound ownerTag = new NBTTagCompound();
            ownerTag.setTag(OWNER, NBTUtil.createUUIDTag(entry.getKey()));
            NBTTagList friendTags = new NBTTagList();
            for (UUID friend : entry.getValue())
            {
                friendTags.appendTag(NBTUtil.createUUIDTag(friend));
            }
            ownerTag.setTag(FRIENDS, friendTags);
            owners.appendTag(ownerTag);
        }
        nbt.setTag(FRIENDS, owners);
        return nbt;
    }
}
